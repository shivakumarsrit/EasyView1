package io.evercam.androidapp.addeditcamera;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.util.ArrayList;

import io.evercam.Model;
import io.evercam.Vendor;
import io.evercam.androidapp.ParentAppCompatActivity;
import io.evercam.androidapp.R;
import io.evercam.androidapp.addeditcamera.ModelSelectorFragment;

public class AddCameraActivity extends ParentAppCompatActivity
{
    private final String TAG = "AddCameraActivity";

    private ViewFlipper mViewFlipper;
    private ModelSelectorFragment mModelSelectorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_camera);

        setUpDefaultToolbar();

        mViewFlipper = (ViewFlipper) findViewById(R.id.add_camera_view_flipper);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mModelSelectorFragment = (ModelSelectorFragment)
                fragmentManager.findFragmentById(R.id.add_camera_model_selector_fragment);
        mModelSelectorFragment.hideModelQuestionMark();
        Button modelSelectorViewNextButton = (Button) findViewById(R.id.model_selector_view_next_button);
        TextView reportModelLink = (TextView) findViewById(R.id.report_model_text_link);

        reportModelLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "Report link clicked");
            }
        });

        modelSelectorViewNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                showConnectCameraView();
            }
        });
    }

    public void buildSpinnerOnVendorListResult(@NonNull ArrayList<Vendor> vendorList)
    {
        mModelSelectorFragment.buildVendorSpinner(vendorList, null);
    }

    public void buildSpinnerOnModelListResult(@NonNull ArrayList<Model> modelList)
    {
        mModelSelectorFragment.buildModelSpinner(modelList, null);
    }

    private void showModelSelectorView()
    {
        mViewFlipper.setDisplayedChild(0);
        setTitle(R.string.title_choose_model);
    }

    private void showConnectCameraView()
    {
        mViewFlipper.setDisplayedChild(1);
        setTitle(R.string.title_connect_camera);
    }
}
