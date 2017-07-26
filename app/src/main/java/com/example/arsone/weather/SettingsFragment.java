package com.example.arsone.weather;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
    private ToggleButton sendNotificationsToggleButton;

    private int mUnitsFormat;
    private int mSortCities;
    private int mSendNotifications;


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
        mSendNotifications = settings.getSendNotifications();

        unitsFormatToggleButton = (ToggleButton) view.findViewById(R.id.unitsFormatToggleButton);
        unitsFormatToggleButton.setChecked(mUnitsFormat == 1);
        unitsFormatToggleButton.setOnCheckedChangeListener(this);

        sortCitiesToggleButton = (ToggleButton) view.findViewById(R.id.sortCitiesToggleButton);
        sortCitiesToggleButton.setChecked(mSortCities == 1);
        sortCitiesToggleButton.setOnCheckedChangeListener(this);

        sendNotificationsToggleButton = (ToggleButton) view.findViewById(R.id.sendNotificationsToggleButton);
        sendNotificationsToggleButton.setChecked(mSendNotifications == 1);
        sendNotificationsToggleButton.setOnCheckedChangeListener(this);

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

        // send notifications: 0 = no, 1 = yes
        values.put(DataContract.SettingsEntry.COLUMN_SEND_NOTIFY, sendNotificationsToggleButton.isChecked() ? 1 : 0);

        getActivity().getContentResolver().update(DataContentProvider.SETTINGS_CONTENT_URI, values, null, null);

        activity.onSettingsChanged();
    }


    // Spinner: onItemSelected
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

     //   Log.d("AAAAA", "onItemSelected - position = " + position);

        writeSettingsToDB();
    }


    // Spinner: onNothingSelected
    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}