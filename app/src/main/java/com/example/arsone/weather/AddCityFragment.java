package com.example.arsone.weather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.SupportMapFragment;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;
import com.mapbox.services.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.services.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.services.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.services.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.services.commons.models.Position;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;


// https://stackoverflow.com/questions/30991087/mapfragment-getmapasyncthis-nullpointerexception


public class AddCityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
 PermissionsListener        {

    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationEngineListener locationEngineListener;


    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(getContext(), "R.string.user_location_permission_explanation", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {

        if (granted) {
            enableLocation();
        } else {
            // finish();
        }
    }

    public interface Callbacks {

        void onSaveCity(int id, String enteredCity);

        void onCancelAddEdit();

        void onMapClicked();

      ///  void refreshCitiesListForTabletDevice();
    }

    private Callbacks activity;

    private TextView cityEditText;

    private String enteredCity;

    private TextView messageTextView;

    private MapView mapView;

    private MapboxMap mMapboxMap;


    // ---------------------------------------------------------
    // implements LoaderManager.LoaderCallbacks<Cursor>
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getContext(),
                DataContentProvider.CITY_CONTENT_URI,
                null, // new String[]{DataContract.CityEntry.COLUMN_LONGITUDE, DataContract.CityEntry.COLUMN_LATITUDE}, // projection
                null, // DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?", // selection:
                null, // selectionArgs, // selectionArgs
                null // DataContract.WeatherEntry._ID // sort order
        );

/*
        return new CursorLoader(getContext(),
                DataContentProvider.WEATHER_CONTENT_ID_URI,
                null,
                DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?", // selection: city ID
                selectionArgs, // selectionArgs
                DataContract.WeatherEntry._ID // sort order
        );
*/
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        //   markerCursorAdapter.swapCursor(cursor);

        ///   Log.d("AAAAA", "MARKERS: onLoadFinished: results count = " + cursor.getCount());

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {

            double lat = cursor.getDouble(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_LATITUDE));
            double lon = cursor.getDouble(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_LONGITUDE));

            if(lat != 0 && lon != 0) {

                LatLng point = new LatLng(lat, lon);

                mMapboxMap.addMarker(new MarkerOptions()
                                .position(point)
                                .title(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)))
                                .snippet(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY)))
                );
            }

            cursor.moveToNext();
        }

        if (cursor.getCount() == 0) {

            ///   showMessageBar("Данные не найдены", false);

            // set mStatus info
            ///    setModeBar(getString(R.string.message_city_not_found), R.color.nothingColor, R.drawable.ic_lamp_nothing);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //   markerCursorAdapter.swapCursor(null);
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

    // Add the mapView lifecycle to the activity's lifecycle methods
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
      //  Button saveCityButton = (Button) view.findViewById(R.id.saveCityButton);
     //   Button cancelButton = (Button) view.findViewById(R.id.editCancelButton);
        messageTextView = (TextView) view.findViewById(R.id.messageTextView);

        // add listeners
   //     saveCityButton.setOnClickListener(saveCityButtonClicked);
   //     cancelButton.setOnClickListener(cancelButtonClicked);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_api_key));

        // Get the location engine object for later use.
        locationEngine = new LocationSource(getContext());
        locationEngine.activate();

        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);


   ///     activity.clearPreviousMenu();

      ///  getActivity().invalidateOptionsMenu();



