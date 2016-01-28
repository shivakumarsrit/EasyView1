package io.evercam.androidapp.addeditcamera;

import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import io.evercam.androidapp.ParentAppCompatActivity;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomToast;
import io.evercam.androidapp.tasks.PortCheckTask;

public class AddCameraParentActivity extends ParentAppCompatActivity {

    protected void checkPort(EditText ipEditText, EditText portEditText, TextView statusView, ProgressBar progressBar) {
        String ipText = ipEditText.getText().toString();

        if (!ipText.isEmpty()) {
            String httpText = portEditText.getText().toString();
            if (!httpText.isEmpty()) {
                launchPortCheckTask(ipText, httpText, statusView, progressBar);
            }
        }
    }

    protected void launchPortCheckTask(String ip, String port, TextView statusView, ProgressBar progressBar) {
        new PortCheckTask(ip, port, getApplicationContext()).bindStatusView(statusView).bindProgressView(progressBar)
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected void showLocalIpWarning() {
        CustomToast.showInCenterLong(this, R.string.msg_local_ip_warning);
    }
}
