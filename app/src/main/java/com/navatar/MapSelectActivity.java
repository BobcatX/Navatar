package com.navatar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import com.navatar.maps.BuildingMapWrapper;
import com.navatar.maps.MapService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class MapSelectActivity extends Activity {
  private Spinner mapSpinner,campusSpinner;
  private TextView mapSelectTextView;
  private ArrayAdapter<String> mapArrayAdapter,campusArrayAdapter;
  private ArrayList<String> maplist;
  private MapService mapService;
  private Intent mapIntent;
  @Override
  protected void onDestroy() {
    super.onDestroy();
    unbindService(mMapConnection);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTitle("Map_Selection");
    setContentView(R.layout.map_select);
    mapSelectTextView = (TextView)findViewById(R.id.tvmapselect);

    mapSpinner = (Spinner) findViewById(R.id.mapSpinner);

    mapSpinner.setVisibility(View.GONE);

    mapSelectTextView.setVisibility(View.GONE);

    mapIntent= new Intent(this, MapService.class);

    campusSpinner = (Spinner)findViewById(R.id.campusSpinner);

    campusArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
            new ArrayList<String>());
    campusArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    ArrayList<String> campuslist = new ArrayList<String>();

    try {

      String[] campusNames = getAssets().list("maps");
      campuslist.add("Select a campus");
      for (int i=0;i<campusNames.length;i++){
        campuslist.add(campusNames[i].replaceAll("_"," "));
        Log.i("NavatarLogs",campusNames[i]);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    campusArrayAdapter.addAll(campuslist);
    campusSpinner.setAdapter(campusArrayAdapter);
    campusSpinner.setOnItemSelectedListener(campusSpinnerSelected);

    mapArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,
            new ArrayList<String>());


  }


  public OnItemSelectedListener campusSpinnerSelected = new OnItemSelectedListener() {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
      if (position != 0) {
        String campusName = campusSpinner.getSelectedItem().toString();
        campusName.replaceAll(" ","_");
        mapSelectTextView.setVisibility(View.VISIBLE);
        mapSpinner.setVisibility(View.VISIBLE);
        mapArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mapIntent.putExtra("path",campusName);
        startService(mapIntent);
        bindService(mapIntent, mMapConnection, BIND_AUTO_CREATE);
      }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
   };


  OnItemSelectedListener mapSpinnerItemSelected = new OnItemSelectedListener() {
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
      if (position != 0) {
        mapService.setActiveMap(position - 1);
        Intent intent = new Intent(MapSelectActivity.this, NavigationSelectionActivity.class);
        startActivity(intent);
      }
    }

    public void onNothingSelected(AdapterView<?> arg0) {}
  };

  /** Defines callback for service binding, passed to bindService() */
  private ServiceConnection mMapConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {

      MapService.MapBinder binder = (MapService.MapBinder) service;
      mapService = binder.getService();
      maplist = new ArrayList<String>();
      maplist.add("Select a map");
      Log.i("NavatarLogs","Found " + mapService.maps().size() + " maps");
      for (BuildingMapWrapper map : mapService.maps()){
        maplist.add(map.getName().replaceAll("_"," "));
        Log.i("NavatarLogs","Found map :" + map.getName());
      }
      mapArrayAdapter.clear();
      mapArrayAdapter.addAll(maplist);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      mapService = null;
    }
  };
}
