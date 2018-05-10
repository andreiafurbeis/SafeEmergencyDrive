package com.sed.andrearubeis.safeemergencydrive;


import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Location;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Sed extends AppCompatActivity implements OnMapReadyCallback {

    private static final int DEFAULT_ZOOM = 18;
    private static final String TAG = "Main";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    ImageView locateMeButton;
    ImageView iconVehicle;
    private GoogleMap mMap;
    LatLng mDefaultLocation;
    FusedLocationProviderClient mFusedLocationProviderClient;
    PlaceDetectionClient mPlaceDetectionClient;
    GeoDataClient mGeoDataClient;
    Boolean mLocationPermissionGranted;
    Location mLastKnownLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sed);

        locateMeButton = (ImageView) findViewById(R.id.sed_locate_icon);
        iconVehicle = (ImageView) findViewById(R.id.sed_vehicle_icon);
        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.sed_map);
        mapFragment.getMapAsync(this);
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        }else{
            mLocationPermissionGranted = false;
        }


        locateMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateLocationUI();
                getDeviceLocation();

            }
        });




    }


    private void updateLocationUI() {
        Log.d(TAG, "Sono dentro all'UpdateLocationUI");

        if (mMap == null) {
            return;
        }
        try {
            if(mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                //mMap.getUiSettings().setMyLocationButtonEnabled(true);
            }else{
                mMap.setMyLocationEnabled(false);
                //mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        }
        catch(SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    private void getDeviceLocation() {
        Log.d(TAG, "Sono dentro al GetDeviceLocation");

        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = (Location) task.getResult();
                            //Preparo la nuova vista della mappa
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()))
                                    .zoom(DEFAULT_ZOOM)
                                    .tilt(20)
                                    .build();
                            //Faccio partire l'animazione
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition),3000,null);
                            //CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom( new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()),DEFAULT_ZOOM);
                            //mMap.animateCamera(cameraUpdate,3000,null);


                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                        }
                    }
                });
            }
        } catch(SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    private void getLocationPermission() {
        Log.d(TAG, "Sono dentro al GetLocationPermission");

        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        Log.d(TAG, "Sono dentro al OnRequestPermissionResult");

        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //buildGoogleAPIClient();
    }

    /*private void buildGoogleAPIClient() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }*/

    /*@Override
    public void onConnected(@Nullable Bundle bundle) {
        //findLocation();
    }*/

    protected void onStart() {
        //mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        //mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        // Turn on the My Location layer and the related control on the map.

        updateLocationUI();

        // Get the current location of the device and set the position of the map.

        getDeviceLocation();
        //getPosition();
    }

    /*@Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Connection suspended", Toast.LENGTH_SHORT).show();
    }*/

    /*@Override
    public void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, CONNECTION_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                // There was an error with the resolution intent. Try again.
                mGoogleApiClient.connect();
            }
        } else {
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), this, 1);
            dialog.show();
        }
    }*/

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONNECTION_RESOLUTION_REQUEST && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }*/

    /*private void findLocation() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_PERMISSION_REQUEST);
        } else {

            //mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //if(mLastLocation != null) {


                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations, this can be null.
                                if (location != null) {
                                    // Logic to handle location object
                                    LatLng myLat = new LatLng(location.getLatitude(), location.getLongitude());
                                    // Add a marker in Sydney and move the camera
                                    LatLng sydney = new LatLng(-34, 151);
                                    mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(myLat));
                                }else{
                                    Log.d("Main","location é null");
                                }
                            }
                        });



        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_LOCATION_PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    findLocation();
                }
            }
        }
    }*/


    //Aggiunto dal vecchio progetto

    /**
     * Fa una chiamata al db e scarica l'ultima posizione aggiornata , e la mostra sulla mappa
     *
     */
    public void getPosition() {

        String url = "http://washit.dek4.net/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI service = retrofit.create(RetrofitAPI.class);
        Call<Gps> call = service.getGpsPosition();
        call.enqueue(new Callback<Gps>() {
            @Override
            public void onResponse(Call<Gps> call, Response<Gps> response) {
                Log.d(" mainAction", "  response " + response.body().getInfo());

                final Gps posizione = new Gps();
                posizione.setInfo(response.body().getInfo());
                posizione.setTimeIstant(response.body().getTimeIstant());
                posizione.setValidity(response.body().getValidity());
                posizione.setLatitude(response.body().getLatitude());
                posizione.setLongitude(response.body().getLongitude());
                posizione.setVelocity(response.body().getVelocity());
                posizione.setPosDate(response.body().getPosDate());
                posizione.setVehicle(response.body().getVehicle());

                Log.d("Main", posizione.toString());

                goToLocation(posizione);

            }

            @Override
            public void onFailure(Call<Gps> call, Throwable t) {
                Log.d("MainActivity ", "  error " + t.toString());

            }
        });
    }

    public void goToLocation(final Gps posizione) {


        /*mapBox.setCameraPosition(new CameraPosition.Builder()
                .target(posizione.getPosition())
                .zoom(11)
                .build());*/


            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(posizione.getPosition())
                    .zoom(DEFAULT_ZOOM)                                 // Sets the center of the map to Maracanã
                    .bearing(270)                               // Sets the orientation of the camera to look west
                    .tilt(20)                                   // Sets the tilt of the camera to 30 degrees
                    .build();                                   // Creates a CameraPosition from the builder

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 6000, null);
            if (posizione.getVehicle().equals("1")) {
                //Ambulanza
                iconVehicle.setImageResource(R.drawable.icona_ambulanza);
            } else {
                //Vigili del fuoco
                iconVehicle.setImageResource(R.drawable.icona_vigili_fuoco);

            }


        /*mapBox.animateCamera(CameraUpdateFactory.newLatLngZoom(
                new LatLng(posizione.getPosition().getLatitude() , posizione.getPosition().getLongitude()),11));*/
        /*mapBox.addMarker(new MarkerOptions()
                .position(posizione.getPosition())
                .title("Ambulanza")
                .snippet("Attenzione")
        );*/
    }

    //ALTRO CODICE VECCHIO PROGETTO - runnable con aggiornamento posizione tramite file GeoJSON
    /*

    //DICHIARAZIONE
    Handler handler;
    Runnable runnable;


     public void addLayerEmergency() {
        try {
            //Log.d("Main" , "Aggiunto il nuovo source1");

            mapBox.addSource(new GeoJsonSource(ID,new URL(URL_GET_DATA)));
            //Log.d("Main" , "Aggiunto il nuovo source2");
        } catch (MalformedURLException e) {
            Log.d("Main" , "Error Source");

            e.printStackTrace();
        }

        SymbolLayer layer = new SymbolLayer(ID,ID);
        layer.setProperties(iconImage("rocket-15"));
        mapBox.addLayer(layer);

        handler = new Handler();
        runnable = new RefreshData(mapBox,handler);
        handler.postDelayed(runnable , 300);
    }

    private class RefreshData implements Runnable {
        private MapboxMap map;
        private Handler handler;

        public RefreshData(MapboxMap mapBox, Handler handler) {
            this.map = mapBox;
            this.handler = handler;
        }

        @Override
        public void run() {
            ((GeoJsonSource)map.getSource(ID)).setUrl(URL_GET_DATA);
            getPosition();

            handler.postDelayed(this,300);

        }
    }

     */





}
