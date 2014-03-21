package info.sunng.muzei.maps.data;

import com.avos.avoscloud.AVObject;

/**
 * Created by nsun on 3/19/14.
 */
public class City {

    private String name;

    private String aname;

    private String country;

    private float lat;

    private float lon;

    private int q;

    public static City fromAVObject (AVObject avCity) {
        City city = new City();
        city.setAname(avCity.getString("aname"));
        city.setName(avCity.getString("name"));
        city.setLat(avCity.getNumber("lat").floatValue());
        city.setLon(avCity.getNumber("lon").floatValue());
        city.setCountry(avCity.getString("cc"));
        city.setQ(avCity.getInt("q"));
        return city;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAname() {
        return aname;
    }

    public void setAname(String aname) {
        this.aname = aname;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public float getLat() {
        return lat;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public float getLon() {
        return lon;
    }

    public void setLon(float lon) {
        this.lon = lon;
    }

    public int getQ() {
        return q;
    }

    public void setQ(int q) {
        this.q = q;
    }
}
