package com.himanshubahuguna.android.popularmovieshb;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import com.himanshubahuguna.android.popularmovieshb.model.Movie;


/**
 * Created by hbahuguna on 11/24/2015.
 */
public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra(Movie.EXTRA_MOVIE)) {
            Movie movie = new Movie(intent.getBundleExtra(Movie.EXTRA_MOVIE));
            ((TextView)findViewById(R.id.movie_title)).setText(movie.getTitle());
            ((TextView)findViewById(R.id.movie_rating)).setText(movie.getRating());
            ((TextView)findViewById(R.id.movie_overview)).setText(movie.getOverview());
            ((TextView)findViewById(R.id.movie_release_date)).setText(movie.getReleaseDate());


            Uri posterUri = movie.buildPosterUri(getString(R.string.api_poster_default_size));
            Picasso.with(this)
                    .load(posterUri)
                    .into((ImageView)findViewById(R.id.movie_poster));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
