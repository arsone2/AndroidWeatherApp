package com.example.arsone.weather;


import android.app.Dialog;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.DialogInterface;
import android.content.OperationApplicationException;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;

public class ConfirmDeleteCityItemsDialog extends AppCompatDialogFragment {

    public interface IConfirm{

        public void hide();
    }

    CitiesFragment callback;

/*    private void hide() {

        CityCursorAdapter.setCheckboxesVisibility(false);

        CityCursorAdapter.uncheckAllItems();

        //   cityCursorAdapter.notifyDataSetChanged();

        isDeleteMode = false;

        RelativeLayout hidePanel = (RelativeLayout) getActivity().findViewById(R.id.delete_panel);

        hidePanel.setVisibility(View.GONE);

        // menu set visible
        mMenuVisible = true;
        getActivity().invalidateOptionsMenu();
    }*/


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final String citySelection = getArguments().getString("citySelection");
        final String weatherSelection = getArguments().getString("weatherSelection");

        final String[] citySelectionArgs = getArguments().getStringArray("citySelectionArgs");


        try {
            callback = (CitiesFragment) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling fragment must implement DialogClickListener interface");
        }


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.delete_dialog_question);
        builder.setMessage(R.string.delete_dialog_text);
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setCancelable(true);

        builder.setPositiveButton(R.string.delete_dialog_ok, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {


                        // as has foreign key from "city" table to child table "weather" need
                        // to delete data in "weather" table first

                        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

                        // delete from "weather" table
                        ops.add(ContentProviderOperation.newDelete(DataContentProvider.WEATHER_CONTENT_URI)
                                .withSelection(weatherSelection, citySelectionArgs)
                                .build());

                        // delete from "city" table
                        ops.add(ContentProviderOperation.newDelete(DataContentProvider.CITY_CONTENT_URI)
                                .withSelection(citySelection, citySelectionArgs)
                                .build());

                        try {
                            /// Uri rowsDeleted;

                            ContentProviderResult[] cpResults = getActivity().getContentResolver()
                                    .applyBatch(DataContentProvider.AUTHORITY, ops);

                            if (cpResults != null) {

                                Log.e("AAAAA","cpResults[1].count = " + cpResults[1].count);
                                //Log.e("AAAAA","cpResults = " + cpResults.);
                                //Log.e("AAAAA","cpResults = " + cpResults.toString());

                                Toast.makeText(getContext(), getString(R.string.rows_deleted)
                                        + cpResults[1].count, Toast.LENGTH_SHORT).show();

                             //   getTargetFragment().setNotify();

                                callback.hide();

                                ///   activity.onDeleteCity(); // refresh CitiesFragment


                            }
                        } catch (RemoteException e) {
                            Log.e("AAAAA","RemoteException " +  e.getMessage());
                        }
                        catch (OperationApplicationException e) {
                            Log.e("AAAAA", "OperationApplicationException: " + e.getMessage());
                            ops.clear();
                        }




//                            activity.onDeleteCity(); // refresh CitiesFragment

                        // Toast.makeText(getContext(),getString(R.string.rows_deleted) + rowsDeleted, Toast.LENGTH_SHORT).show();

                    }
                }
        );

        builder.setNegativeButton(R.string.delete_dialog_cancel, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        callback.hide();
                    }
                }
        );

        return builder.create(); // return the AlertDialog
    }
}