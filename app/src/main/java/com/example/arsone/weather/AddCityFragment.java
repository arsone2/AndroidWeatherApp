package com.example.arsone.weather;

import android.annotation.TargetApi;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.arsone.weather.data.Weather;
import com.mapbox.mapboxsdk.Mapbox;
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
import com.mapbox.services.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.services.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.services.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.services.commons.models.Position;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;


// https://stackoverflow.com/questions/30991087/mapfragment-getmapasyncthis-nullpointerexception


public class AddCityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        PermissionsListener {

    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationEngineListener locationEngineListener;


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(getContext(), getString(R.string.user_location_permission_explanation), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {

        if (granted) {
            enableLocation();
        } else {
            // finish();
            Toast.makeText(getContext(), R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show();
        }
    }

    public interface Callbacks {

        void onSaveCity(int id, String enteredCity);

        void onCancelAddEdit();

        void onMapClicked();
    }

    private Callbacks activity;

    private TextView cityEditText;

    private String enteredCity;

    private TextView messageTextView;

    private MapView mapView;

    private MapboxMap mMapboxMap;

    // IMPORTANT!! Mapbox layer language layers: https://gist.github.com/AlanPew/586715a1ee7a58956575

    // ---------------------------------------------------------
    // implements LoaderManager.LoaderCallbacks<Cursor>
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getContext(),
                DataContentProvider.CITY_CONTENT_URI,
                null, // new String[]{DataContract.CityEntry.COLUMN_LONGITUDE, DataContract.CityEntry.COLUMN_LATITUDE}, // projection
                null, // DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?", // selection:
                null, // selectionArgs, // selectionArgs
                DataContract.WeatherEntry._ID // sort order
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

/*                IconFactory iconFactory = IconFactory.getInstance(getContext());

                Icon icon = iconFactory.fromResource(getContext().getResources().
                        getIdentifier("_" + cursor.getString(cursor
                                        .getColumnIndex(DataContract.WeatherEntry.COLUMN_ICON_NAME)),
                                "drawable", getContext().getPackageName()));*/


                mMapboxMap.addMarker(new MarkerOptions()
                                .position(point)
                                .title(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)))
                                .snippet(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY)))
                        //  .icon(icon)
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
        //  markerCursorAdapter.swapCursor(null);
    }
    // ---------------------------------------------------------


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (AddCityFragment.Callbacks) context;
    }


    @Override
    public void onDetach() {
        super.onDetach();
        activity = null;
    }


    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }


    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }


    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_city, container, false);

        setHasOptionsMenu(true); // show action bar menu

        cityEditText = (TextView) view.findViewById(R.id.cityEditText);
        messageTextView = (TextView) view.findViewById(R.id.messageTextView);

        // adjust z-Index
        view.findViewById(R.id.cityEditText).bringToFront();
        view.findViewById(R.id.messageTextView).bringToFront();

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_api_key));

        // Get the location engine object for later use.
        locationEngine = new LocationSource(getContext());
        locationEngine.activate();

        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        mapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(MapboxMap mapboxMap) {

                mMapboxMap = mapboxMap;

                // set map items language
                setMapLanguage();

                // https://github.com/mapbox/mapbox-android-demo/blob/72f1f41346bb34dd300375ef2760a0778a54d757/MapboxAndroidDemo/src/main/java/com/mapbox/mapboxandroiddemo/examples/styles/LanguageSwitchActivity.java
                // https://www.mapbox.com/studio/tilesets/mapbox.mapbox-streets-v7/

                Log.d("AAAAA", "mMapboxMap.isMyLocationEnabled = " + mMapboxMap.isMyLocationEnabled());

                permissionsManager = new PermissionsManager(AddCityFragment.this);

                if (!PermissionsManager.areLocationPermissionsGranted(getContext())) {
                    permissionsManager.requestLocationPermissions(getActivity());
                } else {
                    enableLocation();
                }

                // Customize the user location icon using the getMyLocationViewSettings object.
                mMapboxMap.getMyLocationViewSettings().setPadding(0, 200, 0, 0);
                mMapboxMap.getMyLocationViewSettings().setForegroundTintColor(Color.parseColor("#56B881"));
                mMapboxMap.getMyLocationViewSettings().setAccuracyTintColor(Color.parseColor("#FBB03B"));

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

                    mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    cursor.close();
                }


                // Click listener
                mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {

                    @Override
                    public void onMapClick(@NonNull LatLng point) {

                        activity.onMapClicked(); // hide keyboard

                   //     Log.d("AAAAA", "point = " + point);

                        if (point != null) {
                            //  geocode(point);
                            reverseGeocode(point);
                        }
                    }
                });
            }
        });


        // add marker - WORKS !!!
        // Create an Icon object for the marker to use
