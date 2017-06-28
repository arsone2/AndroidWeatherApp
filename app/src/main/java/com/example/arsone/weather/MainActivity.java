package com.example.arsone.weather;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;

import com.example.arsone.weather.data.City;


public class MainActivity extends AppCompatActivity implements
        CitiesFragment.Callbacks,
   ///     DetailsFragment.Callbacks,
        AddEditFragment.Callbacks {


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // set constant orientation for different screens
        if (findViewById(R.id.onePaneLayout) != null) { // phone

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else { // tablet
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        // set action bar icon
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.drawable.white_balance_sunny);

        if (savedInstanceState != null) // create activity only one time?
            return;

        if (findViewById(R.id.onePaneLayout) != null) { // phone

            CitiesFragment fragment = new CitiesFragment();

            // use a FragmentTransaction to display the Fragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.onePaneLayout, fragment)
                    //  .addToBackStack(null) // do not add empty activity to backstack !!!
                    .commit(); // causes fragment to display
        }
    }


/*        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/


    // in the CitiesFragment city selected
    @Override
    public void onCityItemSelected(int cityID, String cityName) {

//        Log.d("AAAAA", "onCityItemSelected - cityID = " + cityID);
//        Log.d("AAAAA", "onCityItemSelected - cityName = " + cityName);

        if (findViewById(R.id.onePaneLayout) == null) { // tablet
            showDetails(R.id.rightFrameLayout, cityID, cityName);
        } else { // phone

            showDetails(R.id.onePaneLayout, cityID, cityName);
        }
    }


    // listItem click
    private void showDetails(int viewID, int cityID, String cityName) {

        DetailsFragment fragment = new DetailsFragment();

        // put selected city to DetailsFragment
        Bundle bundle = new Bundle();

        bundle.putInt(City.CITY_ID, cityID);
        bundle.putString(City.ENTERED_CITY, cityName);

        fragment.setArguments(bundle);

        // use a FragmentTransaction to display the fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, fragment);
        ///  transaction.addToBackStack(null); // COMMENTED!!!

        if (findViewById(R.id.onePaneLayout) != null) { // add to backstack for phone screen only
            transaction.addToBackStack(null);
        }

        transaction.commit(); // causes fragment to display
        // getSupportFragmentManager().popBackStack();
    }


    @Override
    public void onSaveCity(String enteredCity) {

        hideKeyboard();

        this.onBackPressed(); // close addEditFragment

        refreshCityListView(); // refresh for tablet only
    }


    public void onCancelAddEdit() {

        hideKeyboard();


        this.onBackPressed(); // close addEditFragment
    }


    // ----------------------------------------------------
    // menu "add item"
    @Override
    public void onAddCity() {

        if (findViewById(R.id.onePaneLayout) == null) { // tablet

            AddEditCity(R.id.rightFrameLayout);
        } else { // phone

            AddEditCity(R.id.onePaneLayout);
        }
    }


    public void onDeleteCity() {

        // remove right fragment on tablet
        if (findViewById(R.id.rightFrameLayout) != null) { // is tablet

            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager()
                    .findFragmentById(R.id.rightFrameLayout))
                    .commit();
        }
        ///  this.onBackPressed(); // close addEditFragment

        //    getSupportFragmentManager().popBackStack(); // remove details fragment on tablet
    }


    private void AddEditCity(int viewID) {

        AddEditFragment fragment = new AddEditFragment();

/*        // put selected city to DetailsFragment
        Bundle bundle = new Bundle();

        bundle.putInt(City.CITY_ID, city.getId());
        bundle.putString(City.ENTERED_CITY, city.getEnteredCity());

        detailsFragment.setArguments(bundle);*/

        // use a FragmentTransaction to display the fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(viewID, fragment);
        transaction.addToBackStack(null);
        transaction.commit(); // causes fragment to display
    }


    public void hideKeyboard() {

        InputMethodManager inputManager = (InputMethodManager)
                this.getSystemService(Context.INPUT_METHOD_SERVICE);

        if (this.getCurrentFocus() != null) {
            inputManager.hideSoftInputFromWindow(this.getCurrentFocus()
                    .getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    // force CitiesFragment ListView refresh for tablet only
    private void refreshCityListView() {

        CitiesFragment citiesFragment = null;

   ///     Fragment f = getSupportFragmentManager().findFragmentById(R.id.onePaneLayout);

        if (findViewById(R.id.citiesContainer) != null) { // is tablet

            citiesFragment = (CitiesFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.citiesContainer);

            // try {
/*                citiesFragment = (CitiesFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.onePaneLayout);*/

/*            } catch (ClassCastException e) {
                throw new ClassCastException("Calling fragment must implement Callback interface");
            }*/
        }
/*        else if (findViewById(R.id.onePaneLayout) != null) { // phone
            try {
                citiesFragment = (CitiesFragment) getSupportFragmentManager().findFragmentById(R.id.onePaneLayout);
            } catch (ClassCastException e) {
               //  throw new ClassCastException("Calling fragment must implement Callback interface");
                Log.d("AAAAA", "ClassCastException");

                citiesFragment = null;
            }

        }*/

        if (citiesFragment != null) {
            citiesFragment.refreshCityList();
        }
    }


 ///   @Override
 ///   public void onDetailsUpdated() {

     //   refreshCityListView();

/*        CitiesFragment citiesFragment = null;

        if (findViewById(R.id.citiesContainer) != null) { // is tablet
            citiesFragment = (CitiesFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.citiesContainer);
        }
       else if(findViewById(R.id.onePaneLayout) != null) { // phone
            citiesFragment = (CitiesFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.onePaneLayout);
        }

        if (citiesFragment != null) {
            citiesFragment.refreshCityList();
        }*/
 ///   }
}