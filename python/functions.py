import cv2
import numpy as np

def OpenImage(file):
	f = open(file, "rb")
	chunk = f.read()
	chunk_arr = np.frombuffer(chunk, dtype=np.uint8)
	image = cv2.imdecode(chunk_arr, cv2.IMREAD_COLOR)

	return image

def ResizeImage(img):

    return cv2.resize(img, (750,750), interpolation = cv2.INTER_AREA)

def GetLocalFeatures(img):
	image = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
	image = ResizeImage(image)

	alg = cv2.xfeatures2d.SIFT_create()
	kps = alg.detect(image)
	kps, dsc = alg.compute(image, kps)

	return dsc

def GetBOWFeatures(img, dictionary):
	alg = cv2.xfeatures2d.SIFT_create()
	flann_params = dict(algorithm = 1, trees = 5)
	matcher = cv2.FlannBasedMatcher(flann_params, {}) 
	bow_extract = cv2.BOWImgDescriptorExtractor( alg , matcher )
	bow_extract.setVocabulary( dictionary )

	image = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
	image = ResizeImage(image)

	kp = alg.detect(image)
	bowsig = bow_extract.compute(image, kp)

	return bowsig.flatten()

def GetColorHist(img):
	image = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)
	image = ResizeImage(image)
	hist  = cv2.calcHist([image], [0, 1, 2], None, [8, 8, 8], [0, 256, 0, 256, 0, 256])
	cv2.normalize(hist, hist)
	return hist.flatten()

def GetShape(img):
	image = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
	image = ResizeImage(image)
	hu = cv2.HuMoments(cv2.moments(image))
	cv2.normalize(hu,hu)
	return hu.flatten()

def GetHOG(img):
	image = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
	image = ResizeImage(image)
	hog = cv2.HOGDescriptor((750, 750), (500, 500), (125, 125), (250, 250), 9)
	return hog.compute(image).flatten()	