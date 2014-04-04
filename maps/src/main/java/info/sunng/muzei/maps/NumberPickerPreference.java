package info.sunng.muzei.maps;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

/**
 * Created by nsun on 4/4/14.
 */
public class NumberPickerPreference extends DialogPreference {

    private NumberPicker np;
    private int mValue;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setDialogLayoutResource(R.layout.number_picker);

        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);

        setDialogIcon(null);

    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
        super.onSetInitialValue(restorePersistedValue, defaultValue);

        if (restorePersistedValue) {
            mValue = Integer.valueOf(getPersistedString("16"));
        } else {
            String sDefaultValue = (String) defaultValue;
            persistString(sDefaultValue);
            mValue = Integer.valueOf(sDefaultValue);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            if (callChangeListener(String.valueOf(np.getValue()))) {
                mValue = np.getValue();
                persistString(String.valueOf(np.getValue()));
            }
        }
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        np = (NumberPicker) view.findViewById(R.id.np);
        np.setMaxValue(19);
        np.setMinValue(12);
        np.setWrapSelectorWheel(false);
        np.setValue(mValue);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

}
