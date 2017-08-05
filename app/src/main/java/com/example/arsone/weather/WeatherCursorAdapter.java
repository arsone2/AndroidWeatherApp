package com.example.arsone.weather;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class WeatherCursorAdapter extends CursorAdapter {

    private static class ViewHolder {

        private ImageView conditionImageView;
        private TextView dayOfWeekTextView;
        private TextView dateTextView;
        private TextView dayTempTextView;
        private TextView nightTempTextView;
        private TextView weatherTextView;
        private TextView humidityTextView;
        private TextView pressureTextView;
        private TextView windSpeedTextView;
        private TextView minMaxTempTextView;
        private TextView morningTempTextView;
        private TextView eveningTempTextView;
        private TextView windDirectionTextView;
        private TextView tempUnitTextView;
    }

    private SimpleDateFormat mFormatter;
    private String mCurrentDate;
    private String mTomorrowDate;
    private static int mUnitsFormat;


    public WeatherCursorAdapter(Context context, Cursor c, int flags) {

        super(context, c, flags);

        mFormatter = new SimpleDateFormat("yyyy-MM-dd");
        mCurrentDate = mFormatter.format(new Date());

        // calculate tomorrow date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date tomorrow = calendar.getTime();
        mTomorrowDate = mFormatter.format(tomorrow);
    }


    public static void setUnitsFormat(int unitsFormat){

        mUnitsFormat = unitsFormat;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = layoutInflater.inflate(R.layout.list_item_weather, parent, false);

        ViewHolder viewHolder = new ViewHolder();

        viewHolder.conditionImageView = (ImageView) view.findViewById(R.id.conditionImageView);
        viewHolder.dayOfWeekTextView = (TextView) view.findViewById(R.id.dayOfWeekTextView);
        viewHolder.weatherTextView = (TextView) view.findViewById(R.id.weatherTextView);
        viewHolder.dateTextView = (TextView) view.findViewById(R.id.dateTextView);
        viewHolder.dayTempTextView = (TextView) view.findViewById(R.id.dayTempTextView);
        viewHolder.nightTempTextView = (TextView) view.findViewById(R.id.nightTempTextView);
        viewHolder.humidityTextView = (TextView) view.findViewById(R.id.humidityTextView);
        viewHolder.minMaxTempTextView = (TextView) view.findViewById(R.id.minMaxTempTextView);
        viewHolder.morningTempTextView = (TextView) view.findViewById(R.id.morningTempTextView);
        viewHolder.eveningTempTextView = (TextView) view.findViewById(R.id.eveningTempTextView);
        viewHolder.pressureTextView = (TextView) view.findViewById(R.id.pressureTextView);
        viewHolder.windSpeedTextView = (TextView) view.findViewById(R.id.windSpeedTextView);
        viewHolder.windDirectionTextView = (TextView) view.findViewById(R.id.windDirectionTextView);
        viewHolder.tempUnitTextView = (TextView)view.findViewById(R.id.tempUnitTextView);

        view.setTag(viewHolder);

        return view;
    }


    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        final ViewHolder viewHolder = (ViewHolder) view.getTag();

        long timeStamp = cursor.getLong(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_TIMESTAMP));

        // convert to date
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp * 1000L);
        String date = mFormatter.format(calendar.getTime());

//        viewHolder.dayOfWeekTextView.setText(convertTimeStampToDay(timeStamp));

     //   LinearLayout itemLinearLayout = (LinearLayout) view.findViewById(R.id.itemLinearLayout);

        if (date.equals(mCurrentDate)) {

       //     itemLinearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.weatherItemColor));

            viewHolder.dayOfWeekTextView.setText(context.getString(R.string.day_of_week_today,
                    convertTimeStampToDay(timeStamp)));

        } else {

      //      itemLinearLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWhite));

            if(date.equals(mTomorrowDate)) {

                viewHolder.dayOfWeekTextView.setText(context.getString(R.string.day_of_week_tomorrow,
                        convertTimeStampToDay(timeStamp)));
            } else {

                viewHolder.dayOfWeekTextView.setText(convertTimeStampToDay(timeStamp));
            }
        }

        viewHolder.conditionImageView.setBackgroundResource(context.getResources().
                getIdentifier("_" + cursor.getString(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_ICON_NAME)),
                        "drawable", context.getPackageName()));


        viewHolder.weatherTextView
                .setText(cursor.getString(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_DESCRIPTION)));

        viewHolder.dateTextView.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(timeStamp * 1000L));

        if (mUnitsFormat == 0) { // metric = Celsius

            viewHolder.dayTempTextView
                    .setText(String.valueOf(cursor.getInt(cursor
                            .getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP))));

            viewHolder.tempUnitTextView.setText("\u00B0C"); // Celsius sign
            viewHolder.nightTempTextView.setText(context.getString(R.string.m_weather_night_temp,
                            String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_NIGHT_TEMP)))));
            viewHolder.minMaxTempTextView.setText(context.getString(R.string.m_weather_min_max_temp,
                    String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_MIN_TEMP))),
                    String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_MAX_TEMP)
                    ))));
            viewHolder.morningTempTextView.setText(context.getString(R.string.m_weather_morning_temp,
                    String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_MORNING_TEMP)))));
            viewHolder.eveningTempTextView.setText(context.getString(R.string.m_weather_evening_temp,
                    String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_EVENING_TEMP)))));
            viewHolder.windSpeedTextView.setText(context.getString(R.string.m_weather_wind_speed,
                    cursor.getDouble(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_SPEED))));

        } else if (mUnitsFormat == 1) { // imperial == Fahrenheit

            viewHolder.dayTempTextView.setText(CelsiusToFahrenheit(cursor.getDouble(cursor
                            .getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP))));

            viewHolder.tempUnitTextView.setText("\u2109"); // Fahrenheit sign

            viewHolder.nightTempTextView
                    .setText(context.getString(R.string.i_weather_night_temp,
                            CelsiusToFahrenheit(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_NIGHT_TEMP)))));

            viewHolder.minMaxTempTextView.setText(context.getString(R.string.i_weather_min_max_temp,
                    CelsiusToFahrenheit(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_MIN_TEMP))),
                    CelsiusToFahrenheit(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_MAX_TEMP)
                    ))));
            viewHolder.morningTempTextView.setText(context.getString(R.string.i_weather_morning_temp,
                    CelsiusToFahrenheit(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_MORNING_TEMP)))));
            viewHolder.eveningTempTextView.setText(context.getString(R.string.i_weather_evening_temp,
                    CelsiusToFahrenheit(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_EVENING_TEMP)))));

            // miles per hour = meters per second × 2.236936
            viewHolder.windSpeedTextView.setText(context.getString(R.string.i_weather_wind_speed,
                    cursor.getDouble(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_SPEED)) * 2.236936));
        }

        int humidity = cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_HUMIDITY));

        if (humidity != 0) {

            viewHolder.humidityTextView.setText(context.getString(R.string.weather_humidity,
                    NumberFormat.getPercentInstance().format(humidity / 100.0)));
        } else {

            viewHolder.humidityTextView.setText(context.getString(R.string.weather_humidity, context.getString(R.string.no_data)));
        }


        double pressure_hPa = cursor.getDouble(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_PRESSURE));

        viewHolder.pressureTextView.setText(context.getString(R.string.weather_pressure,
                pressure_hPa, pressure_hPa * 0.750063755419211)); // hPa & Millimeter of mercury/ "mmHg"

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

        return dateFormatter.format(calendar.getTime());
    }


    private String CelsiusToFahrenheit(double temp) {

        return String.valueOf((int)(temp * 9 / 5 + 32));
    }
}