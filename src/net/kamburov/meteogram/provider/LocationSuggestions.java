package net.kamburov.meteogram.provider;

import android.content.SearchRecentSuggestionsProvider;

public class LocationSuggestions extends SearchRecentSuggestionsProvider {

    public static final String AUTHORITY = LocationSuggestions.class.getName();

    public static final int MODE = DATABASE_MODE_QUERIES;

    public LocationSuggestions() {
        // See method docs
        setupSuggestions(AUTHORITY, MODE);
    }
}
