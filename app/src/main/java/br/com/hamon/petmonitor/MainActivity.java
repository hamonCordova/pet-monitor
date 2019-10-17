package br.com.hamon.petmonitor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.SphericalUtil;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements GoogleMap.OnMyLocationButtonClickListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker mPetMarker;
    private LatLng mCurrentLatLng;
    private AlertDialog mAlertDialog;
    private final int MAX_PET_DISTANCE = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);

    }

    @Override
    public boolean onMyLocationButtonClick() {
        return false;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        PermissionUtils.verifyAndAskForPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, this);

        mMap = googleMap;
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);

        startLocationUpdates();

    }

    private void updateLocation(Location location) {

        if(location == null) return;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if(mCurrentLatLng == null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 19f));
        }

        mCurrentLatLng = latLng;
        updatePetLocation();

    }

    private void updatePetLocation() {

        if(mPetMarker == null) {
            MarkerOptions markerOptions = new MarkerOptions().title("Pet").zIndex(9f);
            markerOptions.position(mCurrentLatLng);
            mPetMarker = mMap.addMarker(markerOptions);
        }

        int distanceMax = (int) (MAX_PET_DISTANCE + (MAX_PET_DISTANCE * 0.15));
        double distance = new Random().nextDouble() * distanceMax;
        double heading = Math.random() * 360.0;

        mPetMarker.setPosition(SphericalUtil.computeOffset(mCurrentLatLng, distance, heading));
        mPetMarker.setSnippet(((int) distance) + "m");

        if(mPetMarker.isInfoWindowShown()) {
            mPetMarker.showInfoWindow();
        }

        if(distance > MAX_PET_DISTANCE) {
            showDialogMaxDistance();
        }

    }

    private void showDialogMaxDistance() {

        if(mAlertDialog != null) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Alerta de Distância Máxima");
        builder.setMessage("O Pet está mais distante do que a distância máxima definida de " + MAX_PET_DISTANCE + "m");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mAlertDialog = null;
            }
        });

        mAlertDialog = builder.create();

        mAlertDialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.black));
            }
        });

        mAlertDialog.show();

    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {

        final FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                updateLocation(location);
            }
        });

        final LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(100);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(5000);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateLocation(location);
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

    }
}