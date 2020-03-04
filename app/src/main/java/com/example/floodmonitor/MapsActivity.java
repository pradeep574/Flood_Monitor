package com.example.floodmonitor;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mylibrary.heatmaps.Gradient;
import com.example.mylibrary.heatmaps.HeatmapTileProvider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;


public class MapsActivity extends BaseDemoActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private boolean mIsRestore;
    private UiSettings mUiSettings;
    private CheckBox mMyLocationButtonCheckbox;
    private CheckBox mMyLocationLayerCheckbox;
    private static final int MY_LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int LOCATION_LAYER_PERMISSION_REQUEST_CODE = 2;
    private boolean mLocationPermissionDenied = false;


    Button radius,gradient,opacity;
    Spinner spinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        radius.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDefaultRadius) {
                    mProvider.setRadius(ALT_HEATMAP_RADIUS);
                } else {
                    mProvider.setRadius(HeatmapTileProvider.DEFAULT_RADIUS);
                }
                mOverlay.clearTileCache();
                mDefaultRadius = !mDefaultRadius;
            }
        });
        opacity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDefaultOpacity) {
                    mProvider.setOpacity(ALT_HEATMAP_OPACITY);
                } else {
                    mProvider.setOpacity(HeatmapTileProvider.DEFAULT_OPACITY);
                }
                mOverlay.clearTileCache();
                mDefaultOpacity = !mDefaultOpacity;
            }
        });

        gradient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDefaultGradient) {
                    mProvider.setGradient(ALT_HEATMAP_GRADIENT);
                } else {
                    mProvider.setGradient(HeatmapTileProvider.DEFAULT_GRADIENT);
                }
                mOverlay.clearTileCache();
                mDefaultGradient = !mDefaultGradient;
            }
        });
        //String dataset = parent.getItemAtPosition(pos).toString();
        String dataset = "Police Stations in Victoria";
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
        // Update attribution
        attribution.setText(Html.fromHtml(String.format(getString(R.string.attrib_format),
                mLists.get(dataset).getUrl())));
    }


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

    private HashMap<String, MapsActivity.DataSet> mLists = new HashMap<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_maps;
    }

    @Override
    protected void startDemo(boolean isRestore) {
        if (!isRestore) {
            getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-25, 143), 4));
        }
        System.out.println("START DEMO FUNCTION");
        // Set up the spinner/dropdown list
        spinner = findViewById(R.id.spinner);
        //      spinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, CountryData.areas));
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.heatmaps_datasets_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new SpinnerActivity());

        try {

            mLists.put(getString(R.string.police_stations), new MapsActivity.DataSet(readItems(R.raw.police)));

        } catch (JSONException e) {
            Toast.makeText(this, "Problem reading list of markers.", Toast.LENGTH_LONG).show();
        }

    }

    // Dealing with spinner choices
    public class SpinnerActivity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            String dataset = parent.getItemAtPosition(pos).toString();

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
            // Update attribution
            attribution.setText(Html.fromHtml(String.format(getString(R.string.attrib_format),
                    mLists.get(dataset).getUrl())));

        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }

    // Datasets from http://data.gov.au
    private ArrayList<LatLng> readItems(int resource) throws JSONException {
        ArrayList<LatLng> list = new ArrayList<LatLng>();
        InputStream inputStream = getResources().openRawResource(resource);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
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
            return mDataset;
        }

        public String getUrl() {
            return mUrl;
        }
    }










    private boolean isChecked(int id) {
        return ((CheckBox) findViewById(id)).isChecked();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled(true);

      /*  mUiSettings = mMap.getUiSettings();
        // Keep the UI Settings state in sync with the checkboxes.
        mUiSettings.setCompassEnabled(true);
        mUiSettings.setZoomControlsEnabled(isChecked(R.id.zoom_buttons_toggle));
        mUiSettings.setCompassEnabled(isChecked(R.id.compass_toggle));
        mUiSettings.setMyLocationButtonEnabled(isChecked(R.id.mylocationbutton_toggle));
        mMap.setMyLocationEnabled(isChecked(R.id.mylocationlayer_toggle));
        mUiSettings.setScrollGesturesEnabled(isChecked(R.id.scroll_toggle));
        mUiSettings.setZoomGesturesEnabled(isChecked(R.id.zoom_gestures_toggle));
        mUiSettings.setTiltGesturesEnabled(isChecked(R.id.tilt_toggle));
        mUiSettings.setRotateGesturesEnabled(isChecked(R.id.rotate_toggle));*/

        LatLng Bellandur = new LatLng(12.9304, 77.6784);
        mMap.addMarker(new MarkerOptions().position(Bellandur).title("Bellandur"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Bellandur));
    }


 /*   public void onType(View view) {
        if (mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }*/

   /* public void onSearch(View view) {
        List<Address> addressList = null;
        EditText et = findViewById(R.id.et1);
        String location = et.getText().toString();
        if (location != "null" || location.equals("")) {
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            assert addressList != null;

            Address address = addressList.get(0);
            LatLng latlan = new LatLng(address.getLatitude(),address.getLongitude());
            mMap.addCircle(new CircleOptions().center(latlan).radius(1000).strokeColor(Color.RED).fillColor(Color.BLUE));
            mMap.addMarker(new MarkerOptions().position(latlan).title(location));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latlan));
        }
    }*/
   /* private boolean checkReady() {
        if (mMap == null) {
            Toast.makeText(this, R.string.map_not_ready, Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }*/

    public void back(View view){
        if(view.getId()==R.id.back) {
            Intent intent = new Intent(MapsActivity.this, HomePage.class);
            startActivity(intent);
        }
    }
    /*
    public void setZoomButtonsEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables the zoom controls (+/- buttons in the bottom-right of the map for LTR
        // locale or bottom-left for RTL locale).
        mUiSettings.setZoomControlsEnabled(((CheckBox) v).isChecked());
    }

    public void setCompassEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables the compass (icon in the top-left for LTR locale or top-right for RTL
        // locale that indicates the orientation of the map).
        mUiSettings.setCompassEnabled(((CheckBox) v).isChecked());
    }

    public void setMyLocationButtonEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables the my location button (this DOES NOT enable/disable the my location
        // dot/chevron on the map). The my location button will never appear if the my location
        // layer is not enabled.
        // First verify that the location permission has been granted.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mUiSettings.setMyLocationButtonEnabled(mMyLocationButtonCheckbox.isChecked());
        } else {
            // Uncheck the box and request missing location permission.
            mMyLocationButtonCheckbox.setChecked(false);
            requestLocationPermission(MY_LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public void setMyLocationLayerEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables the my location layer (i.e., the dot/chevron on the map). If enabled, it
        // will also cause the my location button to show (if it is enabled); if disabled, the my
        // location button will never show.
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(mMyLocationLayerCheckbox.isChecked());
        } else {
            // Uncheck the box and request missing location permission.
            mMyLocationLayerCheckbox.setChecked(false);
            PermissionUtils.requestPermission(this,LOCATION_LAYER_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, false);
        }
    }

    public void setScrollGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables scroll gestures (i.e. panning the map).
        mUiSettings.setScrollGesturesEnabled(((CheckBox) v).isChecked());
    }

    public void setZoomGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables zoom gestures (i.e., double tap, pinch & stretch).
        mUiSettings.setZoomGesturesEnabled(((CheckBox) v).isChecked());
    }

    public void setTiltGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables tilt gestures.
        mUiSettings.setTiltGesturesEnabled(((CheckBox) v).isChecked());
    }

    public void setRotateGesturesEnabled(View v) {
        if (!checkReady()) {
            return;
        }
        // Enables/disables rotate gestures.
        mUiSettings.setRotateGesturesEnabled(((CheckBox) v).isChecked());
    } */

    /**
     * Requests the fine location permission. If a rationale with an additional explanation should
     * be shown to the user, displays a dialog that triggers the request.
     */
    public void requestLocationPermission(int requestCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Display a dialog with rationale.
            PermissionUtils.RationaleDialog
                    .newInstance(requestCode, false).show(
                    getSupportFragmentManager(), "dialog");
        } else {
            // Location permission has not been granted yet, request it.
            PermissionUtils.requestPermission(this, requestCode,
                    Manifest.permission.ACCESS_FINE_LOCATION, false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == MY_LOCATION_PERMISSION_REQUEST_CODE) {
            // Enable the My Location button if the permission has been granted.
            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                mUiSettings.setMyLocationButtonEnabled(true);
                mMyLocationButtonCheckbox.setChecked(true);
            } else {
                mLocationPermissionDenied = true;
            }

        } else if (requestCode == LOCATION_LAYER_PERMISSION_REQUEST_CODE) {
            // Enable the My Location layer if the permission has been granted.
            if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                mMap.setMyLocationEnabled(true);
                mMyLocationLayerCheckbox.setChecked(true);
            } else {
                mLocationPermissionDenied = true;
            }
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mLocationPermissionDenied) {
            PermissionUtils.PermissionDeniedDialog
                    .newInstance(false).show(getSupportFragmentManager(), "dialog");
            mLocationPermissionDenied = false;
        }
    }

}
