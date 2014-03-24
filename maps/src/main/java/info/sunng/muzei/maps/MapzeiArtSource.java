package info.sunng.muzei.maps;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.avos.avoscloud.AVOSCloud;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.android.apps.muzei.api.Artwork;

import java.util.Calendar;

import info.sunng.muzei.maps.data.City;
import info.sunng.muzei.maps.data.CityClient;
import info.sunng.muzei.maps.maps.GoogleMapsStatic;
import info.sunng.muzei.maps.maps.GoogleSatelliteStatic;
import info.sunng.muzei.maps.maps.MapboxStatic;
import info.sunng.muzei.maps.maps.OSMStatic;

/**
 * Created by nsun on 3/19/14.
 */
public class MapzeiArtSource extends RemoteMuzeiArtSource {

    private static final String SOURCE_NAME = "MapsArtSource";

    public static final long SOME_DAY = 484070400000l; // 1985.5.5

    public static final String REFRESH = "REFRESH";

    public MapzeiArtSource() {
        super(SOURCE_NAME);
    }

    public MapSource getMapSource() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String mapSource = sp.getString("MAP_SOURCE", "osm");
        Log.d(SOURCE_NAME, mapSource);
        switch (mapSource){
            case "osm":
                return new OSMStatic();
            case "google":
                return new GoogleMapsStatic();
            case "googles":
                return new GoogleSatelliteStatic();
            case "mapbox":
                String mapKey = sp.getString("MAPBOX_MAP_KEY", null);
                return new MapboxStatic(mapKey.trim());
            default:
                return new OSMStatic();
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();

        AVOSCloud.useAVCloudCN();
        AVOSCloud.initialize(this,
                getResources().getString(R.string.avoscloud_api_id),
                getResources().getString(R.string.avoscloud_api_secret));
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        // check network
        if (! isNetworkConnected()) {
            // schedule a short term retry
            throw new RetryException();
        } else {
            // WIFI only
            if (sp.getBoolean("WIFI_ONLY", false) && !isWifi()) {
                throw new RetryException();
            }
        }

        CityClient cc = new CityClient();
        int q = getQForToday(cc);
        Log.d("mapartsurce", "Q for toady: " + q);

        City city = cc.getCity(q);

        float lat = city.getLat();
        float lon = city.getLon();

        int zoom = Integer.valueOf(sp.getString("ZOOM_LEVEL", "16"));
        String url = getMapSource().getMapUrlFor(lat, lon, zoom, 1080, 1080);
        Log.d("mapartsource", url);

        if (getCurrentArtwork() != null &&
                (getCurrentArtwork().getImageUri().toString()).equals(url)) {
            // map unchanged, skip
            return ;
        };

        //String wikiUrl = String.format("https://en.wikipedia.com/wiki/%s", city.getAname().replaceAll(" ", "_"));
        String geoUri = String.format("geo:%f,%f?z=%d", city.getLat(), city.getLon(), zoom);

        publishArtwork(new Artwork.Builder()
                .title(city.getAname())
                .byline(city.getCountry())
                .imageUri(Uri.parse(url))
                .token(String.valueOf(city.getQ()))
                .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)))
                .build());

        // schedule next update: 3h later
        scheduleUpdate(System.currentTimeMillis() + 3 * 60 * 60 * 1000);

    }

    /**
     * @return
     */
    public static int getQForToday(CityClient c) {
        long current = Calendar.getInstance().getTimeInMillis();
        long dateDiff = (current - SOME_DAY) / (1000 * 60 * 60 * 24);

        return (int)(dateDiff % c.getTotalCities());
    }

    public boolean isNetworkConnected(){
        NetworkInfo ni = ((ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        return (ni != null && ni.isConnected()) ;
    }

    public boolean isWifi() {
        NetworkInfo ni = ((ConnectivityManager) getSystemService(
                Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();

        return ni.getType() == ConnectivityManager.TYPE_WIFI;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);

        if (intent.getBooleanExtra(REFRESH, false)) {

            scheduleUpdate(System.currentTimeMillis() + 1000);
        }
    }
}
