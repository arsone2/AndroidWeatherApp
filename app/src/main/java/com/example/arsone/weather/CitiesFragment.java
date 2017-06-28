package com.example.arsone.weather;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;



public class CitiesFragment extends Fragment implements
        OnItemClickListener,
        View.OnClickListener,
   ///     AdapterView.OnItemLongClickListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        ConfirmDeleteCityItemsDialog.IConfirm{


    private TextView titleTextView;

    private ListView citiesListView;

    private static boolean isDeleteMode = false;


    public interface Callbacks {

        public void onCityItemSelected(int id, String cityName);

    ///    public void onCityItemLongTap(String enteredCity);

        public void onAddCity();

        public void onDeleteCity();

        ///   public void onCancelDelete();
    }

    private static Callbacks activity;

    // The loader's unique id. Loader ids are specific to the Activity or
    // Fragment in which they reside.
    public static final int LOADER_CITIES_ID = 0;

    public static final String TAG = "AAAAA";

    private CursorAdapter cityCursorAdapter;

    private static boolean mMenuVisible = true;


    // constructor
    public CitiesFragment() {
    }


    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        activity = (Callbacks) context;
    }


    @Override
    public void onDetach() {

        super.onDetach();
        activity = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_cities, container, false);

        setHasOptionsMenu(true); // show action bar menu


/*        Cursor cursor = getActivity().getContentResolver().query(DataContentProvider.CITY_CONTENT_URI,
                DataContract.CityEntry.CITY_ALL_COLUMNS, null, null, null, null);*/

/*        String[] from = { DataContract.CityEntry.COLUMN_ENTERED_CITY};
        int[] to = { android.R.id.text1 };*/

//        CursorAdapter cursorAdapter = new SimpleCursorAdapter(getContext(),
//                android.R.layout.simple_list_item_1, cursor, from, to, 0);


        Button cancelDelete = (Button) view.findViewById(R.id.button_cancel_delete);
        cancelDelete.setOnClickListener(this);

        Button deleteItems = (Button) view.findViewById(R.id.button_delete);
        deleteItems.setOnClickListener(this);

        // add!!!
        ((Button) view.findViewById(R.id.button_backup)).setOnClickListener(this);

        cityCursorAdapter = new CityCursorAdapter(getContext(), null, 0); // ADD !!!

        citiesListView = (ListView) view.findViewById(R.id.citiesListView);

        citiesListView.setOnItemClickListener(this);

      ///  citiesListView.setOnItemLongClickListener(this);

/*        citiesListView.setAdapter(cityCursorAdapter);

        getLoaderManager().initLoader(LOADER_CITIES_ID, null, this);*/

        citiesListView.setAdapter(cityCursorAdapter);

        initLoader();


        titleTextView = (TextView) view.findViewById(R.id.titleTextView);

        return view;
    }


/*    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);


    ///    refreshCityList();
    }*/


    @Override
    public void onResume() {

        super.onResume();

/*        citiesListView.setAdapter(cityCursorAdapter);

        initLoader();*/

       // getLoaderManager().initLoader(LOADER_CITIES_ID, null, this);

        // --------------------------------------------------------------
        // IMPORTANT !!! Change loader for different query
/*        Loader loader = getLoaderManager().getLoader(LOADER_CITIES_ID);

        if (loader != null && !loader.isReset()) {
            getLoaderManager().restartLoader(LOADER_CITIES_ID, null, this);
        } else {
            getLoaderManager().initLoader(LOADER_CITIES_ID, null, this);
        }*/
        // --------------------------------------------------------------
    }


    private void initLoader(){

        // --------------------------------------------------------------
        // IMPORTANT !!! Change loader for different query
        Loader loader = getLoaderManager().getLoader(LOADER_CITIES_ID);

        if (loader != null && !loader.isReset()) {
            getLoaderManager().restartLoader(LOADER_CITIES_ID, null, this);
        } else {
            getLoaderManager().initLoader(LOADER_CITIES_ID, null, this);
        }
        // --------------------------------------------------------------
    }

    // ------------------------------------------------------------
    // LoaderManager.LoaderCallbacks methods
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        return new CursorLoader(getContext(), DataContentProvider.CITY_CONTENT_URI,
                null, null, null, null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        cityCursorAdapter.swapCursor(cursor);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        cityCursorAdapter.swapCursor(null);
    }
    // ------------------------------------------------------------


    public void refreshCityList() {

  ///      initLoader();

/*        getLoaderManager().restartLoader(LOADER_CITIES_ID, null, this);

        cityCursorAdapter.notifyDataSetChanged();*/

   ///   cityCursorAdapter.notifyDataSetChanged();

        Log.d("AAAAA", "refreshCityList !!!");

        // scroll ListView to start position
        citiesListView.smoothScrollToPosition(0);
    }


    // citiesListView item click
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (isDeleteMode) {
            return;
        }

        Cursor cursor = (Cursor) parent.getItemAtPosition(position);
        int cityID = cursor.getInt(cursor.getColumnIndex(DataContract.CityEntry._ID));
        String enteredCity = cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY));

        activity.onCityItemSelected(cityID, enteredCity); // pass selection to MainActivity
    }




