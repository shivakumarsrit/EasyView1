package io.evercam.androidapp.sharing;

import android.os.Bundle;
import android.view.MenuItem;
import io.evercam.androidapp.ParentAppCompatActivity;
import io.evercam.androidapp.R;
import io.evercam.androidapp.dto.EvercamCamera;
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch(item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
