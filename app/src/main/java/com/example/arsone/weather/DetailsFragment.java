package com.example.arsone.weather;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class DetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public interface Callbacks {

        boolean isMyServiceRunning(Class<?> serviceClass);

        MainActivity.Settings readSettingsFromDB();
    }

    private DetailsFragment.Callbacks activity;

    // Fragment view title
    private TextView titleTextView;
    private TextView updateTimeTextView;

    // weather ListView
    private ListView weatherListView;

    private WeatherCursorAdapter weatherCursorAdapter;

    // selected city database table _id
    private int mID;

    // selected city name
    private String mEnteredCity;

    private int mUnitsFormat;

    private String mDataUpdateTime;


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

        return new CursorLoader(getContext(),
                Uri.parse(DataContentProvider.WEATHER_CONTENT_URI.toString() + "/" + mID),
                null,
                null,// DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?", // selection: city ID
                null, // new String[]{ formattedDate }, // selectionArgs
                null // DataContract.WeatherEntry._ID // sort order
        );
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        weatherCursorAdapter.swapCursor(cursor);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        weatherCursorAdapter.swapCursor(null);
    }
    // ---------------------------------------------------------------


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_details, container, false);

        Bundle bundle = getArguments();

        if (bundle != null) {
            mID = bundle.getInt(MainActivity.CITY_ID);
            mEnteredCity = bundle.getString(MainActivity.ENTERED_CITY);
            mUnitsFormat = bundle.getInt(MainActivity.UNITS_FORMAT);
            mDataUpdateTime = bundle.getString(MainActivity.UPDATE_TIME);
        }

        // Title
        titleTextView = (TextView) view.findViewById(R.id.titleTextView);
        updateTimeTextView = (TextView) view.findViewById(R.id.updateTimeTextView);

        // weather ListView
        weatherListView = (ListView) view.findViewById(R.id.detailsListView);

        weatherCursorAdapter = new WeatherCursorAdapter(getContext(), null, 0);
        weatherListView.setAdapter(weatherCursorAdapter);

        initLoader();

        return view;
    }


    public void initLoader() {

        Log.d("AAAAA", "DetailsFragment: initLoader");

        // read entered city name and Data Update Time
        Cursor cursor = getActivity().getContentResolver()
                .query(Uri.parse(DataContentProvider.CITY_CONTENT_URI.toString() + "/" + mID),
                        null, null, null, null);

        String returnedName = null;

        if (cursor != null && cursor.getCount() > 0) {

            cursor.moveToFirst();

            // sort: by id = 0, alphabetic = 1
            mEnteredCity = cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY));

            // 0 = English, 1 = Russian
            mDataUpdateTime = cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_UPDATE_TIMESTAMP));

            returnedName = cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY));

            cursor.close();
        }

        // Title
        titleTextView.setText(mEnteredCity);

        if (TextUtils.isEmpty(returnedName)) {

            updateTimeTextView.setText(R.string.data_not_found);

        } else {

            titleTextView.setText(mEnteredCity);

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            try {
                Date date = format.parse(mDataUpdateTime);

                String stringDate = DateFormat.getDateTimeInstance().format(date);

                updateTimeTextView.setText(getContext().getString(R.string.weather_data_obtained, stringDate));

            } catch (ParseException e) {
                Log.d("AAAAA", "Date ParseException: " + e.getMessage());
            }
        }

        //  int unitsFormat = 0; // metric/Celsius units format by default
        // get settings data from DB
        MainActivity.Settings settings = activity.readSettingsFromDB();

        mUnitsFormat = settings.getUnitsFormat();

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

        // run sync single time
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
}