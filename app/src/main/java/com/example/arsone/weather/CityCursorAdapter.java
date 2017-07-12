package com.example.arsone.weather;


import android.content.Context;
import android.database.Cursor;
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


    public class ViewHolder {

        public int id;
        public TextView enteredCityTextView;
        //     public TextView returnedNameTextView;
        public CheckBox checkboxForDelete;

        public ImageView conditionImageView;
        public TextView weatherTextView;
        public TextView dayTempTextView;
        public TextView tempUnitTextView;

        //     public int layoutType;

//        public LinearLayout weatherInfoLinearLayout;

    }


    public class ViewHolderFull {

        public int id;
        public TextView enteredCityTextView;
        public CheckBox checkboxForDelete;
        public ImageView conditionImageView;
        public TextView weatherTextView;
        public TextView dayTempTextView;
        public TextView tempUnitTextView;
    }


    public static class ViewHolderEmpty {

        public int id;
        public TextView enteredCityTextView;
        public CheckBox checkboxForDelete;

/*        public ImageView conditionImageView;
        public TextView weatherTextView;
        public TextView dayTempTextView;
        public TextView tempUnitTextView;*/
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


    public static void setUnitsFormat(int unitsFormat) {

        mUnitsFormat = unitsFormat;
    }


    public static void uncheckAllItems() {

        selectedItemsArray.clear();
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

//         Log.d("AAAAA", "newView()");


/*        int returnedCityColumnIndex = cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY);
        String returnedCity = cursor.getString(returnedCityColumnIndex);

        int enteredCityColumnIndex = cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY);
        String enteredCity = cursor.getString(enteredCityColumnIndex);*/

        //  int position = getItemViewType(cursor);
        //     int type = getItemViewType(position);

/*        Log.d("AAAAA", "newView: position = " + cursor.getPosition() + ", returnedCity = " + returnedCity
                + ", enteredCity = " + enteredCity);*/

        //    if(returnedCity != null) {

        View view = layoutInflater.inflate(R.layout.list_item_city, parent, false);
        ViewHolder viewHolder = new ViewHolder();

        viewHolder.enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);
        //   viewHolder.returnedNameTextView = (TextView) view.findViewById(R.id.returnedNameTextView);
        viewHolder.checkboxForDelete = (CheckBox) view.findViewById(R.id.checkboxForDelete);
        viewHolder.conditionImageView = (ImageView) view.findViewById(R.id.conditionImageView);
        viewHolder.weatherTextView = (TextView) view.findViewById(R.id.weatherTextView);
        viewHolder.dayTempTextView = (TextView) view.findViewById(R.id.dayTempTextView);
        viewHolder.tempUnitTextView = (TextView) view.findViewById(R.id.tempUnitTextView);


/*
            viewHolder.enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);
            viewHolder.conditionImageView = (ImageView) view.findViewById(R.id.conditionImageView);
*/

/*
        } else {

                view = layoutInflater.inflate(R.layout.list_item_city_short, parent, false);

                viewHolder.enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);
                viewHolder.checkboxForDelete = (CheckBox) view.findViewById(R.id.checkboxForDelete);

        }
*/

        view.setTag(viewHolder);

        //    convertView.setTag(viewHolder);

/*
        int columnIndex = cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY);
        String returnedCity = cursor.getString(columnIndex);

        if (returnedCity != null) { // full data

            view = layoutInflater.inflate(R.layout.list_item_city, parent, false);

            viewHolder.enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);
            //   holder.returnedNameTextView = (TextView) view.findViewById(R.id.returnedNameTextView);
            viewHolder.checkboxForDelete = (CheckBox) view.findViewById(R.id.checkboxForDelete);
            viewHolder.conditionImageView = (ImageView) view.findViewById(R.id.conditionImageView);
            viewHolder.weatherTextView = (TextView) view.findViewById(R.id.weatherTextView);
            viewHolder.dayTempTextView = (TextView) view.findViewById(R.id.dayTempTextView);
            viewHolder.tempUnitTextView = (TextView) view.findViewById(R.id.tempUnitTextView);

        } else { // short data

            view = layoutInflater.inflate(R.layout.list_item_city_short, parent, false);

            viewHolder.enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);
            viewHolder.checkboxForDelete = (CheckBox) view.findViewById(R.id.checkboxForDelete);
        }


        view.setTag(viewHolder);*/





 /*       if (returnedCity != null){ // full

            Log.d("AAAAA" , "FULL: returnedCity = " + returnedCity + ", entered_city = " +
                    cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)));

            view = layoutInflater.inflate(R.layout.list_item_city, parent, false);
           ViewHolderFull viewHolderFull = new ViewHolderFull();

      //      viewHolderFull.enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);

      //      viewHolderFull.conditionImageView = (ImageView) view.findViewById(R.id.conditionImageView);

            view.setTag(viewHolderFull);

        } else { // empty

            Log.d("AAAAA" , "EMPTY: returnedCity = " + returnedCity + ", entered_city = " +
                    cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)));

            view = layoutInflater.inflate(R.layout.list_item_city_empty, parent, false);
           ViewHolderEmpty viewHolderEmpty = new ViewHolderEmpty();

      //      viewHolderEmpty.enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);

            view.setTag(viewHolderEmpty);
        }*/



/*//        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // set different layouts for rows
        /// if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY)))) {
        if (returnedCity != null) {

            view = layoutInflater.inflate(R.layout.list_item_city, parent, false);
           // viewHolder.layoutType = 0;

        } else {

            view = layoutInflater.inflate(R.layout.list_item_city_empty, parent, false);
        //    viewHolder.layoutType = 1;
        }

        viewHolder.enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);
        //   holder.returnedNameTextView = (TextView) view.findViewById(R.id.returnedNameTextView);
        viewHolder.checkboxForDelete = (CheckBox) view.findViewById(R.id.checkboxForDelete);
        viewHolder.conditionImageView = (ImageView) view.findViewById(R.id.conditionImageView);
        viewHolder.weatherTextView = (TextView) view.findViewById(R.id.weatherTextView);
        viewHolder.dayTempTextView = (TextView) view.findViewById(R.id.dayTempTextView);
        viewHolder.tempUnitTextView = (TextView) view.findViewById(R.id.tempUnitTextView);*/




/*        // set different layouts for rows
        if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY)))) {

            view = layoutInflater.inflate(R.layout.list_item_city, parent, false);

            viewHolder.enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);
            //   holder.returnedNameTextView = (TextView) view.findViewById(R.id.returnedNameTextView);
            viewHolder.checkboxForDelete = (CheckBox) view.findViewById(R.id.checkboxForDelete);
            viewHolder.conditionImageView = (ImageView) view.findViewById(R.id.conditionImageView);
            viewHolder.weatherTextView = (TextView) view.findViewById(R.id.weatherTextView);
            viewHolder.dayTempTextView = (TextView) view.findViewById(R.id.dayTempTextView);
            viewHolder.tempUnitTextView = (TextView) view.findViewById(R.id.tempUnitTextView);
            //    viewHolder.weatherInfoLinearLayout = (LinearLayout) view.findViewById(R.id.weather_info);

        } else {

            view = layoutInflater.inflate(R.layout.list_item_city_empty, parent, false);

            viewHolder.enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);
            viewHolder.checkboxForDelete = (CheckBox) view.findViewById(R.id.checkboxForDelete);
        }*/

/*        viewHolder.enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);
        //   holder.returnedNameTextView = (TextView) view.findViewById(R.id.returnedNameTextView);
        viewHolder.checkboxForDelete = (CheckBox) view.findViewById(R.id.checkboxForDelete);

        viewHolder.conditionImageView = (ImageView) view.findViewById(R.id.conditionImageView);
        viewHolder.weatherTextView = (TextView) view.findViewById(R.id.weatherTextView);
        viewHolder.dayTempTextView = (TextView) view.findViewById(R.id.dayTempTextView);
        viewHolder.tempUnitTextView = (TextView) view.findViewById(R.id.tempUnitTextView);

    //    viewHolder.weatherInfoLinearLayout = (LinearLayout) view.findViewById(R.id.weather_info);*/


        return view;
    }


    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        //   Log.d("AAAAA", "bindView()");

        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        int returnedCityColumnIndex = cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY);
        String returnedCity = cursor.getString(returnedCityColumnIndex);

