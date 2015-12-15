package io.evercam.androidapp.feedback;

import android.content.Context;
import android.util.Log;

import com.github.sendgrid.SendGrid;

import io.evercam.User;
import io.evercam.androidapp.dto.AppData;
import io.evercam.androidapp.dto.AppUser;
import io.evercam.androidapp.utils.DataCollector;
import io.evercam.androidapp.utils.PropertyReader;

public class FeedbackSender
{
    private final String TAG = "evercam-FeedbackSender";
    private final String TO_EMAIL = "play@evercam.io";
    private final String TITLE_FEEDBACK = "Evercam Android Feedback";
    private Context context;
    private SendGrid sendgrid;

    public FeedbackSender(Context context)
    {
        this.context = context;

        PropertyReader propertyReader = new PropertyReader(context);
        String sandGridUsername = propertyReader.getPropertyStr(PropertyReader
                .KEY_SENDGRID_USERNAME);
        String sandGridPassword = propertyReader.getPropertyStr(PropertyReader
                .KEY_SENDGRID_PASSWORD);
        sendgrid = new SendGrid(sandGridUsername, sandGridPassword);
    }

    public void send(String feedbackString, String cameraId)
    {
        if(sendgrid != null)
        {
            AppUser user = AppData.defaultUser;
            String fullName = "";
            String userEmail = "";

            sendgrid.setFrom(user.getEmail());

            try
            {
                User evercamUser = new User(user.getUsername());
                fullName = evercamUser.getFirstName() + " " + evercamUser.getLastName();
                userEmail = evercamUser.getEmail();
            }
            catch(Exception e)
            {
                Log.e(TAG, e.toString());
            }

            DataCollector dataCollector = new DataCollector(context);
            sendgrid.addTo(TO_EMAIL);
            if(!userEmail.isEmpty())
            {
                sendgrid.setFrom(userEmail);
            }
            sendgrid.setSubject(TITLE_FEEDBACK);

            String contentString = fullName + " says: \n\n" + feedbackString + "\n\n\n\nApp version: " +
                    dataCollector.getAppVersionName() + "\n\nDevice: " + DataCollector.getDeviceName() +
                    "\n\nAndroid version: " + DataCollector.getAndroidVersion() + "\n\nNetwork: " + dataCollector.getNetworkString();

            if(cameraId != null)
            {
                contentString += "\n\nCamera ID: " + cameraId;
            }
            sendgrid.setText(contentString);

            String response = sendgrid.send();
            Log.d(TAG, "Sendgrid response: " + response);
        }
    }
}
