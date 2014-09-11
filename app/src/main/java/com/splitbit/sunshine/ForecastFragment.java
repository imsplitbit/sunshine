package com.splitbit.sunshine;

/**
 * Created by imsplitbit on 7/16/14.
 */

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class ForecastFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    // Make the array adapter a private class variable so that it can be shared everywhere in
    // main thread.
    private ArrayAdapter<String> mForecastAdapter;

    // Make a log tag for app logger
    private static final String LOG_TAG = ForecastFragment.class.getSimpleName();

    // Constructor
    public ForecastFragment() {
    }

    /*
     * Private methods
     */
    private void updateWeather() {
        FetchWeatherTask weatherTask = new FetchWeatherTask(getActivity(), mForecastAdapter);
        String location = Utility.getPreferredLocation(getActivity());
        weatherTask.execute(location);
    }

    /*
     * Overrides
     */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "Entered onCreate");
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle the action bar activities
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Need to set this in order to get the callback methods needed to handle the options
        // menu.
        setHasOptionsMenu(true);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new ArrayAdapter<String>(
                // Context
                getActivity(),
                // ID of list item layout
                R.layout.list_item_forecast,
                // ID of text view
                R.id.list_item_forecast_textview,
                // list of data
                new ArrayList<String>()
        );

        // find a view from the expanded rootView
        final ListView listForecastView = (ListView) rootView.findViewById(R.id.listview_forecast);
        // bind the adapter to the View
        listForecastView.setAdapter(mForecastAdapter);

        // Setup click listener
        listForecastView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Send Toast
//                Context context = getActivity();
//                int duration = Toast.LENGTH_SHORT;
//                String message = mForecastAdapter.getItem(position).toString();
//                Toast.makeText(context, message, duration).show();

                // Setup Intent to start DetailActivity.  Must send data for activity packaged
                // inside the intent so that the activity has something to display.
                Intent detailIntent = new Intent(getActivity(), DetailActivity.class);
                String data = mForecastAdapter.getItem(position).toString();
                detailIntent.putExtra(Intent.EXTRA_TEXT, data);
                startActivity(detailIntent);

            }
        });

        return rootView;
    }
}
