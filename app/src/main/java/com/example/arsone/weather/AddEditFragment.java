package com.example.arsone.weather;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

// https://stackoverflow.com/questions/4165414/how-to-hide-soft-keyboard-on-android-after-clicking-outside-edittext


public class AddEditFragment extends Fragment {


    public interface Callbacks {

        public  void onSaveCity(String enteredCity);

        public void onCancelAddEdit();
    }

    private Callbacks activity;

    private TextView cityEditText;

 //   private TextView titleAddEditTextView;

    private String enteredCity;

    // constructor
    public AddEditFragment(){}


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_add_edit, container, false);

        cityEditText = (TextView) view.findViewById(R.id.cityEditText);
     //   titleAddEditTextView = (TextView) view.findViewById(R.id.titleAddEditTextView);
        Button saveCityButton = (Button) view.findViewById(R.id.saveCityButton);
        Button cancelButton = (Button) view.findViewById(R.id.editCancelButton);

        // add listeners
        saveCityButton.setOnClickListener(saveCityButtonClicked);
        cancelButton.setOnClickListener(cancelButtonClicked);

/*        view.findViewById(R.id.add_edit_city_layout).setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
            }
        });*/

        return view;
    }


    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        activity = (AddEditFragment.Callbacks) context;
    }


    @Override
    public void onDetach() {

        super.onDetach();
        activity = null;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if (menu != null) {

            MenuItem m = menu.findItem(R.id.action_add);

            if(m != null)
                m.setVisible(false); // hide options menu
        }
        super.onCreateOptionsMenu(menu, inflater);
    }


    // "cancel" button clicked
    View.OnClickListener cancelButtonClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

          activity.onCancelAddEdit();
        }
    };


    // "save" button clicked
    View.OnClickListener saveCityButtonClicked = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            enteredCity = cityEditText.getText().toString().trim();

            if (enteredCity.length() != 0)
            {
                // save entered city to database
                ContentValues values = new ContentValues();

                values.put(DataContract.CityEntry.COLUMN_ENTERED_CITY, enteredCity);

                Uri cityUri = getActivity().getContentResolver().insert(DataContentProvider.CITY_CONTENT_URI, values);

                Log.d("AAAAA", "Inserted = " + cityUri.getLastPathSegment());

               activity.onSaveCity(enteredCity);

            }
            else // required entered_city returned_name is blank, so display error dialog
            {

                Toast.makeText(getActivity(), R.string.message_fill_city, Toast.LENGTH_SHORT).show();


                /*
                DialogFragment errorSaving =
                        new DialogFragment()
                        {
                            @Override
                            public Dialog onCreateDialog(Bundle savedInstanceState)
                            {
                                AlertDialog.Builder builder =
                                        new AlertDialog.Builder(getActivity());
                                builder.setMessage(R.string.error_message);
                                builder.setPositiveButton(R.string.ok, null);
                                return builder.create();
                            }
                        };

                errorSaving.show(getFragmentManager(), "error saving contact");
                */
            }
        } // end method onClick
    };
}