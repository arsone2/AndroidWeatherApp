package com.example.arsone.weather;

import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

// SOLUTIONS !!
// https://stackoverflow.com/questions/22022216/inflating-multiple-views-from-cursoradapter?noredirect=1&lq=1
// https://stackoverflow.com/questions/8479833/cursoradapter-with-different-row-layouts
// http://android.amberfog.com/?p=296
// https://stackoverflow.com/questions/30751155/change-row-color-of-a-listview-with-cursoradapter-and-bindview?rq=1
// http://www.vogella.com/tutorials/AndroidListView/article.html#adapterown


public class CityCursorAdapter extends CursorAdapter {

    private LayoutInflater layoutInflater;

    private static boolean checkboxVisibility = false;

    private static SparseBooleanArray selectedItemsArray;

    private static int mUnitsFormat;

    public class ViewHolder {

        public int id;
        public TextView enteredCityTextView;
        public CheckBox checkboxForDelete;

        public ImageView conditionImageView;
        public TextView weatherTextView;
        public TextView dayTempTextView;
        public TextView tempUnitTextView;
    }

    // constructor
    public CityCursorAdapter(Context context, Cursor c, int flags) {

        super(context, c, flags);
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedItemsArray = new SparseBooleanArray();
    }


    public static void setCheckboxesVisibility(boolean visibility) {

        checkboxVisibility = visibility;
    }


    public static void setUnitsFormat(int unitsFormat) {

        mUnitsFormat = unitsFormat;
    }


    public static void uncheckAllItems() {

        selectedItemsArray.clear();
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = layoutInflater.inflate(R.layout.list_item_city, parent, false);
        ViewHolder viewHolder = new ViewHolder();

        viewHolder.enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);
        viewHolder.checkboxForDelete = (CheckBox) view.findViewById(R.id.checkboxForDelete);
        viewHolder.conditionImageView = (ImageView) view.findViewById(R.id.conditionImageView);
        viewHolder.weatherTextView = (TextView) view.findViewById(R.id.weatherTextView);
        viewHolder.dayTempTextView = (TextView) view.findViewById(R.id.dayTempTextView);
        viewHolder.tempUnitTextView = (TextView) view.findViewById(R.id.tempUnitTextView);

        view.setTag(viewHolder);

        return view;
    }


    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        int returnedCityColumnIndex = cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY);
        String returnedCity = cursor.getString(returnedCityColumnIndex);

        int enteredCityColumnIndex = cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY);
        String enteredCity = cursor.getString(enteredCityColumnIndex);

        if (returnedCity != null) { // full

            viewHolder.enteredCityTextView
                    .setText(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)));

            viewHolder.conditionImageView.setBackgroundResource(context.getResources().
                    getIdentifier("_" + cursor.getString(cursor
                                    .getColumnIndex(DataContract.WeatherEntry.COLUMN_ICON_NAME)),
                            "drawable", context.getPackageName()));

            viewHolder.weatherTextView.setText(cursor.getString(cursor
                    .getColumnIndex(DataContract.WeatherEntry.COLUMN_DESCRIPTION)));

            if (mUnitsFormat == 0) { // metric = Celsius

                viewHolder.tempUnitTextView.setText("\u00B0C"); // Celsius sign
                viewHolder.dayTempTextView.setText(String.valueOf(cursor.getInt(cursor
                        .getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP))));

            } else if (mUnitsFormat == 1) { // imperial == Fahrenheit

                viewHolder.tempUnitTextView.setText("\u2109"); // Fahrenheit sign
                int dayTemp = cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP));
                viewHolder.dayTempTextView.setText(String.valueOf(dayTemp * 9 / 5 + 32)); // Celsius to Fahrenheit
            }

            viewHolder.dayTempTextView.setVisibility(View.VISIBLE);
            viewHolder.tempUnitTextView.setVisibility(View.VISIBLE);
            viewHolder.weatherTextView.setVisibility(View.VISIBLE);
            viewHolder.conditionImageView.setVisibility(View.VISIBLE);

        } else {

            viewHolder.enteredCityTextView
                    .setText(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)));

            viewHolder.tempUnitTextView.setText(R.string.data_not_found);

            viewHolder.dayTempTextView.setVisibility(View.GONE);
            viewHolder.weatherTextView.setVisibility(View.GONE);
            viewHolder.conditionImageView.setVisibility(View.GONE);
            viewHolder.dayTempTextView.setVisibility(View.GONE);
        }

        final int position = cursor.getPosition();
        int id = cursor.getInt(cursor.getColumnIndex(DataContract.CityEntry._ID));
        viewHolder.checkboxForDelete.setTag(id);

        viewHolder.checkboxForDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (viewHolder.checkboxForDelete.isChecked()) {

                    selectedItemsArray.put(position, true);

                } else if (!viewHolder.checkboxForDelete.isChecked()) {

                    selectedItemsArray.put(position, false);
                }
            }
        });

        viewHolder.checkboxForDelete.setChecked(selectedItemsArray.get(position));

        // set checkboxes visibility
        if (checkboxVisibility)
            viewHolder.checkboxForDelete.setVisibility(View.VISIBLE);
        else
            viewHolder.checkboxForDelete.setVisibility(View.GONE);
    }
}