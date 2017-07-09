package com.example.arsone.weather;

import android.app.IntentService;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
import java.util.ArrayList;
import java.util.List;

// http://guides.codepath.com/android/Starting-Background-Services#communicating-with-a-broadcastreceiver

// https://github.com/loopj/android-async-http/blob/master/sample/src/main/java/com/loopj/android/http/sample/services/ExampleIntentService.java

// https://github.com/loopj/android-async-http/blob/master/sample/src/main/java/com/loopj/android/http/sample/IntentServiceSample.java


public class GetDataService extends IntentService {


    class City {

        int id;
        String city;

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
    protected void onHandleIntent(@Nullable Intent intent) {

        // Do the task here
        //// Log.i("AAAAA", "Service running");

        // int time = intent.getIntExtra(MainActivity.PARAM_TIME, 1);

        // get task
        int mTask = intent.getIntExtra(MainActivity.PARAM_TASK, 0);

        if (mTask == MainActivity.TASK_GET_WEATHER_ONE_CITY) {

            String city = intent.getStringExtra(MainActivity.PARAM_ENTERED_CITY);
            int id = intent.getIntExtra(MainActivity.PARAM_CITY_ID, -1);

            if (city != null && id != -1) {

                Log.d("AAAAA", "Service running for ONE CITY.");

                Intent i = new Intent(MainActivity.BROADCAST_ACTION);

                // inform about task starting
                i.putExtra(MainActivity.PARAM_TASK, mTask);
                i.putExtra(MainActivity.PARAM_STATUS, MainActivity.STATUS_GET_WEATHER_ONE_CITY_START);
                sendBroadcast(i);

                getWeather(id, city);

                // Inform about task finish
                Intent newIntent = new Intent(MainActivity.BROADCAST_ACTION);
                i.putExtra(MainActivity.PARAM_TASK, mTask);
                newIntent.putExtra(MainActivity.PARAM_STATUS, MainActivity.STATUS_GET_WEATHER_ONE_CITY_FINISH);
                sendBroadcast(newIntent);
            }
        } else if (mTask == MainActivity.TASK_GET_WEATHER_ALL_CITIES) {

            Log.d("AAAAA", "Service running for all cities");

            Intent i = new Intent(MainActivity.BROADCAST_ACTION);

            // inform about task starting
            i.putExtra(MainActivity.PARAM_TASK, mTask);
            i.putExtra(MainActivity.PARAM_STATUS, MainActivity.STATUS_GET_WEATHER_ALL_CITIES_START);
            sendBroadcast(i);

/*

        String city = intent.getStringExtra(MainActivity.SERVICE_PARAM_ENTERED_CITY);
        int id = intent.getIntExtra(MainActivity.SERVICE_PARAM_CITY_ID, -1);

        if(city != null && id != -1){
            Log.i("AAAAA", "Service running for ONE CITY.");

            getWeather(id, city);
*/
/*        }
        else {*/
            //          Log.i("AAAAA", "Service running for all cities...");

            ContentResolver contentResolver = getContentResolver();

            Cursor cursor = contentResolver.query(DataContentProvider.CITY_CONTENT_URI, // @NonNull Uri uri,
                    null,// @Nullable String[] projection,
                    null,//@Nullable String selection,
                    null,//@Nullable String[] selectionArgs,
                    null//@Nullable String sortOrder)
            );

            if (cursor.getCount() == 0) {
                Log.d("AAAAA", "cursor.getCount() = 0");
                return;
            }

            cursor.moveToFirst();

            List<City> citiesList = new ArrayList<City>();

            while (!cursor.isAfterLast()) {

                //   Log.d("AAAAA", "CITY = " + cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)));

                City newCity = new City(cursor.getInt(cursor.getColumnIndex(DataContract.CityEntry._ID)),
                        cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY))
                );

                citiesList.add(newCity);

                cursor.moveToNext();
            }


            for (City c : citiesList) {

                getWeather(c.id, c.city);
            }

