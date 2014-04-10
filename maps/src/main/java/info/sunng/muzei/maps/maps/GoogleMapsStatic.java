package info.sunng.muzei.maps.maps;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import info.sunng.muzei.maps.MapSource;

/**
 * Created by nsun on 4/10/14.
 */
public class GoogleMapsStatic implements MapSource{

    private String mapType = "roadmap";

    private String[] styleConfig;

    public String getMapType() {
        return mapType;
    }

    public void setMapType(String mapType) {
        this.mapType = mapType;
    }

    public String[] getStyleConfig() {
        return styleConfig;
    }

    public void setStyleConfig(String[] styleConfig) {
        this.styleConfig = styleConfig;
    }

    @Override
    public String getMapUrlFor(float lat, float lon, int zoom, int width, int height) {
        StringBuilder sb = new StringBuilder();
        for (String styleRule: getStyleConfig()) {
            String encodedStyleConfig;
            try {
                encodedStyleConfig = URLEncoder.encode(styleRule, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                encodedStyleConfig = "";
            }
            sb.append("&style=").append(encodedStyleConfig);
        }

        return String.format("http://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=%d&size=%dx%d&sensor=true&maptype=%s&scale=2%s",
                lat, lon, zoom, width, height, getMapType(), sb.toString());
    }
}
