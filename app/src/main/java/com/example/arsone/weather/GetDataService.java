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
import java.util.Calendar;
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
//    public final static String PARAM_SEND_NOTIFICATIONS = "notifications";

    private final static int TASK_GET_WEATHER_ONE_CITY = 1;
    private final static int TASK_GET_WEATHER_ALL_CITIES = 2;
    private final static int TASK_GET_DATA_PERIODICALLY = 3;

    private final static int STATUS_GET_WEATHER_ONE_CITY_START = 101;
    private final static int STATUS_GET_WEATHER_ONE_CITY_FINISH = 102;

    private final static int STATUS_GET_WEATHER_ALL_CITIES_START = 103;
    private final static int STATUS_GET_WEATHER_ALL_CITIES_FINISH = 104;

    private final String BROADCAST_DYNAMIC_ACTION = "com.example.arsone.weather.dynamic.broadcast";

    private final int TIME_TO_WAIT = 1000; // time to wait in milliseconds

    private int mTask;
    private int mSendNotificationCityID;
/*    private int mUnitsFormat;
    private int mCityID;*/


 //   private boolean cityFound;


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

                //     Log.d("AAAAA", "GetDataService: No data in cities table");
                cursorCities.close();
                return;
            }
            cursorCities.close();
        } else
            return;

        // ----------------------------------------------------------------
        // read settings
        Cursor cursor = getContentResolver().query(DataContentProvider.SETTINGS_CONTENT_URI,
                new String[]{DataContract.SettingsEntry.COLUMN_NOTIFY_CITY_ID,
                        DataContract.SettingsEntry.COLUMN_UNITS_FORMAT},
                null, null, null);

        if (cursor != null && cursor.getCount() > 0) {

            cursor.moveToFirst();

            mSendNotificationCityID = cursor.getInt(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_NOTIFY_CITY_ID));

            Log.d("AAAAA", "cursor - mSendNotificationCityID = " + mSendNotificationCityID);

            // units format: 0 = metric, 1 = imperial
        //    mUnitsFormat = cursor.getInt(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_UNITS_FORMAT));

            // cursor.close();
        }

        cursor.close();
        // ----------------------------------------------------------------

        // get task
        mTask = intent.getIntExtra(PARAM_TASK, 0);

