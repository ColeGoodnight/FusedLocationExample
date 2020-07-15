package com.example.fusedlocationexample;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener{

    private Location location;
    private TextView locationTv;
    private TextView locationTv2;
    private GoogleApiClient googleApiClient;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private LocationRequest locationRequest;
    private static final long UPDATE_INTERVAL = 5000, FASTEST_INTERVAL = 5000; // = 5 seconds
    // lists for permissions
    private ArrayList<String> permissionsToRequest;
    private ArrayList<String> permissionsRejected = new ArrayList<>();
    private ArrayList<String> permissions = new ArrayList<>();
    // integer for permissions results request
    private static final int ALL_PERMISSIONS_RESULT = 1011;

    Location corner1A = new Location("");
    Location corner1B = new Location("");
    Location corner2A = new Location("");
    Location corner2B = new Location("");
    Location corner3A = new Location("");
    Location corner3B = new Location("");

    ArrayList<MediaPlayer> fence1;
    ArrayList<MediaPlayer> fence2;

    MediaPlayer fence3;

    geofence Gfence1;
    geofence Gfence2;
    geofence Gfence3;



    /**
     * geofence creates a rectangular region that when the user enters, will play certain audio
     * files in a predetermined order
     *
     */
    public static class geofence {

        Location corner1; //southwest corner of fence
        Location corner2; //northeast corner of fence

        ArrayList<MediaPlayer> mediaPlayers; //file to be played in fence

        int fileIterator = 0; //keeps track of files played in fence

        boolean audioFinished = false;

        //geofence    nextfence; //next fence (for linear experiences)
        //geofence    prevfence; //previous fence (for linear experiences)

        /**
         * defines a geofence
         * @param mediaPlayer media to be played in fence
         * @param corner1 Location of NW corner of fence
         * @param corner2 Location of SE corner of fence
         */
        public geofence(Location corner1, Location corner2, MediaPlayer mediaPlayer) {

            // adds mediaplayers to mediaPlayers list
            mediaPlayers.add(mediaPlayer);

            // sets oncompletion method
            mediaPlayers.get(0).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                public void onCompletion(MediaPlayer mp) {
                    performOnEnd();
                }
            });

            //if (validateCorners(corner1, corner2)) {
                this.corner1 = corner1;
                this.corner2 = corner2;
            /*} else {
                System.out.println("Geofence not valid");
            }*/
        }

        /**
         * defines a geofence
         * @param mediaPlayers media files to be played in fence
         * @param corner1 Location of NW corner of fence
         * @param corner2 Location of SE corner of fence
         */
        public geofence(Location corner1, Location corner2, ArrayList<MediaPlayer> mediaPlayers) {
            // copy external arraylist to arraylist in class
            this.mediaPlayers = mediaPlayers;

            // sets oncompletion method for all mediaplayers in list
            for (int i = 0; i < mediaPlayers.size(); i++) {
                mediaPlayers.get(i).setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

                    public void onCompletion(MediaPlayer mp) {
                        performOnEnd();
                    }
                });
            }

            //if (validateCorners(corner1, corner2)) {
            this.corner1 = corner1;
            this.corner2 = corner2;
            /*} else {
                System.out.println("Geofence not valid");
            }*/
        }

        /**
         * Iterates the file iterator when a music file ends
         * controls overflow of file iterator
         */
        public void performOnEnd() {
            if (fileIterator+1 < mediaPlayers.size()) {
                fileIterator++;
            } else {
                audioFinished  = true;
            }
        }

        /**
         * checks if two corners make a valid geofence (corner 1 must correlate
         * to the southwest corner of the fence, corner 2 must correlate to the
         * northeast corner of the fence)
         * @param corner1
         * @param corner2
         * @return
         */
        public boolean validateCorners(Location corner1, Location corner2) {
            return corner1.getLongitude() < corner2.getLongitude() &&
                    corner1.getLatitude() < corner2.getLatitude();
        }

        /**
         * start music if within fence and not already playing, stop if not
         * @param userLocation
         */
        public void update(Location userLocation) {
            if (userInFence(userLocation) && !mediaPlayers.get(fileIterator).isPlaying()) {
                mediaPlayers.get(fileIterator).start();
            } else if (!userInFence(userLocation) && mediaPlayers.get(fileIterator).isPlaying()) {
                mediaPlayers.get(fileIterator).pause();
            }
        }

        /**
         * checks if user is within geofence
         * @param userLocation
         * @return
         */
        private boolean userInFence(Location userLocation) {
            return (userLocation.getLongitude() >= corner1.getLongitude() &&
                    userLocation.getLatitude()  <= corner1.getLatitude()  &&
                    userLocation.getLongitude() <= corner2.getLongitude() &&
                    userLocation.getLatitude()  >= corner2.getLatitude());
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationTv = findViewById(R.id.location);
        locationTv2 = findViewById(R.id.location2);

        //geofence 1 bounds

        corner1A.setLatitude(47.816608);
        corner1A.setLongitude(-122.369876);

        corner1B.setLatitude(47.816089);
        corner1B.setLongitude(-122.3698208);

        //geofence 2 bounds

        corner2A.setLatitude(47.817127);
        corner2A.setLongitude(-122.369876);

        corner2B.setLatitude(47.816608);
        corner2B.setLongitude(-122.369208);

        //geofence 3 bounds

        corner3A.setLatitude(47.817636);
        corner3A.setLongitude(-122.369876);

        corner3B.setLatitude(47.817127);
        corner3B.setLongitude(-122.369208);

        //geofence 1 audio

        fence1.add(MediaPlayer.create(this, R.raw.fence1_1));
        fence1.add(MediaPlayer.create(this, R.raw.fence1_2));
        fence1.add(MediaPlayer.create(this, R.raw.fence1_3));

        //geofence 2 audio

        fence2.add(MediaPlayer.create(this, R.raw.fence2_1));
        fence2.add(MediaPlayer.create(this, R.raw.fence2_2));

        //geofence 3 audio

        fence3 = MediaPlayer.create(this, R.raw.fence3_1);

        geofence Gfences[] = new geofence[3];

        Gfences[0] = new geofence(corner1A, corner1B, fence1);
        Gfences[1] = new geofence(corner2A, corner2B, fence2);
        Gfences[3] = new geofence(corner3A, corner3B, fence3);

        // we add permissions we need to request location of the users
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);



        permissionsToRequest = permissionsToRequest(permissions);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (permissionsToRequest.size() > 0) {
                requestPermissions(permissionsToRequest.toArray(
                        new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
            }
        }

        // we build google api client
        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();
    }

    private ArrayList<String> permissionsToRequest(ArrayList<String> wantedPermissions) {
        ArrayList<String> result = new ArrayList<>();

        for (String perm : wantedPermissions) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()) {
            locationTv.setText("You need to install Google Play Services to use the App properly");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop location updates
        if (googleApiClient != null  &&  googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Permissions ok, we get last location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null) {
            locationTv.setText("Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude());
        }

        startLocationUpdates();
    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            //TODO: set up linear system for iterating through Gfences
            //TODO: set up system for checking linearity of Gfences
            //TODO: mediaplayer closeout/release state 
            Gfence1.update(location);
            Gfence2.update(location);
            locationTv2.setText("Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
            if (Gfence1.userInFence(location)) {
                locationTv.setText("User in fence1");
            } else if (Gfence2.userInFence(location)) {
                locationTv.setText("User in fence2");
            } else {
                locationTv.setText("User not in fence");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case ALL_PERMISSIONS_RESULT:
                for (String perm : permissionsToRequest) {
                    if (!hasPermission(perm)) {
                        permissionsRejected.add(perm);
                    }
                }

                if (permissionsRejected.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                            new AlertDialog.Builder(MainActivity.this).
                                    setMessage("These permissions are mandatory to get your location. You need to allow them.").
                                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                                requestPermissions(permissionsRejected.
                                                        toArray(new String[permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                            }
                                        }
                                    }).setNegativeButton("Cancel", null).create().show();

                            return;
                        }
                    }
                } else {
                    if (googleApiClient != null) {
                        googleApiClient.connect();
                    }
                }

                break;
        }
    }
}