//        Log.d("AAAAA", "bindView: returnedCity = " +  returnedCity);

        int enteredCityColumnIndex = cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY);
        String enteredCity = cursor.getString(enteredCityColumnIndex);

/*        Log.d("AAAAA", "bindView: position = " + cursor.getPosition() + ", returnedCity = " + returnedCity
                + ", enteredCity = " + enteredCity);*/


        if (returnedCity != null) { // full

            Log.d("AAAAA", "bindView: (returnedCity != null) position = " + cursor.getPosition() + ", returnedCity = " + returnedCity
                    + ", enteredCity = " + enteredCity);

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

                //      Log.d("AAAAA", "Celsius");

            } else if (mUnitsFormat == 1) { // imperial == Fahrenheit

                //     Log.d("AAAAA", "Fahrenheit");

                viewHolder.tempUnitTextView.setText("\u2109"); // Fahrenheit sign

                int dayTemp = cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP));

                viewHolder.dayTempTextView.setText(String.valueOf(dayTemp * 9 / 5 + 32)); // Celsius to Fahrenheit
            }

            viewHolder.weatherTextView.setVisibility(View.VISIBLE);
            viewHolder.conditionImageView.setVisibility(View.VISIBLE);
            viewHolder.tempUnitTextView.setVisibility(View.VISIBLE);
            viewHolder.dayTempTextView.setVisibility(View.VISIBLE);

        } else {

            Log.d("AAAAA", "bindView: (returnedCity == null) !!! position = " + cursor.getPosition() + ", returnedCity = " + returnedCity
                    + ", enteredCity = " + enteredCity);

            viewHolder.enteredCityTextView
                    .setText(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)));

            viewHolder.weatherTextView.setVisibility(View.GONE);
            viewHolder.conditionImageView.setVisibility(View.GONE);
            viewHolder.tempUnitTextView.setVisibility(View.GONE);
            viewHolder.dayTempTextView.setVisibility(View.GONE);
        }

        final int position = cursor.getPosition();
        int id = cursor.getInt(cursor.getColumnIndex(DataContract.CityEntry._ID));
        viewHolder.checkboxForDelete.setTag(id);

        Log.d("AAAAA", "bindView: position = " +  position);

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


