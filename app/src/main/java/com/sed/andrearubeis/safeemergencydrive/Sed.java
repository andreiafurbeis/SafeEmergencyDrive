package com.sed.andrearubeis.safeemergencydrive;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import com.sed.andrearubeis.safeemergencydrive.TouchableSupportMapFragment;
import com.sed.andrearubeis.safeemergencydrive.TouchableWrapper.*;

import java.util.Iterator;
import java.util.List;

public class Sed extends AppCompatActivity implements OnMapReadyCallback , TouchActionDown , TouchActionUp {

    private static final int DEFAULT_ZOOM = 18;
    private static final String TAG = "Main";
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final String url = "http://washit.dek4.net/";

    Handler handler;
    Runnable runnable;

    ImageView locateMeButton;
    ImageView iconVehicle;
    GoogleMap mMap;
    LatLng mDefaultLocation;
    FusedLocationProviderClient mFusedLocationProviderClient;
    PlaceDetectionClient mPlaceDetectionClient;
    GeoDataClient mGeoDataClient;
    Boolean mLocationPermissionGranted;
    Location mLastKnownLocation;
    LocationManager location_manager;
    TextView velocity;
    MediaPlayer media_player;
    Marker emergency;
    int contatore=0;


    LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            getDeviceLocation(1);
            Log.d("Main","Sono dentro all'onLocationChanged");
            //avviare controllo sul server
            //getPosition();
            getPositionWarning(location);

        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };


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

        location_manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        velocity = (TextView) findViewById(R.id.sed_velocity);

        media_player = MediaPlayer.create(getApplicationContext(),R.raw.attenzione_mezzo_alice);


    }

    @Override
    public void onTouchDown(MotionEvent event) {
        Log.d("Main","Dentro all'OnTouchDown");
        location_manager.removeUpdates(listener);
    }

    @Override
    public void onTouchUp(MotionEvent event) {
        Log.d("Main","Dentro all'OnTouchUp");
    }


    private void updateLocationUI() {
        Log.d(TAG, "Sono dentro all'UpdateLocationUI");

        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                //mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                //mMap.getUiSettings().setMyLocationButtonEnabled(false);
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    private void getDeviceLocation(final int animation_velocity) {
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

                            //imposto la velocitá corrente sulla textview
                            if(mLastKnownLocation.getSpeed() > 8.0) {
                                velocity.setVisibility(TextView.VISIBLE);
                                velocity.setText(mLastKnownLocation.getSpeed() + " Km/h");
                            }else{
                                velocity.setVisibility(TextView.INVISIBLE);
                            }
                            //Fa parte la voce
                            //media_player.start();

                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()))
                                    .zoom(DEFAULT_ZOOM)
                                    .tilt(20)
                                    .build();
                            //Faccio partire l'animazione
                            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), animation_velocity, null);
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
        } catch (SecurityException e) {
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

    }



    protected void onStart() {
        super.onStart();

        emergency = null;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.sed_map);
        mapFragment.getMapAsync(this);
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            mLocationPermissionGranted = false;
        }


        locateMeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation(3000);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                requestUpdateLocation();

            }
        });






    }

    public void requestUpdateLocation() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location_manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1, 0, listener);

    }

    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        // Turn on the My Location layer and the related control on the map.

        updateLocationUI();

        //getPosition();

        // Get the current location of the device and set the position of the map.

        getDeviceLocation(3000);
        try {
            Thread.sleep(3000);
            Log.d("main","Sto aspettando");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //getPosition();
        requestUpdateLocation();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d("Main","Le coordinate sono : \n" + "- Latitudine --> " + latLng.latitude + "\n - Longitude --> " + latLng.longitude);
                float [] distance=new float[1];
                Location.distanceBetween(mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude(),latLng.latitude,latLng.longitude,distance);
                Log.d("Main","La distanza da quel punto é : " + distance[0]);
                if(distance[0] < 1000.0) {
                    media_player.start();
                }
            }
        });






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
    //versione per richiesta senza dati in uscita
    public void getPosition() {


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
                //PROVA
                LatLng prova = posizione.getPositionConverted();


                mMap.clear();


                emergency = mMap.addMarker(new MarkerOptions().position(prova).title("Ambulanza").icon(BitmapDescriptorFactory.fromResource(R.drawable.ambulanza_location)));

                emergency = mMap.addMarker(new MarkerOptions().position(prova).title("Ambulanza").icon(BitmapDescriptorFactory.fromResource(R.drawable.ambulanza)));

                Log.d("Main" , "Latitude --> " + prova.latitude + "Longitude --> " + prova.longitude);

                //goToLocation(posizione);

            }

            @Override
            public void onFailure(Call<Gps> call, Throwable t) {
                Log.d("MainActivity ", "  error " + t.toString());

            }
        });
    }

    //versione con controllo posizione utente
    public void getPositionWarning(Location location) {


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RetrofitAPI service = retrofit.create(RetrofitAPI.class);
        Call<List<Gps>> call = service.getWarningPosition(location.getLatitude()+"" , location.getLongitude()+"");
        call.enqueue(new Callback<List<Gps>>() {
            @Override
            public void onResponse(Call<List<Gps>> call, Response<List<Gps>> response) {


                if(response.body().size() != 0) {
                    //costruire iteratore
                    if(contatore % 6 == 0) {
                        media_player.start();

                    }
                    contatore = contatore + 1;
                    mMap.clear();
                    iconVehicle.setVisibility(View.VISIBLE);

                    for (Iterator iteratore = response.body().iterator(); iteratore.hasNext(); ) {
                        Gps appoggio = (Gps) iteratore.next();
                        Log.d("Main", "Stampo la variabile d'appoggio" + appoggio.toString() + "contatore --> " + contatore);
                        if (appoggio.getVehicle().equals("1")) {
                            //Ambulanza
                            iconVehicle.setImageResource(R.drawable.icona_ambulanza);
                            mMap.addMarker(new MarkerOptions().position(appoggio.getLatLngPosition()).title("Ambulanza").icon(BitmapDescriptorFactory.fromResource(R.drawable.ambulanza)));
                        } else {
                            //Vigili del fuoco
                            iconVehicle.setImageResource(R.drawable.icona_vigili_fuoco);
                            mMap.addMarker(new MarkerOptions().position(appoggio.getLatLngPosition()).title("Ambulanza").icon(BitmapDescriptorFactory.fromResource(R.drawable.vigili_fuoco)));


                        }



                    }
                } else {
                    contatore = 0;
                    mMap.clear();
                    iconVehicle.setVisibility(View.INVISIBLE);
                }



                //Log.d("Main", posizione.toString());
                //PROVA
                //LatLng posizioneLatLng = posizione.getLatLngPosition();

                //toglie tutti i marker dalla mappa e aggiungo quelli aggiornati





                //emergency = mMap.addMarker(new MarkerOptions().position(posizioneLatLng).title("Ambulanza").icon(BitmapDescriptorFactory.fromResource(R.drawable.ambulanza)));


                //Log.d("Main" , "Latitude --> " + posizioneLatLng.latitude + "Longitude --> " + posizioneLatLng.longitude);

                //goToLocation(posizione);
            }

            @Override
            public void onFailure(Call<List<Gps>> call, Throwable t) {
                Log.d("MainActivity ", "  error " + t.toString());

            }

        });
    }


    public void goToLocation(final Gps posizione) {




            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(posizione.getLatLngPosition())
                    .zoom(DEFAULT_ZOOM)                                 // Sets the center of the map to Maracanã
                    .bearing(270)                               // Sets the orientation of the camera to look west
                    .tilt(20)                                   // Sets the tilt of the camera to 30 degrees
                    .build();                                   // Creates a CameraPosition from the builder

            CameraPosition camera_position = new CameraPosition.Builder().target(posizione.getLatLngPosition()).zoom(DEFAULT_ZOOM).tilt(20).build();

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(camera_position), 6000, null);
            if (posizione.getVehicle().equals("1")) {
                //Ambulanza
                iconVehicle.setImageResource(R.drawable.icona_ambulanza);
            } else {
                //Vigili del fuoco
                iconVehicle.setImageResource(R.drawable.icona_vigili_fuoco);

            }



    }




    //ALTRO CODICE VECCHIO PROGETTO - runnable con aggiornamento posizione tramite file GeoJSON


    //DICHIARAZIONE
    //Handler handler;
    //Runnable runnable;

    /*
     public void addLayerEmergency() {    //MapBox
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
