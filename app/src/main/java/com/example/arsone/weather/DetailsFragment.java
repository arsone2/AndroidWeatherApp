package com.example.arsone.weather;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.arsone.weather.data.City;
import com.example.arsone.weather.data.Weather;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;


// openweathermap icons list: https://openweathermap.org/weather-conditions

public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


/*    public interface Callbacks {

   //     public void onDetailsUpdated();

    }*/

    // Fragment`s host activity
 ///   private static DetailsFragment.Callbacks activity;

    // Fragment view title
    private TextView titleTextView;

    // mode panel
    private LinearLayout modePanelLayout;
    private TextView modeTextView;
    private AppCompatImageView modeImageView;

    // message panel
    private LinearLayout messageBarLayout;
    private TextView messageTextView;
    private ProgressBar messageProgressBar;

    // weather ListView
    private ListView weatherListView;

    // weather objects List
    private List<Weather> weatherList = new ArrayList<>();

    // ArrayAdapter between  weather List &&  weather ListView
    private WeatherArrayAdapter weatherArrayAdapter;

    private WeatherCursorAdapter weatherCursorAdapter;

    private GetWeatherTask mGetWeatherTask;

    // selected city database table _id
    private int mID;

    // selected city name
    private String mEnteredCity;

    private enum messageEnum {
        OK,
        IO_EXCEPTION,
        CITY_NOT_FOUND,
        SOCKET_TIMEOUT,
        JSON_EXCEPTION
    }

    // current weather data operations status
    private messageEnum mStatus = messageEnum.OK;

    // offline data is found?
  //  private boolean mOfflineDataFound;

    //A ProgressDialog object
    /// private ProgressDialog progressDialog;

    // Loader ID
    public static final int LOADER_WEATHER_ID = 1;
 //   private static final String LOADER_BUNDLE_CITY_ID = "CITY_ID";


    // ---------------------------------------------------------------
    // LoaderManager.LoaderCallbacks<Cursor> methods:

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] selectionArgs = new String[]{String.valueOf(mID)};

        Log.d("AAAAA", "onCreateLoader: selectionArgs[0] = " + selectionArgs[0]);

        return new CursorLoader(getContext(),
                DataContentProvider.WEATHER_CONTENT_ID_URI,
                null,
                DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?", // selection: city ID
                selectionArgs, // selectionArgs
                DataContract.WeatherEntry._ID // sort order
        );
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        weatherCursorAdapter.swapCursor(cursor);

        Log.d("AAAAA", "onLoadFinished: results count = " + cursor.getCount());

        if (cursor.getCount() == 0) {

            showMessageBar("Данные не найдены", false);

       //     mOfflineDataFound = false;
            ///messageTextView.setText("Оффлайн-данных не найдено");
        }
        else {
    //        mOfflineDataFound = true;
        }
