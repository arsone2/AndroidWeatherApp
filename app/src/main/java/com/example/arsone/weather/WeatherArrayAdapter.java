package com.example.arsone.weather;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.arsone.weather.data.Weather;


public class WeatherArrayAdapter extends ArrayAdapter<Weather> {

    // Кэш для уже загруженных объектов Bitmap
    private Map<String, Bitmap> bitmaps = new HashMap<>();

    // layout ListView item resource
    private int listViewItemResource;

    // false = phone, true = tablet
 //   private boolean isPhone;

/*    // constructor
    public WeatherArrayAdapter(Context context, List<Weather> forecast) {

        super(context, -1, forecast);
    }*/

    // constructor
    public WeatherArrayAdapter(Context context, List<Weather> forecast, int itemResource) {

        super(context, -1, forecast);

        this.listViewItemResource = itemResource;
   //     this.isPhone = isPhone;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        Weather day = getItem(position);

        ViewHolder viewHolder;

        if (convertView == null) { // Объекта ViewHolder нет, создать его

            viewHolder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(getContext());

            /// convertView = inflater.inflate(R.layout.list_item_weather, parent, false);
            convertView = inflater.inflate(listViewItemResource, parent, false);

            viewHolder.conditionImageView = (ImageView)convertView.findViewById(R.id.conditionImageView);
            viewHolder.dayOfWeekTextView = (TextView)convertView.findViewById(R.id.dayOfWeekTextView);
            viewHolder.weatherTextView = (TextView)convertView.findViewById(R.id.weatherTextView);
            viewHolder.dateTextView = (TextView)convertView.findViewById(R.id.dateTextView);
            viewHolder.dayTempTextView = (TextView)convertView.findViewById(R.id.dayTempTextView);
            viewHolder.nightTempTextView = (TextView)convertView.findViewById(R.id.nightTempTextView);
//            viewHolder.lowTextView = (TextView)convertView.findViewById(R.id.lowTextView);
//            viewHolder.hiTextView = (TextView)convertView.findViewById(R.id.hiTextView);
            viewHolder.humidityTextView = (TextView)convertView.findViewById(R.id.humidityTextView);

            viewHolder.minMaxTempTextView = (TextView)convertView.findViewById(R.id.minMaxTempTextView);
            viewHolder.morningTempTextView = (TextView)convertView.findViewById(R.id.morningTempTextView);
            viewHolder.eveningTempTextView = (TextView)convertView.findViewById(R.id.eveningTempTextView);
            viewHolder.pressureTextView = (TextView)convertView.findViewById(R.id.pressureTextView);
            viewHolder.windSpeedTextView = (TextView)convertView.findViewById(R.id.windSpeedTextView);
            viewHolder.windDirectionTextView = (TextView)convertView.findViewById(R.id.windDirectionTextView);

            convertView.setTag(viewHolder);

        } else { // Cуществующий объект ViewHolder используется заново
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // imageView.setImageBitmap(bitmap);

//        Log.d("AAAAA", "day.iconName = " + day.iconName);

        viewHolder.conditionImageView.setBackgroundResource(getContext().getResources().
                getIdentifier("_" + day.iconName, "drawable", getContext().getPackageName()));

      ///  viewHolder.conditionImageView.setImageBitmap(bitmaps.get(day.iconURL));

/*        // Если значок погодных условий уже загружен, использовать его;
        // в противном случае загрузить в отдельном потоке
        if (bitmaps.containsKey(day.iconURL)) {
            viewHolder.conditionImageView.setImageBitmap(bitmaps.get(day.iconURL));
        } else {
            // Загрузить и вывести значок погодных условий
              new LoadImageTask(viewHolder.conditionImageView).execute(day.iconURL);
        }*/


        Context context = getContext();


   //     if(listViewItemResource == R.layout.list_item_weather) { // phone

            viewHolder.dayOfWeekTextView.setText(day.dayOfWeek);

            viewHolder.weatherTextView.setText(day.description);

            viewHolder.dateTextView.setText(day.date);

            viewHolder.dayTempTextView.setText(context.getString(R.string.weather_day,
                    day.dayTemp));

            viewHolder.morningTempTextView.setText(context.getString(R.string.weather_morning_temp,
                    day.morningTemp));

            viewHolder.eveningTempTextView.setText(context.getString(R.string.weather_evening_temp,
                    day.eveningTemp));

            viewHolder.nightTempTextView.setText(context.getString(R.string.weather_night_temp,
                    day.nightTemp));

/*
            viewHolder.lowTextView.setText(context.getString(R.string.low_temp, day.minTemp));
            viewHolder.hiTextView.setText(context.getString(R.string.high_temp, day.maxTemp));
*/

            viewHolder.minMaxTempTextView.setText(context.getString(R.string.weather_min_max_temp,
                    day.minTemp, day.maxTemp));

            viewHolder.humidityTextView.setText(context.getString(R.string.weather_humidity, day.humidity));
            viewHolder.windSpeedTextView.setText(context.getString(R.string.weather_wind_speed, day.windSpeed)); // double
            viewHolder.pressureTextView.setText(context.getString(R.string.weather_pressure, day.pressure));
            viewHolder.windDirectionTextView.setText(context.getString(R.string.weather_wind_direction, day.windDirection));

   //     }



/*        viewHolder.dayOfWeekTextView.setText(context.getString(R.string.day_description, day.dayOfWeek));

        viewHolder.weatherTextView.setText(context.getString(R.string.day_description, day.description));

        viewHolder.dateTextView.setText(context.getString(R.string.date_weather, day.date ));

        viewHolder.dayTempTextView.setText(context.getString(R.string.day_temp, day.dayTemp));

        viewHolder.nightTempTextView.setText(context.getString(R.string.night_temp, day.nightTemp));

        viewHolder.lowTextView.setText(context.getString(R.string.low_temp, day.minTemp));

        viewHolder.hiTextView.setText(context.getString(R.string.high_temp, day.maxTemp));

        viewHolder.humidityTextView.setText(context.getString(R.string.humidity, day.humidity));*/

        return convertView;
    }


    public class LoadImageTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView imageView;

        // Сохранение ImageView для загруженного объекта Bitmap
        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        // загрузить изображение; params[0] содержит URL-адрес изображения
        @Override
        protected Bitmap doInBackground(String... params) {

            Bitmap bitmap = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(params[0]); // Создать URL для изображения

                // Открыть объект HttpURLConnection, получить InputStream
                // и загрузить изображение
                connection = (HttpURLConnection) url.openConnection();

                try{
                    InputStream inputStream = connection.getInputStream();
                    bitmap = BitmapFactory.decodeStream(inputStream);
                    bitmaps.put(params[0], bitmap); // Кэширование
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                connection.disconnect();
            }

            return bitmap;
        }

        // Связать значок погодных условий с элементом списка
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
        }
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