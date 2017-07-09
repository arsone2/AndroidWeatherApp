package com.example.arsone.weather;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.net.Uri;
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

import com.example.arsone.weather.data.City;
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


public class MainActivity extends AppCompatActivity implements
        CitiesListFragment.Callbacks,
        AddCityFragment.Callbacks,
        SettingsFragment.Callbacks {


    // 0 = metric (default), 1 = imperial,
    // Metric: temperature in "Celsius", wind speed in "meter/sec", pressure in "hPa"
    // Imperial: temperature in "Fahrenheit", wind speed in "miles/hour", pressure in "hPa"
    private int mUnitsFormat;


    // message panel
    private LinearLayout messageBarLayout;
    private TextView messageTextView;
    private ProgressBar messageProgressBar;


    // Loader ID: CitiesListFragment
    public static final int LOADER_CITIES_ID = 1;

    // Loader ID: DetailsFragment
    public static final int LOADER_WEATHER_ID = 2;

    // Loader ID: AddCityFragment
    public static final int LOADER_MARKER_ID = 3;


    public final static String BROADCAST_ACTION = "com.example.arsone.weather.broadcast";


    /// public final static String SERVICE_PARAM_TASK = "task";


    private BroadcastReceiver broadcastReceiver;

///    private CustomBroadcastReceiver customBroadcastReceiver;

    // public final static String PARAM_TIME = "time";
    public final static String PARAM_TASK = "task";
    ///    public final static String PARAM_RESULT = "result";
    public final static String PARAM_STATUS = "status";
    public final static String PARAM_CITY_ID = "city_id";
    public final static String PARAM_ENTERED_CITY = "city_name";


    public final static int TASK_GET_WEATHER_ONE_CITY = 1;
    public final static int TASK_GET_WEATHER_ALL_CITIES = 2;
    // final int TASK3_CODE = 3;


    public final static int STATUS_GET_WEATHER_ONE_CITY_START = 101;
    public final static int STATUS_GET_WEATHER_ONE_CITY_FINISH = 102;

    public final static int STATUS_GET_WEATHER_ALL_CITIES_START = 103;
    public final static int STATUS_GET_WEATHER_ALL_CITIES_FINISH = 104;


    public static final String UNITS_FORMAT = "units_format";


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

        broadcastReceiver = new BroadcastReceiver() {

            // действия при получении сообщений
            public void onReceive(Context context, Intent intent) {

                int task = intent.getIntExtra(PARAM_TASK, 0);
                int status = intent.getIntExtra(PARAM_STATUS, 0);

                Log.d("AAAAA", "MainActivity: onReceive: task = " + task + ", status = " + status);

                // Ловим сообщения о старте задач
                //   if (status == STATUS_START) {

                switch (status) {

                    case STATUS_GET_WEATHER_ONE_CITY_START:

                        Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ONE_CITY_START");

                        showMessageBar(getString(R.string.message_wait_for_data), true);

                        break;

                    case STATUS_GET_WEATHER_ONE_CITY_FINISH:

                        Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ONE_CITY_FINISH");

                        /// reInitCityListViewLoader();
                        refreshCitiesListForTabletDevice();

                        hideMessageBar();

                        break;

                    case STATUS_GET_WEATHER_ALL_CITIES_START:

                        Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ALL_CITIES_START");

                        showMessageBar(getString(R.string.message_wait_for_data), true);

                        break;

                    case STATUS_GET_WEATHER_ALL_CITIES_FINISH:

                        Log.d("AAAAA", "GET: STATUS_GET_WEATHER_ALL_CITIES_FINISH");

                        /// reInitCityListViewLoader();

                        refreshCitiesListForTabletDevice();

                        hideMessageBar();

                        break;
                }
                //      }
            }
        };

        ///   customBroadcastReceiver = new CustomBroadcastReceiver();

        // создаем фильтр для BroadcastReceiver
        /// IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);

        // регистрируем (включаем) BroadcastReceiver
        ///  registerReceiver(broadcastReceiver, intFilt);

        //    customBroadcastReceiver = new CustomBroadcastReceiver();

        // создаем фильтр для BroadcastReceiver
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        // регистрируем (включаем) BroadcastReceiver
        registerReceiver(broadcastReceiver, intFilt);


        /// syncData();

        ///  scheduleAlarm(); // COMMENTED !!!


        // set action bar icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.white_balance_sunny);


        if (savedInstanceState != null) // create activity only one time?
            return;


//        scheduleAlarm(); // test commented !!!

        readSettingsFromDB();

        // ----------------------------------------------------
        // Show CitiesListFragment
        CitiesListFragment citiesFragment = new CitiesListFragment();

        //    Log.d("AAAAA", "bundle.putInt(MainActivity.UNITS_FORMAT, mUnitsFormat) - mUnitsFormat = " + mUnitsFormat);

        Bundle bundle = new Bundle();
        bundle.putInt(MainActivity.UNITS_FORMAT, mUnitsFormat);
        citiesFragment.setArguments(bundle);

        if (findViewById(R.id.onePaneLayout) != null) { // phone

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.onePaneLayout, citiesFragment)
                    .addToBackStack(null)
                    .commit();

        } else { // tablet

            //    Log.d("AAAAA", "TABLET !!!");

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

        //    Log.d("AAAAA", "bundle.putInt(MainActivity.UNITS_FORMAT, mUnitsFormat) - mUnitsFormat = " + mUnitsFormat);

        Bundle bundle = new Bundle();
        bundle.putInt(MainActivity.UNITS_FORMAT, mUnitsFormat);
        settingsFragment.setArguments(bundle);

        if (findViewById(R.id.onePaneLayout) != null) { // phone

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.onePaneLayout, settingsFragment)
                    .addToBackStack(null)
                    .commit();

        } else { // tablet

            //    Log.d("AAAAA", "TABLET !!!");

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rightFrameLayout, settingsFragment)
                    .commit();
        }
        // ----------------------------------------------------


 /*       int viewID;

        if (findViewById(R.id.onePaneLayout) == null) { // tablet

            viewID = R.id.rightFrameLayout;

        } else { // phone

            viewID = R.id.onePaneLayout;
        }


        // get settings from DB
        readSettingsFromDB();

        // put settings to SettingsFragment
        Bundle bundle = new Bundle();

        bundle.putInt(MainActivity.UNITS_FORMAT, mUnitsFormat);

        SettingsFragment fragment = new SettingsFragment();

        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(viewID, fragment);
        // .addToBackStack(null)

        if (findViewById(R.id.onePaneLayout) != null) // phone only
        {
            transaction.addToBackStack(null);
        }

        transaction.commit(); // causes fragment to display*/
    }


    private void readSettingsFromDB() {

        Log.d("AAAAA", "readSettingsFromDB");

        // read all columns
        Cursor cursor = getContentResolver().query(DataContentProvider.SETTINGS_CONTENT_URI,
                new String[]{DataContract.SettingsEntry.COLUMN_UNITS_FORMAT},
                null, // DataContract.CityEntry.COLUMN_ENTERED_CITY + "=?",
                null, // new String[]{enteredCity},
                null);

        if (cursor != null && cursor.getCount() > 0) {

            cursor.moveToFirst();

            mUnitsFormat = cursor.getInt(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_UNITS_FORMAT));
            cursor.close();

            Log.d("AAAAA", "readSettingsFromDB - mUnitsFormat = " + mUnitsFormat);
        }
    }


    public void writeSettingsToDB(int unitsFormat) {

        ///    mUnitsFormat = unitsFormat;

        ContentValues values = new ContentValues();

        values.put(DataContract.SettingsEntry.COLUMN_UNITS_FORMAT, unitsFormat);

        int updatedRowsCount = getContentResolver().update(DataContentProvider.SETTINGS_CONTENT_URI, values, null, null);

        //    Log.d("AAAAA", "writeSettingsToDB(): updatedRowsCount = " + updatedRowsCount);



        // hideKeyboard();
        // this.onBackPressed(); // close SettingsFragment

/*        // ----------------------------------------------------
        // remove SettingsFragment after save added city in DB
        if (findViewById(R.id.onePaneLayout) != null) { // phone

            this.onBackPressed(); // remove addEditFragment`s activity

        } else {

            SettingsFragment addCityFragment = (AddCityFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.rightFrameLayout);

            if (addCityFragment != null) {

///                Log.d("AAAAA", "detailsFragment != null");
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.remove(addCityFragment);
                transaction.commit();
            }
            // ----------------------------------------------------*/


        // refresh cities list fragment on tablet devices only!
        if (findViewById(R.id.onePaneLayout) == null) { // tablet
            refreshCitiesListForTabletDevice();
        }
    }

