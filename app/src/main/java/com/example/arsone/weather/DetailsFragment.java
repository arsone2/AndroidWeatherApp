package com.example.arsone.weather;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.arsone.weather.data.City;
import com.example.arsone.weather.data.Weather;


public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

/*    // Loader ID
    public static final int LOADER_WEATHER_ID = 1;*/

    // Fragment view title
    private TextView titleTextView;
    private TextView updateTimeTextView;

    // mode panel
    private LinearLayout modePanelLayout;
    private TextView modeTextView;
    private AppCompatImageView modeImageView;

/*    // message panel
    private LinearLayout messageBarLayout;
    private TextView messageTextView;
    private ProgressBar messageProgressBar;*/

    // weather ListView
    private ListView weatherListView;

    // weather objects List
    private List<Weather> weatherList = new ArrayList<>();

    // ArrayAdapter between  weather List &&  weather ListView
    /// private WeatherArrayAdapter weatherArrayAdapter;

    private WeatherCursorAdapter weatherCursorAdapter;

    // selected city database table _id
    private int mID;

    // selected city name
    private String mEnteredCity;

 //   private RefreshDataTask mRefreshDataTask;


    private int mUnitsFormat;

    // ---------------------------------------------------------------
    // LoaderManager.LoaderCallbacks<Cursor> methods:

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

     ///   String[] selectionArgs = new String[]{String.valueOf(mID)};

     //   Log.d("AAAAA", "onCreateLoader: selectionArgs[0] = " + selectionArgs[0]);

        return new CursorLoader(getContext(),
                Uri.parse(DataContentProvider.WEATHER_CONTENT_URI.toString() + "/" + mID),
                null,
                null,// DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?", // selection: city ID
                null, // new String[]{ formattedDate }, // selectionArgs
                null // DataContract.WeatherEntry._ID // sort order
        );
/*        return new CursorLoader(getContext(),
                DataContentProvider.WEATHER_CONTENT_ID_URI,
                null,
                DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?", // selection: city ID
                selectionArgs, // selectionArgs
                DataContract.WeatherEntry._ID // sort order
        );*/
// return null;
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        weatherCursorAdapter.swapCursor(cursor);

  //      Log.d("AAAAA", "onLoadFinished: results count = " + cursor.getCount());

        if (cursor.getCount() == 0) {

            ///showMessageBar("Данные не найдены", false);

            // set mStatus info
            setModeBar(getString(R.string.message_city_not_found),
                    R.color.nothingColor, R.drawable.ic_lamp_nothing);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        weatherCursorAdapter.swapCursor(null);

        //   Log.d("AAAAA", "onLoaderReset");
    }
    // ---------------------------------------------------------------

    @Override
    public void onDestroy() {

/*        if (mRefreshDataTask != null) {

            mRefreshDataTask.cancel(true);
        }*/

        super.onDestroy();
    }

    private String mReturnedCity;

    private String mDataUpdateTime;

/*    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();

        if (bundle != null) {
            mID = bundle.getInt(City.CITY_ID);
            mEnteredCity = bundle.getString(City.ENTERED_CITY);
            mReturnedCity = bundle.getString(City.RETURNED_CITY);
            mUnitsFormat = bundle.getInt(MainActivity.UNITS_FORMAT);
            mDataUpdateTime = bundle.getString(City.UPDATE_TIME);

            Log.d("AAAAA", "if (bundle != null) - mUnitsFormat = " + mUnitsFormat);
        }
    }*/


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_details, container, false);


        Bundle bundle = getArguments();

        if (bundle != null) {
            mID = bundle.getInt(City.CITY_ID);
            mEnteredCity = bundle.getString(City.ENTERED_CITY);
            mReturnedCity = bundle.getString(City.RETURNED_CITY);
            mUnitsFormat = bundle.getInt(MainActivity.UNITS_FORMAT);
            mDataUpdateTime = bundle.getString(City.UPDATE_TIME);

            Log.d("AAAAA", "if (bundle != null) - mUnitsFormat = " + mUnitsFormat);
        }


        // Title
        titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        updateTimeTextView = (TextView) view.findViewById(R.id.updateTimeTextView);

        // mode panel
        modePanelLayout = (LinearLayout) view.findViewById(R.id.modePanelLayout);
        modeTextView = (TextView) view.findViewById(R.id.modeTextView);
        modeImageView = (AppCompatImageView) view.findViewById(R.id.modeImage);

