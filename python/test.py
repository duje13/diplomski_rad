import cv2
import io
import numpy as np
import pickle
import os
from functions import *
import json
import sys

print("Predicing...")

test_path = "SLIKE_TEST"
imgs = [f for f in os.listdir(test_path) if f.endswith(".jpg") or f.endswith(".jpeg") or f.endswith(".JPG")]
#imgs = os.listdir(test_path)
imgs.sort()

resultsFile = open("SLIKE_TEST/vrste.txt", "r")

results = resultsFile.read().splitlines()

with open("dic.pkl","rb") as f:
    dictionary = pickle.load(f)

with open("fishes.txt") as json_file:
	train_labels = json.load(json_file)

correct = 0
i = 0

for file in imgs:

    img = OpenImage("SLIKE_TEST/" + file)

    rt = cv2.ml.RTrees_create()
    rt = rt.load("model.xml")

    img = OpenImage("SLIKE_TEST/" + file)

    bow = GetBOWFeatures(img, dictionary)
    hist = GetColorHist(img)
    hu = GetShape(img)
    hog = GetHOG(img)

    data = np.hstack([bow, hist, hu, hog])

    p = rt.predict(np.matrix(data, dtype=np.float32))
    id = int(p[1][0][0])

    fish = [i['name'] for i in train_labels if i['id'] == id][0]

    print(file + " - " + fish)

    if(fish == results[i]):
        correct = correct + 1
    
    i = i + 1

print("Correct: " + str(correct) + " / " + str(len(imgs)))
print("Correct (%)" + str((correct/len(imgs))*100))