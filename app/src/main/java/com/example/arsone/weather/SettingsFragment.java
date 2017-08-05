package com.example.arsone.weather;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;


public class SettingsFragment extends Fragment implements
        CompoundButton.OnCheckedChangeListener {

    public interface Callbacks {

        void onSettingsChanged();

        MainActivity.Settings readSettingsFromDB();
    }

    private SettingsFragment.Callbacks activity;

    private ToggleButton mUnitsFormatToggleButton;
    private ToggleButton mSortCitiesToggleButton;

    private int mUnitsFormat;
    private int mSortCities;


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

        mUnitsFormatToggleButton = (ToggleButton) view.findViewById(R.id.unitsFormatToggleButton);
        mUnitsFormatToggleButton.setChecked(mUnitsFormat == 1);
        mUnitsFormatToggleButton.setOnCheckedChangeListener(this);

        mSortCitiesToggleButton = (ToggleButton) view.findViewById(R.id.sortCitiesToggleButton);
        mSortCitiesToggleButton.setChecked(mSortCities == 1);
        mSortCitiesToggleButton.setOnCheckedChangeListener(this);

        return view;
    }


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

          writeSettingsToDB();
    }


    private void writeSettingsToDB() {

        ContentValues values = new ContentValues();

        // units format: metric = 0, imperial = 1
        values.put(DataContract.SettingsEntry.COLUMN_UNITS_FORMAT, mUnitsFormatToggleButton.isChecked() ? 1 : 0);

        // sort: by id = 0, alphabetic = 1
        values.put(DataContract.SettingsEntry.COLUMN_SORT_CITIES, mSortCitiesToggleButton.isChecked() ? 1 : 0);

        getActivity().getContentResolver().update(DataContentProvider.SETTINGS_CONTENT_URI, values, null, null);

        activity.onSettingsChanged();
    }
}