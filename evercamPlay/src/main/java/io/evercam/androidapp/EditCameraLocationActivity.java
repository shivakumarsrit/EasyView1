package io.evercam.androidapp;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONException;
import org.json.JSONObject;
import io.evercam.PatchCameraBuilder;
import io.evercam.androidapp.addeditcamera.ValidateHostInput;
import io.evercam.androidapp.custom.CustomedDialog;
import io.evercam.androidapp.dto.EvercamCamera;
import io.evercam.androidapp.tasks.DeleteCameraTask;
import io.evercam.androidapp.tasks.PatchCameraTask;
import io.evercam.androidapp.utils.EnumConstants;
import io.intercom.android.sdk.Intercom;

public class EditCameraLocationActivity extends ParentAppCompatActivity implements OnMapReadyCallback{

        protected GoogleApiClient mGoogleApiClient;
        private GoogleMap mMap;
        Location location;
        LatLng tappedLatLng;
        double lat =53.350140, lng=-6.266155;
        public final static int PERMISSIONS_CODE = 56022;

        private LocationManager locManager;
        private LocationListener myLocationListener;
        private EvercamCamera cameraToUpdate;

        private ValidateHostInput mValidateHostInput;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_edit_camera_location);

            cameraToUpdate = ViewCameraActivity.evercamCamera;

            setUpDefaultToolbar();

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync((OnMapReadyCallback) this);

        }


        @Override
        public void onMapReady(GoogleMap googleMap) {

            mMap = googleMap;
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            addMapTapListner();

            // Add a marker in Sydney and move the camera
            // && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},PERMISSIONS_CODE);
                return;
            }else{

                mMap.setMyLocationEnabled(true);
                addMarkerOnMap();
                addMyLocationButtonClickListner();

//                locationManager.requestLocationUpdates(provider, 2000, 1, (android.location.LocationListener) this);

//                locManager = (LocationManager) getSystemService(LOCATION_SERVICE);


//                mMap.addMarker(new MarkerOptions().position(latLng.latitude,location.getLongitude()))

//                List providersList = locManager.getAllProviders();
//                provider =locManager.getProvider(providersList.get(0));
//                precision = provider.getAccuracy();
//                req = new Criteria();
//                req.setAccuracy(Criteria.ACCURACY_FINE);
            }
        }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_CODE) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                int grantResult = grantResults[i];

                if (permission.equals(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


                        mMap.setMyLocationEnabled(true);
                        addMarkerOnMap();
                        addMyLocationButtonClickListner();

                    }else{
                        //No Permission granted
                        addMarkerOnMap();
                    }
                }
            }
        }
    }


    public void addMarkerOnMap(){
        LatLng loc = new LatLng(cameraToUpdate.getLatitude(), cameraToUpdate.getLongitude());
        mMap.addMarker(new MarkerOptions().position(loc));
        float zoomLevel = (float) 16.0; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, zoomLevel));
    }

    public void addMyLocationButtonClickListner(){
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
            @Override
            public boolean onMyLocationButtonClick()
            {
                //TODO: Any custom actions

                if (ActivityCompat.checkSelfPermission(EditCameraLocationActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    mMap.clear();

                    LocationManager locationManager =
                            (LocationManager)getSystemService(LOCATION_SERVICE);
                    Criteria criteria = new Criteria();
                    String provider = locationManager.getBestProvider(criteria, false);
                    location = locationManager.getLastKnownLocation(provider);
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new
                            LatLng(location.getLatitude(),
                            location.getLongitude()), 15));
                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    tappedLatLng  = latLng;

                    mMap.addMarker(new MarkerOptions().position(latLng));

                    new CallMashapeAsync().execute();
                }

                return false;
            }
        });
    }

    public void addMapTapListner(){
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.v("Lat and Lng",""+latLng.toString());
                mMap.clear();
                tappedLatLng = latLng;
                mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

                new CallMashapeAsync().execute();
            }
        });
    }

    private class CallMashapeAsync extends AsyncTask<String, Integer, HttpResponse<JsonNode>> {

        protected HttpResponse<JsonNode> doInBackground(String... msg) {

            HttpResponse<JsonNode> jsonResponse = null;
            try {
                String latitude = String.valueOf(tappedLatLng.latitude);
                String longitude = String.valueOf(tappedLatLng.longitude);
                jsonResponse = Unirest.get("https://maps.googleapis.com/maps/api/timezone/json?location=" + latitude + "," + longitude + "&timestamp=1482151170&key=AIzaSyAXwqGkwI87v4YoSGCq0FStNXr0_Gy_qj8")
                        .asJson();
                String str = jsonResponse.getBody().toString();
                Log.v("json First",str);
            } catch (UnirestException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return jsonResponse;
        }

        protected void onProgressUpdate(Integer...integers) {
        }

        protected void onPostExecute(HttpResponse<JsonNode> response) {

            String jsonString = response.getBody().toString();

            try {
                JSONObject jsonobj = new JSONObject(jsonString);
                String timeZone =  jsonobj.getString("timeZoneId");
                cameraToUpdate.setTimezone(timeZone);
                cameraToUpdate.setLatitude(tappedLatLng.latitude);
                cameraToUpdate.setLongitude(tappedLatLng.longitude);
                Log.v("Time Zone",timeZone);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    private PatchCameraBuilder buildPatchCameraWithLocalCheck() {

        PatchCameraBuilder patchCameraBuilder = new PatchCameraBuilder(cameraToUpdate.getCameraId());

        patchCameraBuilder.setTimeZone(cameraToUpdate.getTimezone());

        patchCameraBuilder.setLocation(String.format("%.7f",cameraToUpdate.getLatitude()),String.format("%.7f",cameraToUpdate.getLongitude()));

        return patchCameraBuilder;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.activity_edit_camera_location, menu);

        MenuItem supportMenuItem = menu.findItem(R.id.menu_action_support);
        if (supportMenuItem != null) {
            LinearLayout menuLayout = (LinearLayout) LayoutInflater.from(this)
                    .inflate(R.layout.edit_location_menu_item, null);
            supportMenuItem.setActionView(menuLayout);
            supportMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

            menuLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PatchCameraBuilder patchCameraBuilder = buildPatchCameraWithLocalCheck();
                    if (patchCameraBuilder != null) {
                        new PatchCameraTask(patchCameraBuilder.build(),
                                EditCameraLocationActivity.this).executeOnExecutor(AsyncTask
                                .THREAD_POOL_EXECUTOR);
                    } else {
                        Log.e("Log", "Camera to patch is null");
                    }
                }
            });
        }
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem editItem = menu.findItem(R.id.menu_action_edit_camera);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            finish();
        }
        return true;
    }

}
