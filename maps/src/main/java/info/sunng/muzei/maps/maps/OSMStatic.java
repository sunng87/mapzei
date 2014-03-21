package info.sunng.muzei.maps.maps;

import info.sunng.muzei.maps.MapSource;

/**
 * Created by nsun on 3/20/14.
 */
public class OSMStatic implements MapSource {

    @Override
    public String getMapUrlFor(float lat, float lon, int zoom, int width, int height) {
        return String.format("http://staticmap.openstreetmap.de/staticmap.php?center=%f,%f&zoom=%d&size=%dx%d&maptype=mapnik",
                lat, lon, zoom, width, height);
    }
}
