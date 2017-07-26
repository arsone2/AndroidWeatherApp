package com.example.arsone.weather;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;

// custom infobox: https://github.com/mapbox/mapbox-android-demo/blob/master/MapboxAndroidDemo/src/main/java/com/mapbox/mapboxandroiddemo/examples/annotations/CustomInfoWindowActivity.java


public class ViewWeatherFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        PermissionsListener, MapboxMap.OnCameraChangeListener {


    private int mUnitsFormat;


    public interface Callbacks {

        MainActivity.Settings readSettingsFromDB();

        void syncAllData();
    }

    private ViewWeatherFragment.Callbacks activity;


    private MapView mapView;
    private MapboxMap map;
    private LocationEngine locationEngine;
    private PermissionsManager permissionsManager;
    private LocationEngineListener locationEngineListener;

    private CameraPosition mCameraPosition;
    private int mMarkerIndex;
    private Marker mMarker;

    // ---------------------------------------------------------
    // implements LoaderManager.LoaderCallbacks<Cursor>
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c.getTime());

        //      String sortOrder = DataContract.CityEntry._ID; // default sort order: _id

/*        if (mSortCities == 0)
            sortOrder = "ORDER BY c." + DataContract.CityEntry._ID + " DESC";
        else if (mSortCities == 1)
            sortOrder = "ORDER BY " + DataContract.CityEntry.COLUMN_ENTERED_CITY + " ASC";*/

        return new CursorLoader(getContext(),
                Uri.parse(DataContentProvider.CITY_WEATHER_CONTENT_URI.toString() + "/" + formattedDate),
                null,
                null, //DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?", // selection: city ID
                null, // new String[]{ formattedDate }, // selectionArgs
                null //sortOrder // DataContract.WeatherEntry._ID // sort order
        );
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        ///   Log.d("AAAAA", "MARKERS: onLoadFinished: results count = " + cursor.getCount());

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            double lat = cursor.getDouble(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_LATITUDE));
            double lon = cursor.getDouble(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_LONGITUDE));

            if (lat != 0 && lon != 0) {

                LatLng point = new LatLng(lat, lon);

                IconFactory iconFactory = IconFactory.getInstance(getContext());

                Icon icon = iconFactory.fromResource(getContext().getResources().
                        getIdentifier("map_" + cursor.getString(cursor
                                        .getColumnIndex(DataContract.WeatherEntry.COLUMN_ICON_NAME)),
                                "drawable", getContext().getPackageName()));

                String descText = cursor.getString(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_DESCRIPTION))
                        + "\n"; // + cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP));

                double dayTemp = cursor.getDouble(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP));



                if (mUnitsFormat == 0) { // metric = Celsius

                    descText += (int)dayTemp + " \u00B0C";

                } else if (mUnitsFormat == 1) { // imperial == Fahrenheit

                    descText += CelsiusToFahrenheit(dayTemp) + " \u2109";
                }

/*                MarkerOptions markerOptions = new MarkerOptions()
                        .position(point)
                        .title(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)))
                        .snippet(descText)
                        .icon(icon);*/

                map.addMarker(new MarkerOptions()
                        .position(point)
                        .title(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)))
                        .snippet(descText)
                        .icon(icon)
                );


                if (mShowEnteredCity && cursor.getInt(cursor.getColumnIndex(DataContract.CityEntry._ID)) == mID) {

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(cursor.getDouble(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_LATITUDE)),
                                    cursor.getDouble(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_LONGITUDE))))    // Sets the center of the map
                            .zoom(5) // Set default zoom
                            .build(); // Creates a CameraPosition from the builder

                    map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
            cursor.moveToNext();
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    // ---------------------------------------------------------


