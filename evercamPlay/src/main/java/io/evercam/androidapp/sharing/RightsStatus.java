package io.evercam.androidapp.sharing;

import android.app.Activity;
import android.util.Log;

import io.evercam.CameraShareInterface;
import io.evercam.Right;
import io.evercam.androidapp.R;
import io.evercam.androidapp.tasks.UpdateShareTask;

public class RightsStatus
{
    private final String TAG = "RightsStatus";
    private String description;
    private String rightString = "";
    private Activity activity;

    public RightsStatus(Activity activity, String description)
    {
        this.activity = activity;
        String fullRightsDescription = activity.getString(R.string.full_rights);
        String readOnlyDescription = activity.getString(R.string.read_only);
        String noAccessDescription = activity.getString(R.string.no_access);

        if(description.equals(fullRightsDescription))
        {
            rightString = Right.FULL_RIGHTS;
        }
        else if(description.equals(readOnlyDescription))
        {
            rightString = Right.READ_ONLY;
        }
        else if(description.equals(noAccessDescription))
        {
            rightString = null;
        }
    }

    public void updateOnShare(CameraShareInterface shareInterface)
    {
        UpdateShareTask.launch(activity, shareInterface, rightString);
    }
}
