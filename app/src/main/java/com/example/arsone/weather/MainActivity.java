package com.example.arsone.weather;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.mapbox.mapboxsdk.Mapbox;

// https://www.thorntech.com/2016/03/parsing-json-android-using-volley-library/
// https://www.smashingmagazine.com/2017/03/simplify-android-networking-volley-http-library/
// http://www.grokkingandroid.com/better-performance-with-contentprovideroperation/
// https://stackoverflow.com/questions/4655291/what-are-the-semantics-of-withvaluebackreference
// https://www.codota.com/android/methods/android.content.ContentProvider/applyBatch
// http://www.java2s.com/Open-Source/Android_Free_Code/PhoneGap/JQuery/org_apache_cordova_contactsContactAccessorSdk5_java.htm
// http://www.programcreek.com/java-api-examples/index.php?api=android.content.ContentProviderResult
// openweathermap icons list: https://openweathermap.org/weather-conditions
// https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext
// https://stackoverflow.com/questions/35496493/getmapasync-in-fragment
// https://developers.google.com/android/guides/setup
// http://www.mobilab.ru/androiddev/androidalarmmanagertutorial.html


public class MainActivity extends AppCompatActivity implements
        CitiesListFragment.Callbacks,
        DetailsFragment.Callbacks,
        AddCityFragment.Callbacks,
        SettingsFragment.Callbacks,
        ViewWeatherFragment.Callbacks {

    // 0 = metric (default), 1 = imperial,
    // Metric: temperature in "Celsius", wind speed in "meter/sec", pressure in "hPa"
    // Imperial: temperature in "Fahrenheit", wind speed in "miles/hour", pressure in "hPa"
    //   private int mUnitsFormat;

    // sort cities: 0 = by adding (default), 1 = alphabetically
    //   private int mSortCities;

    // 0 = English(default), 1 = Russian

    // message panel
    private LinearLayout messageBarLayout;
    private TextView messageTextView;
    private ProgressBar messageProgressBar;

    // -------------------------------------------
    // Unique loaders ID:
    // Loader ID: CitiesListFragment
    public static final int LOADER_CITIES_ID = 1;

    // Loader ID: DetailsFragment
    public static final int LOADER_WEATHER_ID = 2;

    // Loader ID: AddCityFragment
    public static final int LOADER_MARKER_ID = 3;
    // -------------------------------------------

    private final String BROADCAST_STATIC_ACTION = "com.example.arsone.weather.static.broadcast";
    private static final String BROADCAST_DYNAMIC_ACTION = "com.example.arsone.weather.dynamic.broadcast";

    private BroadcastReceiver dynamicBroadcastReceiver;

    public final static String PARAM_TASK = "task";
    public final static String PARAM_STATUS = "status";
    public final static String PARAM_CITY_ID = "city_id";
    public final static String PARAM_ENTERED_CITY = "city_name";

    public final static int TASK_GET_WEATHER_ONE_CITY = 1;
    public final static int TASK_GET_WEATHER_ALL_CITIES = 2;

    public final static int STATUS_GET_WEATHER_ONE_CITY_START = 101;
    public final static int STATUS_GET_WEATHER_ONE_CITY_FINISH = 102;

    public final static int STATUS_GET_WEATHER_ALL_CITIES_START = 103;
    public final static int STATUS_GET_WEATHER_ALL_CITIES_FINISH = 104;

    public static final String CITY_ID = "city_id";
    public static final String ENTERED_CITY = "entered_city";
    public static final String UPDATE_TIME = "update_time";
    public static final String UNITS_FORMAT = "units_format";


    final class Settings {

        private final int unitsFormat;
        private final int sortCities;

        public Settings(int unitsFormat, int sortCities) { // , int mapLanguageIndex) {

            this.unitsFormat = unitsFormat;
            this.sortCities = sortCities;
        }

        public int getUnitsFormat() {
            return unitsFormat;
        }

        public int getSortCities() {
            return sortCities;
        }
    }


    // https://dhimitraq.wordpress.com/2012/11/27/using-intentservice-with-alarmmanager-to-schedule-alarms/


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Mapbox.getInstance(this, getString(R.string.mapbox_api_key));

        // set constant orientation for different screens
        if (findViewById(R.id.onePaneLayout) != null) { // phone

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else { // tablet
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        // message bar panel
        messageBarLayout = (LinearLayout) findViewById(R.id.messageBar);
        messageTextView = (TextView) findViewById(R.id.messageTextView);
        messageProgressBar = (ProgressBar) findViewById(R.id.messageProgressBar);


        dynamicBroadcastReceiver = new BroadcastReceiver() {

            public void onReceive(Context context, Intent intent) {

                if (intent.getAction().equalsIgnoreCase(BROADCAST_DYNAMIC_ACTION)) {

                    int status = intent.getIntExtra(PARAM_STATUS, 0);

                    switch (status) {

                        case STATUS_GET_WEATHER_ONE_CITY_START:

                            Log.d("AAAAA", "dynamicBroadcastReceiver GET: STATUS_GET_WEATHER_ONE_CITY_START");

                            showMessageBar(getString(R.string.message_wait_for_data), true);

                            break;

                        case STATUS_GET_WEATHER_ONE_CITY_FINISH:

                            Log.d("AAAAA", "dynamicBroadcastReceiver GET: STATUS_GET_WEATHER_ONE_CITY_FINISH");

                            refreshData();

                            break;

                        case STATUS_GET_WEATHER_ALL_CITIES_START:

                            Log.d("AAAAA", "dynamicBroadcastReceiver GET: STATUS_GET_WEATHER_ALL_CITIES_START");

                            showMessageBar(getString(R.string.message_wait_for_data), true);

                            break;

                        case STATUS_GET_WEATHER_ALL_CITIES_FINISH:

                            Log.d("AAAAA", "dynamicBroadcastReceiver GET: STATUS_GET_WEATHER_ALL_CITIES_FINISH");

                            refreshData();

                            break;
                    }
                }
            }
        };

        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilt = new IntentFilter(BROADCAST_DYNAMIC_ACTION);

        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(dynamicBroadcastReceiver, intFilt);

        scheduleAlarm();

/*        // set action bar icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.white_balance_sunny);*/


        if (savedInstanceState != null) // create activity only one time?
            return;

        // ----------------------------------------------------
        // Show CitiesListFragment
        CitiesListFragment citiesFragment = new CitiesListFragment();

/*        Bundle bundle = new Bundle();
        bundle.putInt(MainActivity.UNITS_FORMAT, mUnitsFormat);
        citiesFragment.setArguments(bundle);*/

        if (findViewById(R.id.onePaneLayout) != null) { // phone

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.onePaneLayout, citiesFragment)
                    .commit();

        } else { // tablet

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.citiesContainer, citiesFragment)
                    .commit();
        }
        // ----------------------------------------------------
    }


    public void showSettings() {

        // ----------------------------------------------------
        // Show SettingsFragment
        SettingsFragment settingsFragment = new SettingsFragment();

        if (findViewById(R.id.onePaneLayout) != null) { // phone

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.onePaneLayout, settingsFragment)
                    .addToBackStack(null)
                    .commit();

        } else { // tablet

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rightFrameLayout, settingsFragment)
                    .commit();
        }
        // ----------------------------------------------------
    }

    public Settings readSettingsFromDB() {

        Log.d("AAAAA", "readSettingsFromDB()");

        // read all columns
        Cursor cursor = getContentResolver().query(DataContentProvider.SETTINGS_CONTENT_URI,
                new String[]{DataContract.SettingsEntry.COLUMN_UNITS_FORMAT,
                        DataContract.SettingsEntry.COLUMN_SORT_CITIES,
                        DataContract.SettingsEntry.COLUMN_MAP_LANGUAGE},
                null, // DataContract.CityEntry.COLUMN_ENTERED_CITY + "=?",
                null, // new String[]{enteredCity},
                null);

        if (cursor != null && cursor.getCount() > 0) {

            cursor.moveToFirst();

            // units format: 0 = metric, 1 = imperial
            int unitsFormat = cursor.getInt(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_UNITS_FORMAT));

            // sort: by id = 0, alphabetic = 1
            int sortCities = cursor.getInt(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_SORT_CITIES));

            cursor.close();

            return new Settings(unitsFormat, sortCities);
        } else {
            return null;
        }
    }


    @Override
    public void onAddCity() {

        // ----------------------------------------------------
        // Show AddCityFragment

        AddCityFragment addCityFragment = new AddCityFragment();

        if (findViewById(R.id.onePaneLayout) != null) { // phone

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.onePaneLayout, addCityFragment)
                    .addToBackStack(null)
                    .commit();

        } else { // tablet

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rightFrameLayout, addCityFragment)
                    .commit();
        }
        // ----------------------------------------------------
    }


    // CitiesListFragment: ListView -> city selected
    @Override
    public void onCityItemSelected(int cityID, String enteredName, String dataUpdateTime, int unitsFormat) {

        // ----------------------------------------------------
        // Show DetailsFragment

        DetailsFragment detailsFragment = new DetailsFragment();

        // put data to DetailsFragment
        Bundle bundle = new Bundle();

        bundle.putInt(MainActivity.CITY_ID, cityID);
        bundle.putString(MainActivity.ENTERED_CITY, enteredName);
/*        bundle.putString(MainActivity.UPDATE_TIME, dataUpdateTime);
        bundle.putInt(MainActivity.UNITS_FORMAT, unitsFormat);*/

        detailsFragment.setArguments(bundle);

        if (findViewById(R.id.onePaneLayout) != null) { // phone

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.onePaneLayout, detailsFragment)
                    .addToBackStack(null)
                    .commit();

        } else { // tablet

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rightFrameLayout, detailsFragment)
                    .commit();
        }
        // ----------------------------------------------------
    }


    @Override
    public void onSaveCity(int id, String enteredCity) {

        hideKeyboard();

        // ----------------------------------------------------
        // remove addCityFragment after save added city in DB
        if (findViewById(R.id.onePaneLayout) != null) { // phone

            this.onBackPressed(); // remove addEditFragment`s activity

        } else { // tablet

            AddCityFragment addCityFragment = (AddCityFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.rightFrameLayout);

            if (addCityFragment != null) {

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.remove(addCityFragment);
                transaction.commit();
            }
            // ----------------------------------------------------
        }

        syncOneCity(id, enteredCity);
    }


    private void syncOneCity(int id, String enteredCity) {

        // -----------------------------------------------------------
        // get detailed data for ONE added city
        Intent oneIntent = new Intent(this, GetDataService.class)
                //  .setAction(BROADCAST_DYNAMIC_ACTION)
                .putExtra(PARAM_TASK, TASK_GET_WEATHER_ONE_CITY) // get weather data for one city only!
                .putExtra(PARAM_CITY_ID, id) //  "CITIES" table: column "_id"
                .putExtra(PARAM_ENTERED_CITY, enteredCity); // "CITIES" table: column "entered_city"
        // .addCategory(Intent.CATEGORY_DEFAULT);

        // start service for added a city details and weather data
        startService(oneIntent);
        // -----------------------------------------------------------
    }


    public void onCancelAddEdit() {

        hideKeyboard();

        // ----------------------------------------------------
        // remove addCityFragment after save added city in DB
        if (findViewById(R.id.onePaneLayout) != null) { // phone

            this.onBackPressed(); // remove addEditFragment`s activity

        } else {

            AddCityFragment addCityFragment = (AddCityFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.rightFrameLayout);

            if (addCityFragment != null) {

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.remove(addCityFragment);
                transaction.commit();
            }
        }
        // ----------------------------------------------------
    }


    private void refreshData() {

        hideMessageBar();

        // determine what is the fragments are in activity?
        if (findViewById(R.id.onePaneLayout) == null) { // tablet

            Fragment leftFragment = getSupportFragmentManager().findFragmentById(R.id.citiesContainer);
            Fragment rightFragment = getSupportFragmentManager().findFragmentById(R.id.rightFrameLayout);

            // show cities fragment
            CitiesListFragment citiesListFragment = (CitiesListFragment) leftFragment;
            citiesListFragment.initLoader();


            if (rightFragment instanceof DetailsFragment) { // refresh details: weather ListView

                Log.d("AAAAA", "tablet = refreshData: citiesListFragment - initLoader();");

                DetailsFragment detailsFragment = (DetailsFragment) rightFragment;

                detailsFragment.initLoader();
            }
        } else { // phone

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.onePaneLayout);

            if (fragment instanceof CitiesListFragment) { // refresh details: weather ListView

                Log.d("AAAAA", "phone = refreshData: citiesListFragment - initLoader();");

                CitiesListFragment citiesListFragment = (CitiesListFragment) fragment;

                citiesListFragment.initLoader();

            } else if (fragment instanceof DetailsFragment) { // refresh cities: cities ListView

                Log.d("AAAAA", "phone = refreshData: DetailsFragment - initLoader()");

                DetailsFragment detailsFragment = (DetailsFragment) fragment;

                detailsFragment.initLoader();

            } else if (fragment instanceof ViewWeatherFragment) { // refresh cities: cities ListView

                Log.d("AAAAA", "phone = refreshData: ViewWeatherFragment - initLoader()");

                ViewWeatherFragment viewWeatherFragment = (ViewWeatherFragment) fragment;

                viewWeatherFragment.initLoader();
            }
        }
    }


    // icon "sync" pressed
    public void syncData() {

        //   hideMessageBar();

        // determine what is the fragments are in activity?
        if (findViewById(R.id.onePaneLayout) == null) { // tablet

            //Fragment leftFragment = getSupportFragmentManager().findFragmentById(R.id.citiesContainer);
            //   Fragment rightFragment = getSupportFragmentManager().findFragmentById(R.id.rightFrameLayout);

            //   CitiesListFragment citiesListFragment = (CitiesListFragment) leftFragment;

            syncAllData();

        } else { // phone

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.onePaneLayout);

            if (fragment instanceof CitiesListFragment) { // refresh details: weather ListView

                syncAllData();

            } else if (fragment instanceof DetailsFragment) { // refresh cities: cities ListView

                DetailsFragment detailsFragment = (DetailsFragment) fragment;

                detailsFragment.syncCurrentCity();
            }
        }
    }


    public void syncAllData() {

        // run sync single time
        if (!isMyServiceRunning(GetDataService.class)) {

            // -----------------------------------------------------------
            // get detailed data for ALL cities
            Intent intent = new Intent(this, GetDataService.class)
                    .putExtra(PARAM_TASK, TASK_GET_WEATHER_ALL_CITIES); // get weather data for all cities

            // start service for added a city details and weather data
            startService(intent);
            // -----------------------------------------------------------
        }
    }


    // Setup a recurring alarm every half hour
    public void scheduleAlarm() {

        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), StaticBroadcastReceiver.class);

        intent.setAction(BROADCAST_STATIC_ACTION);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, StaticBroadcastReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away

        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
    }


