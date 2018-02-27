package br.com.dsx.vendamais.component;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

/**
 * Created by salazar on 27/10/17.
 */

public class AppSpinner extends MaterialBetterSpinner {

    public AppSpinner(Context context) {
        super(context);
        setOnItemClickListener(this);
    }

    public AppSpinner(Context arg0, AttributeSet arg1) {
        super(arg0, arg1);
        setOnItemClickListener(this);
    }

    public AppSpinner(Context arg0, AttributeSet arg1, int arg2) {
        super(arg0, arg1, arg2);
        setOnItemClickListener(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            return super.onTouchEvent(event);
        }
        return false;
    }

}
