package com.himanshubahuguna.android.popularmovieshb;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by hbahuguna on 12/25/2015.
 */
public class FavoritesActivity extends AppCompatActivity implements FavoritesFragment.Callback {

    private boolean mTwoPane;
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            setContentView(R.layout.activity_favorites);
            if (findViewById(R.id.movie_detail_container) != null) {
                mTwoPane = true;
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.favmovie_detail_container, new DetailActivityFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            } else {
                mTwoPane = false;
                getSupportActionBar().setElevation(0f);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_favorites, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Uri contentUri) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using a
            // fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailActivityFragment.DETAIL_URI, contentUri);

            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.favmovie_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, MovieDetailsActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }


}