/*      MapboxMapOptions options = new MapboxMapOptions()
                .styleUrl(Style.LIGHT)

                              .camera(new CameraPosition.Builder()
                        .target(new LatLng(43.7383, 7.4094))
                        .zoom(12)
                        .build());
        // create map
        mapView = new MapView(getContext(), options);*/


        mapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(MapboxMap mapboxMap) {


/*                Layer mapText = mapboxMap.getLayer("country-label-lg");
                mapText.setProperties(textField("{name_ru}"));*/

                mMapboxMap = mapboxMap;

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


             //   Log.d("AAAAA", "mMapboxMap.isMyLocationEnabled = ");

                //    mapboxMap.setStyleUrl("mapbox://styles/<your-account-name>/<your-style-ID>");
                //   mapboxMap.setStyleUrl("mapbox://styles/mapbox/streets-v10");




 /*               /// Layer mapText = mMapboxMap.getLayer("country-label-lg");
                Layer mapText = mMapboxMap.getLayer("country-label-lg");
                mapText.setProperties(textField("{name_ru}"));*/


                // add markers
                addMarkers();

                // Click listener
                mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {

                    @Override
                    public void onMapClick(@NonNull LatLng point) {

                        ///    mapboxMap.clear();

                        activity.onMapClicked(); // hide keyboard

                        geocode(point);
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
        mMapboxMap.setMyLocationEnabled(true);
    }


    private void addMarkers() {

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

    // https://github.com/mapbox/mapbox-java/blob/master/mapbox/app/src/main/java/com/mapbox/services/android/testapp/geocoding/GeocodingReverseActivity.java
    private void geocode(LatLng point) {

        Position position = Position.fromCoordinates(point.getLongitude(), point.getLatitude());

        MapboxGeocoding client = new MapboxGeocoding.Builder()
                .setAccessToken(getString(R.string.mapbox_api_key))
                .setCoordinates(position)
                .setGeocodingType(GeocodingCriteria.TYPE_PLACE)
                .build();

        client.enqueueCall(new Callback<GeocodingResponse>() {

            @Override
            public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

                messageTextView.setText("Getting place name, please wait...");

                if (response == null) {
//                    messageTextView.setText(""); // clear city nam
                    return;
                }

                List<CarmenFeature> results = response.body().getFeatures();

                if (results.size() > 0) {
                    /// String placeName = results.get(0).getPlaceName();

                    double coord[] = results.get(0).getCenter();

      /*              Log.d("AAAAA", "long = " + coord[0]);
                    Log.d("AAAAA", "lat = " + coord[1]);*/


                    cityEditText.setText(results.get(0).getText());

                    messageTextView.setText("");
                } else {
                    messageTextView.setText("No results");
                }
            }

            @Override
            public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
                // messageTextView.setText(throwable.getMessage());
                messageTextView.setText("Can`t get place name...");
            }
        });
    }

  /*  // "cancel" button clicked
    View.OnClickListener cancelButtonClicked = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            activity.onCancelAddEdit();
        }
    };

    // "save" button clicked
    View.OnClickListener saveCityButtonClicked = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            enteredCity = cityEditText.getText().toString().trim();

            if (enteredCity.length() != 0) {

                // check if database has such city?
                Cursor cursor = getActivity().getContentResolver().query(DataContentProvider.CITY_CONTENT_URI,
                        new String[]{DataContract.CityEntry.COLUMN_ENTERED_CITY},
                        DataContract.CityEntry.COLUMN_ENTERED_CITY + "=?",
                        new String[]{enteredCity},
                        null);

                if (cursor.getCount() > 0) {

                    Toast.makeText(getActivity(), "Такой город уже есть в списке", Toast.LENGTH_SHORT).show();
                    return;
                }

                // save entered city to database
                ContentValues values = new ContentValues();

                values.put(DataContract.CityEntry.COLUMN_ENTERED_CITY, enteredCity);

                Uri cityUri = getActivity().getContentResolver().insert(DataContentProvider.CITY_CONTENT_URI, values);

                  /// Log.d("AAAAA", "Inserted = " + cityUri.getLastPathSegment());
                   Log.d("AAAAA", "Inserted ID = " + Integer.parseInt(cityUri.getLastPathSegment()));
               /// Log.d("AAAAA", "Inserted = " + cityUri);

                activity.onSaveCity(Integer.parseInt(cityUri.getLastPathSegment()), enteredCity);

            } else // required entered_city returned_name is blank, so display error dialog
            {
                Toast.makeText(getActivity(), R.string.message_fill_city, Toast.LENGTH_SHORT).show();
            }
        }
    };*/



    private void saveCity(){

        enteredCity = cityEditText.getText().toString().trim();

        if (enteredCity.length() != 0) {

            // check if database has such city?
            Cursor cursor = getActivity().getContentResolver().query(DataContentProvider.CITY_CONTENT_URI,
                    new String[]{DataContract.CityEntry.COLUMN_ENTERED_CITY},
                    DataContract.CityEntry.COLUMN_ENTERED_CITY + "=?",
                    new String[]{enteredCity},
                    null);

            if (cursor.getCount() > 0) {

                Toast.makeText(getActivity(), "Такой город уже есть в списке", Toast.LENGTH_SHORT).show();
                return;
            }

            // INSERT entered city to database
            ContentValues values = new ContentValues();

            values.put(DataContract.CityEntry.COLUMN_ENTERED_CITY, enteredCity);

            Uri cityUri = getActivity().getContentResolver().insert(DataContentProvider.CITY_CONTENT_URI, values);

            // Log.d("AAAAA", "Inserted = " + cityUri.getLastPathSegment());
           Log.d("AAAAA", "Inserted to DB city _id = " + Integer.parseInt(cityUri.getLastPathSegment()));
            // Log.d("AAAAA", "Inserted = " + cityUri);

            activity.onSaveCity(Integer.parseInt(cityUri.getLastPathSegment()), enteredCity);

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


/*    @Override
    public void onPrepareOptionsMenu(Menu menu) {

   //     menu.clear();

        for(int index = 0 ; index < menu.size() ; index ++) {

            MenuItem menuItem = menu.getItem(index);

*//*            if (menuItem != null) {
                // hide the menu items if the drawer is open
                menuItem.setVisible(mMenuVisible);
            }*//*
        }

        super.onPrepareOptionsMenu(menu);
    }*/


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