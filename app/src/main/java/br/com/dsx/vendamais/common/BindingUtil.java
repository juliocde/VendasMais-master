package br.com.dsx.vendamais.common;

import android.databinding.BindingAdapter;
import android.databinding.InverseBindingAdapter;
import android.databinding.InverseBindingListener;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

/**
 * Created by salazar on 2/9/17.
 */
public class BindingUtil {


    @InverseBindingAdapter(attribute = "spinnerValue", event = "spinnerValueAttrChanged")
    public static int getSpinnerValue(Spinner spinner) {
        return spinner.getSelectedItemPosition();
    }

    @BindingAdapter(value = {"onSpinnerValueChange", "spinnerValueAttrChanged"}, requireAll = false)
    public static void setSpinnerValueListener(Spinner spinner,
                                        final AdapterView.OnItemSelectedListener listener,
                                        final InverseBindingListener spinnerValueChange) {
        if (spinnerValueChange == null) {
            spinner.setOnItemSelectedListener(null);
        } else {
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    if (listener!=null) {
                        listener.onItemSelected(parent, view, position, id);
                    }
                    spinnerValueChange.onChange();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    if (listener!=null) {
                        listener.onNothingSelected(parent);
                    }
                    spinnerValueChange.onChange();
                }
            });
        }
    }


    @BindingAdapter("spinnerValue")
    public static void setSpinnerValue(Spinner spinner, int position){
        if (spinner.getSelectedItemPosition() != position) {
            spinner.setSelection(position);
        }
    }


}
