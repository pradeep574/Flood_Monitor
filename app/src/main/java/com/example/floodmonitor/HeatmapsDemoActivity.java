package com.example.floodmonitor;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.mylibrary.SphericalUtil;
import com.example.mylibrary.heatmaps.Gradient;
import com.example.mylibrary.heatmaps.HeatmapTileProvider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class HeatmapsDemoActivity extends BaseDemoActivity2 {

    public Circle mCircle;
    public Marker mMarker;

    private static final int ALT_HEATMAP_RADIUS = 10;
    private static final double ALT_HEATMAP_OPACITY = 0.4;
    private static final int[] ALT_HEATMAP_GRADIENT_COLORS = {
            Color.argb(0, 0, 255, 255),// transparent
            Color.argb(255 / 3 * 2, 0, 255, 255),
            Color.rgb(0, 191, 255),
            Color.rgb(0, 0, 127),
            Color.rgb(255, 0, 0)
    };

    public static final float[] ALT_HEATMAP_GRADIENT_START_POINTS = {
            0.0f, 0.10f, 0.20f, 0.60f, 1.0f
    };

    public static final Gradient ALT_HEATMAP_GRADIENT = new Gradient(ALT_HEATMAP_GRADIENT_COLORS,
            ALT_HEATMAP_GRADIENT_START_POINTS);

    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;

    private boolean mDefaultGradient = true;
    private boolean mDefaultRadius = true;
    private boolean mDefaultOpacity = true;

    /**
     * Maps name of data set to data (list of LatLngs)
     * Also maps to the URL of the data set for attribution
     */
    private HashMap<String, DataSet> mLists = new HashMap<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_maps;
    }

    @Override
    protected void startDemo(boolean isRestore) {
        if (!isRestore) {
            UiSettings mUiSettings;
            getMap().setMyLocationEnabled(true);
            mUiSettings = getMap().getUiSettings();
            mUiSettings.setCompassEnabled(true);
            mUiSettings.setScrollGesturesEnabled(true);
            mUiSettings.setZoomControlsEnabled(true);
            mUiSettings.setZoomGesturesEnabled(true);
            mUiSettings.setRotateGesturesEnabled(true);
            mUiSettings.setMapToolbarEnabled(true);
            mUiSettings.setTiltGesturesEnabled(true);
            mUiSettings.setScrollGesturesEnabledDuringRotateOrZoom(true);
            mUiSettings.setIndoorLevelPickerEnabled(true);
            mUiSettings.setAllGesturesEnabled(true);
            mUiSettings.setIndoorLevelPickerEnabled(true);
            getMap().setBuildingsEnabled(true);
            getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            getMap().setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    Geocoder myLocation = new Geocoder(HeatmapsDemoActivity.this, Locale.getDefault());
                    List<Address> myList = null;
                    Double lat = null;
                    Double lng = null;
                    try {
                        myList = myLocation.getFromLocation(latLng.latitude,latLng.longitude, 1);
                    } catch (IOException e) {
                        return;
                    }
                    assert myList != null;
                    Address address = (Address) myList.get(0);
                    String addressStr = "";
                    addressStr += address.getAddressLine(0);
                    System.out.println(addressStr);
                    System.out.println(address);
                    getMap().clear();
                    //noinspection deprecation
                    mCircle =  getMap().addCircle(new CircleOptions()
                            .center(latLng)
                            .radius(2000)
                            .strokeColor(getResources().getColor(R.color.transred))
                            .fillColor(getResources().getColor(R.color.transblue)));

                    System.out.println("center LAti: "+ mCircle.getCenter().latitude);
                    System.out.println("center Long: "+ mCircle.getCenter().longitude);
                    System.out.println(mCircle.getRadius());
                    System.out.println(mCircle);
                    getMap().addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(addressStr.toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                             LatLng from = new LatLng(latLng.latitude,latLng.longitude);

                    ArrayList<LatLng> latlist = new ArrayList<LatLng>();
                    Iterator mIterator = latlist.iterator();
                    try {
                        latlist = readItems(R.raw.police);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    for(int i=0;i<latlist.size();i++) {
                        lat = latlist.get(i).latitude;
                        lng = latlist.get(i).longitude;

                        LatLng to = new LatLng(lat, lng);
                        float[] results = new float[1];
                        Location.distanceBetween(latLng.latitude, latLng.longitude, lat, lng, results);
                        System.out.println("Results: " + results[0]);
                        System.out.println(SphericalUtil.computeDistanceBetween(from, to));
                        if (results[0] < mCircle.getRadius()) {
                            String addr = "";
                            addr += address.getAddressLine(0);
                            System.out.println(addressStr);
                            System.out.println(address);
                            getMap().addMarker(new MarkerOptions().position(new LatLng(lat, lng)).title(addr.toString()));
                        }
                    }
                }
            });
            LatLng Bellandur = new LatLng(12.9304, 77.6784);
            getMap().addMarker(new MarkerOptions().position(Bellandur).title("Bellandur"));
            getMap().moveCamera(CameraUpdateFactory.newLatLng(Bellandur));
        }

        // Set up the spinner/dropdown list
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.heatmaps_datasets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new SpinnerActivity());

        try {
            mLists.put(getString(R.string.police_stations), new DataSet(readItems(R.raw.police)));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Dealing with spinner choices
    public class SpinnerActivity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,int pos, long id) {
            String dataset = parent.getItemAtPosition(pos).toString();
            System.out.println("dataset name is" + dataset);
            TextView attribution = findViewById(R.id.attribution);
            // Check if need to instantiate (avoid setData etc twice)
            if (mProvider == null) {
                mProvider = new HeatmapTileProvider.Builder().data(
                        mLists.get(getString(R.string.police_stations)).getData()).build();
                mOverlay = getMap().addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
                // Render links
                attribution.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                mProvider.setData(mLists.get(dataset).getData());
                mOverlay.clearTileCache();
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    private ArrayList<LatLng> readItems(int resource) throws JSONException {
        System.out.println("Read INtemssd,s ");
        final ArrayList<LatLng> list = new ArrayList<LatLng>();
        InputStream inputStream = getResources().openRawResource(resource);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            final JSONObject object = array.getJSONObject(i);
            final double lat = object.getDouble("lat");
            final double lng = object.getDouble("lng");
          /*  getMap().setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng latLng) {
                    Geocoder myLocation = new Geocoder(HeatmapsDemoActivity.this, Locale.getDefault());
                    List<Address> myList = null;
                    try {
                        myList = myLocation.getFromLocation(latLng.latitude,latLng.longitude, 1);
                    } catch (IOException e) {
                        return;
                    }
                    assert myList != null;
                    Address address = (Address) myList.get(0);
                    String addressStr = "";
                    addressStr += address.getAddressLine(0);
                    System.out.println(addressStr);
                    System.out.println(address);
                    //noinspection deprecation
                    mCircle =  getMap().addCircle(new CircleOptions()
                            .center(latLng)
                            .radius(2000)
                            .strokeColor(getResources().getColor(R.color.transred))
                            .fillColor(getResources().getColor(R.color.transblue)));

                    System.out.println("center LAti: "+ mCircle.getCenter().latitude);
                    System.out.println("center Long: "+ mCircle.getCenter().longitude);
                    System.out.println(mCircle.getRadius());
                    System.out.println(mCircle);
                    getMap().addMarker(new MarkerOptions()
                            .position(latLng)
                            .title(addressStr.toString())
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                    System.out.println("Object Type: "+object);
                    System.out.println("LatLong Type: "+latLng);
                    LatLng from = new LatLng(latLng.latitude,latLng.longitude);
                    LatLng to = new LatLng(lat,lng);
                    float[] results = new float[1];
                    Location.distanceBetween(latLng.latitude,latLng.longitude,lat,lng,results);
                    System.out.println("Results: "+results[0]);
                    System.out.println(SphericalUtil.computeDistanceBetween(from,to));
                    if(results[0] < mCircle.getRadius()) {
                        getMap().addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title("safe"));
                    }
                    }
            });*/

            //getMap().addMarker(new MarkerOptions().position(new LatLng(lat,lng)).title("safe"));
            list.add(new LatLng(lat, lng));
        }
        return list;
    }

    private class DataSet {
        private ArrayList<LatLng> mDataset;
        private String mUrl;

        public DataSet(ArrayList<LatLng> dataSet) {
            this.mDataset = dataSet;
        }

        public ArrayList<LatLng> getData() {
            //  System.out.println(mDataset);
            return mDataset;
        }

        public String getUrl() {
            return mUrl;
        }
    }

    public void changeRadius(View view) {
        if (mDefaultRadius) {
            mProvider.setRadius(ALT_HEATMAP_RADIUS);
        } else {
            mProvider.setRadius(HeatmapTileProvider.DEFAULT_RADIUS);
        }
        mOverlay.clearTileCache();
        mDefaultRadius = !mDefaultRadius;
    }

    public void changeGradient(View view) {
        if (mDefaultGradient) {
            mProvider.setGradient(ALT_HEATMAP_GRADIENT);
        } else {
            mProvider.setGradient(HeatmapTileProvider.DEFAULT_GRADIENT);
        }
        mOverlay.clearTileCache();
        mDefaultGradient = !mDefaultGradient;
    }

    public void changeOpacity(View view) {
        if (mDefaultOpacity) {
            mProvider.setOpacity(ALT_HEATMAP_OPACITY);
        } else {
            mProvider.setOpacity(HeatmapTileProvider.DEFAULT_OPACITY);
        }
        mOverlay.clearTileCache();
        mDefaultOpacity = !mDefaultOpacity;
    }

}
