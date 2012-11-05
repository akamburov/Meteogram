package net.kamburov.meteogram.provider;

import android.provider.BaseColumns;

import java.io.InvalidObjectException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class Favorites {

    private static final String ItemSeparator = "|";

    private static final String FieldSeparator = "#";

    public Favorites() {
    }

    public List<HashMap<String, String>> asItems(String favorites) {
        List<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
        if (favorites.length() > 0) {
            String[] favoritesItems = favorites.split("\\" + ItemSeparator);
            for (String favoriteItem : favoritesItems) {
                String[] fields = favoriteItem.split(FieldSeparator);

                HashMap<String, String> item = new HashMap<String, String>();
                item.put(BaseColumns._ID, fields[0]);
                item.put(Locations.CITY, fields[1]);
                item.put(Locations.COUNTY, fields[2]);

                items.add(item);
            }
        }

        return items;
    }

    public String add(String favorites, int locationId, String locationCity, String locationCountry) throws InvalidObjectException {
        List<HashMap<String, String>> items = asItems(favorites);

        // Check if item exists
        for (HashMap<String, String> item : items) {
            if (item.get(BaseColumns._ID) == Integer.toString(locationId)) {
                throw new InvalidObjectException("Item exists");
            }
        }

        int size = items.size();
        while (size >= 5) {
            --size;
            items.remove(size);
        }

        HashMap<String, String> newItem = new HashMap<String, String>();
        newItem.put(BaseColumns._ID, Integer.toString(locationId));
        newItem.put(Locations.CITY, locationCity);
        newItem.put(Locations.COUNTY, locationCountry);
        items.add(0, newItem);

        return asString(items);
    }

    public static String asString(List<HashMap<String, String>> items) {
        if (items == null || items.isEmpty())
            return "";

        Iterator<HashMap<String, String>> i = items.iterator();
        StringBuilder builder = new StringBuilder(asString(i.next()));
        while (i.hasNext()) {
            builder.append(ItemSeparator).append(asString(i.next()));
        }

        return builder.toString();
    }

    private static String asString(HashMap<String, String> item) {
        return String.format("%s%s%s%s%s",
                item.get(BaseColumns._ID),
                FieldSeparator,
                item.get(Locations.CITY),
                FieldSeparator,
                item.get(Locations.COUNTY));
    }
}
