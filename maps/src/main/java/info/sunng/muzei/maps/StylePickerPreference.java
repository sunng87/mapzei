package info.sunng.muzei.maps;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;

import com.avos.avoscloud.LogUtil;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.Collections;
import java.util.List;

import info.sunng.muzei.maps.data.Style;
import info.sunng.muzei.maps.data.StyleClient;

/**
 * Created by nsun on 12/28/14.
 */
public class StylePickerPreference extends DialogPreference {

    static final String TAG = StylePickerPreference.class.getCanonicalName();

    private final StyleAdaptor adaptor =  new StyleAdaptor(getContext(), R.layout.style_preview_view);;
    public StylePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.style_picker_layout);
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();

        GridView gv = (GridView)view.findViewById(R.id.style_grid);
        gv.setNumColumns(3);
        gv.setAdapter(adaptor);

        new StyleLoadTask().execute(1);

        return view;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

    }

    private class StyleLoadTask extends AsyncTask<Integer, Void, List<Style>> {

        @Override
        protected List<Style> doInBackground(Integer... params) {
            try {
                return StyleClient.fetchStyles(params[0], getContext());
            } catch (Exception e) {
                Log.e(TAG, "Error fetching snazzymaps styles", e);
                return Collections.emptyList();
            }
        }

        @Override
        protected void onPostExecute(List<Style> styles) {
            adaptor.addAll(styles);
            adaptor.notifyDataSetChanged();
        }
    }

    public static class StyleAdaptor extends ArrayAdapter<Style> {
        private LayoutInflater mLayoutInflater;

        public StyleAdaptor(Context context, int resource) {
            super(context, resource);
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Style s = this.getItem(position);
            Log.d(TAG, "rendering " + position);

            convertView = mLayoutInflater.inflate(R.layout.style_preview_view, null);
            ImageView im = (ImageView)convertView.findViewById(R.id.style_preview_image);

            ImageLoader.getInstance().displayImage(s.getImageUrl(), im);
            return convertView;
        }
    }
}
