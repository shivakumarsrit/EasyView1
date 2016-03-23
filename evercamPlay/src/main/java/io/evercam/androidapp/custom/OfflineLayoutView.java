package io.evercam.androidapp.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import io.evercam.androidapp.R;

public class OfflineLayoutView extends RelativeLayout {

    private ImageView mRefreshImageView;
    private ProgressBar mRefreshProgressBar;

    public OfflineLayoutView(Context context) {
        super(context);
    }

    public OfflineLayoutView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OfflineLayoutView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void show() {
        show(true);
    }

    public void hide() {
        show(false);
    }

    public void startProgress() {
        showProgressView(true);
    }

    public void stopProgress() {
        showProgressView(false);
    }

    private void initChildenViews() {
        mRefreshImageView = (ImageView) findViewById(R.id.offline_refresh_image_view);
        mRefreshProgressBar = (ProgressBar) findViewById(R.id.offline_refresh_progress_bar);
    }

    private void show(boolean show) {
        setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showProgressView(boolean show) {
        mRefreshProgressBar.setVisibility(show ? View.GONE : View.VISIBLE);
        mRefreshImageView.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
