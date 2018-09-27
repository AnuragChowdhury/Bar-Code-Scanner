package it.jaschke.alexandria;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import it.jaschke.alexandria.api.BookListAdapter;
import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.data.AlexandriaContract;


public class ListOfBooks extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private BookListAdapter bookListAdapter;
    private ListView bookList;
    private int position = ListView.INVALID_POSITION;
    private EditText searchText;
    private String searchTextValue;
    // Get the clear search button image button to show or hide depending on the search values
    private ImageButton clearSearchButton;

    private final int LOADER_ID = 10;

    private static final String LOG_TAG = "MainActivity";

    public ListOfBooks() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Don't open the keyboard automatically when the activity starts (I find it annoying)
        this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Cursor cursor = getActivity().getContentResolver().query(
                AlexandriaContract.BookEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );


        bookListAdapter = new BookListAdapter(getActivity(), cursor, 0);
        View rootView = inflater.inflate(R.layout.fragment_list_of_books, container, false);

        clearSearchButton = (ImageButton) rootView.findViewById(R.id.clearSearchButton);

        searchText = (EditText) rootView.findViewById(R.id.searchText);
        searchText.addTextChangedListener(new TextWatcher()
        {

            @Override
            public void onTextChanged( CharSequence arg0, int arg1, int arg2, int arg3)
            {
                searchTextValue = searchText.getText().toString();
                // TODO Auto-generated method stub
                // Restart the loader on text change so the results are displayed dynamically
                // as you type
                ListOfBooks.this.restartLoader();

            }


            @Override
            public void beforeTextChanged( CharSequence arg0, int arg1, int arg2, int arg3)
            {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged( Editable arg0)
            {
                Log.e(LOG_TAG, "afterTextChanged-> " + searchTextValue);
                // TODO Auto-generated method stub
                if ( searchTextValue.length() > 0 ){
                    clearSearchButton.setVisibility(View.VISIBLE);
                }else{
                    clearSearchButton.setVisibility(View.GONE);
                }

            }
        });


        rootView.findViewById(R.id.clearSearchButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        searchText.setText("");
                        }
                    }
        );

        // I made some improvements here but decided to implemented a search as you type
        // functionality on the search edit box and remove the search button
        /**rootView.findViewById(R.id.searchButton).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String searchTextValue = searchText.getText().toString();
                        Log.e(LOG_TAG, "onClick searchText-> " + searchTextValue);
                        // Only restart the loader if the search text is not empty
                        // Otherwise the app will crash
                        if ( searchTextValue.length() > 0 ) {
                            ListOfBooks.this.restartLoader();
                        }
                        // Display a toast if the search is empty
                        else {
                            Context context = getActivity();
                            CharSequence text = "Your search is empty. It's ok, just type something and try again.";
                            int duration = Toast.LENGTH_LONG;
                            Toast toast = Toast.makeText(context, text, duration);
                            toast.show();
                        }
                    }
                }
        );**/

        bookList = (ListView) rootView.findViewById(R.id.listOfBooks);
        bookList.setAdapter(bookListAdapter);

        bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Log.e(LOG_TAG, "onItemClick-> " + true);
                Cursor cursor = bookListAdapter.getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    ((Callback) getActivity())
                            .onItemSelected(cursor.getString(cursor.getColumnIndex(AlexandriaContract.BookEntry._ID)));
                }
            }
        });
        // Close the cursor after use

        return rootView;
    }

    private void restartLoader(){
        getLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        final String selection = AlexandriaContract.BookEntry.TITLE +" LIKE ? OR " + AlexandriaContract.BookEntry.SUBTITLE + " LIKE ? ";
        String searchString = searchText.getText().toString();
        Log.e(LOG_TAG, "onCreateLoader-> " + searchString);
        if(searchString.length()>0){
            searchString = "%"+searchString+"%";
            return new CursorLoader(
                    getActivity(),
                    AlexandriaContract.BookEntry.CONTENT_URI,
                    null,
                    selection,
                    new String[]{searchString,searchString},
                    null
            );
        }

        return new CursorLoader(
                getActivity(),
                AlexandriaContract.BookEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        bookListAdapter.swapCursor(data);
        if (position != ListView.INVALID_POSITION) {
            bookList.smoothScrollToPosition(position);
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        bookListAdapter.swapCursor(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        activity.setTitle(R.string.books);
    }



}
