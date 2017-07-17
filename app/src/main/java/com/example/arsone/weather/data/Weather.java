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
    public final double dayTemp;
    public final double nightTemp;
    public final double minTemp;
    public final double maxTemp;
    public final int humidity;
    public final String description;
 //   public final String iconURL;
    public final double windSpeed;
    public final int windDirection;
    public final double pressure;
    public final double morningTemp;
    public final double eveningTemp;
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
        this.date = formatter.format(new Date(timeStamp * 1000L)); // convert to milliseconds

        this.morningTemp = morningTemp;
        this.dayTemp = dayTemp;
        this.eveningTemp = eveningTemp;
        this.nightTemp = nightTemp;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.humidity = humidity;
        this.pressure = pressure; // hPa
        this.windSpeed = windSpeed;
        this.windDirection = windDirection;
        this.description = description;
        this.iconName = iconName;
   //     this.iconURL = "http://openweathermap.org/img/w/" + iconName + ".png";
        this.weatherID = weatherID;
    }


    // Преобразование временной метки в название дня недели (Monday, ...)
    private static String convertTimeStampToDay(long timeStamp) {

        Calendar calendar = Calendar.getInstance(); // Объект Calendar

        calendar.setTimeInMillis(timeStamp * 1000); // get time in milliseconds

        TimeZone tz = TimeZone.getDefault(); // Часовой пояс устройства

        // Поправка на часовой пояс устройства
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));

        // Объект SimpleDateFormat, возвращающий название дня недели
        SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE");
        /// SimpleDateFormat dateFormatter = new SimpleDateFormat("EEEE", Locale.getDefault());

        return dateFormatter.format(calendar.getTime());
    }
}