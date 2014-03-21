package info.sunng.muzei.maps;

/**
 * Created by nsun on 3/20/14.
 */
public interface MapSource {

    public String getMapUrlFor(float lat, float lon, int zoom, int width, int height);

}
