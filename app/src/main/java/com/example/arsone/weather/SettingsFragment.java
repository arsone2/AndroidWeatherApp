package com.example.arsone.weather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;


public class SettingsFragment extends Fragment implements
        CompoundButton.OnCheckedChangeListener
 //       Spinner.OnItemSelectedListener
//        LoaderManager.LoaderCallbacks<Cursor>
{

    public interface Callbacks {

        void onSettingsChanged();

        MainActivity.Settings readSettingsFromDB();
    }

    private SettingsFragment.Callbacks activity;

    private ToggleButton mUnitsFormatToggleButton;
    private ToggleButton mSortCitiesToggleButton;
    //   private ToggleButton sendNotificationsToggleButton;
/*    private Spinner mNotifySpinner;
    private CheckBox mSendNotifyCheckBox;*/


    private int mUnitsFormat;
    private int mSortCities;
 ///   private int mSendNotificationsCityID;

 //   private NotifyCursorAdapter mNotifyCursorAdapter;


  /*  // ------------------------------------------------------------
    // LoaderManager.LoaderCallbacks methods
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        /// String sortOrder = DataContract.CityEntry._ID; // default sort order: _id
        String sortOrder = null;

        if (mSortCities == 0) {
            /// sortOrder = "ORDER BY c." + DataContract.CityEntry._ID + " DESC";
            sortOrder = DataContract.CityEntry._ID + " DESC";
        } else if (mSortCities == 1) {
///            sortOrder = "ORDER BY " + DataContract.CityEntry.COLUMN_ENTERED_CITY + " ASC";
            sortOrder = DataContract.CityEntry.COLUMN_ENTERED_CITY;
        }

        //   Log.d("AAAAA", "sortOrder = " + sortOrder);

        return new CursorLoader(getContext(),
                DataContentProvider.CITY_CONTENT_URI,
                new String[]{DataContract.CityEntry._ID, DataContract.CityEntry.COLUMN_ENTERED_CITY},
                null, //DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?", // selection: city ID
                null, // new String[]{ formattedDate }, // selectionArgs
                sortOrder // DataContract.WeatherEntry._ID // sort order
        );
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        int position = 0;
        boolean found = false;

        if (cursor.getCount() > 0) {

            cursor.moveToFirst();

            while (!cursor.isAfterLast()) {

                int id = cursor.getInt(cursor.getColumnIndex(DataContract.CityEntry._ID));

                if (mSendNotificationsCityID == id) {

                    found = true;
              //      Log.d("AAAAA", "setSelection found !!!!! - mSendNotificationsCityID = " + mSendNotificationsCityID);
                    break;
                }
                position++;
                cursor.moveToNext();
            }
        }

     //   Log.d("AAAAA", "position = " + position);

        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        mNotifyCursorAdapter.swapCursor(cursor);

        if (found) {

            mSendNotifyCheckBox.setChecked(true);

            // set spinner selection
            mNotifySpinner.setSelection(position);

        } else {

            mSendNotifyCheckBox.setChecked(false);

            // set spinner selection
            mNotifySpinner.setSelection(0);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        mNotifyCursorAdapter.swapCursor(null);
    }
    // ------------------------------------------------------------*/


    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        activity = (SettingsFragment.Callbacks) context;
    }


    @Override
    public void onDetach() {

        super.onDetach();
        activity = null;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // get settings data from DB
        MainActivity.Settings settings = activity.readSettingsFromDB();

        mUnitsFormat = settings.getUnitsFormat();
        mSortCities = settings.getSortCities();
   ///     mSendNotificationsCityID = settings.getSendNotifications();

   //     Log.d("AAAAA", "onCreateView - mSendNotificationsCityID = " + mSendNotificationsCityID);

        mUnitsFormatToggleButton = (ToggleButton) view.findViewById(R.id.unitsFormatToggleButton);
        mUnitsFormatToggleButton.setChecked(mUnitsFormat == 1);
        mUnitsFormatToggleButton.setOnCheckedChangeListener(this);

        mSortCitiesToggleButton = (ToggleButton) view.findViewById(R.id.sortCitiesToggleButton);
        mSortCitiesToggleButton.setChecked(mSortCities == 1);
        mSortCitiesToggleButton.setOnCheckedChangeListener(this);

/*        mSendNotifyCheckBox = (CheckBox) view.findViewById(R.id.sendNotifyCheckBox);
        mSendNotifyCheckBox.setOnCheckedChangeListener(this);

        mNotifySpinner = (Spinner) view.findViewById(R.id.notifySpinner);
        mNotifySpinner.setOnItemSelectedListener(this);

        mNotifyCursorAdapter = new NotifyCursorAdapter(getContext(), null, 0);
        mNotifySpinner.setAdapter(mNotifyCursorAdapter);*/

 ///       initLoader();

/*        sendNotificationsToggleButton = (ToggleButton) view.findViewById(R.id.sendNotificationsToggleButton);
        sendNotificationsToggleButton.setChecked(mSendNotifications == 1);
        sendNotificationsToggleButton.setOnCheckedChangeListener(this);*/

        return view;
    }


