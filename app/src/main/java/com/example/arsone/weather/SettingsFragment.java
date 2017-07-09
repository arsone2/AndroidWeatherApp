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
import android.widget.CompoundButton;
import android.widget.ToggleButton;


public class SettingsFragment extends Fragment implements
        CompoundButton.OnCheckedChangeListener {

    public interface Callbacks {

        void onSettingsChanged();
    }

    private SettingsFragment.Callbacks activity;

    private ToggleButton unitsFormatToggleButton;

    private int mUnitsFormat;


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

        // select all settings frpm DB
        Cursor cursor = getActivity().getContentResolver().query(DataContentProvider.SETTINGS_CONTENT_URI,
                new String[]{DataContract.SettingsEntry.COLUMN_UNITS_FORMAT},
                null, // DataContract.CityEntry.COLUMN_ENTERED_CITY + "=?",
                null, // new String[]{enteredCity},
                null);

        if (cursor != null && cursor.getCount() > 0) {

            cursor.moveToFirst();

            mUnitsFormat = cursor.getInt(cursor.getColumnIndex(DataContract.SettingsEntry.COLUMN_UNITS_FORMAT));
            cursor.close();

        ///    Log.d("AAAAA", "SettingsFragment: readSettingsFromDB - mUnitsFormat = " + mUnitsFormat);
        }

        unitsFormatToggleButton = (ToggleButton) view.findViewById(R.id.unitsFormatToggleButton);
        unitsFormatToggleButton.setChecked(mUnitsFormat == 1);
        unitsFormatToggleButton.setOnCheckedChangeListener(this);


        return view;
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

        /// Log.d("AAAAA", "unitsFormatToggleButton.isChecked() ? 1 : 0 = " + (unitsFormatToggleButton.isChecked() ? 1 : 0));
        // transmit data to mainActivity to save it in DB
       ///  activity.writeSettingsToDB(unitsFormatToggleButton.isChecked() ? 1 : 0);

        writeSettingsToDB();
    }


    private void writeSettingsToDB(){

        ContentValues values = new ContentValues();

        values.put(DataContract.SettingsEntry.COLUMN_UNITS_FORMAT, unitsFormatToggleButton.isChecked() ? 1 : 0);

        int updatedRowsCount = getActivity().getContentResolver()
                .update(DataContentProvider.SETTINGS_CONTENT_URI, values, null, null);

     ///   Log.d("AAAAA", "writeSettingsToDB(): updatedRowsCount = " + updatedRowsCount);

        activity.onSettingsChanged();
    }
}