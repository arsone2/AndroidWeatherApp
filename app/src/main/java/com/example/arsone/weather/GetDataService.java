package com.example.arsone.weather;

import android.annotation.TargetApi;
import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.arsone.weather.data.Weather;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// http://guides.codepath.com/android/Starting-Background-Services#communicating-with-a-broadcastreceiver
// https://github.com/loopj/android-async-http/blob/master/sample/src/main/java/com/loopj/android/http/sample/services/ExampleIntentService.java
// https://github.com/loopj/android-async-http/blob/master/sample/src/main/java/com/loopj/android/http/sample/IntentServiceSample.java


public class GetDataService extends IntentService {

    private final static String PARAM_TASK = "task";
    private final static String PARAM_STATUS = "status";
    private final static String PARAM_CITY_ID = "city_id";
    private final static String PARAM_ENTERED_CITY = "city_name";

    private final static int TASK_GET_WEATHER_ONE_CITY = 1;
    private final static int TASK_GET_WEATHER_ALL_CITIES = 2;
    private final static int TASK_GET_DATA_PERIODICALLY = 3;

    public final static int STATUS_GET_WEATHER_ONE_CITY_START = 101;
    public final static int STATUS_GET_WEATHER_ONE_CITY_FINISH_SUCCESS = 102;
    public final static int STATUS_GET_WEATHER_ONE_CITY_FINISH_FAIL = 103;

    public final static int STATUS_GET_WEATHER_ALL_CITIES_START = 104;
    public final static int STATUS_GET_WEATHER_ALL_CITIES_FINISH_SUCCESS = 105;
    public final static int STATUS_GET_WEATHER_ALL_CITIES_FINISH_FAIL = 106;

    private final String BROADCAST_DYNAMIC_ACTION = "com.example.arsone.weather.dynamic.broadcast";

    private final int TIME_TO_WAIT = 1000; // time to wait in milliseconds

    private int mTask;
    private int mCitiesTotal;

    private int mCitiesCounter = 1;


    class City {

        public int id;
        public String city;

        public City(int id, String city) {

            this.id = id;
            this.city = city;
        }
    }


    // constructor
    public GetDataService() {

        super("GetDataService");
    }


    @Override
    protected synchronized void onHandleIntent(@Nullable Intent intent) {

        //      Log.i("AAAAA", "Service running");

        if (intent == null)
            return;

        // ----------------------------------------------------------------
        // check if database has cities
        Cursor cursorCities = getContentResolver().query(DataContentProvider.CITY_CONTENT_URI,
                new String[]{DataContract.CityEntry._ID},
                null, null, null);

        if (cursorCities != null) {

            if (cursorCities.getCount() == 0) {

                cursorCities.close();
                return;
            } else {

                mCitiesTotal = cursorCities.getCount();
            }
            cursorCities.close();
        } else
            return;

        // get task
        mTask = intent.getIntExtra(PARAM_TASK, 0);

        if (mTask == TASK_GET_WEATHER_ONE_CITY) {

            int id = intent.getIntExtra(PARAM_CITY_ID, -1);

            String city = intent.getStringExtra(PARAM_ENTERED_CITY);

            if (city != null && id != -1) {

                sendBroadcastData(STATUS_GET_WEATHER_ONE_CITY_START);

                getWeather(id, city);
            }
        } else if (mTask == TASK_GET_WEATHER_ALL_CITIES) {

            sendBroadcastData(STATUS_GET_WEATHER_ALL_CITIES_START);

            getAllCities();

        } else if (mTask == TASK_GET_DATA_PERIODICALLY) {

            sendBroadcastData(STATUS_GET_WEATHER_ALL_CITIES_START);

            getAllCities();

        } else {
            Log.d("AAAAA", "GetDataService: CATCH OTHER TASK FOR TEST !!!");
        }
    }


