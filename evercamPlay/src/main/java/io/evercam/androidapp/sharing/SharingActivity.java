package io.evercam.androidapp.sharing;

import android.content.Context;
import android.graphics.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.evercam.CameraShare;
import io.evercam.CameraShareInterface;
import io.evercam.CameraShareRequest;
import io.evercam.PatchCameraBuilder;
import io.evercam.Right;
import io.evercam.androidapp.ParentAppCompatActivity;
import io.evercam.androidapp.R;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.tasks.FetchShareListTask;
import io.evercam.androidapp.tasks.PatchCameraTask;
import io.evercam.androidapp.video.VideoActivity;
import io.evercam.network.discovery.DiscoveredCamera;

public class SharingActivity extends ParentAppCompatActivity
{
    private static final String TAG = "SharingActivity";

    private static EvercamCamera evercamCamera;

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

    public static class SharingListFragment extends ListFragment
    {
        private ImageView mSharingStatusImageView;
        private TextView mSharingStatusTextView;
        private TextView mSharingStatusDetailTextView;

        private ShareListArrayAdapter mShareAdapter;
        private List<CameraShareInterface> mShareList = new ArrayList<>();

        @Override
        public void onListItemClick(ListView listView, View view, int position, long id)
        {
            //If list header is clicked
            if(position == 0)
            {
                CustomedDialog.getShareStatusDialog(this).show();
            }
            else //If share item is clicked
            {
                CameraShareInterface shareInterface = mShareList.get(position - 1);
                Log.e(TAG, position + " clicked" + shareInterface.toString());
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState)
        {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState)
        {
            super.onActivityCreated(savedInstanceState);

            View headerView = getActivity().getLayoutInflater().inflate(R.layout.share_list_header,getListView(),false);

            mShareAdapter = new ShareListArrayAdapter(getActivity(),
                    R.layout.share_list_item, mShareList);

            setListAdapter(null);
            //Add header for the sharing status
            getListView().addHeaderView(headerView);
            //Remove divider from list
            getListView().setDivider(null);

            setListAdapter(mShareAdapter);

            mSharingStatusImageView = (ImageView) headerView.findViewById(R.id.share_status_icon_image_view);
            mSharingStatusTextView = (TextView) headerView.findViewById(R.id.sharing_status_text_view);
            mSharingStatusDetailTextView = (TextView) headerView.findViewById(R.id.sharing_status_detail_text_view);

            retrieveSharingStatusFromCamera();
            launchFetchSharingTask();
        }

        private void launchFetchSharingTask()
        {
            new FetchShareListTask(evercamCamera.getCameraId(), getActivity())
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        public void updateShareListOnUi(ArrayList<CameraShareInterface> shareList)
        {
            mShareList.clear();
            mShareList.addAll(shareList);
            mShareAdapter.notifyDataSetChanged();
        }

        public void retrieveSharingStatusFromCamera()
        {
            SharingStatus status = new SharingStatus(evercamCamera.isDiscoverable(),
                    evercamCamera.isPublic());

            updateSharingStatusUi(status);
        }

        public void updateSharingStatusUi(SharingStatus status)
        {
            mSharingStatusImageView.setImageResource(status.getImageResourceId());
            mSharingStatusTextView.setText(status.getStatusStringId());
            mSharingStatusDetailTextView.setText(status.getStatusDetailStringId());
        }

        public void patchSharingStatusAndUpdateUi(SharingStatus status)
        {
            new PatchCameraTask(buildPatchCamera(status).build(),
                    getActivity()).executeOnExecutor(AsyncTask
                    .THREAD_POOL_EXECUTOR);
        }

        private PatchCameraBuilder buildPatchCamera(SharingStatus status)
        {
            PatchCameraBuilder patchCameraBuilder = new PatchCameraBuilder(evercamCamera.getCameraId());
            patchCameraBuilder.setPublic(status.isPublic()).setDiscoverable(status.isDiscoverable());
            return patchCameraBuilder;
        }
    }

    public static class ShareListArrayAdapter extends ArrayAdapter<CameraShareInterface>
    {
        private List<CameraShareInterface> mCameraShareList;

        public ShareListArrayAdapter(Context context, int resource, List<CameraShareInterface>
                objects)
        {
            super(context, resource, objects);
            mCameraShareList = objects;
        }

        @Override
        public int getCount()
        {
            return mCameraShareList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view = convertView;
            if (view == null)
            {
                LayoutInflater layoutInflater = (LayoutInflater)getContext().getSystemService(Context
                        .LAYOUT_INFLATER_SERVICE);
                view = layoutInflater.inflate(R.layout.share_list_item, null);
            }

            TextView fullNameTextView = (TextView) view.findViewById(R.id.sharing_fullname_text_view);
            TextView emailTextView = (TextView) view.findViewById(R.id.sharing_email_text_view);
            TextView statusTextView = (TextView) view.findViewById(R.id.sharing_item_status_text_view);
            statusTextView.setText("");

            CameraShareInterface cameraShareInterface = mCameraShareList.get(position);

            if(cameraShareInterface != null)
            {
                Right rights = null;

                if(cameraShareInterface instanceof CameraShare)
                {
                    fullNameTextView.setText(((CameraShare) cameraShareInterface).getFullName());
                    emailTextView.setText(((CameraShare) cameraShareInterface).getUserEmail());
                    rights = ((CameraShare) cameraShareInterface).getRights();
                }
                else if(cameraShareInterface instanceof CameraShareRequest)
                {
                    fullNameTextView.setText(((CameraShareRequest) cameraShareInterface).getEmail());
                    emailTextView.setText(R.string.pending);
                    rights = ((CameraShareRequest) cameraShareInterface).getRights();
                }

                if(rights != null)
                {
                    if(rights.isFullRight()) statusTextView.setText(R.string.full_rights);
                    else if(rights.isReadOnly()) statusTextView.setText(R.string.read_only);
                }
            }

            return view;
        }
    }
}
