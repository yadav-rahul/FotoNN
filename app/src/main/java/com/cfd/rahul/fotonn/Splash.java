package com.cfd.rahul.fotonn;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.cfd.rahul.fotonn.ui.GADownloadingView;


public class Splash extends Activity {

    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mProgress += 10;
            if (!isSuccess && mProgress >= 60) {
                mGADownloadingView.onFail();
            }
            mGADownloadingView.updateProgress(mProgress);
            mHandler.postDelayed(mRunnable, UPDATE_PROGRESS_DELAY);
        }
    };

    private static final int UPDATE_PROGRESS_DELAY = 500;

    private GADownloadingView mGADownloadingView;
    private View mShowSuccess, mShowFailed;
    private int mProgress;
    private Handler mHandler = new Handler();
    private boolean isSuccess = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);

        mGADownloadingView = (GADownloadingView) findViewById(R.id.ga_downloading);

        mGADownloadingView.releaseAnimation();
        mHandler.removeCallbacks(mRunnable);
        isSuccess = true;
        mProgress = 0;
        mGADownloadingView.performAnimation();
        mGADownloadingView.updateProgress(0);
        mHandler.postDelayed(mRunnable, 0);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(Splash.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }, 6600);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnable);
    }
}