/*        int enteredCityColumnIndex = cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY);
        String enteredCity = cursor.getString(enteredCityColumnIndex);*/

/*        Log.d("AAAAA", "bindView: position = " + cursor.getPosition() + ", returnedCity = " + returnedCity
                + ", enteredCity = " + enteredCity);*/

/*        final int position = cursor.getPosition();
        int type = getItemViewType(position);*/

        //   final ViewHolder viewHolder = (ViewHolder) view.getTag();

        //      if (returnedCity != null) { // full

/*        int columnIndex = cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY);
        String returnedCity = cursor.getString(columnIndex);*/


/*
        viewHolder.enteredCityTextView
                .setText(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)));

        viewHolder.checkboxForDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (viewHolder.checkboxForDelete.isChecked()) {

                    selectedItemsArray.put(position, true);

                } else if (!viewHolder.checkboxForDelete.isChecked()) {

                    selectedItemsArray.put(position, false);
                }
            }
        });*/

     /*   if (returnedCity != null) { // full

           TextView enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);
           ImageView conditionImageView = (ImageView) view.findViewById(R.id.conditionImageView);

            enteredCityTextView
                    .setText(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)));

            conditionImageView.setBackgroundResource(context.getResources().
                    getIdentifier("_" + cursor.getString(cursor
                                    .getColumnIndex(DataContract.WeatherEntry.COLUMN_ICON_NAME)),
                            "drawable", context.getPackageName()));

        } else {

            TextView enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);

            enteredCityTextView
                    .setText(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)));


        }
*/
        //   Object viewHolder = view.getTag();

 /*     if(type == 1){

         //   ViewHolderFull viewHolderFull = (ViewHolderFull) viewHolder;


          viewHolder.enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);
          viewHolder.conditionImageView = (ImageView) view.findViewById(R.id.conditionImageView);

          viewHolder.enteredCityTextView
                    .setText(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)));

          viewHolder.conditionImageView.setBackgroundResource(context.getResources().
                  getIdentifier("_" + cursor.getString(cursor
                                  .getColumnIndex(DataContract.WeatherEntry.COLUMN_ICON_NAME)),
                          "drawable", context.getPackageName()));

        } else if(type == 0){

     //       ViewHolderEmpty viewHolderEmpty = (ViewHolderEmpty) viewHolder;

          viewHolder.enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);

          viewHolder.enteredCityTextView
                    .setText(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)));

        }*/



