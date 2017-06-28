package com.example.arsone.weather;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class WeatherCursorAdapter extends CursorAdapter {

    private Context context;
    private LayoutInflater layoutInflater;

    // layout ListView item resource
    private int listViewItemResource;


    public WeatherCursorAdapter(Context context, Cursor c, int flags, int itemResource) {

        super(context, c, flags);
        this.context = context;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listViewItemResource = itemResource;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = layoutInflater.inflate(listViewItemResource, parent, false);

        ViewHolder viewHolder = new ViewHolder();

        viewHolder.conditionImageView = (ImageView)view.findViewById(R.id.conditionImageView);
        viewHolder.dayOfWeekTextView = (TextView)view.findViewById(R.id.dayOfWeekTextView);
        viewHolder.weatherTextView = (TextView)view.findViewById(R.id.weatherTextView);
        viewHolder.dateTextView = (TextView)view.findViewById(R.id.dateTextView);
        viewHolder.dayTempTextView = (TextView)view.findViewById(R.id.dayTempTextView);
        viewHolder.nightTempTextView = (TextView)view.findViewById(R.id.nightTempTextView);
//            viewHolder.lowTextView = (TextView)view.findViewById(R.id.lowTextView);
//            viewHolder.hiTextView = (TextView)view.findViewById(R.id.hiTextView);
        viewHolder.humidityTextView = (TextView)view.findViewById(R.id.humidityTextView);

        viewHolder.minMaxTempTextView = (TextView)view.findViewById(R.id.minMaxTempTextView);
        viewHolder.morningTempTextView = (TextView)view.findViewById(R.id.morningTempTextView);
        viewHolder.eveningTempTextView = (TextView)view.findViewById(R.id.eveningTempTextView);
        viewHolder.pressureTextView = (TextView)view.findViewById(R.id.pressureTextView);
        viewHolder.windSpeedTextView = (TextView)view.findViewById(R.id.windSpeedTextView);
        viewHolder.windDirectionTextView = (TextView)view.findViewById(R.id.windDirectionTextView);

        view.setTag(viewHolder);

        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.conditionImageView.setBackgroundResource(context.getResources().
        getIdentifier("_" + cursor.getString(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_ICON_NAME)),
        "drawable", context.getPackageName()));

        long timeStamp = cursor.getLong(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_TIMESTAMP));
        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        viewHolder.dayOfWeekTextView.setText(convertTimeStampToDay(timeStamp));

        viewHolder.weatherTextView
        .setText(cursor.getString(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_DESCRIPTION)));

        viewHolder.dateTextView.setText(formatter.format(new Date(timeStamp * 1000L)).toString());

        viewHolder.dayTempTextView
        .setText(context.getString(R.string.weather_day,
        String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP)))));
                // + " \u00B0C"));

        viewHolder.nightTempTextView
        .setText(context.getString(R.string.weather_night_temp,
        String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_NIGHT_TEMP)))));
                // + " \u00B0C"));

/*        viewHolder.lowTextView
        .setText(context.getString(R.string.low_temp,
        String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_MIN_TEMP)))
                + " \u00B0C"));

        viewHolder.hiTextView
        .setText(context.getString(R.string.high_temp,
        String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_MAX_TEMP)))
                + " \u00B0C"));*/

        /// this.humidity = NumberFormat.getPercentInstance().format(humidity / 100.0);

        viewHolder.humidityTextView
        .setText(context.getString(R.string.weather_humidity,
        NumberFormat.getPercentInstance()
        .format(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_HUMIDITY)) / 100.0)));
        ///String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_HUMIDITY)))));

        viewHolder.minMaxTempTextView.setText(context.getString(R.string.weather_min_max_temp,
                String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_MIN_TEMP))),
                String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_MAX_TEMP)
                ))));

        viewHolder.morningTempTextView.setText(context.getString(R.string.weather_morning_temp,
                String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_MORNING_TEMP)))));

        viewHolder.eveningTempTextView.setText(context.getString(R.string.weather_evening_temp,
                String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_EVENING_TEMP)))));

        viewHolder.pressureTextView.setText(context.getString(R.string.weather_pressure,
                String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_PRESSURE)))));

        viewHolder.windSpeedTextView.setText(context.getString(R.string.weather_wind_speed,
                cursor.getDouble(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_SPEED))));
        /// String.valueOf(cursor.getDouble(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_SPEED)))));

        viewHolder.windDirectionTextView.setText(context.getString(R.string.weather_wind_direction,
                String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_DIRECTION)))));
    }


    // Преобразование временной метки в название дня недели (Monday, ...)
    private static String convertTimeStampToDay(long timeStamp) {

        Calendar calendar = Calendar.getInstance(); // Объект Calendar

        calendar.setTimeInMillis(timeStamp * 1000); // millseconds

        TimeZone tz = TimeZone.getDefault(); // Часовой пояс устройства

        // Поправка на часовой пояс устройства
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));

        // Объект SimpleDateFormat, возвращающий название дня недели
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE");
        /// SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE", Locale.getDefault());

        return dateFormatter.format(calendar.getTime());
    }


    private static class ViewHolder {

        private ImageView conditionImageView;
        private TextView dayOfWeekTextView;
        private TextView dateTextView;
        private TextView dayTempTextView;
        private TextView nightTempTextView;
        private TextView weatherTextView;
        //        private TextView lowTextView;
//        private TextView hiTextView;
        private TextView humidityTextView;

        private TextView pressureTextView;
        private TextView windSpeedTextView;
        private TextView minMaxTempTextView;
        private TextView morningTempTextView;
        private TextView eveningTempTextView;
        private TextView windDirectionTextView;
    }
}