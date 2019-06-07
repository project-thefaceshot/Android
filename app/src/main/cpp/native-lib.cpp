#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>
#include <iostream>
#include <android/log.h>

using namespace std;
using namespace cv;

extern "C" {

float resize_a(Mat img_src, Mat &img_resize, int resize_width) {

    float scale = resize_width / (float) img_src.cols;
    if (img_src.cols > resize_width) {
        int new_height = cvRound(img_src.rows * scale);
        resize(img_src, img_resize, Size(resize_width, new_height));
    } else {
        img_resize = img_src;
    }
    return scale;
}
}
extern "C" {
JNIEXPORT jlong JNICALL
Java_com_example_administrator_woosukjoa_MainActivity_loadCascade(JNIEnv *env, jobject type,
                                                                  jstring cascadeFileName_) {
    const char *nativeFileNameString = env->GetStringUTFChars(cascadeFileName_, 0);

    string baseDir("/storage/emulated/0/");
    baseDir.append(nativeFileNameString);
    const char *pathDir = baseDir.c_str();

    jlong ret = 0;
    ret = (jlong) new CascadeClassifier(pathDir);
    if (((CascadeClassifier *) ret)->empty()) {
        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "CascadeClassifier로 로딩 실패  %s", nativeFileNameString);
    } else
        __android_log_print(ANDROID_LOG_DEBUG, "native-lib :: ",
                            "CascadeClassifier로 로딩 성공 %s", nativeFileNameString);


    env->ReleaseStringUTFChars(cascadeFileName_, nativeFileNameString);

    return ret;
}
}
extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_administrator_woosukjoa_MainActivity_detect(JNIEnv *env, jobject type,
                                                             jlong cascadeClassifier_face,
                                                             jlong matAddrInput,
                                                             jlong matAddrResult,
                                                             jlong matGray) {

    Mat &img_input = *(Mat *) matAddrInput;
    Mat &img_result = *(Mat *) matAddrResult;
    Mat &img_gray = *(Mat *) matGray;
    int num_face=0;

    img_result = img_input.clone();

    std::vector<Rect> faces;


    cvtColor(img_input, img_gray, COLOR_RGB2GRAY);
    equalizeHist(img_gray, img_gray);

    Mat img_resize;
    float resizeRatio = resize_a(img_gray, img_resize, 640);

    Rect face_area;

    //-- Detect faces
    ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.1, 3,
                                                                         0 | 2,
                                                                         Size(30, 30));

    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.2, 3,
                                                                         0 | 2,
                                                                         Size(30, 30));
    }



    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.3, 3,
                                                                         0 | 2,
                                                                         Size(30, 30));
    }



    if(faces.size()!=1) {
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.1, 7,
                                                                         0 | 2,
                                                                         Size(30, 30));
    }


    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.2, 7,
                                                                         0 | 2,
                                                                         Size(30, 30));
    }


    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.3, 7,
                                                                         0 | 2,
                                                                         Size(30, 30));
    }



    if(faces.size()!=1) {
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.1, 5,
                                                                         0 | 2,
                                                                         Size(30, 30));
    }


    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.2, 5,
                                                                         0 | 2,
                                                                         Size(30, 30));
    }

    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.3, 5,
                                                                         0 | 2,
                                                                         Size(30, 30));
    }


    ///////

    if(faces.size()!=1) {
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.1, 3,
                                                                         0 | 2,
                                                                         Size(100, 100));
    }

    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.2, 3,
                                                                         0 | 2,
                                                                         Size(100, 100));
    }



    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.3, 3,
                                                                         0 | 2,
                                                                         Size(100, 100));
    }



    if(faces.size()!=1) {
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.1, 7,
                                                                         0 | 2,
                                                                         Size(100, 100));
    }


    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.2, 7,
                                                                         0 | 2,
                                                                         Size(100, 100));
    }


    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.3, 7,
                                                                         0 | 2,
                                                                         Size(100, 100));
    }



    if(faces.size()!=1) {
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.1, 5,
                                                                         0 | 2,
                                                                         Size(100, 100));
    }


    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.2, 5,
                                                                         0 | 2,
                                                                         Size(100, 100));
    }

    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.3, 5,
                                                                         0 | 2,
                                                                         Size(100, 100));
    }


    ///////

    if(faces.size()!=1) {
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.1, 3,
                                                                         0 | 2,
                                                                         Size(50, 50));
    }

    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.2, 3,
                                                                         0 | 2,
                                                                         Size(50, 50));
    }



    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.3, 3,
                                                                         0 | 2,
                                                                         Size(50, 50));
    }



    if(faces.size()!=1) {
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.1, 7,
                                                                         0 | 2,
                                                                         Size(50, 50));
    }


    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.2, 7,
                                                                         0 | 2,
                                                                         Size(50, 50));
    }


    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.3, 7,
                                                                         0 | 2,
                                                                         Size(50, 50));
    }



    if(faces.size()!=1) {
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.1, 5,
                                                                         0 | 2,
                                                                         Size(50, 50));
    }


    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.2, 5,
                                                                         0 | 2,
                                                                         Size(50, 50));
    }

    if(faces.size()!=1){
        ((CascadeClassifier *) cascadeClassifier_face)->detectMultiScale(img_resize, faces, 1.3, 5,
                                                                         0 | 2,
                                                                         Size(50, 50));
    }



    num_face = faces.size();
    if (num_face == 1) {
        //얼굴의 위치를 인식할 좌표
        double real_facesize_x = faces[0].x / resizeRatio;
        double real_facesize_y = faces[0].y / resizeRatio;
        double real_facesize_width = faces[0].width / resizeRatio;
        double real_facesize_height = faces[0].height / resizeRatio;

        Point center(real_facesize_x + real_facesize_width / 2, real_facesize_y + real_facesize_height / 2);
        Point start(real_facesize_x, real_facesize_y);
        Point end(real_facesize_x + real_facesize_width, real_facesize_y + real_facesize_height);

        //얼굴모양부근에 사각형을 그린 후 img_result에 저장합니다.
        rectangle(img_result, start, end, Scalar(255, 51, 255), 10, 8, 0);
        face_area = faces[0];

        //얼굴부분 사각형을 그립니다 -> 이후 그만큼의 이미지를 잘라내서 리사이징 합니다.
        Rect rect(real_facesize_x,real_facesize_y,real_facesize_width,real_facesize_height);
        img_gray = img_gray(rect); //잘라냇슴

        resize(img_gray, img_gray, Size(96, 96), 0, 0, 1);
        //오늘은 여기까지
        //img_face.convertTo(img_face,CV_32FC1);
    }

    return num_face;
}
}