///                Icon icon = IconFactory.getInstance(DrawCustomMarkerActivity.this)
///                        .fromResource(R.drawable.purple_marker);

/*
                mMapboxMap.addMarker(new MarkerOptions()
                                .position(new LatLng(43.7383, 7.4094))
                                .title("title") //getString(R.string.draw_marker_options_title))
                                .snippet("snippet") //getString(R.string.draw_marker_options_snippet)));
                        /// .icon(icon));
                );
*/


        return view;
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
                mMapboxMap.getLayer(layerName).setProperties(textField(property));
            } catch (Exception e) {
                Log.d("AAAAA", "e.getMessage() = " + e.getMessage());
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void enableLocation() {
        // https://github.com/mapbox/mapbox-android-demo/blob/master/MapboxAndroidDemo/src/main/java/com/mapbox/mapboxandroiddemo/examples/location/CustomizeUserLocationActivity.java

        // If we have the last location of the user, we can move the camera to that position.
        Location lastLocation = locationEngine.getLastLocation();

        if (lastLocation != null) {
            mMapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 4));
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
                    mMapboxMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 4));
                    locationEngine.removeLocationEngineListener(this);
                }
            }
        };

        locationEngine.addLocationEngineListener(locationEngineListener);

        //Enable or disable the location layer on the map
