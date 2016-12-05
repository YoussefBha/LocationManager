package bha.com.locationmanager.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.List;

import bha.com.locationmanager.R;
import bha.com.locationmanager.utils.Constants;
import bha.com.locationmanager.utils.NamedGeofence;

public class AllGeofencesAdapter extends RecyclerView.Adapter<AllGeofencesAdapter.ViewHolder> {

  // region Properties

  private List<NamedGeofence> namedGeofences;

  private AllGeofencesAdapterListener listener;
  Context context;
  private Gson gson = new Gson();
  private SharedPreferences prefs;


  public void setListener(AllGeofencesAdapterListener listener) {
    this.listener = listener;
  }

  // endregion

  // Constructors

  public AllGeofencesAdapter(List<NamedGeofence> namedGeofences, Context context) {
    this.namedGeofences = namedGeofences;
    this.context = context;
    prefs = this.context.getSharedPreferences(Constants.SharedPrefs.Geofences, Context.MODE_PRIVATE);
  }

  // endregion

  // region Overrides

  @Override
  public AllGeofencesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    ViewGroup v = (ViewGroup) LayoutInflater.from(parent.getContext()).inflate(R.layout.listitem_geofence, parent, false);
    return new ViewHolder(v);
  }

  @Override
  public void onBindViewHolder(ViewHolder holder, int position) {
    final NamedGeofence geofence = namedGeofences.get(position);

    holder.name.setText(geofence.name);
    holder.active.setChecked(geofence.active);
    holder.active.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        geofence.active = b;
        String json = gson.toJson(geofence);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(geofence.id, json);
        editor.apply();

      }
    });


    holder.deleteButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
        builder.setMessage("Delete ?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                    if (listener != null) {
                      listener.onDeleteTapped(geofence);
                    }
                  }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                  public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                  }
                })
                .create()
                .show();
      }
    });

  }

  @Override
  public int getItemCount() {
    return namedGeofences.size();
  }

  // endregion

  // region Interfaces

  public interface AllGeofencesAdapterListener {
    void onDeleteTapped(NamedGeofence namedGeofence);
  }

  // endregion

  // region Inner classes

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView name;
    TextView latitide;
    TextView longitude;
    TextView radius;
    Button deleteButton;
    Switch active;

    public ViewHolder(ViewGroup v) {
      super(v);

      name = (TextView) v.findViewById(R.id.listitem_geofenceName);
      latitide = (TextView) v.findViewById(R.id.listitem_geofenceLatitude);
      longitude = (TextView) v.findViewById(R.id.listitem_geofenceLongitude);
      radius = (TextView) v.findViewById(R.id.listitem_geofenceRadius);
      deleteButton = (Button) v.findViewById(R.id.listitem_deleteButton);
      active = (Switch) v.findViewById(R.id.active);
    }
  }

  // endregion
}
