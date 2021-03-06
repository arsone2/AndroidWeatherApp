package com.example.arsone.weather;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
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
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class CitiesListFragment extends Fragment implements
        OnItemClickListener,
        View.OnClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {


    private TextView titleTextView;

    private ListView citiesListView;

    private static boolean isDeleteMode;


    public interface Callbacks {

        void onCityItemSelected(int id, String enteredName, String dataUpdateTime, int unitsFormat);

        void onAddCity();

        void onDeleteCity();

        void viewData();

        MainActivity.Settings readSettingsFromDB();

        void showSettings();

        void syncData();
    }

    private Callbacks activity;

    private CursorAdapter cityCursorAdapter;

    private static boolean mMenuVisible = true;

    private int mUnitsFormat;
    private int mSortCities;


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

        Button cancelDelete = (Button) view.findViewById(R.id.button_cancel_delete);
        cancelDelete.setOnClickListener(this);

        Button deleteItems = (Button) view.findViewById(R.id.button_delete);
        deleteItems.setOnClickListener(this);

        //  ((Button) view.findViewById(R.id.button_backup)).setOnClickListener(this);

        citiesListView = (ListView) view.findViewById(R.id.citiesListView);

        citiesListView.setOnItemClickListener(this);

        titleTextView = (TextView) view.findViewById(R.id.titleTextView);

        // uncheck all items for delete
        CityCursorAdapter.setCheckboxesVisibility(false);

        cityCursorAdapter = new CityCursorAdapter(getContext(), null, 0);
        citiesListView.setAdapter(cityCursorAdapter);

        initLoader();

        return view;
    }


    public void initLoader() {

     //   Log.d("AAAAA", "CitiesListFragment - initLoader");

        // get settings data from DB
        MainActivity.Settings settings = activity.readSettingsFromDB();
        // MainActivity.Settings settings = activity.readSettings();
        mUnitsFormat = settings.getUnitsFormat();
        mSortCities = settings.getSortCities();

        // set units format
        CityCursorAdapter.setUnitsFormat(mUnitsFormat);

        // --------------------------------------------------------------
        // IMPORTANT !!! Change loader for different query
        Loader loader = getLoaderManager().getLoader(MainActivity.LOADER_CITIES_ID);

        if (loader != null && !loader.isReset()) {
            getLoaderManager().restartLoader(MainActivity.LOADER_CITIES_ID, null, this);
        //    Log.d("AAAAA", "restartLoader");
        } else {
            getLoaderManager().initLoader(MainActivity.LOADER_CITIES_ID, null, this);
        //    Log.d("AAAAA", "initLoader");
        }
        // --------------------------------------------------------------
    }


    // ------------------------------------------------------------
    // LoaderManager.LoaderCallbacks methods
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        Calendar c = Calendar.getInstance();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = df.format(c.getTime());

        String sortOrder = DataContract.CityEntry._ID; // default sort order: _id

        if (mSortCities == 0)
            sortOrder = "ORDER BY c." + DataContract.CityEntry._ID + " DESC";
        else if (mSortCities == 1)
            sortOrder = "ORDER BY " + DataContract.CityEntry.COLUMN_ENTERED_CITY + " ASC";

        return new CursorLoader(getContext(),
                Uri.parse(DataContentProvider.CITY_WEATHER_CONTENT_URI.toString() + "/" + formattedDate),
                null,
                null, //DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?", // selection: city ID
                null, // new String[]{ formattedDate }, // selectionArgs
                sortOrder // DataContract.WeatherEntry._ID // sort order
        );
    }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {

        // Swap the new cursor in.  (The framework will take care of closing the
        // old cursor once we return.)
        cityCursorAdapter.swapCursor(cursor);

        // set title
        if (cursor.getCount() > 0) {

            titleTextView.setVisibility(View.GONE);

        } else { // cities DB empty

            titleTextView.setVisibility(View.VISIBLE);
            titleTextView.setText(R.string.cities_title_cities_empty);
        }
    }


    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed.  We need to make sure we are no
        // longer using it.
        cityCursorAdapter.swapCursor(null);
    }
    // ------------------------------------------------------------

    // citiesListView item click
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        if (isDeleteMode) {
            return;
        }

        Cursor cursor = (Cursor) parent.getItemAtPosition(position);
        int cityID = cursor.getInt(cursor.getColumnIndex(DataContract.CityEntry._ID));
        String enteredCity = cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_ENTERED_CITY));
        String dataUpdateTime = cursor.getString(cursor.getColumnIndex(DataContract.CityEntry.COLUMN_UPDATE_TIMESTAMP));

        ///    Log.d("AAAAA", "dataUpdateTime = " + dataUpdateTime);

        activity.onCityItemSelected(cityID, enteredCity, dataUpdateTime, mUnitsFormat); // pass selection to MainActivity
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button_cancel_delete:

                onButtonCancelDelete();

                break;

            case R.id.button_delete:

                onButtonDelete();

                break;

 /*           case R.id.button_backup:

                backupDatabase();

                break;*/
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

        final List<String> cityWhereList = new ArrayList<>();
        List<String> weatherWhereList = new ArrayList<>();

        final List<String> argsList = new ArrayList<>();

        for (int i = 0; i < CityCursorAdapter.selectedItemsArray.size(); i++) {

            int key = CityCursorAdapter.selectedItemsArray.keyAt(i);

            if (CityCursorAdapter.selectedItemsArray.get(key)) {

                //      Log.d("AAAAA" , "key = " + key);
                cityWhereList.add(DataContract.CityEntry._ID + "=?");
                weatherWhereList.add(DataContract.WeatherEntry.COLUMN_CITY_ID_FK + "=?");
                argsList.add(String.valueOf(key));
            }
        }

        // ic_delete_item only checked items
        if (cityWhereList.size() > 0) {

            final String citySelection = TextUtils.join(" OR ", cityWhereList);
            final String weatherSelection = TextUtils.join(" OR ", weatherWhereList);
            //Log.d("AAAAA", "citySelection = " + citySelection);
            //   String selectionArgs = TextUtils.join(" ", argsList);
            //   Log.d("AAAAA", "selectionArgs = " + selectionArgs);

            final String[] citySelectionArgs = argsList.toArray(new String[argsList.size()]);

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppCompatAlertDialogStyle);
            builder.setTitle(R.string.delete_dialog_question);
            builder.setMessage(R.string.delete_dialog_text);
            builder.setPositiveButton(R.string.delete_dialog_ok, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {


/*                    Log.d("AAAAA" , "citySelection = " + citySelection);
                    Log.d("AAAAA" , "weatherSelection = " + weatherSelection);

                    for(int i = 0; i < citySelectionArgs.length; i++)
                        Log.d("AAAAA" , "citySelectionArgs = " + citySelectionArgs[i]);*/

                    // Log.d("AAAAA" , "argsList = " + argsList);

                    // as has foreign key from "city" table to child table "weather" need
                    // to ic_delete_item data in "weather" table first
                    ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

                    // ic_delete_item from "weather" table
                    ops.add(ContentProviderOperation.newDelete(DataContentProvider.WEATHER_CONTENT_URI)
                            .withSelection(weatherSelection, citySelectionArgs)
                            .build());

                    // ic_delete_item from "city" table
                    ops.add(ContentProviderOperation.newDelete(DataContentProvider.CITY_CONTENT_URI)
                            .withSelection(citySelection, citySelectionArgs)
                            .build());

                    try {
                        ContentProviderResult[] cpResults = getActivity().getContentResolver()
                                .applyBatch(DataContentProvider.AUTHORITY, ops);

                        ///  if (cpResults != null) {

                        ///     Log.e("AAAAA", "cpResults[1].count = " + cpResults[1].count);
                        //Log.e("AAAAA","cpResults = " + cpResults.);
                        //Log.e("AAAAA","cpResults = " + cpResults.toString());

    /*                            Toast.makeText(getContext(), getString(R.string.rows_deleted)
                                        + cpResults[1].count, Toast.LENGTH_SHORT).show();*/

                        Toast.makeText(getContext(), getString(R.string.rows_deleted,
                                cpResults[1].count.toString()), Toast.LENGTH_SHORT).show();
                        hide(); // hide delete panel
                    } catch (RemoteException e) {
                        Log.e("AAAAA", "RemoteException " + e.getMessage());
                    } catch (OperationApplicationException e) {
                        Log.e("AAAAA", "OperationApplicationException: " + e.getMessage());
                        ops.clear();
                    }
                }
            });

            builder.setNegativeButton(R.string.delete_dialog_cancel, new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    hide(); // hide delete panel
                }
            });

            builder.show(); // show dialog
        } else {

            Toast.makeText(getContext(), R.string.select_records_for_delete, Toast.LENGTH_SHORT).show();
        }
    }


    public void hide() {

        CityCursorAdapter.setCheckboxesVisibility(false);

        CityCursorAdapter.uncheckAllItems();

        cityCursorAdapter.notifyDataSetChanged();

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

        if (deletePanel == null)
            return;

        if (visible)
            deletePanel.setVisibility(View.VISIBLE);
        else
            deletePanel.setVisibility(View.GONE);
    }


    private void menuVisibility(boolean visibility) {

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

        for (int index = 0; index < menu.size(); index++) {

            MenuItem menuItem = menu.getItem(index);

            if (menuItem != null) {

                // hide the menu items if the drawer is open
                menuItem.setVisible(mMenuVisible);
            }
        }

        super.onPrepareOptionsMenu(menu);
    }


    // handle choice from options menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_add:

                activity.onAddCity();

                return true;

            case R.id.action_delete:

                onActionDeleteCity();

                return true;

            case R.id.action_view:

                activity.viewData();

                return true;

            case R.id.action_settings:

                activity.showSettings();

                return true;

            case R.id.action_sync:

                activity.syncData();

                return true;
        }
        return super.onOptionsItemSelected(item);
    }


