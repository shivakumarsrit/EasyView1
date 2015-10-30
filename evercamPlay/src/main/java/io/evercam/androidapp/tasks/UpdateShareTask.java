package io.evercam.androidapp.tasks;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import com.nispok.snackbar.Snackbar;
import com.nispok.snackbar.SnackbarManager;

import io.evercam.CameraShare;
import io.evercam.CameraShareInterface;
import io.evercam.CameraShareRequest;
import io.evercam.EvercamException;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.sharing.SharingActivity;

public class UpdateShareTask extends AsyncTask<Void, Void, Boolean>
{
    private final String TAG = "PatchShareTask";
    private CameraShareInterface shareInterface;
    private String  mNewRights;
    private Activity activity;
    private CustomProgressDialog customProgressDialog;
    private String errorMessage;

    public UpdateShareTask(Activity activity, CameraShareInterface shareInterface, String newRights)
    {
        this.activity = activity;
        this.shareInterface = shareInterface;
        mNewRights = newRights;
    }

    @Override
    protected void onPreExecute()
    {
        errorMessage = activity.getString(R.string.unknown_error);
        customProgressDialog = new CustomProgressDialog(activity);
        customProgressDialog.show(activity.getString(R.string.patching_camera));
    }

    @Override
    protected Boolean doInBackground(Void... params)
    {
        return updateShare();
    }

    @Override
    protected void onPostExecute(Boolean isSuccess)
    {
        customProgressDialog.dismiss();

        if(isSuccess)
        {
            //TODO: Replace Snackbar with Android design API
            Snackbar snackbar = Snackbar.with(activity).text(R.string.msg_share_updated)
                    .duration(Snackbar.SnackbarDuration.LENGTH_SHORT)
                    .color(activity.getResources().getColor(R.color.dark_gray_background));
            SnackbarManager.show(snackbar);

            //Update share list in the sharing activity
            FetchShareListTask.launch(SharingActivity.evercamCamera.getCameraId(), activity);
        }
        else
        {
            CustomToast.showInCenterLong(activity, errorMessage);
        }
    }

    protected boolean updateShare()
    {
        //Delete share / share request
        if(mNewRights == null)
        {
            return deleteShare();
        }
        else //Patch camera share / share request
        {
            /**
             * The rights string should never be empty
             * If it's empty, don't break the app and print error message
             */
            if(mNewRights.isEmpty())
            {
                Log.e(TAG, "Right to patch is empty");
            }
            else
            {
                return patchShare(mNewRights);
            }
        }
        return false;
    }

    protected boolean deleteShare()
    {
        try
        {
            if(shareInterface instanceof CameraShare)
            {
                String cameraId = ((CameraShare) shareInterface).getCameraId();
                String userEmail = ((CameraShare) shareInterface).getUserEmail();
                return CameraShare.delete(cameraId, userEmail);
            }
            else if(shareInterface instanceof CameraShareRequest)
            {
                String cameraId = ((CameraShareRequest) shareInterface).getCameraId();
                String userEmail = ((CameraShareRequest) shareInterface).getEmail();
                return CameraShareRequest.delete(cameraId, userEmail);
            }
        }
        catch(EvercamException e)
        {
            errorMessage = e.getMessage();
        }

        return false;
    }

    protected boolean patchShare(String newRights)
    {
        CameraShareInterface patchedShare = null;

        try
        {
            if(shareInterface instanceof CameraShare)
            {
                String cameraId = ((CameraShare) shareInterface).getCameraId();
                String userEmail = ((CameraShare) shareInterface).getUserEmail();

                patchedShare = CameraShare.patch(cameraId, userEmail, newRights);
            }
            else if(shareInterface instanceof CameraShareRequest)
            {
                String cameraId = ((CameraShareRequest) shareInterface).getCameraId();
                String userEmail = ((CameraShareRequest) shareInterface).getEmail();
                patchedShare = CameraShareRequest.patch(cameraId, userEmail, newRights);
            }

            if(patchedShare != null) return true;
        }
        catch(EvercamException e)
        {
            errorMessage = e.getMessage();
        }

        return false;
    }

    public static void launch(Activity activity, CameraShareInterface shareInterface, String newRights)
    {
        new UpdateShareTask(activity, shareInterface, newRights)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }
}
