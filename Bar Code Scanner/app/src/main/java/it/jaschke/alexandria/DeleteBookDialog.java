package it.jaschke.alexandria;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import it.jaschke.alexandria.services.BookService;

/**
 * DeleteBookDialog
 * This class is used to prompt the user for deletion of a book in
 * the book detail layout
 **/

public class DeleteBookDialog extends DialogFragment {
    private static final String LOG_TAG = "DeleteBookDialog";
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        final Bundle arguments = getArguments();
        final String ean;
        ean = arguments.getString("ean");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
        // Get the inflated view
        View myDialogView = inflater.inflate(R.layout.delete_book_dialog, null, false);
        // Set myDialog as the AlertDialog view
        builder.setView(myDialogView);
        // Set cancelable to true
        builder.setCancelable(true);

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.e(LOG_TAG, "Delete -> " + true);
                Log.e(LOG_TAG, "arguments -> " + arguments);

                    Intent bookIntent = new Intent(getActivity(), BookService.class);
                    bookIntent.putExtra(BookService.EAN, ean);
                    bookIntent.setAction(BookService.DELETE_BOOK);
                    getActivity().startService(bookIntent);
                    getActivity().getSupportFragmentManager().popBackStack();


            }
        });
        // Create the final Alert Dialog
        AlertDialog myDialog = builder.create();
        myDialog.setCanceledOnTouchOutside(true);
        return myDialog;
    }
}
