package com.example.arsone.weather.data;


import android.os.Bundle;

public class City {

    //	constants for field references
    public static final String CITY_ID = "city_id";
    public static final String ENTERED_CITY = "entered_city";
    public static final String RETURNED_CITY = "retutned_city";
    public static final String UPDATE_TIME = "update_time";

/*    public static final String IMAGE_RESOURCE = "imageResource";
    public static final String PRICE = "price";
    public static final String INSTRUCTIONS = "instructions";*/

    private int id;
    private String enteredCity;
    private String returnedCity;


    public City(int id, String enteredCity, String returnedCity) {

        this.id = id;
        this.enteredCity = enteredCity;
        this.returnedCity = returnedCity;
    }


    //	Create from a bundle
    public City(Bundle b) {
        if (b != null) {
            this.enteredCity = b.getString(ENTERED_CITY);
//            this.imageResource = b.getInt(IMAGE_RESOURCE);
//            this.price = b.getDouble(PRICE);
//            this.instructions = b.getString(INSTRUCTIONS);
        }
    }


    //	Package data for transfer between activities
    public Bundle toBundle() {

        Bundle b = new Bundle();

        b.putString(ENTERED_CITY, this.enteredCity);
//        b.putInt(IMAGE_RESOURCE, this.imageResource);
//        b.putDouble(PRICE, this.price);
//        b.putString(INSTRUCTIONS, this.instructions);

        return b;
    }


/*    public int getId() {
        return id;
    }


    public void setId(int id) {
        this.id = id;
    }


    public String getEnteredCity() {
        return enteredCity;
    }


    public void setEnteredCity(String enteredCity) {
        this.enteredCity = enteredCity;
    }


    public String getReturnedCity() {
        return returnedCity;
    }


    public void setReturnedCity(String returnedCity) {
        this.returnedCity = returnedCity;
    }*/
}