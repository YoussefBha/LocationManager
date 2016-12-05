package bha.com.locationmanager.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import java.util.ArrayList;
import java.util.List;

import bha.com.locationmanager.R;
import bha.com.locationmanager.adapters.AllGeofencesAdapter;
import bha.com.locationmanager.utils.AddGeofence;
import bha.com.locationmanager.utils.GeofenceController;
import bha.com.locationmanager.utils.NamedGeofence;

public class AddLocation extends AppCompatActivity implements AddGeofence{

    // region Properties

    private RecyclerView geofenceRecyclerView;

    private FloatingActionButton button;

    private AllGeofencesAdapter allGeofencesAdapter;

    public static final int PERMISSIONS= 123;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        GeofenceController.getInstance().init(this);

        checkPermission(AddLocation.this);


        geofenceRecyclerView = (RecyclerView) findViewById(R.id.fragment_all_geofences_geofenceRecyclerView);
        button = (FloatingActionButton) findViewById(R.id.fragment_all_geofences_actionButton);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        geofenceRecyclerView.setLayoutManager(layoutManager);
        allGeofencesAdapter = new AllGeofencesAdapter(GeofenceController.getInstance().getNamedGeofences(),getApplicationContext());
        geofenceRecyclerView.setAdapter(allGeofencesAdapter);

        allGeofencesAdapter.setListener(new AllGeofencesAdapter.AllGeofencesAdapterListener() {
            @Override
            public void onDeleteTapped(NamedGeofence namedGeofence) {
                List<NamedGeofence> namedGeofences = new ArrayList<>();
                namedGeofences.add(namedGeofence);
                GeofenceController.getInstance().removeGeofences(namedGeofences, geofenceControllerListener);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int PLACE_PICKER_REQUEST = 1;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(AddLocation.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });



    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, getApplicationContext());
                String toastMsg = String.format("%s", place.getAddress());
                NamedGeofence geofence = new NamedGeofence();
                geofence.name = toastMsg;
                geofence.latitude = place.getLatLng().latitude;
                geofence.longitude = place.getLatLng().longitude;
                geofence.radius = 0.025f* 1000.0f;
                geofence.active = true;
                GeofenceController.getInstance().addGeofence(geofence, geofenceControllerListener);

            }
        }
    }



    // region GeofenceControllerListener

    private GeofenceController.GeofenceControllerListener geofenceControllerListener = new GeofenceController.GeofenceControllerListener() {
        @Override
        public void onGeofencesUpdated() {
            refresh();
        }

        @Override
        public void onError() {
            showErrorToast();
        }
    };

    // endregion


    // region Private

    private void refresh() {
        allGeofencesAdapter.notifyDataSetChanged();

        invalidateOptionsMenu();

    }

    private void showErrorToast() {
        Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDialogPositiveClick(NamedGeofence geofence) {
        GeofenceController.getInstance().addGeofence(geofence, geofenceControllerListener);
    }

    @Override
    public void onDialogNegativeClick() {

    }

    // endregion




    //check location permession for Android 5.0/+
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.ACCESS_FINE_LOCATION) ) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS);

                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS);

                }
                return false;
            }
            else {
                return true;
            }
        } else {
            return true;
        }
    }
}