    // Inform about task status
    private void sendBroadcastData(int status) {

        SystemClock.sleep(TIME_TO_WAIT); // wait to update data in database!!

        Intent finishIntent = new Intent()
                .putExtra(PARAM_TASK, mTask)
                .putExtra(PARAM_STATUS, status)
                .setAction(BROADCAST_DYNAMIC_ACTION);

        sendBroadcast(finishIntent);
    }


    private void getAllCities() {

        ContentResolver contentResolver = getContentResolver();

        // read cities list from DB
        Cursor cursor = contentResolver.query(DataContentProvider.CITY_CONTENT_URI, // @NonNull Uri uri,
                null,// @Nullable String[] projection,
                null,//@Nullable String selection,
                null,//@Nullable String[] selectionArgs,
                null//@Nullable String sortOrder)
        );

        if (cursor == null)
            return;

        if (cursor.getCount() == 0) {
            cursor.close();
            return;
        }

        cursor.moveToFirst();

        List<City> citiesList = new ArrayList<>();

        while (!cursor.isAfterLast()) {

            City newCity = new City(cursor.getInt(cursor.getColumnIndex(DataContract.CityEntry._ID)),
                    cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY))
            );

            citiesList.add(newCity);

            cursor.moveToNext();
        }

        cursor.close();

        for (City c : citiesList) {

            getWeather(c.id, c.city);
        }
    }


    private void getWeather(final int id, final String city) {

        String url;

        try {
            String locale = getCurrentLocale().getCountry().toLowerCase();

            url = getString(R.string.web_service_url)
                    + URLEncoder.encode(city, "UTF-8")
                    /// + "&units=metric&lang=ru&cnt=5&APPID="
                    + "&units=metric" // default units = metric
                    /// + "&lang=ru"  // default language = English
                    + "&lang=" + locale // languageCodeArray[languageIndex]
                    + "&cnt=5"        // default for 5 days
                    + "&APPID=" + getString(R.string.openweathermap_api_key);

        } catch (UnsupportedEncodingException e) {
            Log.d("AAAAA", "UnsupportedEncodingException:" + e.getMessage());
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(url,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        String returnedCityName;
                        long cityID;
                        String countryCode;
                        double cityLongitude;
                        double cityLatitude;

                        List<Weather> weatherList = new ArrayList<>();
                        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

                        try {
                            JSONArray list;

                            list = response.getJSONArray("list");

                            JSONObject jsonCity = response.getJSONObject("city");

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

                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            String currentDateTime = dateFormat.format(new Date()); // Find todays date

                            cv.put(DataContract.CityEntry.COLUMN_UPDATE_TIMESTAMP, currentDateTime);

                            Uri updateUri = Uri.parse(DataContentProvider.CITY_CONTENT_URI.toString() + "/" + id);

                            // update city data
                            ops.add(ContentProviderOperation.newUpdate(updateUri)
                                    /// .withSelection(citySelection, selectionArgs)
                                    .withValues(cv)
                                    .build());

                            // ic_delete_item old data in from weather table
                            Uri deleteUri = Uri.parse(DataContentProvider.WEATHER_CONTENT_URI.toString() + "/" + id);

                            ops.add(ContentProviderOperation.newDelete(deleteUri)
                                    // .withSelection(weatherSelection, selectionArgs)
                                    .build());

                            // insert new data in weather table
                            for (int j = 0; j < weatherList.size(); j++) {

                                ops.add(ContentProviderOperation.newInsert(DataContentProvider.WEATHER_CONTENT_URI)
                                        .withValue(DataContract.WeatherEntry.COLUMN_CITY_ID_FK, id)                              // "city_id" - foreign key (int/INTEGER)
                                        .withValue(DataContract.WeatherEntry.COLUMN_TIMESTAMP, weatherList.get(j).timeStamp) // day.getLong("dt"))                 // timestamp in seconds (int/INTEGER)
                                        .withValue(DataContract.WeatherEntry.COLUMN_MORNING_TEMP, weatherList.get(j).morningTemp) // temperatures.getDouble("morn"))          // "temp_day"
                                        .withValue(DataContract.WeatherEntry.COLUMN_DAY_TEMP, weatherList.get(j).dayTemp) // temperatures.getDouble("day")) - "temp_day"
                                        .withValue(DataContract.WeatherEntry.COLUMN_EVENING_TEMP, weatherList.get(j).eveningTemp) // temperatures.getDouble("eve")) -"temp_day"
                                        .withValue(DataContract.WeatherEntry.COLUMN_NIGHT_TEMP, weatherList.get(j).nightTemp) // temperatures.getDouble("night"))  - "temp_night"
                                        .withValue(DataContract.WeatherEntry.COLUMN_MIN_TEMP, weatherList.get(j).minTemp) // temperatures.getDouble("min"))      - "temp_min"
                                        .withValue(DataContract.WeatherEntry.COLUMN_MAX_TEMP, weatherList.get(j).maxTemp) //  temperatures.getDouble("max"))      // "temp_max"
                                        .withValue(DataContract.WeatherEntry.COLUMN_HUMIDITY, weatherList.get(j).humidity) // day.getInt("humidity"))          // "humidity"
                                        .withValue(DataContract.WeatherEntry.COLUMN_PRESSURE, weatherList.get(j).pressure) // day.getDouble("pressure"))          // "pressure"
                                        .withValue(DataContract.WeatherEntry.COLUMN_SPEED, weatherList.get(j).windSpeed) // day.getDouble("speed"))          // "windSpeed"
                                        .withValue(DataContract.WeatherEntry.COLUMN_DIRECTION, weatherList.get(j).windDirection) // day.getInt("deg"))          // "windDirection"
                                        .withValue(DataContract.WeatherEntry.COLUMN_DESCRIPTION, weatherList.get(j).description) // weather.getString("description"))// "description"
                                        .withValue(DataContract.WeatherEntry.COLUMN_ICON_NAME, weatherList.get(j).iconName) // weather.getString("icon"))         // "icon_name"
                                        .build());
                            }
                            getContentResolver().applyBatch(DataContentProvider.AUTHORITY, ops);

                       //     Log.d("AAAAA", "END: mCitiesCounter = " + mCitiesCounter);
                       //     Log.d("AAAAA", "END: mCitiesTotal = " + mCitiesTotal);

                            mCitiesCounter++;

                            if (mTask == TASK_GET_WEATHER_ONE_CITY) {

                                sendBroadcastData(STATUS_GET_WEATHER_ONE_CITY_FINISH_SUCCESS);
                            } else {

                                if (mCitiesCounter >= mCitiesTotal) {

                                    sendBroadcastData(STATUS_GET_WEATHER_ALL_CITIES_FINISH_SUCCESS);
                                }
                            }
                        } catch (Exception e) {
                            Log.d("AAAAA", "Exception: " + e.getMessage());
                        } finally {
                            ops.clear();
                        }
                    }
                }, new Response.ErrorListener() {

            // ERROR MODES
            @Override
            public void onErrorResponse(VolleyError error) {

                mCitiesCounter++;

                if (mTask == TASK_GET_WEATHER_ONE_CITY) {

                    sendBroadcastData(STATUS_GET_WEATHER_ONE_CITY_FINISH_FAIL);

                } else {

                    if (mCitiesCounter >= mCitiesTotal) {

                        sendBroadcastData(STATUS_GET_WEATHER_ALL_CITIES_FINISH_FAIL);
                    }
                }

                NetworkResponse response = error.networkResponse;

                if (response != null && response.data != null) {

                    if (response.statusCode == 404) {

                        Log.d("AAAA", "onErrorResponse: 404");
                    }
                }
            }
        });

        // Defining the Volley request queue that handles the URL request concurrently
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Add a request (in this example, called stringRequest) to your RequestQueue.
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
}