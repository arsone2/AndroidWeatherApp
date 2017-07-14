package com.example.arsone.weather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.ToggleButton;


public class SettingsFragment extends Fragment implements
        CompoundButton.OnCheckedChangeListener,
        Spinner.OnItemSelectedListener
{


    public interface Callbacks {

        void onSettingsChanged();

        MainActivity.Settings readSettingsFromDB();
    }

    private SettingsFragment.Callbacks activity;

    private ToggleButton unitsFormatToggleButton;
    private ToggleButton sortCitiesToggleButton;
 //   private Spinner mapStyleSpinner;
 //   private Spinner mapLanguageSpinner;

    private int mUnitsFormat;
    private int mSortCities;
 //   private int mMapStyleIndex;
    private int mMapLanguageIndex;


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


    @Override
    public void onPause() {

        super.onPause();

        // transmit data to mainActivity to save it in DB
     //   activity.writeSettingsToDB(unitsFormatToggleButton.isChecked() ? 1 : 0);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

/*        Bundle bundle = getArguments();

        if (bundle != null) {
            mUnitsFormat = bundle.getInt(MainActivity.UNITS_FORMAT);
        }*/

     ///   Log.d("AAAAA", "readSettingsFromDB");

 /*       // read all settings from DB
        Cursor cursor = getActivity().getContentResolver().query(DataContentProvider.SETTINGS_CONTENT_URI,
                new String[]{ DataContract.SettingsEntry.COLUMN_UNITS_FORMAT,
                              DataContract.SettingsEntry.COLUMN_SORT_CITIES },
                null, // DataContract.CityEntry.COLUMN_ENTERED_CITY + "=?",
                null, // new String[]{enteredCity},
                null);

        if (cursor != null && cursor.getCount() > 0) {

            cursor.moveToFirst();

            mUnitsFormat = cursor.getInt(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_UNITS_FORMAT));
            mSortCities = cursor.getInt(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_SORT_CITIES));
            cursor.close();

        ///    Log.d("AAAAA", "SettingsFragment: readSettingsFromDB - mUnitsFormat = " + mUnitsFormat);
        }*/

        // get settings data from DB
        MainActivity.Settings settings = activity.readSettingsFromDB();

        mUnitsFormat = settings.getUnitsFormat();
        mSortCities = settings.getSortCities();
  //      mMapStyleIndex = settings.getMapStyleIndex();
   //     mMapLanguageIndex = settings.getMapLanguageIndex();

        unitsFormatToggleButton = (ToggleButton) view.findViewById(R.id.unitsFormatToggleButton);
        unitsFormatToggleButton.setChecked(mUnitsFormat == 1);
        unitsFormatToggleButton.setOnCheckedChangeListener(this);

        sortCitiesToggleButton = (ToggleButton) view.findViewById(R.id.sortCitiesToggleButton);
        sortCitiesToggleButton.setChecked(mSortCities == 1);
        sortCitiesToggleButton.setOnCheckedChangeListener(this);

/*        mapStyleSpinner = (Spinner) view.findViewById(R.id.mapStyleSpinner);
        mapStyleSpinner.setOnItemSelectedListener(this);
        mapStyleSpinner.setSelection(mMapStyleIndex);*/

/*        mapLanguageSpinner = (Spinner) view.findViewById(R.id.mapLanguageSpinner);
        mapLanguageSpinner.setOnItemSelectedListener(this);
        mapLanguageSpinner.setSelection(mMapLanguageIndex);*/

        return view;
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        writeSettingsToDB();
    }


    private void writeSettingsToDB(){

        ContentValues values = new ContentValues();

        // units format: metric = 0, imperial = 1
        values.put(DataContract.SettingsEntry.COLUMN_UNITS_FORMAT, unitsFormatToggleButton.isChecked() ? 1 : 0);

        // sort: by id = 0, alphabetic = 1
        values.put(DataContract.SettingsEntry.COLUMN_SORT_CITIES, sortCitiesToggleButton.isChecked() ? 1 : 0);

     //   Log.d("AAAAA", "mapStyleSpinner.getSelectedItemPosition() = " + mapStyleSpinner.getSelectedItemPosition());
      //  Log.d("AAAAA", "mapLanguageSpinner.getSelectedItemPosition() = " + mapLanguageSpinner.getSelectedItemPosition());

    //    values.put(DataContract.SettingsEntry.COLUMN_MAP_STYLE, mapStyleSpinner.getSelectedItemPosition());

   //     values.put(DataContract.SettingsEntry.COLUMN_MAP_LANGUAGE, mapLanguageSpinner.getSelectedItemPosition());

        getActivity().getContentResolver().update(DataContentProvider.SETTINGS_CONTENT_URI, values, null, null);

     ///   Log.d("AAAAA", "writeSettingsToDB(): updatedRowsCount = " + updatedRowsCount);

        activity.onSettingsChanged();
    }


    // Spinner: onItemSelected
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

     //   Log.d("AAAAA", "onItemSelected - position = " + position);

        writeSettingsToDB();

       /* switch (view.getId()){

            case R.id.mapLanguageSpinner:

                Log.d("AAAAA", "onItemSelected - mapLanguageSpinner position = " + position);

                writeSettingsToDB();

                break;

            case R.id.mapStyleSpinner:

                Log.d("AAAAA", "onItemSelected - mapStyleSpinner position = " + position);
                writeSettingsToDB();

                break;
        }*/
    }


    // Spinner: onNothingSelected
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}