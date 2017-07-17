package com.example.arsone.weather;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
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
    //private MarkerView userMarker;
    private LocationEngine locationEngine;
    // private LocationEngineListener locationListener;
    private PermissionsManager permissionsManager;
    private LocationEngineListener locationEngineListener;


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

//    private static String descText;

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
                        + "\n" + cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP));

                if (mUnitsFormat == 0) { // metric = Celsius

                    descText += " \u00B0C";

                } else if (mUnitsFormat == 1) { // imperial == Fahrenheit

                    descText += " \u2109";
                }

                map.addMarker(new MarkerOptions()
                        .position(point)
                        .title(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)))
                        // .snippet(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY)))
                        .snippet(descText)
                        .icon(icon)
                );
            }
            cursor.moveToNext();
        }




/*        if (cursor.getCount() == 0) {

            ///   showMessageBar("Данные не найдены", false);

            // set mStatus info
            ///    setModeBar(getString(R.string.message_city_not_found), R.color.nothingColor, R.drawable.ic_lamp_nothing);
        }*/
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
    // ---------------------------------------------------------


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
/*

// Save to database

    latitude
    longitude
    // altitude
    bearing
    tilt
    zoom

    */

        // save camera position to DB
        /*    CameraPosition(LatLng target, double zoom, double tilt, double bearing) {
        this.target = target;
        this.bearing = bearing;
        this.tilt = tilt;
        this.zoom = zoom;
    }*/

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

        //   Log.d("AAAAA", "mapStyleSpinner.getSelectedItemPosition() = " + mapStyleSpinner.getSelectedItemPosition());
        //  Log.d("AAAAA", "mapLanguageSpinner.getSelectedItemPosition() = " + mapLanguageSpinner.getSelectedItemPosition());

        //    values.put(DataContract.SettingsEntry.COLUMN_MAP_STYLE, mapStyleSpinner.getSelectedItemPosition());

        //     values.put(DataContract.SettingsEntry.COLUMN_MAP_LANGUAGE, mapLanguageSpinner.getSelectedItemPosition());

        getActivity().getContentResolver().update(DataContentProvider.SETTINGS_CONTENT_URI, values, null, null);

        ///   Log.d("AAAAA", "writeSettingsToDB(): updatedRowsCount = " + updatedRowsCount);
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


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_view_weather, container, false);

        setHasOptionsMenu(true); // show action bar menu

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_api_key));
//        Mapbox.getInstance(this, getString(R.string.access_token));

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
        // MainActivity.Settings settings = activity.readSettings();
        mUnitsFormat = settings.getUnitsFormat();
        //  mSortCities = settings.getSortCities();
    }


/*    private void animateMarker(MarkerView marker) {

        View view = map.getMarkerViewManager().getView(marker);
        if (view != null) {

            View backgroundView = view.findViewById(R.id.background_imageview);

            ValueAnimator scaleCircleX = ObjectAnimator.ofFloat(backgroundView, "scaleX", 1.8f);
            ValueAnimator scaleCircleY = ObjectAnimator.ofFloat(backgroundView, "scaleY", 1.8f);
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(backgroundView, "alpha", 1f, 0f);

            scaleCircleX.setRepeatCount(ValueAnimator.INFINITE);
            scaleCircleY.setRepeatCount(ValueAnimator.INFINITE);
            fadeOut.setRepeatCount(ObjectAnimator.INFINITE);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(scaleCircleX).with(scaleCircleY).with(fadeOut);
            animatorSet.setDuration(1000);
            animatorSet.start();
        }
    }*/


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
            ///   finish();
        }
    }


    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // clear previous menu items
        menu.clear();

        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.view_weather_menu, menu);
    }


/*    @Override
    public void onPrepareOptionsMenu(Menu menu) {

       for (int index = 0; index < menu.size(); index++) {

            MenuItem menuItem = menu.getItem(index);

            if (menuItem != null) {

                int id = menuItem.getItemId();

                // hide "view" and "settings" icons
                if (id == R.id.action_view || id == R.id.action_settings) {

                    menuItem.setVisible(false);
                }

                // hide the menu items if the drawer is open
                //   menuItem.setVisible(mMenuVisible);
            }
        }

        super.onPrepareOptionsMenu(menu);
    }*/

    private int mMarkerIndex;

    private void nextPlace() {

        mMarkerIndex++;

        if (mMarkerIndex >= map.getMarkers().size()) {

            mMarkerIndex = 0;
        }

        Marker marker = map.getMarkers().get(mMarkerIndex);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(marker.getPosition()))
                //  .bearing(0)
                //  .tilt(0)
                //        .bearing(cursor.getDouble(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_CAMERA_BEARING)))
                //        .zoom(cursor.getDouble(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_CAMERA_ZOOM)))                                   // Sets the zoom
                //        .tilt(cursor.getDouble(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_CAMERA_TILT)))                                   // Sets the tilt of the camera
                .build(); // Creates a CameraPosition from the builder

        //   map.getUiSettings().getHeight(

        //     mapView.clearFocus();

        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(marker.getPosition()))); // .newCameraPosition(cameraPosition));

        //  map.invalidateCameraPosition();
        //    map.getMarkerViewManager().invalidateViewMarkersInVisibleRegion();
    }


    private void previousPlace() {

        mMarkerIndex--;

        if (mMarkerIndex < 0) { // (map.getMarkers().size() - 1)) {

            mMarkerIndex = map.getMarkers().size() - 1;
        }

        Marker marker = map.getMarkers().get(mMarkerIndex);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(marker.getPosition()))
                //  .bearing(0)
                //  .tilt(0)
                //        .bearing(cursor.getDouble(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_CAMERA_BEARING)))
                //        .zoom(cursor.getDouble(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_CAMERA_ZOOM)))                                   // Sets the zoom
                //        .tilt(cursor.getDouble(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_CAMERA_TILT)))                                   // Sets the tilt of the camera
                .build(); // Creates a CameraPosition from the builder

        //   map.getUiSettings().getHeight(

        //    mapView.clearFocus();

        map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(marker.getPosition()))); // .newCameraPosition(cameraPosition));
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


    private CameraPosition mCameraPosition;

    @Override
    public void onCameraChange(CameraPosition position) {

        if (position.target != null)
            mCameraPosition = position;
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
