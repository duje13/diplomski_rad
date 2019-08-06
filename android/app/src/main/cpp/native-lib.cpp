#include <jni.h>
#include "opencv2/core/core.hpp"
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/xfeatures2d.hpp>
#include <stdio.h>
#include <opencv2/ml/ml.hpp>
#include <opencv2/objdetect/objdetect.hpp>

using namespace std;
using namespace cv;
using namespace xfeatures2d;

void toGray(Mat img, Mat& gray);
void toHSV(Mat img, Mat& hsv);
void resizeImg(Mat img, Mat& resized_img);

extern "C" {

JNIEXPORT jint JNICALL Java_com_example_diplomski_OpenCVFunctions_getColorHist(JNIEnv*, jobject, jlong addrImg, jlong addrHist);
JNIEXPORT jint JNICALL Java_com_example_diplomski_OpenCVFunctions_getShape(JNIEnv*, jobject, jlong addrImg, jlong addrHu);
JNIEXPORT jint JNICALL Java_com_example_diplomski_OpenCVFunctions_getBOWFeatures(JNIEnv*, jobject, jlong addrImg, jlong addrDic, jlong addrOut);
JNIEXPORT jint JNICALL Java_com_example_diplomski_OpenCVFunctions_predict(JNIEnv*, jobject, jstring file, jlong addrTrainData);

JNIEXPORT jint JNICALL Java_com_example_diplomski_OpenCVFunctions_getColorHist(JNIEnv*, jobject, jlong addrImg, jlong addrHist)
{
    Mat& img = *(Mat*)addrImg;
    Mat& hist = *(Mat*)addrHist;
    toHSV(img, img);
    resizeImg(img, img);

    int channels[] = {0, 1, 2};
    int histSize[] = {8, 8, 8};
    float hranges[] = { 0, 256 };
    float sranges[] = { 0, 256 };
    float vranges[] = {0, 256};
    const float* ranges[] = { hranges, sranges, vranges};

    calcHist(&img, 1, channels, Mat(), hist, 3, histSize, ranges);

    normalize(hist, hist);
    hist = hist.reshape(1,1);

    return 0;
}

JNIEXPORT jint JNICALL Java_com_example_diplomski_OpenCVFunctions_getShape(JNIEnv*, jobject, jlong addrImg, jlong addrHu)
{
    Mat& img = *(Mat*)addrImg;
    Mat& hu = *(Mat*)addrHu;

    toGray(img, img);
    resizeImg(img, img);

    Moments moments = cv::moments(img);
    HuMoments(moments, hu);
    normalize(hu, hu);
    hu = hu.reshape(1,1);
    hu.convertTo(hu, CV_32F);

    return 0;
}

JNIEXPORT jint JNICALL Java_com_example_diplomski_OpenCVFunctions_getBOWFeatures(JNIEnv*, jobject, jlong addrImg, jlong addrDic, jlong addrOut)
{
    Mat& img = *(Mat*)addrImg;
    Mat& dic = *(Mat*)addrDic;
    Mat& out = *(Mat*)addrOut;

    Ptr<cv::flann::KDTreeIndexParams> params = new flann::KDTreeIndexParams(5);
    params->setAlgorithm(1);

    Ptr<DescriptorMatcher> matcher(new FlannBasedMatcher(params));
    Ptr<SiftFeatureDetector> detector = SiftFeatureDetector::create();
    Ptr<SiftDescriptorExtractor> extractor = SiftDescriptorExtractor::create();

    BOWImgDescriptorExtractor bow(extractor, matcher);
    bow.setVocabulary(dic);

    toGray(img, img);
    resizeImg(img, img);

    vector<KeyPoint> keypoints;
    detector->detect(img, keypoints);

    bow.compute(img, keypoints, out);
    out = out.reshape(1,1);

    return 0;
}

JNIEXPORT jint JNICALL Java_com_example_diplomski_OpenCVFunctions_getHOG(JNIEnv* env, jobject, jlong addrImg, jlong addrHog)
{
    Mat& img = *(Mat*)addrImg;
    Mat& hog = *(Mat*)addrHog;

    toGray(img, img);
    resizeImg(img, img);

    HOGDescriptor hd = HOGDescriptor(Size(750,750), Size(500,500), Size(125,125), Size(250,250), 9);

    vector<float> hog_vec;
    hd.compute(img, hog_vec);
    hog = Mat(hog_vec).reshape(1,1);

    return 0;
}

JNIEXPORT jint JNICALL Java_com_example_diplomski_OpenCVFunctions_predict(JNIEnv* env, jobject, jstring file, jlong addrTrainData)
{
    const char *path = env->GetStringUTFChars(file, NULL);

    Ptr<ml::RTrees> rt = ml::RTrees::load(path);

    Mat& trainData = *(Mat*)addrTrainData;

    float result = rt->predict(trainData);

    return (int)result;
}

}

void toGray(Mat img, Mat& gray)
{
    cvtColor(img, gray, COLOR_RGB2GRAY);
}

void toHSV(Mat img, Mat& hsv)
{
    cvtColor(img, hsv, cv::COLOR_RGB2HSV);
}

void resizeImg(Mat img, Mat& resized_img)
{
    cv::resize(img, resized_img, Size(750, 750), 0, 0, INTER_AREA);
}