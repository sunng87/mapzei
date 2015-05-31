package info.sunng.muzei.maps;

import android.content.Context;
import android.os.AsyncTask;
import android.preference.DialogPreference;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.sunng.muzei.maps.data.Style;
import info.sunng.muzei.maps.data.StyleClient;

/**
 * Created by nsun on 12/28/14.
 */
public class StylePickerPreference extends DialogPreference implements View.OnClickListener {

    static final String TAG = StylePickerPreference.class.getCanonicalName();

    private static DisplayImageOptions defaultUILOptions = new DisplayImageOptions.Builder()
            .cacheOnDisk(true).imageScaleType(ImageScaleType.EXACTLY_STRETCHED).build();

    private RecyclerView rv;
    private StyleAdapter rva;
    private LinearLayoutManager rlm;
    private View loadingView;
    private EditText styleEditor;
    private String storedStyle;
    private boolean snazzySelectorLoading = false;
    private int snazzySelectorPage = 1;

    public StylePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.style_picker_layout);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);
        if (!restorePersistedValue) {
            storedStyle = (String)defaultValue;
        } else {
            storedStyle = getPersistedString("");
        }
    }

    @Override
    protected View onCreateDialogView() {
        View view = super.onCreateDialogView();

        rv = (RecyclerView) view.findViewById(R.id.style_grid);
        rlm = new LinearLayoutManager(this.getContext(), LinearLayoutManager.HORIZONTAL, false);
        rv.setLayoutManager(rlm);
        rv.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int visibleItemCount = rlm.getChildCount();
                int totalItemCount = rlm.getItemCount();
                int pastVisiblesItems = rlm.findFirstVisibleItemPosition();

                if (! snazzySelectorLoading) {
                    if ( (visibleItemCount+pastVisiblesItems) >= totalItemCount) {
                        new StyleLoadTask().execute(snazzySelectorPage++);
                    }
                }
            }
        });

        rva = new StyleAdapter(new ArrayList<Style>(), this);
        rv.setAdapter(rva);

        loadingView = view.findViewById(R.id.progressBar);

        return view;
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        styleEditor = (EditText) view.findViewById(R.id.style_json);
        if (storedStyle != null) {
            styleEditor.setText(storedStyle);
        }

        snazzySelectorPage = 1;
        new StyleLoadTask().execute(snazzySelectorPage++);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            String result = styleEditor.getText().toString();
            if (callChangeListener(result)) {
                persistString(result);
            }
        }
        super.onDialogClosed(positiveResult);
    }

    @Override
    public void onClick(View view) {
        int position = rv.getChildAdapterPosition(view);
        Style s = rva.getStyle(position);
        styleEditor.setText(s.getJson());
    }

    private class StyleLoadTask extends AsyncTask<Integer, Void, List<Style>> {

        @Override
        protected List<Style> doInBackground(Integer... params) {
            try {
                snazzySelectorLoading = true;
                return StyleClient.fetchStyles(params[0], getContext());
            } catch (Exception e) {
                Log.e(TAG, "Error fetching snazzymaps styles", e);
                return Collections.emptyList();
            }
        }

        @Override
        protected void onPostExecute(List<Style> styles) {
            rva.addStyles(styles);
            loadingView.setVisibility(View.GONE);
            snazzySelectorLoading = false;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView im;
        public TextView tv;

        public ViewHolder(View v) {
            super(v);
            this.im = (ImageView) v.findViewById(R.id.style_preview_image);
            this.tv = (TextView) v.findViewById(R.id.style_name);
        }
    }

    public static class StyleAdapter extends RecyclerView.Adapter<ViewHolder> {
        private LayoutInflater mLayoutInflater;
        private List<Style> styles;
        private View.OnClickListener handler;

        public StyleAdapter(List<Style> styles, View.OnClickListener handler) {
            this.styles = styles;
            this.handler = handler;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View v = mLayoutInflater.from(viewGroup.getContext()).inflate(
                    R.layout.style_preview_view, viewGroup, false);
            ViewHolder vh = new ViewHolder(v);
            v.setOnClickListener(handler);
            return vh;
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, int i) {
            ImageLoader.getInstance().displayImage(styles.get(i).getImageUrl(), viewHolder.im, defaultUILOptions);
            viewHolder.tv.setText(styles.get(i).getName());
        }

        @Override
        public int getItemCount() {
            return styles.size();
        }

        public void addStyles (List<Style> ns) {
            this.styles.addAll(ns);
            this.notifyDataSetChanged();
        }

        public Style getStyle (int i) {
            return this.styles.get(i);
        }

    }
}
