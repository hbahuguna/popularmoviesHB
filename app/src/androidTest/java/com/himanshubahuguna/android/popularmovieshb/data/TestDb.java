package com.himanshubahuguna.android.popularmovieshb.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by hbahuguna on 12/6/2015.
 */
public class TestDb extends AndroidTestCase {

    private MovieDbHelper movieDbHelper;

    public static final String LOG_TAG = TestDb.class.getSimpleName();

    void deleteTheDatabase() {
        mContext.deleteDatabase(MovieDbHelper.DATABASE_NAME);
    }

    public void setup() {
        deleteTheDatabase();
        movieDbHelper = new MovieDbHelper(mContext);
    }

    public void testTablesCreated() throws Throwable {
        deleteTheDatabase();
        movieDbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = movieDbHelper.getReadableDatabase();
        Set<String> tables = new HashSet<String>();
        tables.add(MovieContract.MovieEntry.TABLE_NAME);
        tables.add(MovieContract.TrailerEntry.TABLE_NAME);
        tables.add(MovieContract.ReviewEntry.TABLE_NAME);

        deleteTheDatabase();

        assertTrue("Database not opened at all!", db.isOpen());

        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type = 'table'", null);

        assertTrue("Database not created correctly", cursor.moveToFirst());

        do {
            tables.remove(cursor.getString(0));
        } while (cursor.moveToNext());

        //all removed from tableNames
        assertTrue("Some tables not created!", tables.isEmpty());

        cursor.close();
        db.close();

    }

    public void testMovieTableColumns() {
        deleteTheDatabase();
        movieDbHelper = new MovieDbHelper(mContext);
        SQLiteDatabase db = movieDbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + MovieContract.MovieEntry.TABLE_NAME + ")", null);

        assertTrue("Error: Unable to query the database for table information.", cursor.moveToFirst());

        Set<String> movieTableCols = new HashSet<>();
        movieTableCols.add(MovieContract.MovieEntry.COLUMN_TITLE);
        movieTableCols.add(MovieContract.MovieEntry.COLUMN_RELEASE_DATE);
        movieTableCols.add(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE);
        movieTableCols.add(MovieContract.MovieEntry.COLUMN_VOTE_COUNT);
        movieTableCols.add(MovieContract.MovieEntry.COLUMN_OVERVIEW);
        movieTableCols.add(MovieContract.MovieEntry.COLUMN_POSTER_PATH);
        movieTableCols.add(MovieContract.MovieEntry.COLUMN_POPULARITY);
        movieTableCols.add(MovieContract.MovieEntry.COLUMN_RUNTIME);

        final int COL_NAME_INDEX = cursor.getColumnIndex("name");
        do {
            String colName = cursor.getString(COL_NAME_INDEX);
            Log.d(LOG_TAG, colName);
            movieTableCols.remove(colName);
        } while (cursor.moveToNext());
        assertTrue("Some columns not created on " + MovieContract.MovieEntry.TABLE_NAME + " table", movieTableCols.isEmpty());
        cursor.close();
        db.close();
    }
}