///        Log.d("AAAAA", "GetDataService: mSendNotificationCityID = " + mSendNotificationCityID);

        if (mTask == TASK_GET_WEATHER_ONE_CITY) {

            //  String action = intent.getAction();
            //   Log.d("AAAAA", "GetDataService: mTask = TASK_GET_WEATHER_ONE_CITY");

            int id = intent.getIntExtra(PARAM_CITY_ID, -1);

     //       mCityID = id;

            String city = intent.getStringExtra(PARAM_ENTERED_CITY);

            //  Log.d("AAAAA", "GetDataService: city = " + city);

            if (city != null && id != -1) {

                //       Log.d("AAAAA", "GetDataService: Service running for ONE CITY.");

                sendBroadcastData(STATUS_GET_WEATHER_ONE_CITY_START);

                getWeather(id, city);

                sendBroadcastData(STATUS_GET_WEATHER_ONE_CITY_FINISH);
            }
        } else if (mTask == TASK_GET_WEATHER_ALL_CITIES) {

            //     Log.d("AAAAA", "GetDataService: TASK_GET_WEATHER_ALL_CITIES");

            sendBroadcastData(STATUS_GET_WEATHER_ALL_CITIES_START);

            getAllCities();

            sendBroadcastData(STATUS_GET_WEATHER_ALL_CITIES_FINISH);

        } else if (mTask == TASK_GET_DATA_PERIODICALLY) {

            //      Log.d("AAAAA", "GetDataService: TASK_GET_DATA_PERIODICALLY");

            sendBroadcastData(STATUS_GET_WEATHER_ALL_CITIES_START);

            getAllCities();

            sendBroadcastData(STATUS_GET_WEATHER_ALL_CITIES_FINISH);

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

            //     Log.d("AAAAA" , "GetDataService: locale = " + locale);

            url = getString(R.string.web_service_url)
                    + URLEncoder.encode(city, "UTF-8")
                    /// + "&units=metric&lang=ru&cnt=5&APPID="
                    + "&units=metric" // default units = metric
                    /// + "&lang=ru"  // default language = English
                    + "&lang=" + locale // languageCodeArray[languageIndex]
                    + "&cnt=5"        // default for 5 days
                    + "&APPID=" + getString(R.string.openweathermap_api_key);

            //  Log.d("AAAAA", "getWeather: url = " + url);

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

/*                        // selected city for notification
                        String cityWeatherIcon = null;
                        cityFound = false;
                        String notifyText = null;

                        double dayTemp;
                        double minTemp;
                        double maxTemp;
                        double morningTemp;
                        double eveningTemp;
                        double nightTemp;
                        int humidity;
                        double pressure_hPa;
                        double windSpeed;
                        int windDirection;*/

                        // current date
                        ///SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                        String currentDate = formatter.format(new Date());
                        // Log.d("AAAAA", "currentDate = " + currentDate);
                        Calendar calendar = Calendar.getInstance();

                        List<Weather> weatherList = new ArrayList<>();
                        ArrayList<ContentProviderOperation> ops = new ArrayList<>();

                        try {
                            JSONArray list;

                            //       Log.d("AAAAA", "city: " + city + " response = " + response);

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

/*                                // convert milliseconds to date
                                calendar.setTimeInMillis(day.getLong("dt") * 1000L);
                                String date = formatter.format(calendar.getTime());

                                if (id == mSendNotificationCityID && date.equals(currentDate)) {

                                    notifyText = city + ": " + weather.getString("description") + "\n";

                                    cityWeatherIcon = weather.getString("icon");
                                    dayTemp = temperatures.getDouble("day");
                                    minTemp = temperatures.getDouble("min");
                                    maxTemp = temperatures.getDouble("max");
                                    morningTemp = temperatures.getDouble("morn");
                                    eveningTemp = temperatures.getDouble("eve");
                                    nightTemp = temperatures.getDouble("night");
                                    humidity = day.getInt("humidity");
                                    pressure_hPa = day.getDouble("pressure");
                                    windSpeed = day.getDouble("speed");
                                    windDirection = day.getInt("deg");

                                    if (mUnitsFormat == 0) { // metric = Celsius

                                        notifyText += (getString(R.string.m_weather_day_temp,
                                                String.valueOf((int) dayTemp)) + "\n"
                                                + getString(R.string.m_weather_min_max_temp,
                                                String.valueOf((int) minTemp), String.valueOf((int) maxTemp)) + "\n"
                                                + getString(R.string.m_weather_morning_temp,
                                                String.valueOf((int) morningTemp)) + "\n"
                                                + getString(R.string.m_weather_evening_temp,
                                                String.valueOf((int) eveningTemp)) + "\n"
                                                + getString(R.string.m_weather_night_temp,
                                                String.valueOf((int) nightTemp)) + "\n"
                                                + getString(R.string.m_weather_wind_speed, windSpeed) + "\n");

                                    } else if (mUnitsFormat == 1) { // imperial == Fahrenheit

                                        notifyText += (getString(R.string.i_weather_day_temp,
                                                String.valueOf(CelsiusToFahrenheit(dayTemp)) + "\n"
                                                        + getString(R.string.i_weather_min_max_temp,
                                                        String.valueOf(CelsiusToFahrenheit(minTemp)),
                                                        String.valueOf(CelsiusToFahrenheit(maxTemp))) + "\n"
                                                        + getString(R.string.i_weather_morning_temp,
                                                        String.valueOf(CelsiusToFahrenheit(morningTemp))) + "\n"
                                                        + getString(R.string.i_weather_evening_temp,
                                                        String.valueOf(CelsiusToFahrenheit(eveningTemp))) + "\n"
                                                        + getString(R.string.i_weather_night_temp,
                                                        String.valueOf(CelsiusToFahrenheit(nightTemp))) + "\n"
                                                        + getString(R.string.i_weather_wind_speed, windSpeed * 2.236936) + "\n"));

                                    }

                                    notifyText += (getString(R.string.weather_wind_direction, String.valueOf(windDirection)) + "\n");

                                    if (humidity != 0) {
                                        notifyText += (getString(R.string.weather_humidity, NumberFormat
                                                .getPercentInstance().format(humidity / 100.0)) + "\n");
                                    } else {
                                        notifyText += (getString(R.string.weather_humidity, getString(R.string.no_data)) + "\n");
                                    }

                                    notifyText += (getString(R.string.weather_pressure,
                                            pressure_hPa, pressure_hPa * 0.750063755419211) + "\n"); // hPa & Millimeter of mercury/ "mmHg"

           *//*                         Log.d("AAAAA", "city = " + city);
                                    Log.d("AAAAA", "id = " + id);
                                    Log.d("AAAAA", "mSendNotificationCityID = " + mSendNotificationCityID);
                                    Log.d("AAAAA", "date = " + date);
                                    Log.d("AAAAA", "notifyText = " + notifyText);
                                    Log.d("AAAAA", "cityWeatherIcon = " + cityWeatherIcon);*//*

                                    cityFound = true;
                                }*/
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

/*                            if (cityFound) {
                                sendNotification(notifyText, cityWeatherIcon);
                            }*/

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

/*                /// if (mSendNotificationCityID != 0) {
                if (cityFound) {
                    sendNotification(getString(R.string.service_get_data_error), null); //  + "\n" + getCurrentTime());
                }*/

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

        // Delay for OpenWeatherMap.org limitation (1 query per second)
        /// SystemClock.sleep(1000); // 1 second sleep
    }


    private String CelsiusToFahrenheit(double temp) {

        return String.valueOf((int) (temp * 9 / 5 + 32));
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

/*
    private String getCurrentTime() {

        SimpleDateFormat currentTimeDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String currentDateAndTime = currentTimeDate.format(new Date());

        String stringDate = null;

        //      Log.d("AAAAA", "currentDateAndTime = " + currentDateAndTime);

        try {
            // Date today = new Date();
            Date date = currentTimeDate.parse(currentDateAndTime);

            stringDate = DateFormat.getDateTimeInstance().format(date);

            //   Log.d("AAAAA", "sendNotification: stringDate = " + stringDate);

            return stringDate;

        } catch (ParseException e) {
            Log.d("AAAAA", "Date ParseException: " + e.getMessage());
        }

        return null;
    }*/

/*
    private void sendNotification(String msg, String icon) {

        Log.d("AAAAA", "msg = " + msg);

        // NotificationManager class to notify the user of events
        // that happen. This is how you tell the user that something
        // has   happened in the background.
        NotificationManager alarmNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), 0);

        // set icon, title and message for notification
        NotificationCompat.Builder alamNotificationBuilder = new NotificationCompat.Builder(this)
                //  .setDefaults(NotificationCompat.DEFAULT_ALL)
                //  .setWhen(System.currentTimeMillis())
                .setContentTitle("Weather Info")
             ///   .setSmallIcon(R.drawable.ic_sunny)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), getResources().getIdentifier("_" + icon, "drawable", getPackageName())))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))
                .setAutoCancel(true) // clear notification when clicked
                .setContentText(msg);

        alamNotificationBuilder.setContentIntent(contentIntent);

        alarmNotificationManager.notify(1, alamNotificationBuilder.build());
    }*/
}