/*
    public int getUnitsFormat(){

        return mUnitsFormat;
    }*/

/*
    // Setup a recurring alarm every hour
    public void scheduleAlarm() {

        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), CustomBroadcastReceiver.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, CustomBroadcastReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away

        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_HOUR, pIntent);
    }*/


/*        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/


    @Override
    public void onAddCity() {

        // ----------------------------------------------------
        // Show AddCityFragment

        AddCityFragment addCityFragment = new AddCityFragment();

        //   Log.d("AAAAA", "MainActivity - putInt - unitsFormat = " + unitsFormat);

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

/*        if (findViewById(R.id.onePaneLayout) == null) { // tablet

            Log.d("AAAAA", "findViewById(R.id.onePaneLayout) == null");
            // reInitCityListViewLoader();
            /// refreshCityListView();

            CitiesListFragment citiesListFragment = (CitiesListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.citiesContainer);

//            if (citiesListFragment != null) {

            Log.d("AAAAA", "citiesListFragment != null");

            // citiesListFragment.refreshCityList();
            citiesListFragment.initLoader();

        }*/

/*        if (findViewById(R.id.onePaneLayout) == null) { // tablet

            AddEditCity(R.id.rightFrameLayout);
        } else { // phone

            AddEditCity(R.id.onePaneLayout);
        }*/
    }



    // in the CitiesListFragment city selected
    @Override
    public void onCityItemSelected(int cityID, String enteredName, String returnedName, String dataUpdateTime, int unitsFormat) {


        //  readSettingsFromDB(); // TEST !!!

//        Log.d("AAAAA", "onCityItemSelected - cityID = " + cityID);
//        Log.d("AAAAA", "onCityItemSelected - cityName = " + cityName);

        // ----------------------------------------------------
        // RUN DetailsFragment

        DetailsFragment detailsFragment = new DetailsFragment();

        // put data to DetailsFragment
        Bundle bundle = new Bundle();

        bundle.putInt(City.CITY_ID, cityID);
        bundle.putString(City.ENTERED_CITY, enteredName);
        bundle.putString(City.RETURNED_CITY, returnedName);
        bundle.putString(City.UPDATE_TIME, dataUpdateTime);
        bundle.putInt(MainActivity.UNITS_FORMAT, unitsFormat);

        //   Log.d("AAAAA", "MainActivity - putInt - unitsFormat = " + unitsFormat);

        detailsFragment.setArguments(bundle);

        if (findViewById(R.id.onePaneLayout) != null) { // phone

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.onePaneLayout, detailsFragment)
                    .commit();

        } else { // tablet

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.rightFrameLayout, detailsFragment)
                   // .addToBackStack(null) // DO NOT ADD this root fragment to backstack!!
                    .commit();
        }
