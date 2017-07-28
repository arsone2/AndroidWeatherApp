package com.example.arsone.weather;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
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

 //   private Context context;
  ///  private LayoutInflater layoutInflater;


    private static int mUnitsFormat;


    /// public WeatherCursorAdapter(Context context, Cursor c, int flags, int unitsFormat) {
    public WeatherCursorAdapter(Context context, Cursor c, int flags) {

        super(context, c, flags);
    ///    this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        /// mUnitsFormat = unitsFormat;
    }


    public static void setUnitsFormat(int unitsFormat){

        mUnitsFormat = unitsFormat;
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

       // View view = layoutInflater.inflate(R.layout.list_item_weather, parent, false);
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

        viewHolder.conditionImageView.setBackgroundResource(context.getResources().
                getIdentifier("_" + cursor.getString(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_ICON_NAME)),
                        "drawable", context.getPackageName()));

        long timeStamp = cursor.getLong(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_TIMESTAMP));
       // SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

        viewHolder.dayOfWeekTextView.setText(convertTimeStampToDay(timeStamp));

        viewHolder.weatherTextView
                .setText(cursor.getString(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_DESCRIPTION)));

        viewHolder.dateTextView.setText(DateFormat.getDateInstance(DateFormat.MEDIUM).format(timeStamp * 1000L));

       /// viewHolder.dateTextView.setText(formatter.format(new Date(timeStamp * 1000L)));
        /// viewHolder.dateTextView.setText(formatter.format(new Date(timeStamp * 1000L)));

  //      Log.d("AAAAA", "WeatherCursorAdapter: mUnitsFormat = " + mUnitsFormat);

/*
        viewHolder.dayTempTextView
        .setText(context.getString(R.string.weather_day,
        String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP)))));
                // + " \u00B0C"));
*/

        if (mUnitsFormat == 0) { // metric = Celsius

            viewHolder.dayTempTextView
                    .setText(String.valueOf(cursor.getInt(cursor
                            .getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP))));

            viewHolder.tempUnitTextView.setText("\u00B0C"); // Celsius sign

            viewHolder.nightTempTextView
                    .setText(context.getString(R.string.m_weather_night_temp,
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

            ///String.valueOf(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_HUMIDITY)))));

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

            viewHolder.dayTempTextView
                    .setText(CelsiusToFahrenheit(cursor.getDouble(cursor
                            .getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP))));

            viewHolder.tempUnitTextView.setText("\u2109"); // Fahrenheit sign

/*            viewHolder.dayTempTextView
                    .setText(CelsiusToFahrenheit(cursor.getInt(cursor
                            .getColumnIndex(DataContract.WeatherEntry.COLUMN_DAY_TEMP))));*/

            viewHolder.nightTempTextView
                    .setText(context.getString(R.string.i_weather_night_temp,
                            CelsiusToFahrenheit(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_NIGHT_TEMP)))));
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


/*
        viewHolder.humidityTextView.setText(context.getString(R.string.weather_humidity,
                NumberFormat.getPercentInstance()
                        .format(cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_HUMIDITY)) / 100.0)));
*/

        int h = cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_HUMIDITY));

  //      Log.d("AAAAA", "humidity = " + h);

        if (h != 0) {

            viewHolder.humidityTextView.setText(context.getString(R.string.weather_humidity,
                    NumberFormat.getPercentInstance().format(h / 100.0)));
        } else {

            viewHolder.humidityTextView.setText(context.getString(R.string.weather_humidity, context.getString(R.string.no_data)));
        }


        ///int pressure_hPa = cursor.getInt(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_PRESSURE));
        double pressure_hPa = cursor.getDouble(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_PRESSURE));

   //     Log.d("AAAAA", "pressure_hPa = " + pressure_hPa);

        viewHolder.pressureTextView.setText(context.getString(R.string.weather_pressure,
                pressure_hPa, pressure_hPa * 0.750063755419211)); // hPa & Millimeter of mercury/ "mmHg"

/*
        viewHolder.windSpeedTextView.setText(context.getString(R.string.weather_wind_speed,
                cursor.getDouble(cursor.getColumnIndex(DataContract.WeatherEntry.COLUMN_SPEED))));
*/
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


/*    private String CelsiusToFahrenheit(int temp) {

        return String.valueOf(temp * 9 / 5 + 32);
    }*/

    private String CelsiusToFahrenheit(double temp) {

        // return (int) (temp * 9 / 5 + 32);
        return String.valueOf((int)(temp * 9 / 5 + 32));
    }
}