/*
        final int position = cursor.getPosition();

        int id = cursor.getInt(cursor.getColumnIndex(DataContract.CityEntry._ID));
        final ViewHolder viewHolder = (ViewHolder) view.getTag();


        int columnIndex = cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY);
        String returnedCity = cursor.getString(columnIndex);


        viewHolder.enteredCityTextView
                .setText(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)));

        /// if(viewHolder.weatherTextView != null) {
        // if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY)))) {

        if (returnedCity != null) {
            viewHolder.conditionImageView.setBackgroundResource(context.getResources().
                    getIdentifier("_" + cursor.getString(cursor
                                    .getColumnIndex(DataContract.WeatherEntry.COLUMN_ICON_NAME)),
                            "drawable", context.getPackageName()));
        }

        viewHolder.weatherTextView.setText(cursor.getString(cursor
                .getColumnIndex(DataContract.WeatherEntry.COLUMN_DESCRIPTION)));

        if (mUnitsFormat == 0) { // metric = Celsius

            viewHolder.tempUnitTextView.setText("\u00B0C"); // Celsius sign

            viewHolder.dayTempTextView.setText(String.valueOf(cursor.getInt(cursor
                    .getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP))));

            //      Log.d("AAAAA", "Celsius");

        } else if (mUnitsFormat == 1) { // imperial == Fahrenheit

            //     Log.d("AAAAA", "Fahrenheit");

            viewHolder.tempUnitTextView.setText("\u2109"); // Fahrenheit sign

            int dayTemp = cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP));

            viewHolder.dayTempTextView.setText(String.valueOf(dayTemp * 9 / 5 + 32)); // Celsius to Fahrenheit
        }
        ///       }*/

        // T(°F) = T(°C) × 9/5 + 32


        // Log.d("AAAAA" , "RET CITY = " + cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY)));

