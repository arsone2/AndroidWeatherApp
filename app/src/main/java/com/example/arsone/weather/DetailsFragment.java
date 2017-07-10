package com.example.arsone.weather;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
import java.util.Locale;

import com.example.arsone.weather.data.City;
import com.example.arsone.weather.data.Weather;


public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface Callbacks {

        boolean isMyServiceRunning(Class<?> serviceClass);

        MainActivity.Settings readSettingsFromDB();
    }

    private DetailsFragment.Callbacks activity;

    // Fragment view title
    private TextView titleTextView;
    private TextView updateTimeTextView;

    // mode panel
    private LinearLayout modePanelLayout;
    private TextView modeTextView;
    private AppCompatImageView modeImageView;

    // weather ListView
    private ListView weatherListView;

    // weather objects List
    private List<Weather> weatherList = new ArrayList<>();

    private WeatherCursorAdapter weatherCursorAdapter;

    // selected city database table _id
    private int mID;

    // selected city name
    private String mEnteredCity;

    private int mUnitsFormat;
    //  private int mSortCities;


    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        activity = (DetailsFragment.Callbacks) context;
    }


    @Override
    public void onDetach() {

        super.onDetach();
        activity = null;
    }


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

    ///   private String mReturnedCity;

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
            mID = bundle.getInt(MainActivity.CITY_ID);
            mEnteredCity = bundle.getString(MainActivity.ENTERED_CITY);
            ///       mReturnedCity = bundle.getString(City.RETURNED_CITY);
            mUnitsFormat = bundle.getInt(MainActivity.UNITS_FORMAT);
            mDataUpdateTime = bundle.getString(MainActivity.UPDATE_TIME);

            ///     Log.d("AAAAA", "if (bundle != null) - mUnitsFormat = " + mUnitsFormat);
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


        weatherCursorAdapter = new WeatherCursorAdapter(getContext(), null, 0);
        weatherListView.setAdapter(weatherCursorAdapter);


        initLoader();


/*        // Title
        titleTextView.setText(mEnteredCity);*/

/*        if (TextUtils.isEmpty(mReturnedCity))
            titleTextView.setText(mEnteredCity);
        else
            titleTextView.setText(mEnteredCity + "/ " + mReturnedCity);*/

///        Log.d("AAAAA", "onCreateView: mID = " + mID);

/*        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date date = format.parse(mDataUpdateTime);

            String stringDate = DateFormat.getDateTimeInstance().format(date);

            updateTimeTextView.setText(getContext().getString(R.string.weather_data_obtained, stringDate));

        } catch (ParseException e) {
            Log.d("AAAAA", "Date ParseException: " + e.getMessage());
        }*/

        ///    showDetails();

        return view;
    }


    private void showDetails() {

/*        weatherCursorAdapter = new WeatherCursorAdapter(getContext(), null, 0);
        weatherListView.setAdapter(weatherCursorAdapter);*/

        //      initLoader();

/*        // --------------------------------------------------------------
        // IMPORTANT !!! Change loader for different query
        Loader loader = getLoaderManager().getLoader(MainActivity.LOADER_WEATHER_ID);

        if (loader != null && !loader.isReset()) {
            getLoaderManager().restartLoader(MainActivity.LOADER_WEATHER_ID, null, DetailsFragment.this);
        } else {
            getLoaderManager().initLoader(MainActivity.LOADER_WEATHER_ID, null, DetailsFragment.this);
        }
        // --------------------------------------------------------------*/

    }


    public void initLoader() {

        Log.d("AAAAA", "DetailsFragment: initLoader");

        // read entered city name and Data Update Time
        Cursor cursor = getActivity().getContentResolver()
                .query(Uri.parse(DataContentProvider.CITY_CONTENT_URI.toString() + "/" + mID),
                null, null, null, null);

/*        // read entered city name and Data Update Time
        Cursor cursor = getActivity().getContentResolver().query(DataContentProvider.CITY_CONTENT_URI,
                new String[]{DataContract.CityEntry.COLUMN_ENTERED_CITY,
                        DataContract.CityEntry.COLUMN_UPDATE_TIMESTAMP},
                DataContract.CityEntry._ID + "=?",
                new String[]{String.valueOf(mID)}, //  new String[]{enteredCity},
                null);*/

        if (cursor != null && cursor.getCount() > 0) {

            cursor.moveToFirst();

            // sort: by id = 0, alphabetic = 1
            mEnteredCity = cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY));

            // 0 = English, 1 = Russian
            mDataUpdateTime = cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_UPDATE_TIMESTAMP));

            cursor.close();
        }

        // Title
        titleTextView.setText(mEnteredCity);


        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            Date date = format.parse(mDataUpdateTime);

            String stringDate = DateFormat.getDateTimeInstance().format(date);

            updateTimeTextView.setText(getContext().getString(R.string.weather_data_obtained, stringDate));

        } catch (ParseException e) {
            Log.d("AAAAA", "Date ParseException: " + e.getMessage());
        }


        //  int unitsFormat = 0; // metric/Celsius units format by default

        // get settings data from DB
        MainActivity.Settings settings = activity.readSettingsFromDB();
        mUnitsFormat = settings.getUnitsFormat();
        // mSortCities = settings.getSortCities();


  /*      // read all columns
        Cursor cursor = getActivity().getContentResolver().query(DataContentProvider.SETTINGS_CONTENT_URI,
                new String[]{DataContract.SettingsEntry.COLUMN_UNITS_FORMAT},
                null, // DataContract.CityEntry.COLUMN_ENTERED_CITY + "=?",
                null, // new String[]{enteredCity},
                null);

        if (cursor != null) {

            if (cursor.getCount() > 0) { // has cities in DB

                cursor.moveToFirst();

                mUnitsFormat = cursor.getInt(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_UNITS_FORMAT));

                ///      Log.d("AAAAA", "readSettingsFromDB - mUnitsFormat = " + mUnitsFormat);

                //     titleTextView.setText(R.string.cities_title_cities_present);

            } else { // cities DB empty

                //     titleTextView.setText(R.string.cities_title_cities_empty);
            }
            cursor.close();
        } // if(cursor != null)*/

        // set units format
        WeatherCursorAdapter.setUnitsFormat(mUnitsFormat);

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


    public void syncCurrentCity() {

        ///    Log.d("AAAAA", "syncCurrentCity()");

        // run sync single time
        /// if (!((MainActivity)getActivity()).isMyServiceRunning(GetDataService.class)) {
        if (!activity.isMyServiceRunning(GetDataService.class)) {
            // -----------------------------------------------------------
            // get detailed data for ONE added city
            Intent intent = new Intent(getActivity(), GetDataService.class)
                    .putExtra(MainActivity.PARAM_TASK, MainActivity.TASK_GET_WEATHER_ONE_CITY) // get weather data for one city only!
                    .putExtra(MainActivity.PARAM_CITY_ID, mID) //  "CITIES" table: column "_id"
                    .putExtra(MainActivity.PARAM_ENTERED_CITY, mEnteredCity); // "CITIES" table: column "entered_city"

            // start service for added a city details and weather data
            getActivity().startService(intent);
            // -----------------------------------------------------------
        }
    }


    private void setModeBar(String text, int color, int image) {

        modePanelLayout.setVisibility(View.VISIBLE);
        modeTextView.setText(text);
        modeTextView.setTextColor(ContextCompat.getColor(getContext(), color));
        modeImageView.setImageResource(image);
    }
}
