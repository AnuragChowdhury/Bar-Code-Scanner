package it.jaschke.alexandria;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.v4.app.DialogFragment;

import com.squareup.picasso.Picasso;

import it.jaschke.alexandria.data.AlexandriaContract;


public class BookDetail extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EAN_KEY = "EAN";
    private final int LOADER_ID = 10;
    private View rootView;
    private String ean;
    private String bookTitle;
    // I changed this variable to static to fix crash on orientation change
    private static ShareActionProvider shareActionProvider;
    private static final String LOG_TAG = "BookDetail";
    public BookDetail(){
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Hide the keyboard if it was left open from the previous activity
        hideSoftKeyboard();
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(LOG_TAG, "onCreateView -> " + true);
        Bundle arguments = getArguments();
        if (arguments != null) {
            ean = arguments.getString(BookDetail.EAN_KEY);
            getLoaderManager().restartLoader(LOADER_ID, null, this);
        }

        rootView = inflater.inflate(R.layout.fragment_full_book, container, false);
        rootView.findViewById(R.id.delete_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DialogFragment sortDialog = new DeleteBookDialog();
                Bundle args = new Bundle();
                args.putString("ean", ean);
                sortDialog.setArguments(args);
                // Invoke the show method on the sortDialog object
                sortDialog.show(getFragmentManager(), "delete_book");

                // I disabled this so we can manage the deletion with a dialog fragment.
                // Just deleting the record didn't seem right, we need a confirmation from the user
                // to actually delete it.

                /*Intent bookIntent = new Intent(getActivity(), BookService.class);
                bookIntent.putExtra(BookService.EAN, ean);
                bookIntent.setAction(BookService.DELETE_BOOK);
                getActivity().startService(bookIntent);
                getActivity().getSupportFragmentManager().popBackStack();*/
            }
        });
        return rootView;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        Log.e(LOG_TAG, "onCreateOptionsMenu -> " + true);
        inflater.inflate(R.menu.book_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
    }

    @Override
    public android.support.v4.content.Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.buildFullBookUri(Long.parseLong(ean)),
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        if (!data.moveToFirst()) {
            return;
        }
        Log.e(LOG_TAG, "onLoadFinished -> " + true);
        bookTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.TITLE));
        ((TextView) rootView.findViewById(R.id.fullBookTitle)).setText(bookTitle);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + bookTitle);
        Log.e(LOG_TAG, "onLoadFinished sharedIntent-> " + shareIntent);
        // The app crashed here when turning from portrait to landscape
        // I changed the shareActionProvider to static to fix this
        Log.e(LOG_TAG, "shareActionProvider-> " + shareActionProvider);
        shareActionProvider.setShareIntent(shareIntent);


        String bookSubTitle = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.SUBTITLE));
        ((TextView) rootView.findViewById(R.id.fullBookSubTitle)).setText(bookSubTitle);

        String desc = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.DESC));
        ((TextView) rootView.findViewById(R.id.fullBookDesc)).setText(desc);

        String authors = data.getString(data.getColumnIndex(AlexandriaContract.AuthorEntry.AUTHOR));

        // Check if authors is null before attempting to split array variable
        if ( authors != null ) {
            String[] authorsArr = authors.split(",");
            ((TextView) rootView.findViewById(R.id.authors)).setLines(authorsArr.length);
            ((TextView) rootView.findViewById(R.id.authors)).setText(authors.replace(",", "\n"));
        }

        String imgUrl = data.getString(data.getColumnIndex(AlexandriaContract.BookEntry.IMAGE_URL));

        // Make sure the imgUrl is not null or not empty
        if( imgUrl != null && !imgUrl.isEmpty() && Patterns.WEB_URL.matcher(imgUrl).matches()){

            // This doesn't work when there is not network connection
            // new DownloadImage((ImageView) rootView.findViewById(R.id.fullBookCover)).execute(imgUrl);
            // rootView.findViewById(R.id.fullBookCover).setVisibility(View.VISIBLE);

            // I implemented Picasso to cache the images so they display when offline
            ImageView bookCover = (ImageView) rootView.findViewById(R.id.fullBookCover);
            bookCover.setVisibility(View.VISIBLE);
            Picasso.with(this.getActivity()).
                    load(imgUrl)
                    .into(bookCover);
        }

        String categories = data.getString(data.getColumnIndex(AlexandriaContract.CategoryEntry.CATEGORY));
        ((TextView) rootView.findViewById(R.id.categories)).setText(categories);

        /**
         * I disabled the back button view because as per Android Material Design we shouldn't
         * add back buttons to our app. We should use the default back button provided by the
         * device or ActionBar
        if(rootView.findViewById(R.id.right_container)!=null){
            rootView.findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
        }**/

    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> loader) {

    }


    @Override
    public void onPause() {
        super.onPause();

        Log.e(LOG_TAG, "onPause-> " + true);
        Log.e(LOG_TAG, "onPause IS_TABLE?-> " + MainActivity.IS_TABLET);

        // There is a bug here that causes the screen to go blank on a second click
        if(MainActivity.IS_TABLET && rootView.findViewById(R.id.right_container)==null){
            Log.e(LOG_TAG, "onPause popBackStack? -> " + true);
            getActivity().getSupportFragmentManager().popBackStack();
        }


    }

    /**
     * Hides the soft keyboard
     */
    public void hideSoftKeyboard() {
        if(getActivity().getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }
}