package io.evercam.androidapp;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import io.evercam.Auth;
import io.evercam.CameraBuilder;
import io.evercam.Defaults;
import io.evercam.EvercamException;
import io.evercam.Model;
import io.evercam.PatchCameraBuilder;
import io.evercam.Vendor;
import io.evercam.androidapp.addeditcamera.ModelSelectorFragment;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.custom.PortCheckEditText;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.tasks.AddCameraTask;
import io.evercam.androidapp.tasks.PatchCameraTask;
import io.evercam.androidapp.tasks.PortCheckTask;
import io.evercam.androidapp.tasks.TestSnapshotTask;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.DataCollector;
import io.evercam.androidapp.video.VideoActivity;
import io.evercam.network.discovery.DiscoveredCamera;
import io.intercom.android.sdk.Intercom;

public class AddEditCameraActivity extends ParentAppCompatActivity
{
    private final String TAG = "AddEditCameraActivity";

    private LinearLayout cameraIdLayout;
    private TextView cameraIdTextView;
    private EditText cameraNameEdit;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private PortCheckEditText externalHostEdit;
    private PortCheckEditText externalHttpEdit;
    private PortCheckEditText externalRtspEdit;
    private EditText jpgUrlEdit;
    private EditText rtspUrlEdit;
    private TextView mHttpStatusTextView;
    private TextView mRtspStatusTextView;
    private ProgressBar mHttpProgressBar;
    private ProgressBar mRtspProgressBar;
    private LinearLayout jpgUrlLayout;
    private LinearLayout rtspUrlLayout;
    private Button addEditButton;
    private ModelSelectorFragment modelSelectorFragment;

