package com.example.arsone.weather;


import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.TextView;

public class CityCursorAdapter extends CursorAdapter {

    private Context context;

    private LayoutInflater layoutInflater;

    private static boolean checkboxVisibility = false;

    private static SparseBooleanArray selectedItemsArray;


    public static class ViewHolder {

        public int id;
        public TextView enteredCityTextView;
        public TextView returnedNameTextView;
        public CheckBox checkboxForDelete;
    }


    // constructor
    public CityCursorAdapter(Context context, Cursor c, int flags) {

        super(context, c, flags);
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        selectedItemsArray = new SparseBooleanArray();
    }


    public static void setCheckboxesVisibility(boolean visibility) {

        checkboxVisibility = visibility;
    }

    public static void uncheckAllItems() {

        selectedItemsArray.clear();
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = layoutInflater.inflate(R.layout.list_item_city, parent, false);

        ViewHolder holder = new ViewHolder();

        holder.enteredCityTextView = (TextView) view.findViewById(R.id.enteredCityTextView);
        holder.returnedNameTextView = (TextView) view.findViewById(R.id.returnedNameTextView);
        holder.checkboxForDelete = (CheckBox) view.findViewById(R.id.checkboxForDelete);

        view.setTag(holder);

        return view;
    }


    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {

        final int position = cursor.getPosition();

        int id = cursor.getInt(cursor.getColumnIndex(DataContract.CityEntry._ID));

        final ViewHolder holder = (ViewHolder) view.getTag();

        holder.enteredCityTextView
                .setText(cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY)));

        String returnedCity = cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_RETURNED_CITY));

        if (!TextUtils.isEmpty(returnedCity))
            holder.returnedNameTextView.setText("/ " + returnedCity);
            ///holder.returnedNameTextView.setText(context.getString(R.string.returned_city_format, returnedCity));
        else
            holder.returnedNameTextView.setText(returnedCity);

        holder.checkboxForDelete.setTag(id);

        holder.checkboxForDelete.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (holder.checkboxForDelete.isChecked()) {
                    selectedItemsArray.put(position, true);
                } else if (!holder.checkboxForDelete.isChecked()) {
                    selectedItemsArray.put(position, false);
                }
            }
        });

        holder.checkboxForDelete.setChecked(selectedItemsArray.get(cursor.getPosition()));

        if (checkboxVisibility)
            holder.checkboxForDelete.setVisibility(View.VISIBLE);
        else
            holder.checkboxForDelete.setVisibility(View.GONE);
    }
}