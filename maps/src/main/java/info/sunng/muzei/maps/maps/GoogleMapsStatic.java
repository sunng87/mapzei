package info.sunng.muzei.maps.maps;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.JsonReader;
import android.util.JsonToken;

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
        if (getStyleConfig() != null) {
            for (String styleRule: getStyleConfig()) {
                String encodedStyleConfig;
                try {
                    encodedStyleConfig = URLEncoder.encode(styleRule, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    encodedStyleConfig = "";
                }
                sb.append("&style=").append(encodedStyleConfig);
            }
        }

        return String.format("http://maps.googleapis.com/maps/api/staticmap?center=%f,%f&zoom=%d&size=%dx%d&sensor=true&maptype=%s&scale=2%s",
                lat, lon, zoom, width, height, getMapType(), sb.toString());
    }

    public void setStyleConfigJson(String jsonString) throws IOException {
        JsonReader jr = new JsonReader(new StringReader(jsonString));
        List<String> parsedStyles = new ArrayList<>();

        jr.beginArray();
        while(jr.hasNext()) {
            jr.beginObject();
            String feature = null;
            String element = null;
            Map<String, String> stylers = new HashMap<>();
            while (jr.hasNext()) {
                String name = jr.nextName();
                if (name.equals("featureType")) {
                    feature = jr.nextString();
                } else if (name.equals("elementType")) {
                    element = jr.nextString();
                } else if (name.equals("stylers")) {
                    jr.beginArray();
                    while(jr.hasNext()) {
                        jr.beginObject();
                        String stylerName = jr.nextName();
                        JsonToken jtype = jr.peek();
                        switch (jtype){
                            case BOOLEAN:
                                stylers.put(stylerName, String.valueOf(jr.nextBoolean()));
                                break;
                            default:
                                // number can be read as string according to doc:
                                // http://developer.android.com/reference/android/util/JsonReader.html
                                stylers.put(stylerName, jr.nextString());
                        }
                        jr.endObject();
                    }
                    jr.endArray();
                } else {
                    jr.skipValue();
                }

            }
            jr.endObject();


            if (stylers.isEmpty()) {
                continue;
            }

            StringBuilder styleConfigLine = new StringBuilder();
            if (feature != null) {
                styleConfigLine.append("feature:").append(feature).append("|");
            }
            if (element != null) {
                styleConfigLine.append("element:").append(element).append("|");
            }
            for (String styleKey : stylers.keySet()) {
                String styleValue = stylers.get(styleKey);
                styleValue = styleValue.replaceAll("#", "0x");
                styleConfigLine.append(styleKey).append(":").append(styleValue).append("|");
            }
            // remove the last |
            styleConfigLine.deleteCharAt(styleConfigLine.length()-1);

            parsedStyles.add(styleConfigLine.toString());

        }
        jr.endArray();

        this.setStyleConfig(parsedStyles.toArray(new String[0]));
    }
}
