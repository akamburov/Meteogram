package net.kamburov.meteogram.provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import net.kamburov.meteogram.R;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.Scanner;

public class Locations extends ContentProvider {

    public static final String AUTHORITY = Locations.class.getName();

    public static final String SEARCH_PATH = "search";

    public static final Uri URI = new Uri.Builder()
            .scheme(ContentResolver.SCHEME_CONTENT)
            .authority(Locations.AUTHORITY)
            .appendPath(SEARCH_PATH)
            .build();

    public final static String LOCATIONS_MIME_TYPE = "vnd.android.cursor.dir/vnd.meteogram.locations";

    private static final int URI_MATCH_LOCATIONS = 1;

    public static final String CITY = "city";

    public static final String COUNTY = "country";

    private UriMatcher mUriMatcher;

    public Locations() {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(AUTHORITY, SEARCH_PATH, URI_MATCH_LOCATIONS);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        MatrixCursor cursor = new MatrixCursor(new String[]{BaseColumns._ID, CITY, COUNTY});

        try {
            String url = String.format(getContext().getString(R.string.city_search_url), selection);
            URLConnection connection = new URL(url).openConnection();
            Scanner scanner = new Scanner(connection.getInputStream());
            try {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    //100727011#Sofia#bg#42.697513580#23.324146271#4
                    String[] fields = line.split("#");
                    if (fields.length == 6) {
                        cursor.addRow(new Object[]{
                                fields[0],
                                fields[1],
                                new Locale(
                                        Locale.US.getLanguage(),
                                        fields[2].toUpperCase()).getDisplayCountry(Locale.US)});
                    }
                }
            } finally {
                scanner.close();
            }
        } catch (MalformedURLException e) {
            //no-op
        } catch (IOException e) {
            //no-op
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        if (mUriMatcher.match(uri) == URI_MATCH_LOCATIONS) {
            return LOCATIONS_MIME_TYPE;
        }
        throw new IllegalArgumentException("Unknown Uri");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
