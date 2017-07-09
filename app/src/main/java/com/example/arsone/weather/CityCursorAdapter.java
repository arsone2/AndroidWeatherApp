package com.example.arsone.weather;


import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class CityCursorAdapter extends CursorAdapter {

    private Context context;

    private LayoutInflater layoutInflater;

    private static boolean checkboxVisibility = false;

    private static SparseBooleanArray selectedItemsArray;

    private static int mUnitsFormat;


    public static class ViewHolder {

        public int id;
        public TextView enteredCityTextView;
    //    public TextView returnedNameTextView;
        public CheckBox checkboxForDelete;

        public ImageView conditionImageView;
        public TextView weatherTextView;
        public TextView dayTempTextView;
        public TextView tempUnitTextView;

    }

 //   private int mUnitsFormat;

    // constructor
    public CityCursorAdapter(Context context, Cursor c, int flags) {

        super(context, c, flags);
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedItemsArray = new SparseBooleanArray();
      //  mUnitsFormat = unitsFormat;
    }


    public static void setCheckboxesVisibility(boolean visibility) {

        checkboxVisibility = visibility;
    }


    public static void setUnitsFormat(int unitsFormat){

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
     //   holder.returnedNameTextView = (TextView) view.findViewById(R.id.returnedNameTextView);
        viewHolder.checkboxForDelete = (CheckBox) view.findViewById(R.id.checkboxForDelete);

        viewHolder.conditionImageView = (ImageView)view.findViewById(R.id.conditionImageView);
        viewHolder.weatherTextView = (TextView)view.findViewById(R.id.weatherTextView);
        viewHolder.dayTempTextView = (TextView)view.findViewById(R.id.dayTempTextView);
        viewHolder.tempUnitTextView = (TextView)view.findViewById(R.id.tempUnitTextView);

        view.setTag(viewHolder);

        return view;
    }


    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        final int position = cursor.getPosition();

        int id = cursor.getInt(cursor.getColumnIndex(DataContract.CityEntry._ID));

        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.enteredCityTextView
                .setText(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)));

        viewHolder.conditionImageView.setBackgroundResource(context.getResources().
                getIdentifier("_" + cursor.getString(cursor
                                .getColumnIndex(DataContract.WeatherEntry.COLUMN_ICON_NAME)),
                        "drawable", context.getPackageName()));

        viewHolder.weatherTextView.setText(cursor.getString(cursor
                        .getColumnIndex(DataContract.WeatherEntry.COLUMN_DESCRIPTION)));

        if(mUnitsFormat == 0) { // metric = Celsius

            viewHolder.tempUnitTextView.setText("\u00B0C"); // Celsius sign

            viewHolder.dayTempTextView.setText(String.valueOf(cursor.getInt(cursor
                    .getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP))));

      //      Log.d("AAAAA", "Celsius");

        } else if(mUnitsFormat == 1) { // imperial == Fahrenheit

       //     Log.d("AAAAA", "Fahrenheit");

            viewHolder.tempUnitTextView.setText("\u2109"); // Fahrenheit sign

            int dayTemp = cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP));

            viewHolder.dayTempTextView.setText(String.valueOf(dayTemp * 9 / 5 + 32)); // Celsius to Fahrenheit
        }

        // T(°F) = T(°C) × 9/5 + 32



/*        holder.dayTempTextView.setText(String.valueOf(cursor.getInt(cursor
                .getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP))));*/


/*
        holder.dayTempTextView
                .setText(context.getString(R.string.weather_day,
                        String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP)))));
*/
        // + " \u00B0C"));


/*        String returnedCity = cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY));

        if (!TextUtils.isEmpty(returnedCity))
            holder.returnedNameTextView.setText("/ " + returnedCity);
            ///holder.returnedNameTextView.setText(context.getString(R.string.returned_city_format, returnedCity));
        else
            holder.returnedNameTextView.setText(returnedCity);*/

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

        viewHolder.checkboxForDelete.setChecked(selectedItemsArray.get(cursor.getPosition()));

        if (checkboxVisibility)
            viewHolder.checkboxForDelete.setVisibility(View.VISIBLE);
        else
            viewHolder.checkboxForDelete.setVisibility(View.GONE);
    }
}