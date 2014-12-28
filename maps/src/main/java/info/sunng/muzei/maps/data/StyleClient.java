package info.sunng.muzei.maps.data;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;

import info.sunng.muzei.maps.R;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;

/**
 * Created by nsun on 12/28/14.
 */
public class StyleClient {

    private static final String TAG = StyleClient.class.getCanonicalName();
    private static final String URL = "https://snazzymaps.com/explore.json?page=%s&key=%s";

    public static List<Style> fetchStyles(int page, Context c) throws Exception {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(String.format(URL, page, c.getString(R.string.snazzy_api_key)))
                .build();
        Response response = client.newCall(request).execute();
        String results = response.body().string();

        Log.d(TAG, results);

        JsonParser parser = new JsonParser();
        JsonElement je = parser.parse(results);

        JsonArray ss = je.getAsJsonObject().getAsJsonArray("styles");
        Gson g = new Gson();

        Type styleListType = new TypeToken<List<Style>>(){}.getType();
        List<Style> sss = g.fromJson(ss, styleListType);
        return sss;
    }
}
