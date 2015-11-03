package io.evercam.androidapp.sharing;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import io.evercam.androidapp.ParentAppCompatActivity;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomSnackbar;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.tasks.FetchShareListTask;
import io.evercam.androidapp.utils.Constants;
import io.evercam.androidapp.video.VideoActivity;

public class SharingActivity extends ParentAppCompatActivity
{
    private static final String TAG = "SharingActivity";

    public static EvercamCamera evercamCamera;

    public SharingListFragment sharingListFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        evercamCamera = VideoActivity.evercamCamera;

        setContentView(R.layout.activity_sharing);

        setUpDefaultToolbar();

        sharingListFragment = new SharingListFragment();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, sharingListFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sharing_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;

            case R.id.menu_create_share:
                Intent createShareIntent = new Intent(this, CreateShareActivity.class);
                startActivityForResult(createShareIntent, Constants.REQUEST_CODE_SHARE);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == Constants.REQUEST_CODE_SHARE)
        {
            FetchShareListTask.launch(SharingActivity.evercamCamera.getCameraId(), this);

            if(resultCode == Constants.RESULT_SHARE_CREATED)
            {
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        CustomSnackbar.show(SharingActivity.this, R.string.msg_share_created);
                    }
                }, 1000);
            }
            else if(resultCode == Constants.RESULT_SHARE_REQUEST_CREATED)
            {
                new Handler().postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        CustomSnackbar.showMultiLine(SharingActivity.this, R.string.msg_share_request_created);
                    }
                }, 1000);
            }
        }
    }
}
