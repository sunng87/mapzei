package info.sunng.muzei.maps.maps;

import info.sunng.muzei.maps.MapSource;

/**
 * Created by nsun on 3/25/14.
 */
public class YandexMapsStatic implements MapSource {

    @Override
    public String getMapUrlFor(float lat, float lon, int zoom, int width, int height) {
        return String.format("http://static-maps.yandex.ru/1.x/?lang=en-US&ll=%f,%f&z=%d&l=map&size=%d,%d",
                lon, lat, zoom, width, height);
    }
}