/*    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

        if (isDeleteMode) {
            return true;
        }

        Cursor cursor = (Cursor) parent.getItemAtPosition(position);

        String enteredCity = cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY));

        activity.onCityItemLongTap(enteredCity);

        return true;
    }*/


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button_cancel_delete:

                onButtonCancelDelete();

                break;

            case R.id.button_delete:

                onButtonDelete();

                break;


            case R.id.button_backup:

                backupDatabase();

                break;
        }
    }


    private void onActionDeleteCity() {

        isDeleteMode = true;

        panelForDeleteVisibility(true);

        menuVisibility(false);

        CityCursorAdapter.setCheckboxesVisibility(true);

        cityCursorAdapter.notifyDataSetChanged();
    }


    private void onButtonCancelDelete() {

        isDeleteMode = false;

        panelForDeleteVisibility(false);

        menuVisibility(true);

        CityCursorAdapter.setCheckboxesVisibility(false);

        CityCursorAdapter.uncheckAllItems();

        cityCursorAdapter.notifyDataSetChanged();
    }


    private void onButtonDelete() {

        List<String> cityWhereList = new ArrayList<String>();
        List<String> weatherWhereList = new ArrayList<String>();

        List<String> argsList = new ArrayList<String>();

        // get all checked cities for delete
        for (int i = 0; i < citiesListView.getChildCount(); i++) {

            View v = (View) citiesListView.getChildAt(i);

            if (v != null) {

                CheckBox checkbox = (CheckBox) v.findViewById(R.id.checkboxForDelete);

                if (checkbox != null && checkbox.isChecked()) {

                    cityWhereList.add(DataContract.CityEntry._ID + "=?");
                    weatherWhereList.add(DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?");

                    argsList.add(String.valueOf(checkbox.getTag()));
                }
            }
        }

        // delete only checked items
        if (cityWhereList.size() > 0) {

            String citySelection = TextUtils.join(" OR ", cityWhereList);
            String weatherSelection = TextUtils.join(" OR ", weatherWhereList);
            //Log.d("AAAAA", "citySelection = " + citySelection);

            //   String selectionArgs = TextUtils.join(" ", argsList);
            //   Log.d("AAAAA", "selectionArgs = " + selectionArgs);

            String[] citySelectionArgs = argsList.toArray(new String[argsList.size()]);

            ConfirmDeleteCityItemsDialog deleteDialog = new ConfirmDeleteCityItemsDialog();

            deleteDialog.setTargetFragment(this,1);

            Bundle bundle = new Bundle();

            bundle.putString("citySelection", citySelection);
            bundle.putString("weatherSelection", weatherSelection);

            bundle.putStringArray("citySelectionArgs", citySelectionArgs);

            deleteDialog.setArguments(bundle);

            deleteDialog.show(getFragmentManager(), "delete dialog");

          } else {

            Toast.makeText(getContext(), "Выберите записи для удаления", Toast.LENGTH_SHORT).show();
        }


    }


    public void hide() {

        CityCursorAdapter.setCheckboxesVisibility(false);

        CityCursorAdapter.uncheckAllItems();

        isDeleteMode = false;

        RelativeLayout hidePanel = (RelativeLayout) getActivity().findViewById(R.id.delete_panel);

        hidePanel.setVisibility(View.GONE);

        // menu set visible
        mMenuVisible = true;
        getActivity().invalidateOptionsMenu();


        activity.onDeleteCity();
    }


    private void panelForDeleteVisibility(boolean visible) {

        RelativeLayout deletePanel = (RelativeLayout) getActivity().findViewById(R.id.delete_panel);

        if (visible)
            deletePanel.setVisibility(View.VISIBLE);
        else
            deletePanel.setVisibility(View.GONE);
    }


    private void menuVisibility(boolean visibility){

        mMenuVisible = visibility;
        getActivity().invalidateOptionsMenu();
    }


    // display this fragment's menu items
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.city_fragment_menu, menu);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        for(int index = 0 ; index < menu.size() ; index ++) {

            MenuItem menuItem = menu.getItem(index);

            if (menuItem != null) {
                // hide the menu items if the drawer is open
                menuItem.setVisible(mMenuVisible);
            }
        }

        super.onPrepareOptionsMenu(menu);
