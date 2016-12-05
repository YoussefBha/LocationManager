package bha.com.locationmanager.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bha.com.locationmanager.R;
import bha.com.locationmanager.utils.AddGeofence;
import bha.com.locationmanager.utils.Constants;
import bha.com.locationmanager.utils.NamedGeofence;

public class LocationService extends IntentService {

  // region Properties

  private final String TAG = LocationService.class.getName();

  private SharedPreferences prefs;
  private Gson gson;

  // endregion

  // region Constructors

  public LocationService() {
    super("LocationService");
  }

  // endregion

  // region Overrides

  @Override
  protected void onHandleIntent(Intent intent) {
    prefs = getApplicationContext().getSharedPreferences(Constants.SharedPrefs.Geofences, Context.MODE_PRIVATE);
    gson = new Gson();

    GeofencingEvent event = GeofencingEvent.fromIntent(intent);
    if (event != null) {
      if (event.hasError()) {
        onError(event.getErrorCode());
      } else {
        int transition = event.getGeofenceTransition();
        if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL || transition == Geofence.GEOFENCE_TRANSITION_EXIT) {
          List<String> geofenceIds = new ArrayList<>();
          for (Geofence geofence : event.getTriggeringGeofences()) {
            geofenceIds.add(geofence.getRequestId());
          }
          if (transition == Geofence.GEOFENCE_TRANSITION_ENTER || transition == Geofence.GEOFENCE_TRANSITION_DWELL) {
            onEnteredGeofences(geofenceIds);
          }
        }
      }
    }
  }

  // endregion

  // region Private

  private void onEnteredGeofences(List<String> geofenceIds) {
    for (String geofenceId : geofenceIds) {


      String geofenceName = "";
      boolean active = false;
      // Loop over all geofence keys in prefs and retrieve NamedGeofence from SharedPreference
      Map<String, ?> keys = prefs.getAll();
      for (Map.Entry<String, ?> entry : keys.entrySet()) {
        String jsonString = prefs.getString(entry.getKey(), null);
        NamedGeofence namedGeofence = gson.fromJson(jsonString, NamedGeofence.class);
        //retrieve geofence by id
        if (namedGeofence.id.equals(geofenceId)) {
          geofenceName = namedGeofence.name;
          break;
        }
      }


        // Set the notification text and send the notification
      final NotificationCompat.Builder builder = new android.support.v7.app.NotificationCompat.Builder(getApplicationContext());

      Bitmap bitmap = BitmapFactory.decodeResource( getApplicationContext().getResources(), R.mipmap.ic_launcher);


      builder.setContentTitle("Location")
              .setAutoCancel(true)
              .setLargeIcon(bitmap)
              .setContentText("You are at "+geofenceName)
              .setSmallIcon(R.mipmap.ic_launcher);


      NotificationManager nm =  (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

      Intent i = new Intent(getApplicationContext(), AddGeofence.class);
      i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

      PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 1, i, 0);

      builder.setContentIntent(pi);

      Notification n = builder.build();


      n.flags |= Notification.FLAG_AUTO_CANCEL;

      nm.notify(1, n);




    }
  }

  private void onError(int i) {
    Log.e(TAG, "Geofencing Error: " + i);
  }


  }

