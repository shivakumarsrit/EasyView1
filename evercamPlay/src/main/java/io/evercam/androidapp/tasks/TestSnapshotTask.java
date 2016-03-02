package io.evercam.androidapp.tasks;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;

import io.evercam.Camera;
import io.evercam.Snapshot;
import io.evercam.androidapp.AddEditCameraActivity;
import io.evercam.androidapp.R;
import io.evercam.androidapp.addeditcamera.AddCameraActivity;
import io.evercam.androidapp.custom.CustomProgressDialog;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.feedback.KeenHelper;
import io.evercam.androidapp.feedback.TestSnapshotFeedbackItem;
import io.keen.client.java.KeenClient;

public class TestSnapshotTask extends AsyncTask<Void, Void, Bitmap> {
    private final String TAG = "TestSnapshotTask";
    private String url;
    private String ending;
    private String username;
    private String password;
    private Activity activity;
    private CustomProgressDialog customProgressDialog;
    private String errorMessage = null;

    public TestSnapshotTask(String url, String ending, String username, String password, Activity activity) {
        this.url = url;
        this.ending = ending;
        this.username = username;
        this.password = password;
        this.activity = activity;
    }

    @Override
    protected void onPreExecute() {
        if (activity instanceof AddEditCameraActivity) {
            customProgressDialog = new CustomProgressDialog(activity);
            customProgressDialog.show(activity.getString(R.string.retrieving_snapshot));
        } else if (activity instanceof AddCameraActivity) {
            ((AddCameraActivity) activity).showTestSnapshotProgress(true);
        }
    }

    @Override
    protected Bitmap doInBackground(Void... params) {
        try {
            URL urlObject = new URL(url);
            boolean isReachable = PortCheckTask.isPortOpen(urlObject.getHost(),
                    String.valueOf(urlObject.getPort()));
            if (!isReachable) {
                errorMessage = activity.getString(R.string.snapshot_test_port_closed);
                return null;
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            return null;
        }

        try {
            Snapshot snapshot = Camera.testSnapshot(url, ending, username, password);
            if (snapshot != null) {
                byte[] snapshotData = snapshot.getData();
                return BitmapFactory.decodeByteArray(snapshotData, 0, snapshotData.length);
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        if (activity instanceof AddEditCameraActivity) {
            customProgressDialog.dismiss();
        } else if (activity instanceof AddCameraActivity) {
            ((AddCameraActivity) activity).showTestSnapshotProgress(false);
        }

        KeenClient client = KeenHelper.getClient(activity);

        if (bitmap != null) {
            CustomedDialog.getSnapshotDialog(activity, bitmap).show();

            new TestSnapshotFeedbackItem(activity, AppData.defaultUser.getUsername(), true, true)
                    .setSnapshot_url(url).setCam_username(username).setCam_password(password).sendToKeenIo(client);
        } else {
            String username = "";
            if (AppData.defaultUser != null) {
                username = AppData.defaultUser.getUsername();
            }

            if (errorMessage == null) {
                int messageResourceId = R.string.msg_snapshot_test_failed;
                if (activity instanceof AddCameraActivity) {
                    messageResourceId = R.string.msg_snapshot_test_failed_new;
                }
                CustomToast.showInCenterLong(activity, messageResourceId);
                new TestSnapshotFeedbackItem(activity, username, false, true)
                        .setSnapshot_url(url).setCam_username(username).setCam_password(password).sendToKeenIo(client);
            } else {
                CustomToast.showInCenterLong(activity, errorMessage);
                new TestSnapshotFeedbackItem(activity, username, false, false)
                        .setSnapshot_url(url).setCam_username(username).setCam_password(password).sendToKeenIo(client);
            }
        }
    }
}