/*
            //  showDetails(R.id.rightFrameLayout, cityID, enteredName, returnedName, dataUpdateTime, unitsFormat);

        } else { // tablet

            //   showDetails(R.id.onePaneLayout, cityID, enteredName, returnedName, dataUpdateTime, unitsFormat);
        }*/
        // ----------------------------------------------------
    }


 /*   // listItem click

    private void showDetails(int viewID, int cityID, String enteredName,
                             String returnedName, String dataUpdateTime,  int unitsFormat) {

        DetailsFragment detailsFragment = new DetailsFragment();

        // put data to DetailsFragment
        Bundle bundle = new Bundle();

        bundle.putInt(City.CITY_ID, cityID);
        bundle.putString(City.ENTERED_CITY, enteredName);
        bundle.putString(City.RETURNED_CITY, returnedName);
        bundle.putString(City.UPDATE_TIME, dataUpdateTime);
        bundle.putInt(MainActivity.UNITS_FORMAT, unitsFormat);

     //   Log.d("AAAAA", "MainActivity - putInt - unitsFormat = " + unitsFormat);

        detailsFragment.setArguments(bundle);

        // use a FragmentTransaction to display the fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(viewID, detailsFragment);

        if (findViewById(R.id.onePaneLayout) != null) { // add to backstack for phone screen only
            transaction.addToBackStack(null);
        }

        transaction.commit(); // causes fragment to display
    }*/


    @Override
    public void onSaveCity(int id, String enteredCity) {

        hideKeyboard();

  //      this.onBackPressed(); // remove addEditFragment`s activity

        // ----------------------------------------------------
        // remove addCityFragment after save added city in DB
        if (findViewById(R.id.onePaneLayout) != null) { // phone

            this.onBackPressed(); // remove addEditFragment`s activity

        } else {

            AddCityFragment addCityFragment = (AddCityFragment)getSupportFragmentManager()
                    .findFragmentById(R.id.rightFrameLayout);

            if (addCityFragment != null) {

///                Log.d("AAAAA", "detailsFragment != null");
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.remove(addCityFragment);
                transaction.commit();
            }
        // ----------------------------------------------------

/*
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.rightFrameLayout);
            DetailsFragment detailsFragment = null;

            if (f instanceof DetailsFragment)
                detailsFragment = (DetailsFragment) f;

            if (detailsFragment != null) {

                Log.d("AAAAA", "detailsFragment != null");

                // remove right DetailsFragment after delete city
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.remove(detailsFragment);
                transaction.commit(); // causes fragment to display
            }
*/
        }






        // get data for added city only
        Log.d("AAAAA", "Sync running after add new city...");

        /// scheduleAlarm();


        // -----------------------------------------------------------
        // get detailed data for ONE added city
        Intent intent = new Intent(this, GetDataService.class)
                .putExtra(PARAM_TASK, TASK_GET_WEATHER_ONE_CITY) // get weather data for one city only!
                .putExtra(PARAM_CITY_ID, id) //  "CITIES" table: column "_id"
                .putExtra(PARAM_ENTERED_CITY, enteredCity); // "CITIES" table: column "entered_city"

        // start service for added a city details and weather data
        startService(intent);
        // -----------------------------------------------------------

        /// refreshCitiesListForTabletDevice();
    }


    public void onCancelAddEdit() {

        hideKeyboard();

      //  this.onBackPressed(); // close addEditFragment

        // ----------------------------------------------------
        // remove addCityFragment after save added city in DB
        if (findViewById(R.id.onePaneLayout) != null) { // phone

            this.onBackPressed(); // remove addEditFragment`s activity

        } else {

            AddCityFragment addCityFragment = (AddCityFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.rightFrameLayout);

            if (addCityFragment != null) {

///                Log.d("AAAAA", "detailsFragment != null");
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.remove(addCityFragment);
                transaction.commit();
            }
        }
            // ----------------------------------------------------
    }


    public void refreshCitiesListForTabletDevice() {

        Log.d("AAAAA", "refreshCitiesListForTabletDevice()");

        //  if (findViewById(R.id.onePaneLayout) == null) { // tablet

        //  Log.d("AAAAA", "refreshCitiesListForTabletDevice() - is tablet!");

        CitiesListFragment citiesListFragment = (CitiesListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.citiesContainer);

        if (citiesListFragment != null) {
            // refresh cities ListView
            Log.d("AAAAA", "citiesListFragment.initLoader()");
            citiesListFragment.initLoader();
        }

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.rightFrameLayout);
        DetailsFragment detailsFragment = null;

        if (f instanceof DetailsFragment)
            detailsFragment = (DetailsFragment) f;

