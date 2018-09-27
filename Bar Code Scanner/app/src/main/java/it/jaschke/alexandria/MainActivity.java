package it.jaschke.alexandria;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import it.jaschke.alexandria.api.Callback;
import it.jaschke.alexandria.zxing.IntentIntegrator;
import it.jaschke.alexandria.zxing.IntentResult;


public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks, Callback {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment navigationDrawerFragment;

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence title;
    public static boolean IS_TABLET = false;
    private BroadcastReceiver messageReciever;

    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";

    private DrawerLayout drawerLayout;

    private static final String LOG_TAG = "MainActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(LOG_TAG, "onCreate -> " + true);
        IS_TABLET = isTablet();
        if(IS_TABLET){
            setContentView(R.layout.activity_main_tablet);
        }else {
            setContentView(R.layout.activity_main);
        }

        messageReciever = new MessageReceiver();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(messageReciever,filter);
        setUpNavigationDrawer();

    }

    /**
     * setUpNavigationDrawer
     */
    private void setUpNavigationDrawer(){
        navigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navigation_drawer);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        // Close the drawers if opened. This way if the drawer is opened when you press back,
        // the drawer is closed for a better experience
        drawerLayout.closeDrawers();

        title = getTitle();
        // Set up the drawer.
        navigationDrawerFragment.setUp(R.id.navigation_drawer,
                drawerLayout);

    }

    /**
     * onActivityResult
     * @param requestCode
     * @param resultCode
     * @param intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

        if (scanResult != null) {
            Log.e(LOG_TAG, "onActivityResult -> " + scanResult.getContents());
            // Set the barcode in the EditText input
            EditText editText = (EditText) findViewById(R.id.ean);
            editText.setText(scanResult.getContents());
        }

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment nextFragment;
        // Get the backstack count and set the backstack accordingly.
        // We don't want to go back to previously opened books when the drawer is opened
        // from a book detail and the user navigates to any of the other layouts.
        // Our main view is the list, and you should always come back to it.
        // Pressing back on the list will exit the app.
        int backStackCount = fragmentManager.getBackStackEntryCount();

        switch (position){
            default:
            case 0:
                for(int i = 0; i < backStackCount; ++i) {
                    fragmentManager.popBackStack();
                }
                nextFragment = new ListOfBooks();
                break;
            case 1:
                for(int i = 1; i < backStackCount; ++i) {
                    fragmentManager.popBackStack();
                }
                nextFragment = new AddBook();
                break;
            case 2:
                for(int i = 1; i < backStackCount; ++i) {
                    fragmentManager.popBackStack();
                }
                nextFragment = new About();
                break;

        }

        Log.e(LOG_TAG, "onNavigationDrawer...Title -> " + title);
        fragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment)
                .addToBackStack((String) title)
                .commit();



    }

    public void setTitle(int titleId) {
        title = getString(titleId);
    }

    public void restoreActionBar() {
        Log.e(LOG_TAG, "restoreActionBar -> " + true);


        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        String drawerTitle = sp.getString(NavigationDrawerFragment.STATE_SELECTED_TITLE, title.toString());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        Log.e(LOG_TAG, "restoreActionBar Title -> " + drawerTitle);
        actionBar.setTitle( drawerTitle );
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!navigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(messageReciever);
        super.onDestroy();
    }

    @Override
    public void onItemSelected(String ean) {
        Log.e(LOG_TAG, "onItemSelected -> " + ean);

        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        BookDetail fragment = new BookDetail();
        fragment.setArguments(args);

        int id = R.id.container;
        if(findViewById(R.id.right_container) != null){
            id = R.id.right_container;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(id, fragment)
                .addToBackStack("Book Detail")
                .commit();

    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getStringExtra(MESSAGE_KEY)!=null){
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();
            }
        }
    }


    /*public void goBack(View view){
        getSupportFragmentManager().popBackStack();
    }*/

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onBackPressed() {
        Log.e(LOG_TAG, "onBackPressed-> " + true);

        if(getSupportFragmentManager().getBackStackEntryCount()<2){
            Log.e(LOG_TAG, "onBackPressed <2?" + true);
            finish();
        }
        // Always go back to the books list
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.edit().putInt(NavigationDrawerFragment.STATE_SELECTED_POSITION, 0).apply();
        sp.edit().putString(NavigationDrawerFragment.STATE_SELECTED_TITLE, NavigationDrawerFragment.drawerTitles[0]).apply();
        // Restore the action bar
        restoreActionBar();
        // Reset the navigation drawer
        setUpNavigationDrawer();
        super.onBackPressed();

    }

    @Override
    public void onResume(){
        Log.e(LOG_TAG, "onResume-> " + true);
        // Reset the navigation drawer
        setUpNavigationDrawer();
        // Restore the action bar
        restoreActionBar();
        super.onResume();
    }



}