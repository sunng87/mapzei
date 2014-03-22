package info.sunng.muzei.maps.maps;

import info.sunng.muzei.maps.MapSource;
import info.sunng.muzei.maps.R;

/**
 * Created by nsun on 3/21/14.
 */
public class MapboxStatic implements MapSource{

    private final String mapboxId;

    public MapboxStatic(String mapboxId){
        this.mapboxId = mapboxId;
    }

    @Override
    public String getMapUrlFor(float lat, float lon, int zoom, int width, int height) {
        return String.format("http://api.tiles.mapbox.com/v3/%s/%f,%f,%d/%dx%d.png",
                this.mapboxId,
                lon, lat, zoom, width, height);
    }
}