/*        // message bar panel
        messageBarLayout = (LinearLayout) view.findViewById(R.id.messageBar);
        messageTextView = (TextView) view.findViewById(R.id.messageTextView);
        messageProgressBar = (ProgressBar) view.findViewById(R.id.messageProgressBar);*/

        // weather ListView
        weatherListView = (ListView) view.findViewById(R.id.detailsListView);

        // Title
        if(TextUtils.isEmpty(mReturnedCity))
            titleTextView.setText(mEnteredCity);
        else
            titleTextView.setText(mEnteredCity + "/ " + mReturnedCity);

///        Log.d("AAAAA", "onCreateView: mID = " + mID);

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date date = format.parse(mDataUpdateTime);

            String stringDate = DateFormat.getDateTimeInstance().format(date);

            updateTimeTextView.setText(getContext().getString(R.string.weather_data_obtained, stringDate));

            // System.out.println(date);
        } catch (ParseException e) {
            Log.d("AAAAA", "Date ParseException: " + e.getMessage());
        }

        showDetails();

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

/*        // set array adapter to get weather data from server

        if (getActivity().findViewById(R.id.onePaneLayout) != null) { // phone
            weatherArrayAdapter = new WeatherArrayAdapter(getActivity(),
                    weatherList,
                    R.layout.list_item_weather // phone layout
            );
        } else { // tablet
            weatherArrayAdapter = new WeatherArrayAdapter(getActivity(),
                    weatherList,
                    R.layout.list_item_weather_tablet // tablet layout
            );
        }

        weatherListView.setAdapter(weatherArrayAdapter);*/

        // GET ONLINE DATA FROM WEATHER SERVER
        ////getWeather(); // COMMENTED


    }



    private void showDetails(){

/*        if (getActivity().findViewById(R.id.onePaneLayout) != null) { // phone
            weatherCursorAdapter = new WeatherCursorAdapter(getContext(), null, 0,
                    R.layout.list_item_weather // phone layout
            );
        } else { // tablet
            weatherCursorAdapter = new WeatherCursorAdapter(getContext(), null, 0,
                    R.layout.list_item_weather_tablet // tablet layout
            );
        }*/


        weatherCursorAdapter = new WeatherCursorAdapter(getContext(), null, 0, mUnitsFormat);
        weatherListView.setAdapter(weatherCursorAdapter);

        // --------------------------------------------------------------
        // IMPORTANT !!! Change loader for different query
        Loader loader = getLoaderManager().getLoader(MainActivity.LOADER_WEATHER_ID);

        if (loader != null && !loader.isReset()) {
            getLoaderManager().restartLoader(MainActivity.LOADER_WEATHER_ID, null, DetailsFragment.this);
        } else {
            getLoaderManager().initLoader(MainActivity.LOADER_WEATHER_ID, null, DetailsFragment.this);
        }
        // --------------------------------------------------------------

    }


 /*   private void showMessageBar(String text, boolean showProgressBar) {

        messageBarLayout.setVisibility(View.VISIBLE);
        messageTextView.setText(text);

        if (showProgressBar)
            messageProgressBar.setVisibility(View.VISIBLE);
        else
            messageProgressBar.setVisibility(View.GONE);
    }


    private void hideMessageBar() {

        messageBarLayout.setVisibility(View.GONE);
    }*/


    private void setModeBar(String text, int color, int image) {

        modePanelLayout.setVisibility(View.VISIBLE);
        modeTextView.setText(text);
        modeTextView.setTextColor(ContextCompat.getColor(getContext(), color));
        modeImageView.setImageResource(image);
    }


