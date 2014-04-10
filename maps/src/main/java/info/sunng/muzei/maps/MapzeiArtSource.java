package info.sunng.muzei.maps;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

import com.avos.avoscloud.AVOSCloud;
import com.google.android.apps.muzei.api.Artwork;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.android.apps.muzei.api.UserCommand;

import java.util.Calendar;

import info.sunng.muzei.maps.data.City;
import info.sunng.muzei.maps.data.CityClient;
import info.sunng.muzei.maps.maps.GoogleMapsStatic;
import info.sunng.muzei.maps.maps.MapboxStatic;
import info.sunng.muzei.maps.maps.OSMStatic;

/**
 * Created by nsun on 3/19/14.
 */
public class MapzeiArtSource extends RemoteMuzeiArtSource {

    private static final String SOURCE_NAME = "MapsArtSource";

    public static final long SOME_DAY = 484070400000l; // 1985.5.5

    public static final String REFRESH = "REFRESH";

    public static final int SHARE_ARTWORK = 1;
    public static final int CITY_ON_WIKIPEDIA = 2;

    public MapzeiArtSource() {
        super(SOURCE_NAME);
    }

    public MapSource getMapSource() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String mapSource = sp.getString("MAP_SOURCE", "google");
        Log.d(SOURCE_NAME, mapSource);
        switch (mapSource){
            case "osm":
                return new OSMStatic();
            case "google":
                GoogleMapsStatic gms = new GoogleMapsStatic();
                gms.setMapType(sp.getString("GOOGLE_MAP_TYPE", "roadmap"));
                gms.setStyleConfig(sp.getString("GOOGLE_MAP_STYLE", "").split("\n"));
                return gms;
            case "googles": // legacy 1.2 settings value
                GoogleMapsStatic gmsSatellite = new GoogleMapsStatic();
                gmsSatellite.setMapType("satellite");
                return gmsSatellite;
            case "mapbox":
                String mapKey = sp.getString("MAPBOX_MAP_KEY", null);
                return new MapboxStatic(mapKey.trim());
            default:
                return new GoogleMapsStatic();
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
    protected void onCustomCommand(int id) {
        if (id == SHARE_ARTWORK) {
            Artwork a = getCurrentArtwork();
            String cityName = a.getTitle();
            String countryName = a.getByline();
            String pos = a.getToken();
            String[] latlon = pos.split(",");

            String osmUrlBase = "http://osm.org/#map=12/%s/%s";
            String osmUrl = String.format(osmUrlBase, latlon[0], latlon[1]);

            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("text/plain");
            i.putExtra(Intent.EXTRA_TEXT, "#TodayOnMapzei "
              + cityName + ", "
              + countryName + ". "
              + osmUrl + " "
              + "#Mapzei, random city map android (muzei) wallpaper.");

            i = Intent.createChooser(i, "Share city");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }

        if (id == CITY_ON_WIKIPEDIA) {
            Artwork a = getCurrentArtwork();
            String cityName = a.getTitle();
            String url = String.format("http://en.wikipedia.com/wiki/%s", cityName.replaceAll(" ", "_"));
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        // check network
        if (! isNetworkConnected()) {
            // schedule a short term retry
            scheduleUpdate(System.currentTimeMillis() + 1 * 60 * 60 * 1000);
        } else {
            // WIFI only
            if (sp.getBoolean("WIFI_ONLY", false) && !isWifi()) {
                scheduleUpdate(System.currentTimeMillis() + 1 * 60 * 60 * 1000);
            }
        }

        try {
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
                return;
            }

            String geoUri = String.format("geo:%f,%f?z=%d", lat, lon, zoom);

            publishArtwork(new Artwork.Builder()
                    .title(city.getAname())
                    .byline(city.getCountry())
                    .imageUri(Uri.parse(url))
                    .token(String.format("%f,%f", lat, lon))
                    .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)))
                    .build());

            UserCommand shareCmd = new UserCommand(SHARE_ARTWORK, getString(R.string.share_city));
            UserCommand wikiCmd = new UserCommand(CITY_ON_WIKIPEDIA, getString(R.string.city_wiki));
            setUserCommands(shareCmd, wikiCmd);
        } catch (Exception e) {
            throw new RetryException();
        } finally {
            // schedule next update: 6h later
            scheduleUpdate(System.currentTimeMillis() + 6 * 60 * 60 * 1000);
        }



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
