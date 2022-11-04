package com.slieter.opencv_test;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;

// this activity implements CameraBridgeViewBase and therefore needs to implement
// some standard functions (OnCameraViewStarted, ...Stopped, ...)
public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "MainActivity";
    private Mat mRGBA, mRGBA_;      // colorimage + 90° rotated image
    private Mat mRGBAT, mRGBAT_;    // grayscaleimage + 90° rotated image
    CameraBridgeViewBase cameraBridgeViewBase;

    //
    BaseLoaderCallback baseLoaderCallback= new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) throws IOException {

            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "onManagerConnected: OpenCV loaded");
                    cameraBridgeViewBase.enableView();
                }
            }
        }
    };

    // when activity is created, this function is called
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO}, 1);
        setContentView(R.layout.activity_main);

        cameraBridgeViewBase=(CameraBridgeViewBase)findViewById(R.id.camera_surface);
        cameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        cameraBridgeViewBase.setCvCameraViewListener(this);
    }

    // first test if OpenCV works
    /*static{

        if(OpenCVLoader.initDebug()){
            Log.d(TAG, "Check, OpenCV is configured successfully");
        } else{
            Log.d(TAG, "Check, OpenCV is unfortunately not configured successfully");
        }
    }*/

    // function gets called first time when CameraView was started
    // and allocates all used variables like mats, ...
    @Override
    public void onCameraViewStarted(int width, int height) {
        mRGBA = new Mat(height, width, CvType.CV_8UC4);
        mRGBAT = new Mat(height, width, CvType.CV_8UC1);
        mRGBA_ = new Mat(mRGBA.width(), mRGBA.height(), CvType.CV_8UC4);
        mRGBAT_= new Mat(mRGBA.width(), mRGBA.height(), CvType.CV_8UC1);
    }

    // when cameraView is stopped, release memory
    @Override
    public void onCameraViewStopped() {
        mRGBA.release();
        mRGBAT.release();
        mRGBA_.release();
        mRGBAT_.release();
    }

    // everytime a new frame arrives, assign values to mats
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        // order of channels need to be changed
        Imgproc.cvtColor(inputFrame.rgba(), mRGBA, Imgproc.COLOR_BGRA2RGBA);
        mRGBAT = inputFrame.gray();
        return mRGBA;
    }

    // when app is closed and opened again (resumed), make sure to restart OpenCV
    @Override
    protected void onResume() {
        super.onResume();
        if(OpenCVLoader.initDebug()){
            // if loaded successfully
            Log.d(TAG, "onResume: OpenCV initialized");
            try {
                baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
            Log.d(TAG, "onResume: OpenCV not initialized");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, baseLoaderCallback);
        }
    }

    // before accessing sensors and other sensitive data on android,
    // need to request permission by user
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0) {
            Log.v(TAG, "Permission: " + permissions[0] + "was " + grantResults[0]);
        }

        // if request denied, return empty array
        switch(requestCode){
            case 1:{
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    cameraBridgeViewBase.setCameraPermissionGranted();
                }
                else{
                    // permission denied

                }
                return;
            }
        }
    }
}