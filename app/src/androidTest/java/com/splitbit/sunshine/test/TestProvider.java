package com.splitbit.sunshine.test;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.test.AndroidTestCase;
import android.util.Log;

import com.splitbit.sunshine.data.WeatherContract.LocationEntry;
import com.splitbit.sunshine.data.WeatherContract.WeatherEntry;

/**
 * Created by imsplitbit on 8/18/14.
 */
public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    // brings our database to an empty state
    public void deleteAllRecords() {
        mContext.getContentResolver().delete(
                WeatherEntry.CONTENT_URI,
                null,
                null
        );
        mContext.getContentResolver().delete(
                LocationEntry.CONTENT_URI,
                null,
                null
        );

        Cursor cursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();

        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    // Since we want each test to start with a clean slate, run deleteAllRecords
    // in setUp (called by the test runner before each test).
    public void setUp() {
        deleteAllRecords();
    }

    public void testGetType() {
        // content://com.splitbit.sunshine/weather/
        String type = mContext.getContentResolver().getType(WeatherEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.splitbit.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testLocation = "94074";
        // content://com.splitbit.sunshine/weather/94074
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocation(testLocation));
        // vnd.android.cursor.dir/com.splitbit.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_TYPE, type);

        String testDate = "20140612";
        // content://com.splitbit.sunshine/weather/94074/20140612
        type = mContext.getContentResolver().getType(
                WeatherEntry.buildWeatherLocationWithDate(testLocation, testDate));
        // vnd.android.cursor.item/com.splitbit.sunshine/weather
        assertEquals(WeatherEntry.CONTENT_ITEM_TYPE, type);

        // content://com.splitbit.sunshine/location
        type = mContext.getContentResolver().getType(LocationEntry.CONTENT_URI);
        // vnd.android.cursor.dir/com.splitbit.sunshine/location
        assertEquals(LocationEntry.CONTENT_TYPE, type);

        // content://com.splitbit.sunshine/location/1
        type = mContext.getContentResolver().getType(LocationEntry.buildLocationUri(1L));
        // vnd.android.cursor.item/com.splitbit.sunshine/location
        assertEquals(LocationEntry.CONTENT_ITEM_TYPE, type);
    }

    public void testInsertReadProvider() {
        // Create a new map of values.  This is a helper object used to store values and keys.
        ContentValues values = TestDb.createNorthPoleLocationValues();
        
        // Put the data into the db and assert that we got it back
        Uri locationInsertUri = mContext.getContentResolver().insert(
                LocationEntry.CONTENT_URI, values);
        assertTrue(locationInsertUri != null);
        long locationRowId = ContentUris.parseId(locationInsertUri);

        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        // In theory data is now inserted.  Lets pull it out and stare at it.

        // Use a new cursor to query the results
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI, // Table to query
                null, // All columns
                null, // Columns for the "where" clause
                null, // Values for the "where" clause
                null // sort order
        );

        TestDb.validateCursor(cursor, values);

        // Test LOCATION
        cursor = mContext.getContentResolver().query(
                LocationEntry.CONTENT_URI,
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, values);

        // Test LOCATION_ID
        cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null, // leaving "columns" null just returns all the columns.
                null, // cols for "where" clause
                null, // values for "where" clause
                null  // sort order
        );

        TestDb.validateCursor(cursor, values);

        // Fantastic.  Now that we have a location, add some weather
        ContentValues weatherValues = TestDb.createWeatherValues(locationRowId);

        Uri weatherInserUri = mContext.getContentResolver().insert(
                WeatherEntry.CONTENT_URI, weatherValues);
        assertTrue(weatherInserUri != null);

        // Query the results
        Cursor weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.CONTENT_URI, // Table to query
                null, // all columns
                null, // columns for the "where" clause
                null, // values for the where clause
                null // columns to group by
        );

        TestDb.validateCursor(weatherCursor, weatherValues);

        // Add the location values in with the weather data so that we can make
        // sure that the join worked and we actually get all the values back
        addAllContentValues(weatherValues, values);

        // Get the joined Weather and Location data
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocation(TestDb.TEST_LOCATION),
                null, // all the columns
                null, // columns for "where" clause
                null, // values for "where" clause
                null // sort order
        );
        TestDb.validateCursor(weatherCursor, weatherValues);

        // Get the joined Weather and Location data with a start date
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithStartDate(
                        TestDb.TEST_LOCATION, TestDb.TEST_DATE
                ),
                null, // all columns
                null, // columns for "where" clause
                null, // values for "where" clause
                null // sort order
        );
        TestDb.validateCursor(weatherCursor, weatherValues);

        // Get the joined Weather and Location data with a specific date
        weatherCursor = mContext.getContentResolver().query(
                WeatherEntry.buildWeatherLocationWithDate(
                        TestDb.TEST_LOCATION, TestDb.TEST_DATE
                ),
                null, // all columns
                null, // columns for "where" clause
                null, // values for "where" clause
                null // sort order
        );
        TestDb.validateCursor(weatherCursor, weatherValues);
    }

    public void testUpdateLocation() {
        // Create a new map of values where column names are the keys
        ContentValues values = TestDb.createNorthPoleLocationValues();

        Uri locationUri = mContext.getContentResolver().insert(
                LocationEntry.CONTENT_URI,
                values
        );
        long locationRowId = ContentUris.parseId(locationUri);

        // Verify we got a row back
        assertTrue(locationRowId != -1);
        Log.d(LOG_TAG, "New row id: " + locationRowId);

        ContentValues updatedValues = new ContentValues(values);
        updatedValues.put(LocationEntry._ID, locationRowId);
        updatedValues.put(LocationEntry.COLUMN_CITY_NAME, "Santa's Village");

        int count = mContext.getContentResolver().update(
                LocationEntry.CONTENT_URI,
                updatedValues,
                LocationEntry._ID + "= ?",
                new String[] {Long.toString(locationRowId)}
        );

        // A cursor is your primary interface to query results
        Cursor cursor = mContext.getContentResolver().query(
                LocationEntry.buildLocationUri(locationRowId),
                null,
                null,
                null,
                null
        );

        TestDb.validateCursor(cursor, updatedValues);
    }

    public void testDeleteRecordsAtEnd() {
        deleteAllRecords();
    }

    // The target api annotation is needed for the call to keySet -- we wouldn't want
    // to use this in our app, but in a test it's fine to assume a higher target.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void addAllContentValues(ContentValues destination, ContentValues source) {
        for (String key : source.keySet()) {
            destination.put(key, source.getAsString(key));
        }
    }
}
