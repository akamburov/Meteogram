package net.kamburov.meteogram;

import android.app.ActionBar;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.*;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.SearchRecentSuggestions;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
import net.kamburov.meteogram.provider.LocationSuggestions;
import net.kamburov.meteogram.provider.Locations;

public class SelectLocation extends ListActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int LOADER_ID = 0;

    private String query;

    private CursorAdapter cursorAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            query = intent.getStringExtra(SearchManager.QUERY);

            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(query);

            //TODO Save only if unique?
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    LocationSuggestions.AUTHORITY, LocationSuggestions.MODE);
            suggestions.saveRecentQuery(query, null);
            //TODO Add to settings : suggestions.clearHistory();

            cursorAdapter = new SimpleCursorAdapter(getApplicationContext(),
                    android.R.layout.simple_list_item_2, null,
                    new String[]{Locations.CITY, Locations.COUNTY},
                    new int[]{android.R.id.text1, android.R.id.text2},
                    0);
            setListAdapter(cursorAdapter);

            getLoaderManager().initLoader(LOADER_ID, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.locations, menu);

        // Configure the search info and add any event listeners
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.locations_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryRefinementEnabled(true);
        searchView.setIconifiedByDefault(false);
        searchView.setQuery(query, false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, Meteogram.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, Locations.URI, null, query, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        switch (loader.getId()) {
            case LOADER_ID:
                cursorAdapter.swapCursor(data);
                break;
        }
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        cursorAdapter.swapCursor(null);
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        Intent intent = new Intent(this, Meteogram.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        intent.setAction(Intent.ACTION_VIEW);

        Cursor cursor = (Cursor)getListView().getItemAtPosition(position);
        intent.putExtra(BaseColumns._ID, cursor.getString(cursor.getColumnIndex(BaseColumns._ID)));
        intent.putExtra(Locations.CITY, cursor.getString(cursor.getColumnIndex(Locations.CITY)));
        intent.putExtra(Locations.COUNTY, cursor.getString(cursor.getColumnIndex(Locations.COUNTY)));

        startActivity(intent);
    }
}
