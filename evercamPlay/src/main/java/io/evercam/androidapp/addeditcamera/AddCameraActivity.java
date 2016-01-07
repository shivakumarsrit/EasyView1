package io.evercam.androidapp.addeditcamera;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;

import io.evercam.Model;
import io.evercam.Vendor;
import io.evercam.androidapp.ParentAppCompatActivity;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.ExplanationView;
import io.evercam.androidapp.custom.PortCheckEditText;
import io.evercam.androidapp.tasks.PortCheckTask;

public class AddCameraActivity extends ParentAppCompatActivity
{
    private final String TAG = "AddCameraActivity";

    private ViewFlipper mViewFlipper;

    /** Model selector UI elements*/
    private ModelSelectorFragment mModelSelectorFragment;

    /** Connect camera UI elements */
    private PortCheckEditText mPublicIpEditText;
    private PortCheckEditText mHttpEditText;
    private PortCheckEditText mRtspEditText;
    private TextView mHttpStatusText;
    private TextView mRtspStatusText;
    private ProgressBar mHttpProgressBar;
    private ProgressBar mRtspProgressBar;
    private ExplanationView mConnectExplainView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camera);

        setUpDefaultToolbar();

        mViewFlipper = (ViewFlipper) findViewById(R.id.add_camera_view_flipper);
        FragmentManager fragmentManager = getSupportFragmentManager();

        /** Init UI for model selector screen */
        mModelSelectorFragment = (ModelSelectorFragment)
                fragmentManager.findFragmentById(R.id.add_camera_model_selector_fragment);
        mModelSelectorFragment.hideModelQuestionMark();
        Button modelSelectorViewNextButton = (Button) findViewById(R.id.model_selector_view_next_button);
        TextView reportModelLink = (TextView) findViewById(R.id.report_model_text_link);

        reportModelLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "Report link clicked");
            }
        });

        modelSelectorViewNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                showConnectCameraView();
            }
        });

        /** Init UI for connect camera screen */
        mPublicIpEditText = (PortCheckEditText) findViewById(R.id.external_ip_float_edit_text);
        mHttpEditText = (PortCheckEditText) findViewById(R.id.http_float_edit_text);
        mRtspEditText = (PortCheckEditText) findViewById(R.id.rtsp_float_edit_text);
        mHttpStatusText = (TextView) findViewById(R.id.port_status_text_http);
        mRtspStatusText = (TextView) findViewById(R.id.port_status_text_rtsp);
        mHttpProgressBar = (ProgressBar) findViewById(R.id.progress_bar_http);
        mRtspProgressBar = (ProgressBar) findViewById(R.id.progress_bar_rtsp);
        mConnectExplainView = (ExplanationView) findViewById(R.id.explanation_view_layout);

        mHttpEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean hasFocus)
            {
                if(hasFocus)
                {
                    mHttpEditText.hideStatusViewsOnTextChange(mHttpStatusText);
                    updateMessage(R.string.connect_camera_http_title, R.string.connect_camera_http_message);
                }
                else
                {
                    checkPort(PortCheckTask.PortType.HTTP);
                }
            }
        });

        mRtspEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(hasFocus)
                {
                    mRtspEditText.hideStatusViewsOnTextChange(mRtspStatusText);
                    updateMessage(R.string.connect_camera_rtsp_title, R.string.connect_camera_rtsp_message);
                }
                else
                {
                    checkPort(PortCheckTask.PortType.RTSP);
                }
            }
        });

        mPublicIpEditText.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(hasFocus)
                {
                    mPublicIpEditText.hideStatusViewsOnTextChange(
                            mHttpStatusText, mRtspStatusText);
                    updateMessage(R.string.connect_camera_ip_title, R.string.connect_camera_ip_message);
                }
                else
                {
                    checkPort(PortCheckTask.PortType.HTTP);
                    checkPort(PortCheckTask.PortType.RTSP);
                }
            }
        });
    }

    public void buildSpinnerOnVendorListResult(@NonNull ArrayList<Vendor> vendorList)
    {
        mModelSelectorFragment.buildVendorSpinner(vendorList, null);
    }

    public void buildSpinnerOnModelListResult(@NonNull ArrayList<Model> modelList)
    {
        mModelSelectorFragment.buildModelSpinner(modelList, null);
    }

    private void showModelSelectorView()
    {
        mViewFlipper.setDisplayedChild(0);
        setTitle(R.string.title_choose_model);
    }

    private void showConnectCameraView()
    {
        mViewFlipper.setDisplayedChild(1);
        setTitle(R.string.title_connect_camera);
    }

    private void updateMessage(int titleId, int messageId)
    {
        mConnectExplainView.updateTitle(titleId);
        mConnectExplainView.updateMessage(messageId);
    }

    private void checkPort(PortCheckTask.PortType type)
    {
        if(type == PortCheckTask.PortType.HTTP)
        {
            checkPort(mPublicIpEditText, mHttpEditText, mHttpStatusText, mHttpProgressBar);
        }
        else if(type == PortCheckTask.PortType.RTSP)
        {
            checkPort(mPublicIpEditText, mRtspEditText, mRtspStatusText, mRtspProgressBar);
        }
    }
}
