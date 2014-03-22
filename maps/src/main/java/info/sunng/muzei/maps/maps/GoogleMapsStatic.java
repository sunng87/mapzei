package info.sunng.muzei.maps.maps;

import info.sunng.muzei.maps.MapSource;

/**
 * Created by nsun on 3/22/14.
 */
public class GoogleMapsStatic implements MapSource {

    @Override
    public String getMapUrlFor(float lat, float lon, int zoom, int width, int height) {
        return String.format("http://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=%d&size=%dx%d&sensor=true&maptype=roadmap&scale=2",
                lat, lon, zoom, width, height);
    }
}