/*    private void hideModeBar() {

        modePanelLayout.setVisibility(View.GONE);
    }*/


  /*  private void getWeather() {

        String url = createURL(mEnteredCity);

        Log.d("AAAAA", "URL = " + url);

        if (url != null) {

            getWeatherNew(url);
        }
    }


    //  create URL for openweathermap.org
    private String createURL(String city) {

        try {
            return getString(R.string.web_service_url)
                    + URLEncoder.encode(city, "UTF-8")
                    + "&units=metric&lang=ru&cnt=5&APPID="
                    + getString(R.string.openweathermap_api_key);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null; // incorrect URL
    }


    private void getWeatherNew(String url) {

   ///     showMessageBar(getString(R.string.message_wait_for_data), true);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        String returnedCityName = "";
                        long cityID = -1;
                        String countryCode = "";
                        double cityLongitude;
                        double cityLatitude;

                        try {
                            JSONArray list = null;

                            Log.d("AAAAA", "response = " + response);

                            // set mStatus info
                            setModeBar(getResources().getText(R.string.online).toString(), R.color.onlineColor, R.drawable.ic_lamp_online);

                            list = response.getJSONArray("list");

                            JSONObject jsonCity = response.getJSONObject("city");

                            ///        Log.d("AAAAA", "city = " + jsonCity);

                            // city info
                            returnedCityName = jsonCity.getString("name");
                            cityID = jsonCity.getInt("id");
                            countryCode = jsonCity.getString("country");

                            // city coordinates
                            JSONObject jsonCoords = jsonCity.getJSONObject("coord");
                            cityLongitude = jsonCoords.getDouble("lon");
                            cityLatitude = jsonCoords.getDouble("lat");

                            // Преобразовать каждый элемент списка в объект Weather
                            for (int i = 0; i < list.length(); ++i) {
                                JSONObject day = list.getJSONObject(i); // Данные за день

                                JSONObject temperatures = day.getJSONObject("temp");

                                JSONObject weather = day.getJSONArray("weather").getJSONObject(0);

                                weatherList.add(new Weather(

                                        day.getLong("dt"), // long timeStamp
                                        temperatures.getDouble("morn"), // double morningTemp
                                        temperatures.getDouble("day"), // double dayTemp
                                        temperatures.getDouble("eve"), // double eveningTemp
                                        temperatures.getDouble("night"), // double nightTemp
                                        temperatures.getDouble("min"), // double minTemp
                                        temperatures.getDouble("max"), // double maxTemp
                                        day.getInt("humidity"), // int humidity
                                        day.getDouble("pressure"), // double pressure
                                        day.getDouble("speed"), // double windSpeed
                                        day.getInt("deg"), // int windDirection
                                        weather.getString("description"), // String description
                                        weather.getString("icon"), // String iconName
                                        weather.getInt("id") // int weatherID
                                ));
                            }

                            // database operations transaction:
                            // 1. update city info
                            // 2. ic_delete_item previous weather info
                            // 3. insert new weather info
                            ContentValues cv = new ContentValues();

                            cv.put(DataContract.CityEntry.COLUMN_RETURNED_CITY, returnedCityName);
                            cv.put(DataContract.CityEntry.COLUMN_SERVER_CITY_ID, cityID);
                            cv.put(DataContract.CityEntry.COLUMN_LONGITUDE, cityLongitude);
                            cv.put(DataContract.CityEntry.COLUMN_LATITUDE, cityLatitude);
                            cv.put(DataContract.CityEntry.COLUMN_COUNTRY_CODE, countryCode);

                            mRefreshDataTask = new RefreshDataTask();
                            mRefreshDataTask.execute(cv);

                        } catch (JSONException e) {
                            Log.d("AAAAA", "JSONException: " + e.getMessage());
                        } finally {
                      ///      hideMessageBar();
                        }
                    }
                }, new Response.ErrorListener() {

            // ERROR MODES
            @Override
            public void onErrorResponse(VolleyError error) {

            ///    hideMessageBar();

                NetworkResponse response = error.networkResponse;

                if (response != null && response.data != null) {

                    if (response.statusCode == 404) {

                        Log.d("AAAA", "onErrorResponse: 404");

                        // set mStatus info
                        setModeBar(getString(R.string.message_city_not_found),
                                R.color.nothingColor, R.drawable.ic_lamp_nothing);

                        return;
                    }
                }

                // OFFLINE MODE: show database data

                // set offline mode info
                setModeBar(getString(R.string.offline), R.color.offlineColor, R.drawable.ic_lamp_offline);

                Log.d("AAAAA", "OFF-LINE MODE");

                if (getActivity().findViewById(R.id.onePaneLayout) != null) { // phone
                    weatherCursorAdapter = new WeatherCursorAdapter(getContext(), null, 0,
                            R.layout.list_item_weather // phone layout
                    );
                } else { // tablet
                    weatherCursorAdapter = new WeatherCursorAdapter(getContext(), null, 0,
                            R.layout.list_item_weather_tablet // tablet layout
                    );
                }

                weatherListView.setAdapter(weatherCursorAdapter);

                // --------------------------------------------------------------
                // IMPORTANT !!! Change loader for different query
                Loader loader = getLoaderManager().getLoader(MainActivity.LOADER_WEATHER_ID);

                if (loader != null && !loader.isReset()) {
                    getLoaderManager().restartLoader(MainActivity.LOADER_WEATHER_ID, null, DetailsFragment.this);
                } else {
                    getLoaderManager().initLoader(MainActivity.LOADER_WEATHER_ID, null, DetailsFragment.this);
                }
                // --------------------------------------------------------------
            }
        });

        // Defining the Volley request queue that handles the URL request concurrently
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonObjectRequest);
    }*/

 /*   // ---------------------------------------------------------------------------------------------
    private class RefreshDataTask extends AsyncTask<ContentValues, Void, Void> {

        @Override
        protected Void doInBackground(ContentValues... params) {

            ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

            String citySelection = DataContract.CityEntry._ID + "=?";
            String[] selectionArgs = {String.valueOf(mID)};

            String weatherSelection = DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?";

            try {
                // update city data
                ops.add(ContentProviderOperation.newUpdate(DataContentProvider.CITY_CONTENT_URI)
                        .withSelection(citySelection, selectionArgs)
                        .withValues(params[0])
                        .build());

                // ic_delete_item old data in from weather table
                ops.add(ContentProviderOperation.newDelete(DataContentProvider.WEATHER_CONTENT_URI)
                        .withSelection(weatherSelection, selectionArgs)
                        .build());

                // insert new data in weather table
                for (int i = 0; i < weatherList.size(); i++) {
                    ops.add(ContentProviderOperation.newInsert(DataContentProvider.WEATHER_CONTENT_URI)
                            .withValue(DataContract.WeatherEntry.COLUMN_CITY_ID_FK, mID)                              // "city_id" - foreign key (int/INTEGER)
                            .withValue(DataContract.WeatherEntry.COLUMN_TIMESTAMP, weatherList.get(i).timeStamp) // day.getLong("dt"))                 // timestamp in seconds (int/INTEGER)
                            .withValue(DataContract.WeatherEntry.COLUMN_MORNING_TEMP, weatherList.get(i).morningTemp) // temperatures.getDouble("morn"))          // "temp_day"
                            .withValue(DataContract.WeatherEntry.COLUMN_DAY_TEMP, weatherList.get(i).dayTemp) // temperatures.getDouble("day")) - "temp_day"
                            .withValue(DataContract.WeatherEntry.COLUMN_EVENING_TEMP, weatherList.get(i).eveningTemp) // temperatures.getDouble("eve")) -"temp_day"
                            .withValue(DataContract.WeatherEntry.COLUMN_NIGHT_TEMP, weatherList.get(i).nightTemp) // temperatures.getDouble("night"))  - "temp_night"
                            .withValue(DataContract.WeatherEntry.COLUMN_MIN_TEMP, weatherList.get(i).minTemp) // temperatures.getDouble("min"))      - "temp_min"
                            .withValue(DataContract.WeatherEntry.COLUMN_MAX_TEMP, weatherList.get(i).maxTemp) //  temperatures.getDouble("max"))      // "temp_max"
                            .withValue(DataContract.WeatherEntry.COLUMN_HUMIDITY, weatherList.get(i).humidity) // day.getInt("humidity"))          // "humidity"
                            .withValue(DataContract.WeatherEntry.COLUMN_PRESSURE, weatherList.get(i).pressure) // day.getDouble("pressure"))          // "pressure"
                            .withValue(DataContract.WeatherEntry.COLUMN_SPEED, weatherList.get(i).windSpeed) // day.getDouble("speed"))          // "windSpeed"
                            .withValue(DataContract.WeatherEntry.COLUMN_DIRECTION, weatherList.get(i).windDirection) // day.getInt("deg"))          // "windDirection"
                            .withValue(DataContract.WeatherEntry.COLUMN_DESCRIPTION, weatherList.get(i).description) // weather.getString("description"))// "description"
                            .withValue(DataContract.WeatherEntry.COLUMN_ICON_NAME, weatherList.get(i).iconName) // weather.getString("icon"))         // "icon_name"
                            .build());

                    if (isCancelled()) { // cancel AsyncTask job
                        return null;
                    }
                }

                ContentProviderResult[] cpResults = getActivity().getContentResolver()
                        .applyBatch(DataContentProvider.AUTHORITY, ops);

//               if (cpResults != null) {
//
//                    Log.e("AAAAA", "cpResults[1].count = " + cpResults[1].count);
//                    //Log.e("AAAAA","cpResults = " + cpResults.);
//                    //Log.e("AAAAA","cpResults = " + cpResults.toString());
//
//                    Toast.makeText(getContext(), getString(R.string.rows_deleted)
//                            + cpResults[1].count, Toast.LENGTH_SHORT).show();

                //}
            } catch (RemoteException e) {
                Log.e("AAAAA", "RemoteException " + e.getMessage());
            } catch (OperationApplicationException e) {
                Log.e("AAAAA", "OperationApplicationException: " + e.getMessage());
                ops.clear();
            }
            return null;
        }
    }*/
}


 /*   private class GetWeatherTask extends AsyncTask<URL, Integer, JSONObject> {

        @Override
        protected void onPreExecute() {

            showMessageBar(getString(R.string.message_wait_for_server), true);

            /// progressDialog = ProgressDialog.show(getContext(), "Загрузка...", "Получение данных с сервера, подождите...", false, false);
        }


        @Override
        protected JSONObject doInBackground(URL... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                synchronized (this) {

                    int counter = 0;

                    connection = (HttpURLConnection) params[0].openConnection();

                    // to avoid connection dead hang
                    connection.setRequestProperty("User-agent", System.getProperty("http.agent"));
                    connection.addRequestProperty("x-api-key", getResources().getString(R.string.api_key));
                    // connection.setRequestMethod("GET");
                    connection.setReadTimeout(2000); // timeout in milliseconds
                    connection.setConnectTimeout(2000);
                    connection.setDoInput(true);
                    connection.setRequestProperty("connection", "close");

                    int response = connection.getResponseCode();

                    if (response == HttpURLConnection.HTTP_OK) {

                        StringBuilder builder = new StringBuilder(1024);

                 //       try {
                            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                            String line = "";

                            while ((line = reader.readLine()) != null) {
                                builder.append(line);

                                counter++;

                                if(isCancelled()){ // cancel AsyncTask job
                                    return null;
                                }

                                //Set the current progress.
                                //This value is going to be passed to the onProgressUpdate() method.
                                ///publishProgress(counter);
                            }

                            mStatus = messageEnum.OK;

                            JSONObject data = new JSONObject(builder.toString());

                            return data;

               //         }

                    } else {
                        Log.d("AAAAA", "Exception: city not found.");
                        mStatus = messageEnum.CITY_NOT_FOUND;
                    }
                } // synchronized (this)
            } // try
            catch (JSONException e) {
                Log.d("AAAAA", "JSONException: " + e.getMessage());
                mStatus = messageEnum.JSON_EXCEPTION;
            }
            catch (java.net.SocketTimeoutException e) {

                Log.d("AAAAA", "SocketTimeoutException: " + e.getMessage());
                mStatus = messageEnum.SOCKET_TIMEOUT;
            }
            catch (IOException e) { // no connection to server
                Log.d("AAAAA", "Exception: reader error: " + e.getMessage());
                mStatus = messageEnum.IO_EXCEPTION;
            }
            finally {

                if (connection != null)
                    connection.disconnect();

                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.d("AAAAA", "Error closing stream: "  + e.getMessage());
                    }
                }
            }
            return null;
        }


        //Update the progress
        @Override
        protected void onProgressUpdate(Integer... values) {
            //set the current progress of the progress dialog
            ///progressDialog.setProgress(values[0]);
        }


        @Override
        protected void onPostExecute(JSONObject forecast) {

            //close the progress dialog
            /// progressDialog.dismiss();

            hideMessageBar();

            Log.d("AAAAA", "onPostExecute(): mStatus = " + mStatus);

            Log.d("AAAAA", "JSON data: " + forecast);



            if (mStatus == messageEnum.OK) { //online mode

                // set mStatus info
                setModeBar(getResources().getText(R.string.online).toString(),
                        R.color.onlineColor, R.drawable.ic_lamp_online);

                  String returnedCityName = "";
                long cityID = -1;
                String countryCode = "";
                double cityLongitude;
                double cityLatitude;

                // Получение свойства "list" JSONArray
                JSONArray list = null;
                try {
                    list = forecast.getJSONArray("list");

                    JSONObject jsonCity = forecast.getJSONObject("city");

                    // city info
                    returnedCityName = jsonCity.getString("name");
                    cityID = jsonCity.getInt("id");
                    countryCode = jsonCity.getString("country");

                    // city coordinats
                    JSONObject jsonCoords = jsonCity.getJSONObject("coord");
                    cityLongitude = jsonCoords.getDouble("lon");
                    cityLatitude = jsonCoords.getDouble("lat");

                    if (mID > 0) {

                        // ic_delete_item all previous weather data in database
                        String selection = DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?";

                        String[] selectionArgs = {String.valueOf(mID)};

                        int rowsDeleted = getActivity().getContentResolver()
                                .ic_delete_item(DataContentProvider.WEATHER_CONTENT_URI, selection, selectionArgs);

                        Log.d("AAAAA", "ic_delete_item all previous weather data: rowsDeleted = " + rowsDeleted);

                        // update city data
                        Uri uri = ContentUris.withAppendedId(DataContentProvider.CITY_CONTENT_ID_URI, mID);

                        ContentValues values = new ContentValues();

                        values.put(DataContract.CityEntry.COLUMN_RETURNED_CITY, returnedCityName);
                        values.put(DataContract.CityEntry.COLUMN_SERVER_CITY_ID, cityID);
                        values.put(DataContract.CityEntry.COLUMN_LONGITUDE, cityLongitude);
                        values.put(DataContract.CityEntry.COLUMN_LATITUDE, cityLatitude);
                        values.put(DataContract.CityEntry.COLUMN_COUNTRY_CODE, countryCode);

                        int rowsUpdated = getActivity().getContentResolver().update(uri, values, null, null);

                        Log.d("AAAAA", "City rowsUpdated: " + rowsUpdated);
                    }


                    // Преобразовать каждый элемент списка в объект Weather
                    for (int i = 0; i < list.length(); ++i) {
                        JSONObject day = list.getJSONObject(i); // Данные за день

                        JSONObject temperatures = day.getJSONObject("temp");

                        JSONObject weather = day.getJSONArray("weather").getJSONObject(0);

                        weatherList.add(new Weather(

                        day.getLong("dt"), // long timeStamp
                        temperatures.getDouble("morn"), // double morningTemp
                        temperatures.getDouble("day"), // double dayTemp
                        temperatures.getDouble("eve"), // double eveningTemp
                        temperatures.getDouble("night"), // double nightTemp
                        temperatures.getDouble("min"), // double minTemp
                        temperatures.getDouble("max"), // double maxTemp
                        day.getInt("humidity"), // int humidity
                        day.getDouble("pressure"), // double pressure
                        day.getDouble("speed"), // double windSpeed
                        day.getInt("deg"), // int windDirection
                        weather.getString("description"), // String description
                        weather.getString("icon"), // String iconName
                        weather.getInt("id") // int weatherID
                        ));

                        if (mID > 0) {

                            // insert new weather data to database
                            ContentValues values = new ContentValues();

                            values.put(DataContract.WeatherEntry.COLUMN_CITY_ID_FK, mID);                              // "city_id" - foreign key (int/INTEGER)
                            values.put(DataContract.WeatherEntry.COLUMN_TIMESTAMP, day.getLong("dt"));                 // timestamp in seconds (int/INTEGER)
                            values.put(DataContract.WeatherEntry.COLUMN_MORNING_TEMP, temperatures.getDouble("morn"));          // "temp_day"
                            values.put(DataContract.WeatherEntry.COLUMN_DAY_TEMP, temperatures.getDouble("day"));          // "temp_day"
                            values.put(DataContract.WeatherEntry.COLUMN_EVENING_TEMP, temperatures.getDouble("eve"));          // "temp_day"
                            values.put(DataContract.WeatherEntry.COLUMN_NIGHT_TEMP, temperatures.getDouble("night"));  // "temp_night"
                            values.put(DataContract.WeatherEntry.COLUMN_MIN_TEMP, temperatures.getDouble("min"));      // "temp_min"
                            values.put(DataContract.WeatherEntry.COLUMN_MAX_TEMP, temperatures.getDouble("max"));      // "temp_max"
                            values.put(DataContract.WeatherEntry.COLUMN_HUMIDITY, day.getInt("humidity"));          // "humidity"
                            values.put(DataContract.WeatherEntry.COLUMN_PRESSURE, day.getDouble("pressure"));          // "pressure"
                            values.put(DataContract.WeatherEntry.COLUMN_SPEED, day.getDouble("speed"));          // "windSpeed"
                            values.put(DataContract.WeatherEntry.COLUMN_DIRECTION, day.getInt("deg"));          // "windDirection"
                            values.put(DataContract.WeatherEntry.COLUMN_DESCRIPTION, weather.getString("description"));// "description"
                            values.put(DataContract.WeatherEntry.COLUMN_ICON_NAME, weather.getString("icon"));         // "icon_name"

                            Uri uriInserted = getActivity().getContentResolver().insert(DataContentProvider.WEATHER_CONTENT_URI, values);

                            Log.d("AAAAA", "Inserted = " + uriInserted.getLastPathSegment());
                        }
                    }

                    weatherArrayAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d("AAAAA", "JSONException: " + e.getMessage());
                }
            } // if (mStatus == messageEnum.OK)

            else if (mStatus == messageEnum.CITY_NOT_FOUND) { // city name not found on server

                // set mStatus info
                setModeBar(getString(R.string.message_city_not_found), R.color.nothingColor, R.drawable.ic_lamp_nothing);
            }

            // OFFLINE MODE
            else if (mStatus == messageEnum.SOCKET_TIMEOUT
                    || mStatus == messageEnum.IO_EXCEPTION
                    || mStatus == messageEnum.JSON_EXCEPTION
                    ) {

                // set offline mode info
                setModeBar(getString(R.string.offline), R.color.offlineColor, R.drawable.ic_lamp_offline);

                Log.d("AAAAA", "OFF-LINE MODE");

             ///   weatherCursorAdapter = new WeatherCursorAdapter(getContext(), null, 0);

                if (getActivity().findViewById(R.id.onePaneLayout) != null) { // phone
                    weatherCursorAdapter = new WeatherCursorAdapter(getContext(), null,0,
                            R.layout.list_item_weather // phone layout
                    );
                }
                else { // tablet
                    weatherCursorAdapter = new WeatherCursorAdapter(getContext(), null,0,
                            R.layout.list_item_weather_tablet // tablet layout
                    );
                }

                weatherListView.setAdapter(weatherCursorAdapter);

                // --------------------------------------------------------------
                // IMPORTANT !!! Change loader for different query
                Loader loader = getLoaderManager().getLoader(LOADER_WEATHER_ID);

                if (loader != null && !loader.isReset()) {
                    getLoaderManager().restartLoader(LOADER_WEATHER_ID, null, DetailsFragment.this);
                } else {
                    getLoaderManager().initLoader(LOADER_WEATHER_ID, null, DetailsFragment.this);
                }
                // --------------------------------------------------------------
            }
        }
    }*/