/*        // No offline data found
        if(mOfflineDataFound == false){

            showMessageBar("Данные не найдены", false);
        }*/
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        weatherCursorAdapter.swapCursor(null);

        //   Log.d("AAAAA", "onLoaderReset");
    }
    // ---------------------------------------------------------------


    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
   ///     activity = (DetailsFragment.Callbacks) context;
    }


    @Override
    public void onDetach() {

        super.onDetach();

   //     activity.onDetailsUpdated();

   ///     activity = null;
    }


    @Override
    public void onDestroy() {

        if(mGetWeatherTask != null){

            mGetWeatherTask.cancel(true);
        }

        super.onDestroy();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();

        if (bundle != null) {
            mID = bundle.getInt(City.CITY_ID);
            mEnteredCity = bundle.getString(City.ENTERED_CITY);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_details, container, false);

        // Title
        titleTextView = (TextView) view.findViewById(R.id.titleTextView);

        // mode panel
        modePanelLayout = (LinearLayout) view.findViewById(R.id.modePanelLayout);
        modeTextView = (TextView) view.findViewById(R.id.modeTextView);
        modeImageView = (AppCompatImageView) view.findViewById(R.id.modeImage);

        // message bar panel
        messageBarLayout = (LinearLayout) view.findViewById(R.id.messageBar);
        messageTextView = (TextView) view.findViewById(R.id.messageTextView);
        messageProgressBar = (ProgressBar) view.findViewById(R.id.messageProgressBar);

        // weather ListView
        weatherListView = (ListView) view.findViewById(R.id.detailsListView);

        // Title
        titleTextView.setText(mEnteredCity);

///        Log.d("AAAAA", "onCreateView: mID = " + mID);

/*        // set array adapter to get weather data from server
        weatherArrayAdapter = new WeatherArrayAdapter(getActivity(), weatherList);
        weatherListView.setAdapter(weatherArrayAdapter);

        // GET ONLINE DATA FROM WEATHER SERVER
        getWeather();*/

        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        // set array adapter to get weather data from server

        if (getActivity().findViewById(R.id.onePaneLayout) != null) { // phone
            weatherArrayAdapter = new WeatherArrayAdapter(getActivity(),
                                                          weatherList,
                                                          R.layout.list_item_weather // phone layout
                                                          );
        }
        else { // tablet
            weatherArrayAdapter = new WeatherArrayAdapter(getActivity(),
                                                          weatherList,
                                                          R.layout.list_item_weather_tablet // tablet layout
                                                          );
        }

        weatherListView.setAdapter(weatherArrayAdapter);

        // GET ONLINE DATA FROM WEATHER SERVER
        getWeather();



/*        // --------------------------------------------------------------
        // IMPORTANT !!! Change loader for different query
        Loader loader = getLoaderManager().getLoader(LOADER_WEATHER_ID);

        if (loader != null && !loader.isReset()) {
            getLoaderManager().restartLoader(LOADER_WEATHER_ID, null, this);
        } else {
            getLoaderManager().initLoader(LOADER_WEATHER_ID, null, this);
        }
        // --------------------------------------------------------------*/
    }


    private void showMessageBar(String text, boolean showProgressBar) {

        messageBarLayout.setVisibility(View.VISIBLE);
        messageTextView.setText(text);

        if(showProgressBar)
            messageProgressBar.setVisibility(View.VISIBLE);
        else
            messageProgressBar.setVisibility(View.GONE);
    }


    private void hideMessageBar() {

        messageBarLayout.setVisibility(View.GONE);
    }


    private void setModeBar(String text, int color, int image){

        modePanelLayout.setVisibility(View.VISIBLE);
        ///modeTextView.setVisibility(View.VISIBLE);
        modeTextView.setText(text);
        modeTextView.setTextColor(ContextCompat.getColor(getContext(), color));
        modeImageView.setImageResource(image);
    }


    private void hideModeBar(){

        modePanelLayout.setVisibility(View.GONE);
        ///modeTextView.setVisibility(View.GONE);
    }


    private void getWeather() {

        URL url = createURL(mEnteredCity);

        Log.d("AAAAA", "URL = " + url.toString());

        if (url != null) {

            mGetWeatherTask = new GetWeatherTask();
            mGetWeatherTask.execute(url);

            //new GetWeatherTask().execute(url);
        }
    }


    //  create URL for openweathermap.org
    private URL createURL(String city) {

        String apiKey = getString(R.string.api_key);

        String baseUrl = getString(R.string.web_service_url);

        try {
            // create URL for selected city
            String urlString = baseUrl + URLEncoder.encode(city, "UTF-8")
                    + "&units=metric&lang=ru&cnt=5&APPID="
                    + apiKey;


            return new URL(urlString);

        } catch (Exception e) {
            Log.d("AAAAA", "Exception: createURL() error.");
        }

        return null; // incorrect URL
    }


    private class GetWeatherTask extends AsyncTask<URL, Integer, JSONObject> {

        @Override
        protected void onPreExecute() {

            showMessageBar(getString(R.string.message_wait_for_server), true);

/*            progressDialog = ProgressDialog.show(getContext(), "Загрузка...",
                    "Получение данных с сервера, подождите...", false, false);*/
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

/*                            // This value will be 404 if the request was not successful
                            if(data.getInt("cod") != 200){
                                return null;
                            }*/

                            return data;

               //         }
/*                        catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("AAAAA", "JSONException");
                            mStatus = messageEnum.JSON_EXCEPTION;
                        }*/
/*                        catch (IOException e) { // no connection to server
                            e.printStackTrace();
                            Log.d("AAAAA", "Exception: reader error.");
                            mStatus = messageEnum.IO_EXCEPTION;
                        }*/
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

                        // delete all previous weather data in database
                        String selection = DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?";

                        String[] selectionArgs = {String.valueOf(mID)};

                        int rowsDeleted = getActivity().getContentResolver()
                                .delete(DataContentProvider.WEATHER_CONTENT_URI, selection, selectionArgs);

                        Log.d("AAAAA", "delete all previous weather data: rowsDeleted = " + rowsDeleted);

                        // update city data
                        Uri uri = ContentUris.withAppendedId(DataContentProvider.CITY_CONTENT_ID_URI, mID);

                        ContentValues values = new ContentValues();

                        values.put(DataContract.CityEntry.COLUMN_RETURNED_CITY, returnedCityName);
                        values.put(DataContract.CityEntry.COLUMN_CITY_ID, cityID);
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

/*                                day.getLong("dt"), // Временная метка даты/времени
                                temperatures.getDouble("day"), // add Day temperature
                                temperatures.getDouble("night"), // add Night temperature
                                temperatures.getDouble("min"), // Мин. температура
                                temperatures.getDouble("max"), // Макс. температура
                                day.getDouble("humidity"), // Процент влажности
                                weather.getString("description"), // Погодные условия
                                weather.getString("icon"),  // Имя значка
                                weather.getInt("id") // id описания погоды*/
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

/*                    if(activity != null)
                        activity.onDetailsUpdated();*/

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
    }
}