/*
        // if returned from server data is present then show views
        if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY)))) {

            //  Log.d("AAAAA", "not empty !!!");
            Log.d("AAAAA", "RET CITY = " + cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY)));

            viewHolder.weatherInfoLinearLayout.setVisibility(View.VISIBLE);
            viewHolder.dayTempTextView.setVisibility(View.VISIBLE);
            viewHolder.tempUnitTextView.setVisibility(View.VISIBLE);
        }
*/


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

      /*  viewHolder.checkboxForDelete.setTag(id);

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
            viewHolder.checkboxForDelete.setVisibility(View.GONE);*/
    }


    // SOLUTIONS !!!
    // https://stackoverflow.com/questions/22022216/inflating-multiple-views-from-cursoradapter?noredirect=1&lq=1
    // https://stackoverflow.com/questions/8479833/cursoradapter-with-different-row-layouts
    // http://android.amberfog.com/?p=296
    // https://stackoverflow.com/questions/30751155/change-row-color-of-a-listview-with-cursoradapter-and-bindview?rq=1
    // http://www.vogella.com/tutorials/AndroidListView/article.html#adapterown


 /*   @Override
    public View getView(int position, View convertView, ViewGroup parent) {


        Log.d("AAAAA", "getView()");

        ViewHolder viewHolder = new ViewHolder();

        int type = getItemViewType(position);

        Log.d("AAAAA", "getView " + position + " " + convertView + " type = " + type);

        switch (type) {

            case 1:

                convertView = layoutInflater.inflate(R.layout.list_item_city, parent, false);

                viewHolder.enteredCityTextView = (TextView) convertView.findViewById(R.id.enteredCityTextView);
                //   holder.returnedNameTextView = (TextView) convertView.findViewById(R.id.returnedNameTextView);
                viewHolder.checkboxForDelete = (CheckBox) convertView.findViewById(R.id.checkboxForDelete);
                viewHolder.conditionImageView = (ImageView) convertView.findViewById(R.id.conditionImageView);
                viewHolder.weatherTextView = (TextView) convertView.findViewById(R.id.weatherTextView);
                viewHolder.dayTempTextView = (TextView) convertView.findViewById(R.id.dayTempTextView);
                viewHolder.tempUnitTextView = (TextView) convertView.findViewById(R.id.tempUnitTextView);


                viewHolder.enteredCityTextView = (TextView) convertView.findViewById(R.id.enteredCityTextView);
                viewHolder.conditionImageView = (ImageView) convertView.findViewById(R.id.conditionImageView);

                viewHolder.enteredCityTextView
                        .setText(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)));

                viewHolder.conditionImageView.setBackgroundResource(context.getResources().
                        getIdentifier("_" + cursor.getString(cursor
                                        .getColumnIndex(DataContract.WeatherEntry.COLUMN_ICON_NAME)),
                                "drawable", context.getPackageName()));

                break;

            case 0:

                convertView = layoutInflater.inflate(R.layout.list_item_city_short, parent, false);

                viewHolder.enteredCityTextView = (TextView) convertView.findViewById(R.id.enteredCityTextView);
                viewHolder.checkboxForDelete = (CheckBox) convertView.findViewById(R.id.checkboxForDelete);

                break;
        }

    //    convertView.setTag(viewHolder);

        return convertView;

    }*/


/*    @Override
    public int getItemViewType(int position) {

        return super.getItemViewType(position);
    }*/


/*    private int getItemViewType(Cursor cursor) {

        //  String type = cursor.getString(cursor.getColumnIndex("type"));

        int columnIndex = cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY);
        String returnedCity = cursor.getString(columnIndex);

        if (returnedCity != null) {
            return 1; // full data
        } else {
            return 0; // short data
        }
    }*/


/*    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        return getItemViewType(cursor);
    }*/


/*    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Cursor cursor = (Cursor) getItem(position);

        int returnedCityColumnIndex = cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY);
        String returnedCity = cursor.getString(returnedCityColumnIndex);

        int enteredCityColumnIndex = cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY);
        String enteredCity = cursor.getString(enteredCityColumnIndex);

        Log.d("AAAAA", "getView: position = " + cursor.getPosition() + ", returnedCity = " + returnedCity
                + ", enteredCity = " + enteredCity);

        ViewHolder viewHolder = new ViewHolder();

        if (returnedCity != null) { // full data

            convertView = layoutInflater.inflate(R.layout.list_item_city, parent, false);

            viewHolder.enteredCityTextView = (TextView) convertView.findViewById(R.id.enteredCityTextView);
            //   holder.returnedNameTextView = (TextView) view.findViewById(R.id.returnedNameTextView);
            viewHolder.checkboxForDelete = (CheckBox) convertView.findViewById(R.id.checkboxForDelete);
            viewHolder.conditionImageView = (ImageView) convertView.findViewById(R.id.conditionImageView);
            viewHolder.weatherTextView = (TextView) convertView.findViewById(R.id.weatherTextView);
            viewHolder.dayTempTextView = (TextView) convertView.findViewById(R.id.dayTempTextView);
            viewHolder.tempUnitTextView = (TextView) convertView.findViewById(R.id.tempUnitTextView);

        } else { // short data

            convertView = layoutInflater.inflate(R.layout.list_item_city_short, parent, false);

            viewHolder.enteredCityTextView = (TextView) convertView.findViewById(R.id.enteredCityTextView);
            viewHolder.checkboxForDelete = (CheckBox) convertView.findViewById(R.id.checkboxForDelete);
        }


        return convertView;
    }*/


/*    @Override
    public int getViewTypeCount() {
        return 2;
    }*/
}