package info.sunng.muzei.maps;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.avos.avoscloud.AVOSCloud;
import com.google.android.apps.muzei.api.RemoteMuzeiArtSource;
import com.google.android.apps.muzei.api.Artwork;

import java.util.Calendar;

import info.sunng.muzei.maps.data.City;
import info.sunng.muzei.maps.data.CityClient;
import info.sunng.muzei.maps.maps.MapboxStatic;
import info.sunng.muzei.maps.maps.OSMStatic;

/**
 * Created by nsun on 3/19/14.
 */
public class MapArtSource extends RemoteMuzeiArtSource {

    private static final String SOURCE_NAME = "MapsArtSource";

    public static final long SOME_DAY = 484070400000l; // 1985.5.5
    public static final int TOTAL_CITIES = 22778;

    public MapSource mapSource;

    public MapArtSource() {
        super(SOURCE_NAME);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mapSource = new MapboxStatic(
                getResources().getString(R.string.mapbox_map_id));

        AVOSCloud.useAVCloudCN();
        AVOSCloud.initialize(this,
                getResources().getString(R.string.avoscloud_api_id),
                getResources().getString(R.string.avoscloud_api_secret));
    }

    @Override
    protected void onTryUpdate(int reason) throws RetryException {
        int cityQ = -1;
        if (getCurrentArtwork() != null) {
            cityQ = Integer.valueOf(getCurrentArtwork().getToken());
        }

        int q = getQForToday();
        Log.d("mapartsurce", "Q for toady: " + q);
        if (q != cityQ) {
            CityClient cc = new CityClient();
            City city = cc.getCity(q);

            float lat = city.getLat();
            float lon = city.getLon();

            String url = mapSource.getMapUrlFor(lat, lon, 15, 1080, 1080);
            Log.d("mapartsource", url);
            //String wikiUrl = String.format("https://en.wikipedia.com/wiki/%s", city.getAname().replaceAll(" ", "_"));
            String geoUri = String.format("geo:%f,%f", city.getLat(), city.getLon());

            publishArtwork(new Artwork.Builder()
                    .title(city.getAname())
                    .byline(city.getCountry())
                    .imageUri(Uri.parse(url))
                    .token(String.valueOf(city.getQ()))
                    .viewIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri)))
                    .build());
        }

        // schedule next update: 3h later
        scheduleUpdate(System.currentTimeMillis() + 3 * 60 * 60 * 1000);

    }

    /**
     * @return
     */
    public static int getQForToday() {
        long current = Calendar.getInstance().getTimeInMillis();
        long dateDiff = (current - SOME_DAY) / (1000 * 60 * 60 * 24);

        return (int)(dateDiff % TOTAL_CITIES);
    }

}
