package io.evercam.androidapp.sharing;

import android.app.Activity;
import android.util.Log;

import io.evercam.Camera;
import io.evercam.EvercamException;
import io.evercam.Right;
import io.evercam.androidapp.utils.Constants;

public class ValidateSharingRunnable implements Runnable
{
    private final String TAG = "ValidateSharingRunnable";
    private String mCameraId;
    private Activity mSharingActivity;

    public ValidateSharingRunnable(SharingActivity sharingActivity, String cameraId)
    {
        mCameraId = cameraId;
        this.mSharingActivity = sharingActivity;
    }

    @Override
    public void run()
    {
        try
        {
            Camera camera = Camera.getById(mCameraId, false);
            Right right = camera.getRights();
            //Log.e(TAG, right.toString());
            if(!right.isFullRight())
            {
                finishActivityAndReloadCamerasOnUi();
            }
        }
        catch(EvercamException e)
        {
            e.printStackTrace();
            finishActivityAndReloadCamerasOnUi();
        }
    }

    private void finishActivityAndReloadCamerasOnUi()
    {
        mSharingActivity.runOnUiThread(new Runnable() {
            @Override
            public void run()
            {
                mSharingActivity.setResult(Constants.RESULT_NO_ACCESS);
                mSharingActivity.finish();
            }
        });
    }
}
