package io.evercam.androidapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.ndk.CrashlyticsNdk;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

import io.evercam.API;
import io.evercam.EvercamException;
import io.evercam.User;
import io.evercam.androidapp.authentication.EvercamAccount;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dal.DbCamera;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.tasks.CheckInternetTask;
import io.evercam.androidapp.tasks.CheckKeyExpirationTask;
import io.evercam.androidapp.utils.Commons;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.utils.DataCollector;
import io.evercam.androidapp.utils.PrefsManager;
import io.fabric.sdk.android.Fabric;
import io.intercom.android.sdk.Intercom;

/*
 * Main starting activity. 
 * Checks whether user should login first or load the cameras straight away
 * */
public class MainActivity extends ParentAppCompatActivity
{
    private static final String TAG = "MainActivity";

    private final String SENDER_ID = "40329583114";

    private GoogleCloudMessaging gcm;
    private String registrationId;

    /* For retrying a failed gcm registration */
    private static final long MAX_RETRY = 80000;
    private static long retryTime = 10000;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (isPlayServicesAvailable())
        {
            gcm = GoogleCloudMessaging.getInstance(this);
            registrationId = getRegistrationId(this);
            if (registrationId.isEmpty())
            {
                registerInBackground();
            }
            else
            {
                sendRegistrationIdToBackend(registrationId);
            }
        } else
        {
            Log.i(TAG, "Google Play Services is not available");
        }

        Fabric.with(this, new Crashlytics(), new CrashlyticsNdk());

        setContentView(R.layout.main_activity_layout);

        launch();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        launch();
    }

    private void launch()
    {
        int versionCode = Commons.getAppVersionCode(this);
        boolean isReleaseNotesShown = PrefsManager.isReleaseNotesShown(this, versionCode);

        if(versionCode > 0)
        {
            if(isReleaseNotesShown)
            {
                startApplication();
            }
            else
            {
                Intent act = new Intent(MainActivity.this, ReleaseNotesActivity.class);
                startActivity(act);
                this.finish();
            }
        }
    }

    private void startApplication()
    {
        new MainCheckInternetTask(MainActivity.this).executeOnExecutor(AsyncTask
                .THREAD_POOL_EXECUTOR);
    }

    private void startCamerasActivity()
    {
        if(CamerasActivity.activity != null)
        {
            CamerasActivity.activity.finish();
        }

        this.startActivity(new Intent(this, CamerasActivity.class));

        MainActivity.this.finish();
    }

    public static boolean isUserLogged(Context context)
    {
        AppData.defaultUser = new EvercamAccount(context).getDefaultUser();
        if(AppData.defaultUser != null)
        {
            AppData.evercamCameraList = new DbCamera(context).getCamerasByOwner(AppData
                    .defaultUser.getUsername(), 500);
            API.setUserKeyPair(AppData.defaultUser.getApiKey(), AppData.defaultUser.getApiId());
            return true;
        }
        return false;
    }

    /**
     * Check the API key and ID is valid or not
     *
     * In case the key and ID has already changed by Evercam system
     */
    public static boolean isApiKeyExpired(String username, String apiKey, String apiId)
    {
        try
        {
            API.setUserKeyPair(apiKey, apiId);

            new User(username);
        }
        catch(EvercamException e)
        {
            if(e.getMessage().equals(Constants.API_MESSAGE_UNAUTHORIZED) ||
                    e.getMessage().equals(Constants.API_MESSAGE_INVALID_API_KEY))
            {
                return true;
            }
        }
        return false;
    }

    class MainCheckInternetTask extends CheckInternetTask
    {

        public MainCheckInternetTask(Context context)
        {
            super(context);
        }

        @Override
        protected void onPostExecute(Boolean hasNetwork)
        {
            if(hasNetwork)
            {
                if(isUserLogged(MainActivity.this))
                {
                    AppUser defaultUser = AppData.defaultUser;
                    new CheckKeyExpirationTaskMain(defaultUser.getUsername(),
                            defaultUser.getApiKey(), defaultUser.getApiId())
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
                else
                {
                    finish();
                    Intent slideIntent = new Intent(MainActivity.this, SlideActivity.class);
                    startActivity(slideIntent);
                }
            }
            else
            {
                CustomedDialog.showInternetNotConnectDialog(MainActivity.this);
            }
        }
    }

    class CheckKeyExpirationTaskMain extends CheckKeyExpirationTask
    {
        public CheckKeyExpirationTaskMain(String username, String apiKey, String apiId)
        {
            super(username, apiKey, apiId);
        }

        @Override
        protected void onPostExecute(Boolean isExpired)
        {
            //If API key and ID is no longer valid, show the login page
            if(isExpired)
            {
                new EvercamAccount(MainActivity.this).remove(AppData.defaultUser.getEmail(), null);

                finish();
                Intent slideIntent = new Intent(MainActivity.this, SlideActivity.class);
                startActivity(slideIntent);
            }
            else
            {
                finish();
                startCamerasActivity();
            }
        }
    }

    public boolean isPlayServicesAvailable()
    {
        return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS;
    }

    private String getRegistrationId(Context context)
    {
        SharedPreferences prefs = getSharedPreferences("my_gcm_details", MODE_PRIVATE);
        String registrationId = prefs.getString("my_gcm_registration_id", "");
        if (registrationId.isEmpty())
        {
            Log.d(TAG, "Registration ID not found.");
            return "";
        }
        int registeredVersion = prefs.getInt("my_app_version", Integer.MIN_VALUE);
        int currentVersion = new DataCollector(context).getAppVersionCode();
        if (registeredVersion != 0 && registeredVersion != currentVersion)
        {
            Log.d(TAG, "App version changed.");
            return "";
        }
        Log.d(TAG, "Registration ID = " + registrationId);
        return registrationId;
    }

    private void sendRegistrationIdToBackend(String regId)
    {
        Intercom.client().setupGCM(regId, R.drawable.icon_evercam);
    }

    private void registerInBackground()
    {
        new AsyncTask<Void, Void, Void>()
        {
            @Override
            protected Void doInBackground(Void... params)
            {
                String msg = "";
                try
                {
                    if (gcm == null)
                    {
                        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }
                    registrationId = gcm.register(SENDER_ID);
                    Log.d(TAG, "Registration new ID: " + registrationId);
                    sendRegistrationIdToBackend(registrationId);
                    storeRegistrationId(getApplicationContext(), registrationId);
                    Log.d("GCM_SUCCESS", "Current Device's Registration ID is: " + msg);
                }
                catch (IOException ex)
                {
                    Log.d("GCM_ISSUE", "Error :" + ex.getMessage());
                    //retry the registration after delay
                    new Handler().postDelayed(new Runnable()
                    {
                        @Override public void run()
                        {
                            registerInBackground();
                        }
                    }, retryTime);
                    //increase the time of wait period
                    if (retryTime < MAX_RETRY)
                    {
                        retryTime *=2;
                    }
                }
                return null;
            }
        }.execute(null, null, null);
    }

    private void storeRegistrationId(Context context, String regId)
    {
        SharedPreferences prefs = getSharedPreferences("my_gcm_details", MODE_PRIVATE);
        int appVersion = new DataCollector(context).getAppVersionCode();
        Log.i("GCM_SUCCESS", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("my_gcm_registration_id", regId);
        editor.putInt("my_app_version", appVersion);
        editor.commit();
    }
}