/*    public void initLoader() {

        //     Log.d("AAAAA", "SettingsFragment - initLoader");

        // get settings data from DB
        //       MainActivity.Settings settings = activity.readSettingsFromDB();
//        mUnitsFormat = settings.getUnitsFormat();
//        mSortCities = settings.getSortCities();

        // --------------------------------------------------------------
        // IMPORTANT !!! Change loader for different query
        Loader loader = getLoaderManager().getLoader(MainActivity.LOADER_CITIES_SETTINGS);

        if (loader != null && !loader.isReset()) {
            getLoaderManager().restartLoader(MainActivity.LOADER_CITIES_SETTINGS, null, this);
            //   Log.d("AAAAA", "SettingsFragment() - restartLoader");
        } else {
            getLoaderManager().initLoader(MainActivity.LOADER_CITIES_SETTINGS, null, this);
            //   Log.d("AAAAA", "SettingsFragment() - initLoader");
        }
        // --------------------------------------------------------------
    }*/


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

          writeSettingsToDB();

/*        switch (buttonView.getId()) {

            case R.id.sendNotifyCheckBox:

                if (isChecked) {
                    mNotifySpinner.setVisibility(View.VISIBLE);
//                    writeSettingsToDB();
                } else {
                    mNotifySpinner.setVisibility(View.GONE);
                }

                writeSettingsToDB();

       } else{

            writeSettingsToDB();
        }
                break;
        }*/
    }


/*    @Override
    public void onPause() {
        super.onPause();

        writeSettingsToDB();

    }*/


    private void writeSettingsToDB() {


        ContentValues values = new ContentValues();

        // units format: metric = 0, imperial = 1
        values.put(DataContract.SettingsEntry.COLUMN_UNITS_FORMAT, mUnitsFormatToggleButton.isChecked() ? 1 : 0);

        // sort: by id = 0, alphabetic = 1
        values.put(DataContract.SettingsEntry.COLUMN_SORT_CITIES, mSortCitiesToggleButton.isChecked() ? 1 : 0);

/*        if (mSendNotifyCheckBox.isChecked()) {

      //      Log.d("AAAAA", "writeSettingsToDB - mSendNotifyCheckBox.isChecked() = " + mSendNotificationsCityID);
            // send notifications:
            values.put(DataContract.SettingsEntry.COLUMN_NOTIFY_CITY_ID, mSendNotificationsCityID);
        } else {
        //    Log.d("AAAAA", "writeSettingsToDB - mSendNotifyCheckBox.isChecked() = " + "-1");
            values.put(DataContract.SettingsEntry.COLUMN_NOTIFY_CITY_ID, 0); // nothing selected
        }*/

        getActivity().getContentResolver().update(DataContentProvider.SETTINGS_CONTENT_URI, values, null, null);

        activity.onSettingsChanged();
    }


/*    // Spinner: onItemSelected
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

*//*        switch (parent.getId()) {

            case R.id.notifySpinner:

                Cursor cursor = (Cursor) parent.getItemAtPosition(position);

         //       mSendNotificationsCityID = cursor.getInt(cursor.getColumnIndex(DataContract.SettingsEntry._ID));

          //      Log.d("AAAAA", "notifySpinner: onItemSelected - mSendNotificationsCityID = " + mSendNotificationsCityID);

                writeSettingsToDB();

                break;

        }*//*

        //Log.d("AAAAA", "onItemSelected - position = " + position);

        writeSettingsToDB();
    }*/

/*
    // Spinner: onNothingSelected
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }*/


 /*   public class NotifyCursorAdapter extends CursorAdapter {


        public class ViewHolder {

            public int id;
            public TextView cityTextView;
        }

        private LayoutInflater layoutInflater;


        public NotifyCursorAdapter(Context context, Cursor c, int flags) {
            super(context, c, flags);

            layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }


        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {

//                Log.d("AAAAA", "cursor.getPosition() = " + cursor.getPosition());

            View view = layoutInflater.inflate(R.layout.list_item_city_spinner, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.cityTextView = (TextView) view.findViewById(R.id.cityTextView);
            view.setTag(viewHolder);
            return view;
        }


        @Override
        public void bindView(View view, Context context, Cursor cursor) {

            ViewHolder viewHolder = (ViewHolder) view.getTag();
            int cityColumnIndex = cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY);
            String city = cursor.getString(cityColumnIndex);
            viewHolder.cityTextView.setText(city);
        }
    }*/
}