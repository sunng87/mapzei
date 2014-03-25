package info.sunng.muzei.maps.data;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;

import java.util.List;

/**
 * Created by nsun on 3/19/14.
 */
public class CityClient {

    public City getCity(int q) {
        AVQuery<AVObject> query = new AVQuery<>("City");

        query.whereEqualTo("q", q);

        try {
            List<AVObject> results = query.find();
            if (results.size() > 0) {
                return City.fromAVObject(results.get(0));
            } else {
                return null;
            }
        } catch (AVException e) {
            throw new RuntimeException(e);
        }
    }

    public int getTotalCities() {
        AVQuery<AVObject> query = new AVQuery<>("City");
        try {
            return query.count();
        } catch (AVException e) {
            throw new RuntimeException(e);
        }
    }
}
