package io.evercam.androidapp.addeditcamera;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;

import io.evercam.Defaults;
import io.evercam.EvercamException;
import io.evercam.Model;
import io.evercam.Vendor;
import io.evercam.androidapp.AddEditCameraActivity;
import io.evercam.androidapp.ParentAppCompatActivity;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.ExplanationView;
import io.evercam.androidapp.custom.PortCheckEditText;
import io.evercam.androidapp.tasks.PortCheckTask;
import io.evercam.androidapp.tasks.TestSnapshotTask;

public class AddCameraActivity extends ParentAppCompatActivity
{
    private final String TAG = "AddCameraActivity";

    private ViewFlipper mViewFlipper;

    /** Model selector */
    private ModelSelectorFragment mModelSelectorFragment;
    private Defaults mSelectedModelDefaults;

    /** Connect camera */
    private PortCheckEditText mPublicIpEditText;
    private PortCheckEditText mHttpEditText;
    private PortCheckEditText mRtspEditText;
    private TextView mHttpStatusText;
    private TextView mRtspStatusText;
    private ProgressBar mHttpProgressBar;
    private ProgressBar mRtspProgressBar;
    private ExplanationView mConnectExplainView;
    private LinearLayout mAuthLayout;
    private CheckBox mAuthCheckBox;
    private EditText mCamUsernameEditText;
    private EditText mCamPasswordEditText;
    private Button mCheckSnapshotButton;
    private ValidateHostInput mValidateHostInput;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camera);

        setUpDefaultToolbar();

        mViewFlipper = (ViewFlipper) findViewById(R.id.add_camera_view_flipper);

        /** Init UI for model selector screen */
        initModelSelectorUI();

        /** Init UI for connect camera screen */
        initConnectCameraUI();
    }

    private void initModelSelectorUI()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
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
    }

    private void initConnectCameraUI()
    {
        mPublicIpEditText = (PortCheckEditText) findViewById(R.id.external_ip_float_edit_text);
        mHttpEditText = (PortCheckEditText) findViewById(R.id.http_float_edit_text);
        mRtspEditText = (PortCheckEditText) findViewById(R.id.rtsp_float_edit_text);
        mHttpStatusText = (TextView) findViewById(R.id.port_status_text_http);
        mRtspStatusText = (TextView) findViewById(R.id.port_status_text_rtsp);
        mHttpProgressBar = (ProgressBar) findViewById(R.id.progress_bar_http);
        mRtspProgressBar = (ProgressBar) findViewById(R.id.progress_bar_rtsp);
        mConnectExplainView = (ExplanationView) findViewById(R.id.explanation_view_layout);
        mAuthCheckBox = (CheckBox) findViewById(R.id.auth_check_box);
        mAuthLayout = (LinearLayout) findViewById(R.id.auth_layout);
        TextView requiredAuthText = (TextView) findViewById(R.id.required_auth_text);
        mCamUsernameEditText = (EditText) findViewById(R.id.cam_username_float_edit_text);
        mCamPasswordEditText = (EditText) findViewById(R.id.cam_password_float_edit_text);
        mCheckSnapshotButton = (Button) findViewById(R.id.check_snapshot_button);
        TextView liveSupportLink = (TextView) findViewById(R.id.live_support_text_link);

        liveSupportLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "Live support link clicked");
            }
        });

        mCheckSnapshotButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(mValidateHostInput.passed())
                {
                    final String username = mCamUsernameEditText.getText().toString();
                    final String password = mCamPasswordEditText.getText().toString();
                    final String externalHost = mPublicIpEditText.getText().toString();
                    final String externalHttp = mHttpEditText.getText().toString();
                    String jpgUrlString = "";
                    try
                    {
                        jpgUrlString = mSelectedModelDefaults.getJpgURL();
                        Log.d(TAG, "Snapshot ending: " + jpgUrlString);
                    }
                    catch(EvercamException e)
                    {
                        e.printStackTrace();
                    }
                    final String jpgUrl = AddEditCameraActivity.buildUrlEndingWithSlash(jpgUrlString);

                    String externalUrl = getString(R.string.prefix_http) + externalHost + ":" + externalHttp;

                    new TestSnapshotTask(externalUrl, jpgUrl, username, password,
                            AddCameraActivity.this).executeOnExecutor(AsyncTask
                            .THREAD_POOL_EXECUTOR);
                }
            }
        });

        View.OnFocusChangeListener showAuthTextListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                showAuthExplanation();
            }
        };
        mCamUsernameEditText.setOnFocusChangeListener(showAuthTextListener);
        mCamPasswordEditText.setOnFocusChangeListener(showAuthTextListener);

        mAuthCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                onAuthCheckedChange(isChecked);
            }
        });

        requiredAuthText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                mAuthCheckBox.setChecked(!mAuthCheckBox.isChecked());
                onAuthCheckedChange(mAuthCheckBox.isChecked());
            }
        });

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

        mValidateHostInput = new ValidateHostInput(mPublicIpEditText,
                mHttpEditText, mRtspEditText) {
            @Override
            public void onHostEmpty()
            {
                mPublicIpEditText.requestFocus();
                CustomToast.showInCenter(AddCameraActivity.this, getString(R.string.host_required));
            }

            @Override
            public void onHttpEmpty()
            {
                mHttpEditText.requestFocus();
                CustomToast.showInCenter(AddCameraActivity.this, getString(R.string.external_http_required));
            }

            @Override
            public void onInvalidHttpPort()
            {
                mHttpEditText.requestFocus();
                CustomToast.showInCenter(AddCameraActivity.this, getString(R.string.msg_port_range_error));
            }

            @Override
            public void onInvalidRtspPort()
            {
                mRtspEditText.requestFocus();
                CustomToast.showInCenter(AddCameraActivity.this, getString(R.string.msg_port_range_error));
            }
        };
    }

    public void onDefaultsLoaded(Model model)
    {
        try
        {
            mSelectedModelDefaults = model.getDefaults();
        }
        catch(EvercamException e)
        {
            e.printStackTrace();
        }
    }

    public void buildSpinnerOnVendorListResult(@NonNull ArrayList<Vendor> vendorList)
    {
        mModelSelectorFragment.buildVendorSpinner(vendorList, null);
    }

    public void buildSpinnerOnModelListResult(@NonNull ArrayList<Model> modelList)
    {
        mModelSelectorFragment.buildModelSpinner(modelList, null);
    }

    private void onAuthCheckedChange(boolean isChecked)
    {
        mAuthLayout.setVisibility(isChecked ? View.VISIBLE : View.GONE);

        if(isChecked)
        {
            mCamUsernameEditText.requestFocus();
            showAuthExplanation();
        }
        else
        {
            mCamUsernameEditText.setText("");
            mCamUsernameEditText.setText("");
        }
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
        updateMessage(0, R.string.connect_camera_explain_message);
    }

    private void updateMessage(int titleId, int messageId)
    {
        mConnectExplainView.updateTitle(titleId);
        mConnectExplainView.updateMessage(messageId);
    }

    private void showAuthExplanation()
    {
        updateMessage(R.string.connect_camera_auth_title, R.string.connect_camera_auth_message);
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
