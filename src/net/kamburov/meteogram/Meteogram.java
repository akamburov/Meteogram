package net.kamburov.meteogram;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import net.kamburov.meteogram.provider.Favorites;
import net.kamburov.meteogram.provider.Locations;

import java.io.InvalidObjectException;
import java.util.HashMap;
import java.util.List;

public class Meteogram extends Activity {

    public static final String METEOGRAM_STATE = "meteogram";
    public static final String LOCATION_ID_STATE = "locationId";
    public static final String LOCATION_CITY_STATE = "locationCity";
    public static final String LOCATION_COUNTRY_STATE = "locationCountry";

    public static final String FAVORITES_STATE = "favorites";

    private MenuItem searchMenuItem;

    private int locationId;

    private String locationCity;

    private String locationCountry;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        if (savedInstanceState != null) {
            locationId = savedInstanceState.getInt(LOCATION_ID_STATE, 0);
            locationCity = savedInstanceState.getString(LOCATION_CITY_STATE, "");
            locationCountry = savedInstanceState.getString(LOCATION_COUNTRY_STATE, "");

            Bitmap meteogram = (Bitmap) savedInstanceState.getParcelable(METEOGRAM_STATE);
            if (meteogram != null) {
                refreshMeteogram(meteogram);
            } else {
                refreshMeteogram();
            }
        } else {
            SharedPreferences settings = getPreferences(MODE_PRIVATE);
            locationId = settings.getInt(LOCATION_ID_STATE, 0);
            locationCity = settings.getString(LOCATION_CITY_STATE, "");
            locationCountry = settings.getString(LOCATION_COUNTRY_STATE, "");

            refreshMeteogram();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (searchMenuItem.isActionViewExpanded()) {
            searchMenuItem.collapseActionView();
        }

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            view(intent.getStringExtra(BaseColumns._ID),
                    intent.getStringExtra(Locations.CITY),
                    intent.getStringExtra(Locations.COUNTY));
        }

        super.onNewIntent(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ImageView meteogramView = (ImageView) findViewById(R.id.meteogramView);
        Bitmap bitmap = getBitmap(meteogramView);
        outState.putParcelable(METEOGRAM_STATE, bitmap);

        outState.putInt(LOCATION_ID_STATE, locationId);
        outState.putString(LOCATION_CITY_STATE, locationCity);
        outState.putString(LOCATION_COUNTRY_STATE, locationCountry);
    }

    private Bitmap getBitmap(ImageView imageView) {
        Bitmap bitmap = Bitmap.createBitmap(imageView.getWidth(), imageView.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        imageView.draw(canvas);
        return bitmap;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        // Configure the search info and add any event listeners
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryRefinementEnabled(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.favorites:
                showFavoritesDialog();
                return true;
            case R.id.add_to_favorites:
                addToFavorites();
                return true;
            case R.id.settings:
                //TODO
                return true;
            case R.id.refresh:
                refreshMeteogram();
                return true;
            case android.R.id.home:
                Intent intent = new Intent(this, Meteogram.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addToFavorites() {
        SharedPreferences settings = getPreferences(MODE_PRIVATE);

        try {
            Favorites favorites = new Favorites();
            String favoritesString = favorites.add(
                    settings.getString(FAVORITES_STATE, ""),
                    locationId, locationCity, locationCountry);

            SharedPreferences.Editor editor = settings.edit();
            editor.putString(FAVORITES_STATE, favoritesString);
            editor.commit();
        } catch (InvalidObjectException e) {
            //no-op
        }
    }

    private void showFavoritesDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.select_location);

        Favorites favorites = new Favorites();
        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        final List<HashMap<String, String>> items = favorites.asItems(settings.getString(FAVORITES_STATE, ""));

        ListAdapter locations = new SimpleAdapter(this, items,
                android.R.layout.simple_list_item_2,
                new String[]{Locations.CITY, Locations.COUNTY},
                new int[]{android.R.id.text1, android.R.id.text2});
        dialog.setSingleChoiceItems(locations, -1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                HashMap<String, String> selectedItem = items.get(which);
                view(
                        selectedItem.get(BaseColumns._ID),
                        selectedItem.get(Locations.CITY),
                        selectedItem.get(Locations.COUNTY));
            }
        });

        dialog.show();
    }

    private void view(String id, String city, String country) {
        locationId = Integer.parseInt(id);
        locationCity = city;
        locationCountry = country;

        SharedPreferences settings = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(LOCATION_ID_STATE, locationId);
        editor.putString(LOCATION_CITY_STATE, locationCity);
        editor.putString(LOCATION_COUNTRY_STATE, locationCountry);
        editor.commit();

        refreshMeteogram();
    }

    private void refreshMeteogram() {
        refreshMeteogram(null);
    }

    private void refreshMeteogram(Bitmap bitmap) {
        if (locationId == 0) {
            locationId = 100727011;
            locationCity = "Sofia";
            locationCountry = "Bulgaria";
            bitmap = null;
        }

        TextView meteogramText = (TextView) findViewById(R.id.meteogramText);
        meteogramText.setText(String.format("%s, %s", locationCity, locationCountry));

        ImageView meteogramView = (ImageView) findViewById(R.id.meteogramView);

        if (bitmap != null) {
            meteogramView.setImageBitmap(bitmap);
        } else {
            MeteogramDownloadTask downloadTask = new MeteogramDownloadTask(this, meteogramView);
            downloadTask.execute(Integer.toString(locationId));
        }
    }
}
