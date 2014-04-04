package info.sunng.muzei.maps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

/**
 *
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (savedInstanceState == null) {
            SettingsFragment fragment = new SettingsFragment();
            getFragmentManager()
                    .beginTransaction()
                    .add(android.R.id.content, fragment,
                            fragment.getClass().getSimpleName()).commit();
        }

        getActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        if (sp.getString("MAP_SOURCE", null).equals("mapbox")
                && sp.getString("MAPBOX_MAP_KEY", "").isEmpty()) {
            Toast t = Toast.makeText(this, R.string.mapbox_id_required, 20);
            t.show();
            return;
        } else {
            super.onBackPressed();
            Intent i = new Intent(this, MapzeiArtSource.class);
            i.putExtra("REFRESH", true);
            startService(i);
        }
    }


}