////        mMapboxMap.setMyLocationEnabled(true);
    }


    // add city markers on the map
    private void InitMarkerLoader() {

        // --------------------------------------------------------------
        // IMPORTANT !!! Change loader for different query
        Loader loader = getLoaderManager().getLoader(MainActivity.LOADER_MARKER_ID);

        if (loader != null && !loader.isReset()) {
            getLoaderManager().restartLoader(MainActivity.LOADER_MARKER_ID, null, AddCityFragment.this);
        } else {
            getLoaderManager().initLoader(MainActivity.LOADER_MARKER_ID, null, AddCityFragment.this);
        }
        // --------------------------------------------------------------
    }


 /*
    // https://github.com/mapbox/mapbox-java/blob/master/mapbox/app/src/main/java/com/mapbox/services/android/testapp/geocoding/GeocodingReverseActivity.java
    // geocoding parameters: https://www.mapbox.com/api-documentation/#request-format
    private void geocode(LatLng point) {

        Position position = Position.fromCoordinates(point.getLongitude(), point.getLatitude());

        //    String[] languageCodeArray = getResources().getStringArray(R.array.language_parameter_array);

        String locale = getCurrentLocale().getCountry().toLowerCase();

        // https://www.mapbox.com/api-documentation/#search-for-places
        // https://www.mapbox.com/api-playground/#/reverse-geocoding/?_k=drcw7f

        // https://api.mapbox.com/geocoding/v5/mapbox.places/38,48.json?access_token=pk.eyJ1IjoiYXJzb25lIiwiYSI6ImNqNGp2NDRoeDBid3YzM3FzaHYwMWdiMzAifQ.mYNMcy5YsMKFRXlEt6jNJw

        Log.d("AAAAA", "geocode - locale = " + locale);


        MapboxGeocoding client = new MapboxGeocoding.Builder()
                .setAccessToken(getString(R.string.mapbox_api_key))
                .setCoordinates(position)
                .setLanguage(locale)
                .setGeocodingType(GeocodingCriteria.TYPE_PLACE)
                .build();

        client.enqueueCall(new Callback<GeocodingResponse>() {

            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                //   messageTextView.setText("Getting place name, please wait...");

                if (response == null || response.body() == null) {
//                    messageTextView.setText(""); // clear city nam
                    Log.d("AAAAA", "response == null || response.body() == null");
                    return;
                }

                List<CarmenFeature> results = response.body().getFeatures();

                if (results.size() > 0) {
                    /// String placeName = results.get(0).getPlaceName();

                    double coord[] = results.get(0).getCenter();

                   Log.d("AAAAA", "long = " + coord[0]);
                    Log.d("AAAAA", "lat = " + coord[1]);


                    cityEditText.setText(results.get(0).getText());

                    messageTextView.setText("");
                } else {
                    messageTextView.setText(R.string.no_results);
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                // messageTextView.setText(throwable.getMessage());
                messageTextView.setText(R.string.cannot_get_place_name);
            }
        });
    }
    */


    private void reverseGeocode(LatLng point) {

        String locale = getCurrentLocale().getCountry().toLowerCase();

        String url = "https://api.mapbox.com/geocoding/v5/mapbox.places/"
                + point.getLongitude() + "," + point.getLatitude();

        if (!(locale.equals("en") || locale.equals("us"))) {

            url += ".json?language=" + locale + "&access_token=" + getString(R.string.mapbox_api_key);
        } else {
            url += ".json?access_token=" + getString(R.string.mapbox_api_key);
        }

        //      Log.d("AAAAA", "url: " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url,
                new com.android.volley.Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        try {

                            //     Log.d("AAAAA", "response = " + response);

                            JSONArray featuresArray = response.getJSONArray("features");

                            String placeName = null;

                            for (int i = 0; i < featuresArray.length(); i++) {

                                //   Log.d("AAAAA", "i = " + i + "featuresArray = " + featuresArray.getJSONObject(i));

                                JSONObject element = featuresArray.getJSONObject(i);

                                JSONArray placeTypeArray = element.getJSONArray("place_type");

                                String place = placeTypeArray.getString(0);

                                //  Log.d("AAAAA","place = " + place);

                                if (place.equals("place")) {

                                    placeName = element.getString("text");
                                    //      Log.d("AAAAA","placeName = " + placeName);
                                }
                            }

                            if (placeName != null) {
                                cityEditText.setText(placeName);
                                messageTextView.setText("");
                            } else {
                                messageTextView.setText(R.string.no_results);
                            }
                        } catch (Exception e) {
                            Log.d("AAAAA", "Exception: " + e.getMessage());
                        }
                    }
                }, new com.android.volley.Response.ErrorListener() {

            // ERROR MODES
            @Override
            public void onErrorResponse(VolleyError error) {

                NetworkResponse response = error.networkResponse;

                if (response != null && response.data != null) {

                    if (response.statusCode == 404) {

                        Log.d("AAAA", "onErrorResponse: 404");
                        messageTextView.setText(R.string.cannot_get_place_name);
                    }
                }
            }
        });

        // Defining the Volley request queue that handles the URL request concurrently
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
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


    private void saveCity() {

        enteredCity = cityEditText.getText().toString().trim();

        if (enteredCity.length() != 0) {

            // check if database has such city?
            Cursor cursor = getActivity().getContentResolver().query(DataContentProvider.CITY_CONTENT_URI,
                    new String[]{DataContract.CityEntry.COLUMN_ENTERED_CITY},
                    DataContract.CityEntry.COLUMN_ENTERED_CITY + "=? COLLATE NOCASE",
                    new String[]{enteredCity},
                    null,
                    null);

            if (cursor != null) {

                if (cursor.getCount() > 0) {

                    Toast.makeText(getActivity(), getString(R.string.message_city_has_in_db), Toast.LENGTH_SHORT).show();
                    cursor.close();
                    return;
                }
            }

            // INSERT entered city to database
            ContentValues values = new ContentValues();

            values.put(DataContract.CityEntry.COLUMN_ENTERED_CITY, enteredCity);

            Uri cityUri = getActivity().getContentResolver().insert(DataContentProvider.CITY_CONTENT_URI, values);

            // Log.d("AAAAA", "Inserted = " + cityUri.getLastPathSegment());
            // Log.d("AAAAA", "Inserted to DB city _id = " + Integer.parseInt(cityUri.getLastPathSegment()));
            // Log.d("AAAAA", "Inserted = " + cityUri);

            if (cityUri != null) {
                activity.onSaveCity(Integer.parseInt(cityUri.getLastPathSegment()), enteredCity);
            }

        } else // required entered_city returned_name is blank, so display error dialog
        {
            Toast.makeText(getActivity(), R.string.message_fill_city, Toast.LENGTH_SHORT).show();
        }

    }


    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // clear previous menu items
        menu.clear();

        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.add_fragment_menu, menu);
    }


    // handle choice from options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_save:

                saveCity();

                return true;

            case R.id.action_cancel:

                activity.onCancelAddEdit();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}