package info.sunng.muzei.maps;

import android.app.Application;

import com.avos.avoscloud.AVOSCloud;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by nsun on 12/28/14.
 */
public class MapzeiApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AVOSCloud.useAVCloudCN();
        AVOSCloud.initialize(this,
                getResources().getString(R.string.avoscloud_api_id),
                getResources().getString(R.string.avoscloud_api_secret));

        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .defaultDisplayImageOptions(new DisplayImageOptions.Builder()
                        .cacheOnDisk(true)
                        .cacheInMemory(true)
                        .build())
                        //.writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);
    }
}