    private DiscoveredCamera discoveredCamera;
    private EvercamCamera cameraEdit;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_add_edit_camera);

        setUpDefaultToolbar();

        Bundle bundle = getIntent().getExtras();
        // Edit Camera
        if(bundle != null && bundle.containsKey(Constants.KEY_IS_EDIT))
        {
            EvercamPlayApplication.sendScreenAnalytics(this,
                    getString(R.string.screen_edit_camera));
            cameraEdit = VideoActivity.evercamCamera;

            updateTitleText(R.string.title_edit_camera);
        }
        else
        // Add Camera
        {
            EvercamPlayApplication.sendScreenAnalytics(this, getString(R.string.screen_add_camera));

            // Get camera object from video activity before initial screen
            discoveredCamera = (DiscoveredCamera) getIntent().getSerializableExtra("camera");
        }

        // Initial UI elements
        initialScreen();

        fillDiscoveredCameraDetails(discoveredCamera);

        if(cameraEdit == null)
        {
            //Populate name and IP only when adding camera
            autoPopulateCameraName();
            autoPopulateExternalIP();
        }

        fillEditCameraDetails(cameraEdit);
    }

    @Override
    public void onBackPressed()
    {
        showConfirmQuitIfAddingCamera();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_add_camera, menu);

        MenuItem supportMenuItem = menu.findItem(R.id.menu_action_support);
        if(supportMenuItem != null)
        {
            LinearLayout menuLayout = (LinearLayout) LayoutInflater.from(this)
                    .inflate(R.layout.support_menu_lowercase, null);
            supportMenuItem.setActionView(menuLayout);
            supportMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            menuLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    Intercom.client().displayConversationsList();
                }
            });
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                showConfirmQuitIfAddingCamera();
                return true;
        }
        return true;
    }

    private void showConfirmQuitIfAddingCamera()
    {
        //If edit camera
        if(addEditButton.getText().equals(getString(R.string.save_changes)))
        {
            setResult(Constants.RESULT_FALSE);
            super.onBackPressed();
        }
        //If add camera
        else
        {
            String cameraName = cameraNameEdit.getText().toString();
            String username = usernameEdit.getText().toString();
            String password = passwordEdit.getText().toString();
            String externalHost = externalHostEdit.getText().toString();
            String externalHttp = externalHttpEdit.getText().toString();
            String externalRtsp = externalRtspEdit.getText().toString();
            String jpgUrl = jpgUrlEdit.getText().toString();
            String rtspUrl = rtspUrlEdit.getText().toString();
            if(!(cameraName.isEmpty() && username.isEmpty() && password
                    .isEmpty() && externalHost.isEmpty() && externalHttp.isEmpty() &&
                    externalRtsp.isEmpty() && jpgUrl.isEmpty() && rtspUrl.isEmpty()))
            {
                CustomedDialog.getConfirmCancelAddCameraDialog(this).show();
            }
            else
            {
                setResult(Constants.RESULT_FALSE);
                super.onBackPressed();
            }
        }
    }

    private void initialScreen()
    {
        FragmentManager fragmentManager = getSupportFragmentManager();
        modelSelectorFragment = (ModelSelectorFragment) fragmentManager.findFragmentById(R.id.model_selector_fragment);

        cameraIdLayout = (LinearLayout) findViewById(R.id.add_camera_id_layout);
        cameraIdTextView = (TextView) findViewById(R.id.add_id_txt_view);
        cameraNameEdit = (EditText) findViewById(R.id.add_name_edit);
        ImageView externalIpExplainationImageButton = (ImageView) findViewById(R.id.ip_explanation_btn);
        ImageView httpExplainationImageButton = (ImageView) findViewById(R.id.http_explanation_btn);
        ImageView jpgExplainationImageButton = (ImageView) findViewById(R.id.jpg_explanation_btn);
        ImageView rtspPortExplainationImageButton = (ImageView) findViewById(R.id.rtsp_port_explanation_btn);
        ImageView rtspUrlExplainationImageButton = (ImageView) findViewById(R.id.rtsp_url_explanation_btn);
        usernameEdit = (EditText) findViewById(R.id.add_username_edit);
        passwordEdit = (EditText) findViewById(R.id.add_password_edit);
        externalHostEdit = (PortCheckEditText) findViewById(R.id.add_external_host_edit);
        externalHttpEdit = (PortCheckEditText) findViewById(R.id.add_external_http_edit);
        externalRtspEdit = (PortCheckEditText) findViewById(R.id.add_external_rtsp_edit);
        jpgUrlEdit = (EditText) findViewById(R.id.add_jpg_edit);
        rtspUrlEdit = (EditText) findViewById(R.id.add_rtsp_edit);
        mHttpStatusTextView = (TextView) findViewById(R.id.port_status_text_http);
        mRtspStatusTextView = (TextView) findViewById(R.id.port_status_text_rtsp);
        mHttpProgressBar = (ProgressBar) findViewById(R.id.progress_bar_http);
        mRtspProgressBar = (ProgressBar) findViewById(R.id.progress_bar_rtsp);
        jpgUrlLayout = (LinearLayout) findViewById(R.id.add_jpg_url_layout);
        rtspUrlLayout = (LinearLayout) findViewById(R.id.add_rtsp_url_layout);
        addEditButton = (Button) findViewById(R.id.button_add_edit_camera);
        Button testButton = (Button) findViewById(R.id.button_test_snapshot);

        if(cameraEdit != null)
        {
            addEditButton.setText(getString(R.string.save_changes));
            cameraIdLayout.setVisibility(View.VISIBLE);
        }
        else
        {
            cameraIdLayout.setVisibility(View.GONE);
            addEditButton.setText(getString(R.string.finish_and_add));
        }

        externalIpExplainationImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CustomedDialog.showMessageDialogWithTitle(AddEditCameraActivity.this, R.string
                        .msg_ip_explanation_title, R.string
                        .msg_ip_explanation);
            }
        });
        jpgExplainationImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CustomedDialog.showMessageDialogWithTitle(AddEditCameraActivity.this, R.string
                        .msg_jpg_explanation_title, R.string
                        .msg_jpg_explanation);
            }
        });
        httpExplainationImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CustomedDialog.showMessageDialogWithTitle(AddEditCameraActivity.this, R.string
                        .msg_http_explanation_title, R.string
                        .msg_http_explanation);
            }
        });
        rtspPortExplainationImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CustomedDialog.showMessageDialogWithTitle(AddEditCameraActivity.this, R.string
                        .msg_rtsp_port_explanation_title, R.string
                        .msg_rtsp_port_explanation);
            }
        });
        rtspUrlExplainationImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                CustomedDialog.showMessageDialogWithTitle(AddEditCameraActivity.this, R.string
                        .msg_rtsp_url_explanation_title, R.string
                        .msg_rtsp_url_explanation);
            }
        });

        externalHttpEdit.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View view, boolean hasFocus)
            {
                if(hasFocus)
                {
                    externalHttpEdit.hideStatusViewsOnTextChange(mHttpStatusTextView);
                }
                else
                {
                    checkPort(PortCheckTask.PortType.HTTP);
                }
            }
        });

        externalRtspEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(hasFocus)
                {
                    externalRtspEdit.hideStatusViewsOnTextChange(mRtspStatusTextView);
                }
                else
                {
                    checkPort(PortCheckTask.PortType.RTSP);
                }
            }
        });

        externalHostEdit.setOnFocusChangeListener(new View.OnFocusChangeListener()
        {
            @Override
            public void onFocusChange(View v, boolean hasFocus)
            {
                if(hasFocus)
                {
                    externalHostEdit.hideStatusViewsOnTextChange(
                            mRtspStatusTextView, mHttpStatusTextView);
                }
                else
                {
                    checkPort(PortCheckTask.PortType.HTTP);
                    checkPort(PortCheckTask.PortType.RTSP);
                }
            }
        });

        jpgUrlEdit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(!jpgUrlEdit.isFocusable())
                {
                    CustomedDialog.getMessageDialog(AddEditCameraActivity.this, R.string.msg_url_ending_not_editable).show();
                }
            }
        });

        addEditButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                String externalHost = externalHostEdit.getText().toString();
                if(Commons.isLocalIp(externalHost))
                {
                    CustomedDialog.getStandardAlertDialog(AddEditCameraActivity.this, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            performAddEdit();
                        }
                    }, R.string.msg_local_ip_warning).show();
                }
                else
                {
                    performAddEdit();
                }
            }
        });

        testButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String externalHost = externalHostEdit.getText().toString();
                if(Commons.isLocalIp(externalHost))
                {
                    CustomedDialog.getStandardAlertDialog(AddEditCameraActivity.this, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            launchTestSnapshot();
                        }
                    }, R.string.msg_local_ip_warning).show();
                }
                else
                {
                    launchTestSnapshot();
                }
            }
        });
    }

    private void checkPort(PortCheckTask.PortType type)
    {
        if(type == PortCheckTask.PortType.HTTP)
        {
            checkPort(externalHostEdit,externalHttpEdit, mHttpStatusTextView, mHttpProgressBar);
        }
        else if(type == PortCheckTask.PortType.RTSP)
        {
            checkPort(externalHostEdit, externalRtspEdit, mRtspStatusTextView, mRtspProgressBar);
        }
    }

    private void performAddEdit()
    {
        if(addEditButton.getText().equals(getString(R.string.save_changes)))
        {
            PatchCameraBuilder patchCameraBuilder = buildPatchCameraWithLocalCheck();
            if(patchCameraBuilder != null)
            {
                new PatchCameraTask(patchCameraBuilder.build(),
                        AddEditCameraActivity.this).executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR);
            }
            else
            {
                Log.e(TAG, "Camera to patch is null");
            }
        }
        else
        {
            CameraBuilder cameraBuilder = buildCameraWithLocalCheck();
            if(cameraBuilder != null)
            {
                boolean isFromScan = discoveredCamera != null;

                //Set camera status to be online as a temporary fix for #133
                cameraBuilder.setOnline(true);
                new AddCameraTask(cameraBuilder.build(), AddEditCameraActivity.this,
                        isFromScan).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
            else
            {
                Log.e(TAG, "Camera to add is null");
            }
        }
    }

    private void fillDiscoveredCameraDetails(DiscoveredCamera camera)
    {
        if(camera != null)
        {
            Log.d(TAG, camera.toString());
            if(camera.hasExternalIp())
            {
                externalHostEdit.setText(camera.getExternalIp());
            }
            if(camera.hasExternalHttp())
            {
                externalHttpEdit.setText(String.valueOf(camera.getExthttp()));
            }
            if(camera.hasExternalRtsp())
            {
                externalRtspEdit.setText(String.valueOf(camera.getExtrtsp()));
            }
            if(camera.hasName())
            {
                //The maximum camera name length is 24
                String cameraName = camera.getName();
                if(cameraName.length() > 24)
                {
                    cameraName = cameraName.substring(0, 23);
                }
                cameraNameEdit.setText(cameraName);
            }
            else
            {
                cameraNameEdit.setText((camera.getVendor() + " " + camera.getModel()).toUpperCase());
            }
        }
    }

    /**
     * Auto populate camera name as 'Camera + number'
     */
    private void autoPopulateCameraName()
    {
        if(cameraNameEdit.getText().toString().isEmpty())
        {
            int number = 1;
            boolean matches = true;
            String cameraName;

            while(matches)
            {
                boolean duplicate = false;

                cameraName = "Camera " + number;
                for(EvercamCamera evercamCamera : AppData.evercamCameraList)
                {
                    if(evercamCamera.getName().equals(cameraName))
                    {
                        duplicate = true;
                        break;
                    }
                }

                if(duplicate)
                {
                    number ++;
                }
                else
                {
                    matches = false;
                }
            }

            cameraNameEdit.setText("Camera " + number);
        }
    }

    private void autoPopulateExternalIP()
    {
        /**
         * Auto populate IP as external IP address if on WiFi
         */
        if(new DataCollector(this).isConnectedWifi())
        {
            if(externalHostEdit.getText().toString().isEmpty())
            {

                new AsyncTask<Void, Void, String>()
                {
                    @Override
                    protected String doInBackground(Void... params)
                    {
                        return io.evercam.network.discovery.NetworkInfo.getExternalIP();
                    }

                    @Override
                    protected void onPostExecute(String externalIp)
                    {
                        externalHostEdit.setText(externalIp);
                        autoPopulateDefaultPorts();
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    /**
     * Auto populate default port 80 and 554 and launch port check
     * Only when the port text field is empty
     */
    private void autoPopulateDefaultPorts()
    {
        if(externalHttpEdit.getText().toString().isEmpty())
        {
            externalHttpEdit.setText("80");
            checkPort(PortCheckTask.PortType.HTTP);
        }
        if(externalRtspEdit.getText().toString().isEmpty())
        {
            externalRtspEdit.setText("554");
            checkPort(PortCheckTask.PortType.RTSP);
        }
    }

    private void fillEditCameraDetails(EvercamCamera camera)
    {
        if(camera != null)
        {
            showUrlEndings(!camera.hasModel());

            // Log.d(TAG, cameraEdit.toString());
            cameraIdTextView.setText(camera.getCameraId());
            cameraNameEdit.setText(camera.getName());
            usernameEdit.setText(camera.getUsername());
            passwordEdit.setText(camera.getPassword());
            jpgUrlEdit.setText(camera.getJpgPath());
            rtspUrlEdit.setText(camera.getH264Path());
            externalHostEdit.setText(camera.getExternalHost());
            int externalHttp = camera.getExternalHttp();
            int externalRtsp = camera.getExternalRtsp();
            if(externalHttp != 0)
            {
                externalHttpEdit.setText(String.valueOf(externalHttp));
            }
            if(externalRtsp != 0)
            {
                externalRtspEdit.setText(String.valueOf(externalRtsp));
            }
        }
    }

    public void showUrlEndings(boolean show)
    {
        jpgUrlLayout.setVisibility(show ? View.VISIBLE : View.GONE);
        rtspUrlLayout.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Read and validate user input for add camera.
     */
    private CameraBuilder buildCameraWithLocalCheck()
    {
        String cameraName = cameraNameEdit.getText().toString();

        if(cameraName.isEmpty())
        {
            CustomToast.showInCenter(this, getString(R.string.name_required));
            return null;
        }

        CameraBuilder cameraBuilder = new CameraBuilder(cameraName, false);

        String vendorId = modelSelectorFragment.getVendorIdFromSpinner();
        if(!vendorId.isEmpty())
        {
            cameraBuilder.setVendor(vendorId);
        }

        String modelId = modelSelectorFragment.getModelIdFromSpinner();
        if(!modelId.isEmpty())
        {
            cameraBuilder.setModel(modelId);
        }

        String username = usernameEdit.getText().toString();
        if(!username.isEmpty())
        {
            cameraBuilder.setCameraUsername(username);
        }

        String password = passwordEdit.getText().toString();
        if(!password.isEmpty())
        {
            cameraBuilder.setCameraPassword(password);
        }

        String externalHost = externalHostEdit.getText().toString();
        if(externalHost.isEmpty())
        {
            CustomToast.showInCenter(this, getString(R.string.host_required));
            return null;
        }
        else
        {
            cameraBuilder.setExternalHost(externalHost);

            String externalHttp = externalHttpEdit.getText().toString();
            if(!externalHttp.isEmpty())
            {
                int externalHttpInt = getPortIntByString(externalHttp);
                if(externalHttpInt != 0)
                {
                    cameraBuilder.setExternalHttpPort(externalHttpInt);
                }
                else
                {
                    return null;
                }
            }

            String externalRtsp = externalRtspEdit.getText().toString();
            if(!externalRtsp.isEmpty())
            {
                int externalRtspInt = getPortIntByString(externalRtsp);
                if(externalRtspInt != 0)
                {
                    cameraBuilder.setExternalRtspPort(externalRtspInt);
                }
                else
                {
                    return null;
                }
            }
        }

        String jpgUrl = buildUrlEndingWithSlash(jpgUrlEdit.getText().toString());
        if(!jpgUrl.isEmpty())
        {
            cameraBuilder.setJpgUrl(jpgUrl);
        }

        String rtspUrl = buildUrlEndingWithSlash(rtspUrlEdit.getText().toString());
        if(!rtspUrl.isEmpty())
        {
            cameraBuilder.setH264Url(rtspUrl);
        }

        //Attach additional info for discovered camera as well
        if(discoveredCamera != null)
        {
            cameraBuilder.setInternalHost(discoveredCamera.getIP());

            if(discoveredCamera.hasMac())
            {
                cameraBuilder.setMacAddress(discoveredCamera.getMAC());
            }

            if(discoveredCamera.hasHTTP())
            {
                cameraBuilder.setInternalHttpPort(discoveredCamera.getHttp());
            }

            if(discoveredCamera.hasRTSP())
            {
                cameraBuilder.setInternalRtspPort(discoveredCamera.getRtsp());
            }
        }

        return cameraBuilder;
    }

    /**
     * Convert port string to port int, show error toast if port number is not valid,
     *
     * @return int port number, if port is not valid, return 0.
     */
    private int getPortIntByString(String portString)
    {
        try
        {
            int portInt = Integer.valueOf(portString);
            if(portInt > 0)
            {
                if(portInt <= 65535)
                {
                    return portInt;
                }
                else
                {
                    CustomToast.showInCenter(this, getString(R.string.msg_port_range_error));
                    return 0;
                }
            }
            else
            {
                CustomToast.showInCenter(this, getString(R.string.msg_port_range_error));
                return 0;
            }
        }
        catch(NumberFormatException e)
        {
            CustomToast.showInCenter(this, getString(R.string.msg_port_range_error));
            return 0;
        }
    }

    /**
     * Read and validate user input for edit camera.
     */
    private PatchCameraBuilder buildPatchCameraWithLocalCheck()
    {
        PatchCameraBuilder patchCameraBuilder = new PatchCameraBuilder(cameraEdit.getCameraId());

        String cameraName = cameraNameEdit.getText().toString();
        if(cameraName.isEmpty())
        {
            CustomToast.showInCenter(this, getString(R.string.name_required));
            return null;
        }
        else if(!cameraName.equals(cameraEdit.getName()))
        {
            patchCameraBuilder.setName(cameraName);
        }

        String vendorId = modelSelectorFragment.getVendorIdFromSpinner();
        patchCameraBuilder.setVendor(vendorId);

        String modelName = modelSelectorFragment.getModelIdFromSpinner();
        patchCameraBuilder.setModel(modelName);

        String username = usernameEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        if(!username.equals(cameraEdit.getUsername()) || !password.equals(cameraEdit.getPassword()))
        {
            patchCameraBuilder.setCameraUsername(username);
            patchCameraBuilder.setCameraPassword(password);
        }

        String externalHost = externalHostEdit.getText().toString();
        if(externalHost.isEmpty())
        {
            CustomToast.showInCenter(this, getString(R.string.host_required));
            return null;
        }
        else
        {
            patchCameraBuilder.setExternalHost(externalHost);

            String externalHttp = externalHttpEdit.getText().toString();
            if(!externalHttp.isEmpty())
            {
                int externalHttpInt = getPortIntByString(externalHttp);
                if(externalHttpInt != 0)
                {
                    patchCameraBuilder.setExternalHttpPort(externalHttpInt);
                }
                else
                {
                    return null;
                }
            }

            String externalRtsp = externalRtspEdit.getText().toString();
            if(!externalRtsp.isEmpty())
            {
                int externalRtspInt = getPortIntByString(externalRtsp);
                if(externalRtspInt != 0)
                {
                    patchCameraBuilder.setExternalRtspPort(externalRtspInt);
                }
                else
                {
                    return null;
                }
            }
        }

        String jpgUrl = buildUrlEndingWithSlash(jpgUrlEdit.getText().toString());
        if(!jpgUrl.equals(cameraEdit.getJpgPath()))
        {
            patchCameraBuilder.setJpgUrl(jpgUrl);
        }

        String rtspUrl = buildUrlEndingWithSlash(rtspUrlEdit.getText().toString());
        if(!rtspUrl.equals(cameraEdit.getH264Path()))
        {
            patchCameraBuilder.setH264Url(rtspUrl);
        }

        return patchCameraBuilder;
    }

    public void fillDefaults(Model model)
    {
        try
        {
            // FIXME: Sometimes vendor with no default model, contains default
            // jpg url.
            // TODO: Consider if no default values associated, clear defaults
            // that has been filled.
            Defaults defaults = model.getDefaults();
            Auth basicAuth = defaults.getAuth(Auth.TYPE_BASIC);
            if(basicAuth != null)
            {
                usernameEdit.setText(basicAuth.getUsername());
                passwordEdit.setText(basicAuth.getPassword());
            }
            jpgUrlEdit.setText(defaults.getJpgURL());
            rtspUrlEdit.setText(defaults.getH264URL());

            if(!model.getName().equals(Model.DEFAULT_MODEL_NAME) && !jpgUrlEdit.getText().toString().isEmpty())
            {
                //If user specified a specific model, make it not editable
                jpgUrlEdit.setFocusable(false);
                jpgUrlEdit.setClickable(true);
            }
            else
            {
                //For default model or
                jpgUrlEdit.setFocusable(true);
                jpgUrlEdit.setClickable(true);
                jpgUrlEdit.setFocusableInTouchMode(true);
            }
        }
        catch(EvercamException e)
        {
            Log.e(TAG, "Fill defaults: " + e.toString());
        }
    }
    public void clearDefaults()
    {
        usernameEdit.setText("");
        passwordEdit.setText("");
        jpgUrlEdit.setText("");
        rtspUrlEdit.setText("");

        //Make it editable when defaults are cleared
        jpgUrlEdit.setFocusable(true);
        jpgUrlEdit.setClickable(true);
        jpgUrlEdit.setFocusableInTouchMode(true);
    }

    public static String buildUrlEndingWithSlash(String originalUrl)
    {
        String jpgUrl = "";
        if(originalUrl != null && !originalUrl.equals(""))
        {
            if(!originalUrl.startsWith("/"))
            {
                jpgUrl = "/" + originalUrl;
            }
            else
            {
                jpgUrl = originalUrl;
            }
        }
        return jpgUrl;
    }

    private void launchTestSnapshot()
    {
        String externalHost = externalHostEdit.getText().toString();

        if(externalHost.isEmpty())
        {
            CustomToast.showInCenter(this, getString(R.string.host_required));
        }
        else
        {
            final String username = usernameEdit.getText().toString();
            final String password = passwordEdit.getText().toString();
            String jpgUrlString = jpgUrlEdit.getText().toString();
            final String jpgUrl = buildUrlEndingWithSlash(jpgUrlString);

            String externalUrl = getExternalUrl();
            if(externalUrl != null)
            {
                new TestSnapshotTask(externalUrl, jpgUrl, username, password,
                        AddEditCameraActivity.this).executeOnExecutor(AsyncTask
                        .THREAD_POOL_EXECUTOR);
            }
        }
    }

    /**
     * Check external HTTP port is filled or not and return external URL with
     * snapshot ending.
     */
    private String getExternalUrl()
    {
        String externalHost = externalHostEdit.getText().toString();
        String externalHttp = externalHttpEdit.getText().toString();
        if(externalHttp.isEmpty())
        {
            CustomToast.showInCenter(this, getString(R.string.external_http_required));
            return null;
        }
        else
        {
            int externalHttpInt = getPortIntByString(externalHttp);
            if(externalHttpInt != 0)
            {
                return getString(R.string.prefix_http) + externalHost + ":" + externalHttp;
            }
            else
            {
                return null;
            }
        }
    }

    public boolean isFromDiscoverAndHasVendor()
    {
        return discoveredCamera != null && discoveredCamera.hasVendor();
    }

    public DiscoveredCamera getDiscoveredCamera()
    {
        return discoveredCamera;
    }

    public void buildSpinnerOnModelListResult(@NonNull ArrayList<Model> modelList)
    {
        if(cameraEdit != null && !cameraEdit.getModel().isEmpty())
        {
            modelSelectorFragment.buildModelSpinner(modelList, cameraEdit.getModel());
        }
        else if(discoveredCamera != null && discoveredCamera.hasModel())
        {
            modelSelectorFragment.buildModelSpinner(modelList, discoveredCamera.getModel());
        }
        else
        {
            modelSelectorFragment.buildModelSpinner(modelList, null);
        }
    }

    public void buildSpinnerOnVendorListResult(@NonNull ArrayList<Vendor> vendorList)
    {
        // If the camera has vendor, show as selected in spinner
        if(cameraEdit != null && !cameraEdit.getVendor().isEmpty())
        {
            modelSelectorFragment.buildVendorSpinner(vendorList, cameraEdit.getVendor());
        }
        else
        {
            modelSelectorFragment.buildVendorSpinner(vendorList, null);
        }
    }
}