/*        menu.findItem(R.id.action_add).setVisible(!isDeleteMode);
        menu.findItem(R.id.action_delete).setVisible(!isDeleteMode);*/
    }


    // handle choice from options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_add:

                if (isDeleteMode) {
                    return true;
                }

                activity.onAddCity();
                return true;

            case R.id.action_delete:

                onActionDeleteCity();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private void backupDatabase() {

        Log.d("AAAAA", "backup Database begins");

        Toast.makeText(getContext(), "backupDatabase", Toast.LENGTH_SHORT).show();

        //File sd = Environment.getExternalStorageDirectory();

         //getExternalSDCardDirectory();

        String[] dirs = getStorageDirectories();

        for (String dir:dirs ){

            Log.d("AAAAA", "dir = " + dir);
        }

        // dir = /storage/extSdCard
        // dir = /storage/emulated/0

        String sd = dirs[1];

        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;

        String currentDBPath = "/data/"
                + getActivity().getApplicationContext().getPackageName()
                + "/databases/" + DataContract.DATABASE_NAME;

        // String currentDBPath = getActivity().getDatabasePath(DataContract.DATABASE_NAME).getPath();

        Log.d("AAAAA", "currentDBPath = " + currentDBPath);

        String backupDBPath = DataContract.DATABASE_NAME;
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);

        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(getContext(), "DB Exported!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("AAAAA", "backup error: " + e.getMessage());
        }

    }

/*    private File getExternalSDCardDirectory()
    {
        File innerDir = Environment.getExternalStorageDirectory();
        File rootDir = innerDir.getParentFile();
        File firstExtSdCard = innerDir ;
        File[] files = rootDir.listFiles();
        for (File file : files) {
            if (file.compareTo(innerDir) != 0) {
                firstExtSdCard = file;
                break;
            }
        }
        //Log.i("2", firstExtSdCard.getAbsolutePath().toString());
        return firstExtSdCard;
    }*/

    private static final Pattern DIR_SEPARATOR = Pattern.compile("/");

    /**
     * Raturns all available SD-Cards in the system (include emulated)
     *
     * Warning: Hack! Based on Android source code of version 4.3 (API 18)
     * Because there is no standart way to get it.
     * TODO: Test on future Android versions 4.4+
     *
     * @return paths to all available SD-Cards in the system (include emulated)
     */
    public static String[] getStorageDirectories()
    {
        // Final set of paths
        final Set<String> rv = new HashSet<String>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if(TextUtils.isEmpty(rawEmulatedStorageTarget))
        {
            // Device has physical external storage; use plain paths.
            if(TextUtils.isEmpty(rawExternalStorage))
            {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            }
            else
            {
                rv.add(rawExternalStorage);
            }
        }
        else
        {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                rawUserId = "";
            }
            else
            {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try
                {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                }
                catch(NumberFormatException ignored)
                {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if(TextUtils.isEmpty(rawUserId))
            {
                rv.add(rawEmulatedStorageTarget);
            }
            else
            {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if(!TextUtils.isEmpty(rawSecondaryStoragesStr))
        {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        return rv.toArray(new String[rv.size()]);
    }
}