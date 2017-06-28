package com.example.arsone.weather.data;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class Weather {

    public final String dayOfWeek;
    public final long timeStamp;
    public final String date;
    public final String dayTemp;
    public final String nightTemp;
    public final String minTemp;
    public final String maxTemp;
    public final String humidity;
    public final String description;
    public final String iconURL;
    public final String tempMinMax;
    public final Double windSpeed;
    public final String windDirection;
    public final String pressure;
    public final String morningTemp;
    public final String eveningTemp;
    public final String iconName;
    public final int weatherID;


    // constructor
    public Weather(long timeStamp,
                   double morningTemp,
                   double dayTemp,
                   double eveningTemp,
                   double nightTemp,
                   double minTemp,
                   double maxTemp,
                   int humidity,
                   double pressure,
                   double windSpeed,
                   int windDirection,
                   String description,
                   String iconName,
                   int weatherID) {

        this.timeStamp = timeStamp;

        // NumberFormat для форматирования температур в целое число
        NumberFormat numberFormat = NumberFormat.getInstance();

        numberFormat.setMaximumFractionDigits(0);

        this.dayOfWeek = convertTimeStampToDay(timeStamp);

        SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
        this.date = formatter.format(new Date(timeStamp * 1000L)).toString(); // convert to milliseconds

        // this.date = DateFormat.getDateInstance().format(timeStamp) + "\u00B0C";

        this.tempMinMax = numberFormat.format(minTemp) + "/" + numberFormat.format(maxTemp); // + "\u00B0C";

        this.morningTemp = numberFormat.format(morningTemp);// + "\u00B0C";

        this.dayTemp = numberFormat.format(dayTemp);// + "\u00B0C";

        this.eveningTemp = numberFormat.format(eveningTemp);// + "\u00B0C";

        this.nightTemp = numberFormat.format(nightTemp);// + "\u00B0C";

        this.minTemp = numberFormat.format(minTemp);// + "\u00B0C";

        this.maxTemp = numberFormat.format(maxTemp);// + "\u00B0C";

        this.humidity = NumberFormat.getPercentInstance().format(humidity / 100.0);

        this.pressure = numberFormat.format(pressure * 0.750063755419211); // в мм ртутного столба

        /// this.windSpeed = numberFormat.format(windSpeed);
        this.windSpeed = windSpeed;

        this.windDirection = numberFormat.format(windDirection);

        this.description = description;

        this.iconName = iconName;

        this.iconURL = "http://openweathermap.org/img/w/" + iconName + ".png";

        this.weatherID = weatherID;
    }


    // Преобразование временной метки в название дня недели (Monday, ...)
    private static String convertTimeStampToDay(long timeStamp) {

        Calendar calendar = Calendar.getInstance(); // Объект Calendar

        calendar.setTimeInMillis(timeStamp * 1000); // Получение времени

        TimeZone tz = TimeZone.getDefault(); // Часовой пояс устройства

        // Поправка на часовой пояс устройства
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));

        // Объект SimpleDateFormat, возвращающий название дня недели
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE");
        /// SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE", Locale.getDefault());

        return dateFormatter.format(calendar.getTime());
    }
}