            // Inform about task finish
            Intent newIntent = new Intent(MainActivity.BROADCAST_ACTION);
            i.putExtra(MainActivity.PARAM_TASK, mTask);
            newIntent.putExtra(MainActivity.PARAM_STATUS, MainActivity.STATUS_GET_WEATHER_ALL_CITIES_FINISH);
            sendBroadcast(newIntent);
        }
    }


    private void getWeather(final int id, final String city) {

   //     final Intent intent = new Intent(MainActivity.BROADCAST_ACTION);

        String url;

        try {
            url = getString(R.string.web_service_url)
                    + URLEncoder.encode(city, "UTF-8")
                    /// + "&units=metric&lang=ru&cnt=5&APPID="
                    + "&units=metric" // default units = metric
                    /// + "&lang=ru"  // default language = English
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

                        String returnedCityName = "";
                        long cityID = -1;
                        String countryCode = "";
                        double cityLongitude;
                        double cityLatitude;

                        List<Weather> weatherList = new ArrayList<Weather>();
                        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
                        try {

                            JSONArray list = null;

                            Log.d("AAAAA", "city: " + city + " responce = " + response);

                            // set mStatus info
                            ///     setModeBar(getResources().getText(R.string.online).toString(), R.color.onlineColor, R.drawable.ic_lamp_online);

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

    /*                            mRefreshDataTask = new DetailsFragment.RefreshDataTask();
                                mRefreshDataTask.execute(cv);*/


                            // ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

                            //     String citySelection = DataContract.CityEntry._ID + "=?";
                            //  String[] selectionArgs = {String.valueOf(id)};

                            //   String weatherSelection = DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?";

                            Uri updateUri = Uri.parse(DataContentProvider.CITY_CONTENT_URI.toString() + "/" + id);

                            // update city data
                            /// ops.add(ContentProviderOperation.newUpdate(DataContentProvider.CITY_CONTENT_URI)
                            ops.add(ContentProviderOperation.newUpdate(updateUri)
                                    /// .withSelection(citySelection, selectionArgs)
                                    .withValues(cv)
                                    .build());

                            // ic_delete_item old data in from weather table

                            Uri deleteUri = Uri.parse(DataContentProvider.WEATHER_CONTENT_URI.toString() + "/" + id);

                            /// ops.add(ContentProviderOperation.newDelete(DataContentProvider.WEATHER_CONTENT_URI)
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

                            /// ContentProviderResult[] cpResults =
                            getContentResolver().applyBatch(DataContentProvider.AUTHORITY, ops);


/*                            Intent i = new Intent(MainActivity.BROADCAST_ACTION);

                            // Inform about task finish
                            i.putExtra(MainActivity.PARAM_STATUS, MainActivity.STATUS_FINISH);
                            sendBroadcast(i);*/

//        ContentProviderResult[] cpResults = getActivity().getContentResolver()
                            //            .applyBatch(DataContentProvider.AUTHORITY, ops);

                            //              if (cpResults != null) {

                            //                  Log.e("AAAAA", "cpResults[1].count = " + cpResults[1].count);
                            //Log.e("AAAAA","cpResults = " + cpResults.);
                            //Log.e("AAAAA","cpResults = " + cpResults.toString());

                            //Toast.makeText(getContext(), getString(R.string.rows_deleted)
//                            + cpResults[1].count, Toast.LENGTH_SHORT).show();

//                }
                            //     }

/*                            catch (RemoteException e) {
                                Log.e("AAAAA", "RemoteException " + e.getMessage());
                            } catch (OperationApplicationException e) {
                                Log.e("AAAAA", "OperationApplicationException: " + e.getMessage());
                                ops.clear();
                            }*/


                        }
/*                        catch (RemoteException e) {
                            Log.e("AAAAA", "RemoteException " + e.getMessage());
                        } catch (OperationApplicationException e) {
                            Log.e("AAAAA", "OperationApplicationException: " + e.getMessage());
                            ops.clear();
                        }
                        */
/*                        catch (JSONException e) {
                            Log.d("AAAAA", "JSONException: " + e.getMessage());
                        } */ catch (Exception e) {
                            Log.d("AAAAA", "Exception: " + e.getMessage());
                        } finally {
                            //   hideMessageBar();
                            ops.clear();
                        }
                    }
                }, new Response.ErrorListener() {

            // ERROR MODES
            @Override
            public void onErrorResponse(VolleyError error) {

                //        hideMessageBar();

                NetworkResponse response = error.networkResponse;

                if (response != null && response.data != null) {

                    if (response.statusCode == 404) {

                        Log.d("AAAA", "onErrorResponse: 404");

                        // set mStatus info
/*                            setModeBar(getString(R.string.message_city_not_found),
                                    R.color.nothingColor, R.drawable.ic_lamp_nothing);*/

                        ///         return;
                    }
                }

                // OFFLINE MODE: show database data

                   /* // set offline mode info
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
                    // --------------------------------------------------------------*/
            }
        });

/*        if (url == null)
            return;*/

        // Defining the Volley request queue that handles the URL request concurrently
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonObjectRequest);

        // Delay for OpenWeatherMap.org limitation (1 query per second)
        SystemClock.sleep(1000); // 1 second sleep
    }


}
