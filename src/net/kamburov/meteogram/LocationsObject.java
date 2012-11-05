package net.kamburov.meteogram;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class LocationsObject {

    private JSONObject locationsFile;

    public LocationsObject(Context context) {
        try {
            InputStream stream = context.getResources().openRawResource(R.raw.locations);
            byte[] bytes = new byte[stream.available()];
            stream.read(bytes);
            locationsFile = new JSONObject(new String(bytes));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int GetLocationId(String countryName, String locationName) {
        JSONObject country = GetCountry(countryName);
        try {
            return country.getInt(locationName);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private JSONObject GetCountry(String countryName) {
        try {
            return locationsFile.getJSONObject(countryName);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        //TODO
        return null;
    }

    public String[] GetCountries() {
        return GetKeys(locationsFile);
    }

    public String[] GetLocations(String countryName) {
        JSONObject country = GetCountry(countryName);
        return GetKeys(country);
    }

    private String[] GetKeys(JSONObject object) {
        List<String> resultList = new ArrayList<String>();

        Iterator keys = object.keys();
        while (keys.hasNext()) {
            Object key = keys.next();
            if (key instanceof String) {
                resultList.add((String) key);
            }
        }

        Collections.sort(resultList);
        return resultList.toArray(new String[resultList.size()]);
    }
     /*

    private void showLocationDialog() {
        final LocationsObject locationsObject = new LocationsObject(this);
        final String[] countries = locationsObject.GetCountries();

        if (countries.length == 0) {
            Toast.makeText(this, R.string.no_country, Toast.LENGTH_SHORT).show();
        } else {
            final Activity activity = this;
            AlertDialog.Builder countryDialog = new AlertDialog.Builder(this);
            countryDialog.setTitle(R.string.select_country);
            int checkedItem = 6; //TODO

            countryDialog.setSingleChoiceItems(countries, checkedItem, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, final int country) {
                    dialog.dismiss();

                    final String[] locations = locationsObject.GetLocations(countries[country]);

                    if (locations.length == 0) {
                        Toast.makeText(activity, R.string.no_location, Toast.LENGTH_SHORT).show();
                    } else {
                        final AlertDialog.Builder locationDialog = new AlertDialog.Builder(activity);
                        locationDialog.setTitle(R.string.select_location);
                        int checkedItem = 1; //TODO

                        locationDialog.setSingleChoiceItems(locations, checkedItem, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int location) {
                                dialog.dismiss();

                                String locationName = locations[location];

                                TextView meteogramText = (TextView) findViewById(R.id.meteogramText);
                                meteogramText.setText(locationName); //TODO location, country

                                locationId = locationsObject.GetLocationId(countries[country], locationName);
                                refreshMeteogram();
                            }
                        });

                        locationDialog.show();
                    }
                }
            });

            countryDialog.show();
        }
    }    */
}