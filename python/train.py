import cv2
import numpy as np
import io
import os
import pickle
import random
import glob
from functions import *
import json

train_path = "SLIKE"
train_labels = []

with open("fishes.txt") as json_file:
	train_labels = json.load(json_file)

print("Creating BOW...")

descriptors = []

for training_name in train_labels:
	dir = os.path.join(train_path, training_name['name'])

	images = os.listdir(dir)
	for f in range(0,20):
		file = dir + "/" + images[f]

		img = OpenImage(file)
		dsc = GetLocalFeatures(img)
		descriptors.extend(dsc)

BOW = cv2.BOWKMeansTrainer(100)
dic = BOW.cluster(np.array(descriptors))

print("Saving dictionary...")

#FOR TESTING IN PYTHON
with open("dic.pkl","wb") as f:
    pickle.dump(dic,f)

#FOR ANDORID
with open('dic.txt', 'w') as filehandle:  
    json.dump(dic.tolist(), filehandle)

print("Train RT...")

traindata = []
labels = []
for training_name in train_labels:
	dir = os.path.join(train_path, training_name['name'])

	images = os.listdir(dir)
	for f in range(20,45):
		file = dir + "/" + images[f]

		img = OpenImage(file)

		bow = GetBOWFeatures(img, dic)
		hist = GetColorHist(img)
		hu = GetShape(img)
		hog = GetHOG(img)

		traindata.append( np.hstack([bow, hist, hu, hog]))
		labels.append(training_name['id'])

rt = cv2.ml.RTrees_create()

rt.setTermCriteria((cv2.TERM_CRITERIA_MAX_ITER, 10000, 1e-6))

rt.train(np.matrix(traindata, dtype=np.float32), cv2.ml.ROW_SAMPLE, np.array(labels))

print("Saving RT...")

rt.save("model.xml")

print("Finished!")