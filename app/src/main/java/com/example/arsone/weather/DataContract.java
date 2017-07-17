package com.example.arsone.weather;

import android.provider.BaseColumns;


public final class DataContract {

    public static final String DATABASE_NAME = "weather.db";
    public static final int DATABASE_VERSION = 3;

    // private constructor
    private DataContract(){}

    // table "city"
    public static final class CityEntry implements BaseColumns {

        // Table 'cities'
        public static final String TABLE_NAME = "cities";

        // table 'cities' columns
        public static final String _ID                      = BaseColumns._ID;    // int/INTEGER
        public static final String COLUMN_ENTERED_CITY      = "entered_city";     // String/TEXT
        public static final String COLUMN_RETURNED_CITY     = "returned_city";    // String/TEXT
        public static final String COLUMN_COUNTRY_CODE      = "country_code";     // String/TEXT
        public static final String COLUMN_LATITUDE          = "latitude";         // double/DOUBLE
        public static final String COLUMN_LONGITUDE         = "longitude";        // double/DOUBLE
        public static final String COLUMN_SERVER_CITY_ID    = "server_city_id";   // int/INTEGER
        public static final String COLUMN_UPDATE_TIMESTAMP  = "update_timestamp"; // timestamp, when data was added

/*        public static final String[] DEFAULT_PROJECTION = new String[]{
                DataContract.CityEntry._ID,
                DataContract.CityEntry.COLUMN_ENTERED_CITY,
                DataContract.CityEntry.COLUMN_RETURNED_CITY
        };

        public static final String DEFAULT_SORT_ORDER = _ID + " DESC";*/
    }


    // Table "weather"
    public static final class WeatherEntry implements BaseColumns {

        public static final String TABLE_NAME = "weather";

        // IMPORTANT !!!
        // All data stored in metric units

        // table 'weather' columns
        public static final String _ID                 = BaseColumns._ID; // ID (int/INTEGER)
        public static final String COLUMN_CITY_ID_FK   = "city_id";       // foreign key (int/INTEGER)
        public static final String COLUMN_TIMESTAMP    = "dt";            // weather timestamp in seconds (int/INTEGER)
        public static final String COLUMN_MORNING_TEMP = "temp_morning";  // double/DOUBLE
        public static final String COLUMN_DAY_TEMP     = "temp_day";      // double/DOUBLE
        public static final String COLUMN_EVENING_TEMP = "temp_evening";  // double/DOUBLE
        public static final String COLUMN_NIGHT_TEMP   = "temp_night";    // double/DOUBLE
        public static final String COLUMN_MIN_TEMP     = "temp_min";      // double/DOUBLE
        public static final String COLUMN_MAX_TEMP     = "temp_max";      // double/DOUBLE
        public static final String COLUMN_HUMIDITY     = "humidity";      // int/INTEGER
        public static final String COLUMN_PRESSURE     = "pressure";      // double/DOUBLE
        public static final String COLUMN_SPEED        = "wind_speed";    // double/DOUBLE
        public static final String COLUMN_DIRECTION    = "wind_direction";// int/INTEGER
        public static final String COLUMN_DESCRIPTION  = "description";   // String/TEXT
        public static final String COLUMN_ICON_NAME    = "icon_name";     // String/TEXT
        public static final String COLUMN_INSERT_TIMESTAMP  = "insert_timestamp"; // timestamp, when weather data was added

/*        public static final String[] DEFAULT_PROJECTION = new String[]{
                WeatherEntry._ID,
                WeatherEntry.COLUMN_CITY_ID_FK,
                WeatherEntry.COLUMN_TIMESTAMP,
                WeatherEntry.COLUMN_MORNING_TEMP,
                WeatherEntry.COLUMN_DAY_TEMP,
                WeatherEntry.COLUMN_EVENING_TEMP,
                WeatherEntry.COLUMN_NIGHT_TEMP,
                WeatherEntry.COLUMN_MIN_TEMP,
                WeatherEntry.COLUMN_MAX_TEMP,
                WeatherEntry.COLUMN_HUMIDITY,
                WeatherEntry.COLUMN_PRESSURE,
                WeatherEntry.COLUMN_SPEED,
                WeatherEntry.COLUMN_DIRECTION,
                WeatherEntry.COLUMN_DESCRIPTION,
                WeatherEntry.COLUMN_ICON_NAME
        };

        public static final String DEFAULT_SORT_ORDER = DataContract.WeatherEntry._ID + " ASC";*/
    }


    // Table "settings"
    public static final class SettingsEntry implements BaseColumns {

        public static final String TABLE_NAME = "settings";

        // table 'weather' columns
        public static final String _ID                     = BaseColumns._ID;    //  (int/INTEGER)
        public static final String COLUMN_UNITS_FORMAT     = "units_format";     //  (int/INTEGER)
        public static final String COLUMN_SORT_CITIES      = "sort_cities";      //  (int/INTEGER)
        public static final String COLUMN_MAP_LANGUAGE     = "map_language";     //  (int/INTEGER)
        public static final String COLUMN_CAMERA_LATITUDE  = "camera_latitude";  // (double/DOUBLE)
        public static final String COLUMN_CAMERA_LONGITUDE = "camera_longitude"; // (double/DOUBLE)
        public static final String COLUMN_CAMERA_BEARING   = "camera_bearing";   // (double/DOUBLE)
        public static final String COLUMN_CAMERA_TILT      = "camera_tilt";      // (double/DOUBLE)
        public static final String COLUMN_CAMERA_ZOOM      = "camera_zoom";      // (double/DOUBLE)
    }
}