/*        DetailsFragment detailsFragment = (DetailsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.rightFrameLayout);*/

        if (detailsFragment != null) {

            Log.d("AAAAA", "detailsFragment != null");

            // remove right DetailsFragment after delete city
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(detailsFragment);
            transaction.commit(); // causes fragment to display
        }
        //    }
    }





/*    private void AddEditCity(int viewID) {

        //  AddCityFragment fragment = new AddCityFragment();

        // use a FragmentTransaction to display the fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, new AddCityFragment());
        transaction.addToBackStack(null);
        transaction.commit(); // causes fragment to display

    }*/


    public void onDeleteCity() {

        Log.d("AAAAA", "onDeleteCity()");

        // refresh cities ListView after city was deleted
        // reinitialize CitiesFragment LoadManager IN TABLET DEVICES ONLY!
        // REMARK:
        // On the phone devices CitiesFragment layout LoadManager reinit itself in "OnCreate" fragment`s method

        // ----------------------------------------------------
        // remove right pane fragment after delete city in DB
        Fragment rightFragment = getSupportFragmentManager().findFragmentById(R.id.rightFrameLayout);

        if (rightFragment != null) {

            Log.d("AAAAA", "rightFragment != null");
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.remove(rightFragment);
            transaction.commit();
        }
        // ----------------------------------------------------

        // refresh citiesListView data on the screen
        if (findViewById(R.id.onePaneLayout) == null) { // tablet

            CitiesListFragment citiesListFragment = (CitiesListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.citiesContainer);

            citiesListFragment.initLoader();
        }


/*        // ----------------------------------------------------
        // remove right pane fragment after delete city in DB
        if (findViewById(R.id.onePaneLayout) != null) { // phone

          //  this.onBackPressed(); // remove addEditFragment`s activity

        } else { // tablet

            Fragment rightFragment = getSupportFragmentManager().findFragmentById(R.id.rightFrameLayout);

            if (rightFragment != null) {

                Log.d("AAAAA", "rightFragment != null");
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.remove(rightFragment);
                transaction.commit();
            }
        }
            // ----------------------------------------------------*/


        /*        if (findViewById(R.id.onePaneLayout) == null) { // tablet

            Log.d("AAAAA", "findViewById(R.id.onePaneLayout) == null");
            // reInitCityListViewLoader();
            /// refreshCityListView();

            CitiesListFragment citiesListFragment = (CitiesListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.citiesContainer);

//            if (citiesListFragment != null) {

            Log.d("AAAAA", "citiesListFragment != null");

            // citiesListFragment.refreshCityList();
            citiesListFragment.initLoader();
        }*/

/*       if (findViewById(R.id.onePaneLayout) == null) { // tablet

            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager()
                    .findFragmentById(R.id.rightFrameLayout))
                    .commit();

           // AddEditCity(R.id.rightFrameLayout);
        }*/

/*        // remove right fragment on tablet
        if (findViewById(R.id.rightFrameLayout) != null) { // is tablet

            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager()
                            .findFragmentById(R.id.rightFrameLayout))
                    .commit();
        }
        ///  this.onBackPressed(); // close addEditFragment
        //    getSupportFragmentManager().popBackStack(); // remove details fragment on tablet*/

        ///    reInitCityListViewLoader();
    }


    public void hideKeyboard() {

        InputMethodManager inputManager = (InputMethodManager)
                this.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (this.getCurrentFocus() != null) {
            inputManager.hideSoftInputFromWindow(this.getCurrentFocus()
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    private void reInitCityListViewLoader() {

        Log.d("AAAAA", "reInitCityListViewLoader()");


        CitiesListFragment citiesListFragment = null;

        // show cities fragment
        if (findViewById(R.id.onePaneLayout) != null) { // phone

            Fragment f = getSupportFragmentManager().findFragmentById(R.id.onePaneLayout);

            if (f instanceof CitiesListFragment) {
                citiesListFragment = (CitiesListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.onePaneLayout);
            }
        } else { // tablet

            Log.d("AAAAA", "TABLET !!!");

            Fragment f = getSupportFragmentManager().findFragmentById(R.id.rightFrameLayout);

            if (f instanceof CitiesListFragment)
                citiesListFragment = (CitiesListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.rightFrameLayout);
        }

        if (citiesListFragment != null) {

            Log.d("AAAAA", "citiesListFragment != null");
            citiesListFragment.initLoader();
        }

/*
        CitiesListFragment citiesListFragment = null;

        if (findViewById(R.id.onePaneLayout) == null) { // tablet

            citiesListFragment = (CitiesListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.rightFrameLayout);
            //  AddEditCity(R.id.rightFrameLayout);
        } else { // phone
            citiesListFragment = (CitiesListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.onePaneLayout);
            // AddEditCity(R.id.onePaneLayout);
        }

        if (citiesListFragment != null) {

            Log.d("AAAAA", "citiesListFragment != null");

            citiesListFragment.initLoader();
        }*/

 /*       CitiesListFragment citiesListFragment = null;

        if (findViewById(R.id.onePaneLayout) != null) { // tablet

            Log.d("AAAAA", "onePaneLayout found");

            citiesListFragment = (CitiesListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.onePaneLayout);
        } else { // phone


        }

        if (citiesListFragment != null) {

            Log.d("AAAAA", "citiesListFragment != null");

            citiesListFragment.initLoader();
        }*/


    }


    // force CitiesListFragment ListView refresh for tablet only
    private void refreshCityListView() {

        CitiesListFragment citiesListFragment = null;

        if (findViewById(R.id.leftFragment) != null) { // cities fragment layout is present

            Log.d("AAAAA", "leftFragment found");

            citiesListFragment = (CitiesListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.leftFragment);

            if (citiesListFragment != null) {

                Log.d("AAAAA", "citiesListFragment != null");

                citiesListFragment.refreshCityList();
            }
        }

/*        if (findViewById(R.id.citiesContainer) != null) { // is tablet

            citiesListFragment = (CitiesListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.citiesContainer);

            if (citiesListFragment != null) {
                citiesListFragment.refreshCityList();
            }
        }*/
    }


    @Override
    public void onMapClicked() {

        hideKeyboard();

    }

    //   private CustomBroadcastReceiver customBroadcastReceiver;


    @Override
    protected void onDestroy() {
        super.onDestroy();

        unregisterReceiver(broadcastReceiver);
    }


    // Setup a recurring alarm every half hour
    public void scheduleAlarm() {

        Log.d("AAAAA", "scheduleAlarm() runnn!");

        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), CustomBroadcastReceiver.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(this, CustomBroadcastReceiver.REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Setup periodic alarm every every half hour from this point onwards
        long firstMillis = System.currentTimeMillis(); // alarm is set right away

        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pIntent);
    }


    public void syncAllData() {

        Log.d("AAAAA", "syncAllData()");

        //    scheduleAlarm();

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


    private boolean isMyServiceRunning(Class<?> serviceClass) {
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

    // display this fragment's menu items


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

/*    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        // clear previous menu items
        menu.clear();

        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.city_fragment_menu, menu);
    }*/


/*    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        for (int index = 0; index < menu.size(); index++) {

            MenuItem menuItem = menu.getItem(index);

            if (menuItem != null) {
                // hide the menu items if the drawer is open
                menuItem.setVisible(mMenuVisible);
            }
        }

        super.onPrepareOptionsMenu(menu);
    }*/


    // handle choice from options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_settings:

/*
                if (isDeleteMode) {
                    return true;
                }
*/

                showSettings();

                return true;

            case R.id.action_sync:

                syncAllData();

                ///  activity.syncData();

                return true;

        }
        return super.onOptionsItemSelected(item);
    }


/*    public void showSettings() {

        int viewID;

        if (findViewById(R.id.onePaneLayout) == null) { // tablet

            viewID = R.id.rightFrameLayout;

        } else { // phone

            viewID = R.id.onePaneLayout;
        }


        // get settings from DB
        readSettingsFromDB();

        // put settings to SettingsFragment
        Bundle bundle = new Bundle();

        bundle.putInt(MainActivity.UNITS_FORMAT, mUnitsFormat);

        SettingsFragment fragment = new SettingsFragment();

        fragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(viewID, fragment);
        // .addToBackStack(null)

        if (findViewById(R.id.onePaneLayout) != null) // phone only
        {
            transaction.addToBackStack(null);
        }

        transaction.commit(); // causes fragment to display
    }*/


 /*   public void syncData() {

        AlarmManager alarmManager=(AlarmManager)getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, CustomBroadcastReceiver.class);

        //     intent.putExtra(ONE_TIME, Boolean.FALSE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);



        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);


    }*/


}


//After 5 seconds
/// alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 5 , pi);
/// alarmManager.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), 1000 * 5 , pi);

/*        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(), // start after 1 second delay
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);*/



 /*       AlarmManager alarmManager = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getActivity(), CustomBroadcastReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // На случай, если мы ранее запускали активити, а потом поменяли время,
        // откажемся от уведомления
        alarmManager.cancel(pendingIntent);

        // Устанавливаем разовое напоминание
        /// alarmManager.set(AlarmManager.RTC_WAKEUP, stamp.getTime(), pendingIntent);

        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(), // start after 1 second delay
                AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);*/