/*
    private void runAlarmPeriodically() {

        Intent intent = new Intent();
        intent.setAction(BROADCAST_STATIC_ACTION);
      //  intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
        sendBroadcast(intent);
*/
    //   }


 /*   Параметры
    Методы set(), setRepeating(), setInexactRepeating() используют следующие параметры:
    setInexactRepeating(typeOne, triggerTime, interval, operation)

    - typeOne - тип используемого времени (системное или всемирное время UTC), который определяется константами
    ELAPSED_REALTIME - запускает ожидающее намерение, основываясь на времени,
    которое прошло с момента загрузки устройства, но не с момента выхода из режима ожидания.
    Это время включает любой временной промежуток, в котором устройство находилось в данном режиме.
     Обратите внимание, что прошедшее время вычисляется на основании того, когда устройство было загружено.
     Используется системное время
    ELAPSED_REALTIME_WAKEUP - по прошествии указанного промежутка времени с момента загрузки
    выводит устройство из спящего режима и запускает ожидающее намерение. Используется системное время
    RTC - запускает ожидающее намерение в указанное время, но не выводит устройство из режима ожидания.
    Используется всемирное время UTC
    RTC_WAKEUP - выводит устройство из режима ожидания для запуска ожидающего намерения в указанное время.
     Используется всемирное время UTC

    - triggerTime - время работы оповещения

    - interval - интервал между отправкой повторных сигнализаций в миллисекундах.
     Также можно использовать константы:
    INTERVAL_DAY,I NTERVAL_HALF_DAY, INTERVAL_HOUR, INTERVAL_HALF_HOUR, INTERVAL_FIFTEEN_MINUTES

    - operation - объект PendingIntent, определяющий действие, выполняемое при запуске сигнализации.
     Можно получить через специальные методы:
            PendingIntent.getActivities(Context, int, Intent[], int)
            PendingIntent.getActivity(Context, int, Intent, int)
            PendingIntent.getService(Context, int, Intent, int)
            PendingIntent.getBroadcast(Context, int, Intent, int)
*/


    public void onDeleteCity() {

        // refresh citiesListView data on the screen
        if (findViewById(R.id.onePaneLayout) == null) { // tablet

            CitiesListFragment citiesListFragment = (CitiesListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.citiesContainer);

            citiesListFragment.initLoader();

            // ----------------------------------------------------
            // remove right pane fragment after delete city in DB
            Fragment rightFragment = getSupportFragmentManager().findFragmentById(R.id.rightFrameLayout);

            if (rightFragment != null) {

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.remove(rightFragment);
                transaction.commit();
            }
            // ----------------------------------------------------

        } else { // phone

            CitiesListFragment citiesListFragment = (CitiesListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.onePaneLayout);

            if (citiesListFragment != null)
                citiesListFragment.initLoader();
        }
    }


    public void hideKeyboard() {

        InputMethodManager inputManager = (InputMethodManager)
                this.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (this.getCurrentFocus() != null) {
            inputManager.hideSoftInputFromWindow(this.getCurrentFocus()
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    @Override
    public void onMapClicked() {

        hideKeyboard();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(dynamicBroadcastReceiver);
    }


    // determine is service running now?
    public boolean isMyServiceRunning(Class<?> serviceClass) {

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


    private void showMessageBar(String text, boolean showProgressBar) {

        if (messageBarLayout == null)
            return;

        messageBarLayout.setVisibility(View.VISIBLE);
        messageTextView.setText(text);

        if (showProgressBar)
            messageProgressBar.setVisibility(View.VISIBLE);
        else
            messageProgressBar.setVisibility(View.GONE);
    }


    private void hideMessageBar() {

        if (messageBarLayout != null) {
            messageBarLayout.setVisibility(View.GONE);
        }
    }


    // display menu items
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }


    // handle choice from options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_settings:

                showSettings();

                return true;

            case R.id.action_sync:

                syncData();

                return true;

            case R.id.action_view:

                viewData();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // after settings has changed
    public void onSettingsChanged() {

        // determine what is the fragments are in activity?
        if (findViewById(R.id.onePaneLayout) == null) { // tablet

            Fragment leftFragment = getSupportFragmentManager().findFragmentById(R.id.citiesContainer);
            //      Fragment rightFragment = getSupportFragmentManager().findFragmentById(R.id.rightFrameLayout);

            ///     Log.d("AAAAA", "onSettingsChanged");
            CitiesListFragment citiesListFragment = (CitiesListFragment) leftFragment;
            citiesListFragment.initLoader();

        }/* else { // phone

            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.onePaneLayout);

            if (fragment instanceof CitiesListFragment) { // refresh details: weather ListView

                Log.d("AAAAA", "onSettingsChanged() - refresh cities listView");

                /// CitiesListFragment citiesListFragment = (CitiesListFragment) fragment;

                CitiesListFragment citiesListFragment = (CitiesListFragment) fragment;

                citiesListFragment.initLoader();


            } else if (fragment instanceof DetailsFragment) { // refresh cities: cities ListView

                Log.d("AAAAA", "onSettingsChanged() - refresh weather listView");

                DetailsFragment detailsFragment = (DetailsFragment) fragment;

                detailsFragment.initLoader();
            }*/
    }

    // icon "view" pressed
    private void viewData() {

        // ----------------------------------------------------
        // Show ViewWeatherFragment
        ViewWeatherFragment viewWeatherFragment = new ViewWeatherFragment();

        if (findViewById(R.id.onePaneLayout) != null) { // phone

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.onePaneLayout, viewWeatherFragment)
                    .addToBackStack(null)
                    .commit();

        } else { // tablet

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rightFrameLayout, viewWeatherFragment)
                    .commit();
        }
        // ----------------------------------------------------
    }
}