/*    private void backupDatabase() {

        ///     Log.d("AAAAA", "backup Database begins");

        Toast.makeText(getContext(), "backupDatabase", Toast.LENGTH_SHORT).show();

        String[] dirs = getStorageDirectories();

//        for (String dir : dirs) {
//
//            Log.d("AAAAA", "dir = " + dir);
//        }

        // dir = /storage/extSdCard
        // dir = /storage/emulated/0

        String sd = dirs[1];

        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;

        String currentDBPath = "/data/"
                + getActivity().getApplicationContext().getPackageName()
                + "/databases/" + DataContract.DATABASE_NAME;

        ///      Log.d("AAAAA", "currentDBPath = " + currentDBPath);

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

    }*/


    //  private static final Pattern DIR_SEPARATOR = Pattern.compile("/");

    /**
     * Returns all available SD-Cards in the system (include emulated)
     * <p>
     * Warning: Hack! Based on Android source code of version 4.3 (API 18)
     * Because there is no standart way to get it.
     * Test on future Android versions 4.4+
     *
     * @return paths to all available SD-Cards in the system (include emulated)
     */

 /*   public static String[] getStorageDirectories() {
        // Final set of paths
        final Set<String> rv = new HashSet<String>();
        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                rv.add("/storage/sdcard0");
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        return rv.toArray(new String[rv.size()]);
    }*/
}