/*    private String CelsiusToFahrenheit(double temp) {

        // return (int) (temp * 9 / 5 + 32);
        return String.valueOf((int)(temp * 9 / 5 + 32));
    }*/

    private String CelsiusToFahrenheit(double temp) {

        // return (int) (temp * 9 / 5 + 32);
        return String.valueOf((int)(temp * 9 / 5 + 32));
    }


    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        activity = (ViewWeatherFragment.Callbacks) context;
    }


    @Override
    public void onDetach() {

        super.onDetach();
        activity = null;
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }


    @Override
    public void onStart() {

        super.onStart();

        mapView.onStart();

        if (locationEngine != null && locationEngineListener != null) {
            locationEngine.activate();
            locationEngine.requestLocationUpdates();
            locationEngine.addLocationEngineListener(locationEngineListener);
        }
    }


    @Override
    public void onStop() {

        super.onStop();

        mapView.onStop();

        if (locationEngine != null && locationEngineListener != null) {
            locationEngine.removeLocationEngineListener(locationEngineListener);
            locationEngine.removeLocationUpdates();
            locationEngine.deactivate();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();

        if (mCameraPosition.target != null) {

            saveCameraPositionToDB();
            // Log.d("AAAAA", "mCameraPosition.target = " + mCameraPosition.target + ", mCameraPosition.zoom = " + mCameraPosition.zoom);
        }
    }


    private void saveCameraPositionToDB() {

        if (mCameraPosition.target == null)
            return;

        ContentValues values = new ContentValues();

        values.put(DataContract.SettingsEntry.COLUMN_CAMERA_LATITUDE, mCameraPosition.target.getLatitude());
        values.put(DataContract.SettingsEntry.COLUMN_CAMERA_LONGITUDE, mCameraPosition.target.getLongitude());
        values.put(DataContract.SettingsEntry.COLUMN_CAMERA_BEARING, mCameraPosition.bearing);
        values.put(DataContract.SettingsEntry.COLUMN_CAMERA_TILT, mCameraPosition.tilt);
        values.put(DataContract.SettingsEntry.COLUMN_CAMERA_ZOOM, mCameraPosition.zoom);

        getActivity().getContentResolver().update(DataContentProvider.SETTINGS_CONTENT_URI, values, null, null);
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }


    private int mID;
    private String mEnteredCity;
    private boolean mShowEnteredCity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_weather, container, false);

        setHasOptionsMenu(true); // show action bar menu

        Bundle bundle = getArguments();

        if (bundle != null) {
            mID = bundle.getInt(MainActivity.CITY_ID);
            mEnteredCity = bundle.getString(MainActivity.ENTERED_CITY);
            mShowEnteredCity = true;
        }

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_api_key));

        initLoader();

        // Get the location engine object for later use.
        locationEngine = new LocationSource(getContext());
        locationEngine.activate();

        mapView = (MapView) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                map = mapboxMap;

                map.setOnCameraChangeListener(ViewWeatherFragment.this);

                // set map items language
                setMapLanguage();

                // https://github.com/mapbox/mapbox-android-demo/blob/72f1f41346bb34dd300375ef2760a0778a54d757/MapboxAndroidDemo/src/main/java/com/mapbox/mapboxandroiddemo/examples/styles/LanguageSwitchActivity.java
                // https://www.mapbox.com/studio/tilesets/mapbox.mapbox-streets-v7/

                Log.d("AAAAA", "mMapboxMap.isMyLocationEnabled = " + map.isMyLocationEnabled());

                permissionsManager = new PermissionsManager(ViewWeatherFragment.this);

                if (!PermissionsManager.areLocationPermissionsGranted(getContext())) {
                    permissionsManager.requestLocationPermissions(getActivity());
                } else {
                    enableLocation();
                }

                // Customize the user location icon using the getMyLocationViewSettings object.
                map.getMyLocationViewSettings().setPadding(0, 200, 0, 0);
                map.getMyLocationViewSettings().setForegroundTintColor(Color.parseColor("#56B881"));
                map.getMyLocationViewSettings().setAccuracyTintColor(Color.parseColor("#FBB03B"));

                // add markers
                InitMarkerLoader();

                if (!mShowEnteredCity) {

                    // ------------------------------------
                    // read map camera settings
                    Cursor cursor = getActivity().getContentResolver().query(DataContentProvider.SETTINGS_CONTENT_URI,
                            new String[]{DataContract.SettingsEntry.COLUMN_CAMERA_LATITUDE,
                                    DataContract.SettingsEntry.COLUMN_CAMERA_LONGITUDE,
                                    DataContract.SettingsEntry.COLUMN_CAMERA_BEARING,
                                    DataContract.SettingsEntry.COLUMN_CAMERA_TILT,
                                    DataContract.SettingsEntry.COLUMN_CAMERA_ZOOM
                            },
                            null, // DataContract.CityEntry.COLUMN_ENTERED_CITY + "=?",
                            null, // new String[]{enteredCity},
                            null);

                    if (cursor != null && cursor.getCount() > 0) {

                        cursor.moveToFirst();

                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(cursor.getDouble(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_CAMERA_LATITUDE)),
                                        cursor.getDouble(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_CAMERA_LONGITUDE))))    // Sets the center of the map
                                .bearing(cursor.getDouble(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_CAMERA_BEARING)))
                                .zoom(cursor.getDouble(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_CAMERA_ZOOM)))                                   // Sets the zoom
                                .tilt(cursor.getDouble(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_CAMERA_TILT)))                                   // Sets the tilt of the camera
                                .build(); // Creates a CameraPosition from the builder

                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                        cursor.close();
                        // ------------------------------------
                    }
                }

 /*                // Click listener
                mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {

                    @Override
                    public void onMapClick(@NonNull LatLng point) {

                     ///   activity.onMapClicked(); // hide keyboard

                    ///    geocode(point);
                    }
                });*/
            }
        });

        return view;
    }


    // add city markers on the map
    private void InitMarkerLoader() {

        // --------------------------------------------------------------
        // IMPORTANT !!! Change loader for different query
        Loader loader = getLoaderManager().getLoader(MainActivity.LOADER_MARKER_ID);

        if (loader != null && !loader.isReset()) {
            getLoaderManager().restartLoader(MainActivity.LOADER_MARKER_ID, null, ViewWeatherFragment.this);
        } else {
            getLoaderManager().initLoader(MainActivity.LOADER_MARKER_ID, null, ViewWeatherFragment.this);
        }
        // --------------------------------------------------------------
    }


    private void setMapLanguage() {

        String property;

        String locale = getCurrentLocale().getCountry().toLowerCase();

        switch (locale) {

            case "en": // English
            case "us": // English

                property = "{name_en}";

                break;

            case "ru": // Russian

                property = "{name_ru}";

                break;

            default:
                return;
        }

        for (String layerName : getResources().getStringArray(R.array.symbol_map_layers_array)) {

            try {
                //   Log.d("AAAAA", "layerName = " + layerName);
                map.getLayer(layerName).setProperties(textField(property));
            } catch (Exception e) {
                Log.d("AAAAA", "e.getMessage() = " + e.getMessage());
            }
        }
    }


    public void initLoader() {

        Log.d("AAAAA", "ViewWeatherFragment: initLoader");

        // get settings data from DB
        MainActivity.Settings settings = activity.readSettingsFromDB();
        mUnitsFormat = settings.getUnitsFormat();

        // set units format
       /// WeatherCursorAdapter.setUnitsFormat(mUnitsFormat);
    }


    private void enableLocation() {
        // https://github.com/mapbox/mapbox-android-demo/blob/master/MapboxAndroidDemo/src/main/java/com/mapbox/mapboxandroiddemo/examples/location/CustomizeUserLocationActivity.java

        // If we have the last location of the user, we can move the camera to that position.
        Location lastLocation = locationEngine.getLastLocation();

        if (lastLocation != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 4));
        }

        locationEngineListener = new LocationEngineListener() {

            @Override
            public void onConnected() {
                // No action needed here.
            }

            @Override
            public void onLocationChanged(Location location) {

                if (location != null) {
                    // Move the map camera to where the user location is and then remove the
                    // listener so the camera isn't constantly updating when the user location
                    // changes. When the user disables and then enables the location again, this
                    // listener is registered again and will adjust the camera once again.
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 4));
                    locationEngine.removeLocationEngineListener(this);
                }
            }
        };

        locationEngine.addLocationEngineListener(locationEngineListener);

        //Enable or disable the location layer on the map
        map.setMyLocationEnabled(true);
    }


    @TargetApi(Build.VERSION_CODES.N)
    public Locale getCurrentLocale() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            return getResources().getConfiguration().getLocales().get(0);

        } else {

            //noinspection deprecation
            return getResources().getConfiguration().locale;
        }
    }


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

        Toast.makeText(getContext(), R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show();
    }


    @Override
    public void onPermissionResult(boolean granted) {

        if (granted) {
            enableLocation();
        } else {
            Toast.makeText(getContext(), R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
        }
    }


    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // clear previous menu items
       // menu.clear();

        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.view_weather_menu, menu);
    }


    private void nextPlace() {

        hideAllMarkers();

        mMarkerIndex++;

        if (mMarkerIndex >= map.getMarkers().size()) {

            mMarkerIndex = 0;
        }

        mMarker = map.getMarkers().get(mMarkerIndex);

        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(mMarker.getPosition())));
    }


    private void previousPlace() {

        hideAllMarkers();

        mMarkerIndex--;

        if (mMarkerIndex < 0) {

            mMarkerIndex = map.getMarkers().size() - 1;
        }

        mMarker = map.getMarkers().get(mMarkerIndex);

        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(mMarker.getPosition())));
    }


    private void hideAllMarkers() {

        for (Marker marker : map.getMarkers()) {

            marker.hideInfoWindow();
        }
    }


    // handle choice from options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_next_city:

                nextPlace();

                return true;

            case R.id.action_previous_city:

                previousPlace();

                return true;

            case R.id.action_sync_map:

                activity.syncAllData();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onCameraChange(CameraPosition position) {

        if (position != null && position.target != null)
            mCameraPosition = position;

        if (mMarker != null) {

            mMarker.showInfoWindow(map, mapView);
        }
    }

    /// https://github.com/mapbox/mapbox-gl-native/blob/master/platform/android/MapboxGLAndroidSDK/src/main/java/com/mapbox/mapboxsdk/camera/CameraPosition.java
    /**
     * Constructs a CameraPosition.
     *
     * @param target  The target location to align with the center of the screen.
     * @param zoom    Zoom level at target. See zoom(float) for details of restrictions.
     * @param tilt    The camera angle, in degrees, from the nadir (directly down). See tilt(float)
     *                for details of restrictions.
     * @param bearing Direction that the camera is pointing in, in degrees clockwise from north.
     *                This value will be normalized to be within 0 degrees inclusive and 360 degrees
     *                exclusive.
     * @throws NullPointerException     if target is null
     * @throws IllegalArgumentException if tilt is outside the range of 0 to 90 degrees inclusive.
     */
/*    CameraPosition(LatLng target, double zoom, double tilt, double bearing) {
        this.target = target;
        this.bearing = bearing;
        this.tilt = tilt;
        this.zoom = zoom;
    }*/
}
