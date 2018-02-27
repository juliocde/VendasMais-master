package br.com.dsx.vendamais.component;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.redmadrobot.inputmask.MaskedTextChangedListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.R;

/**
 * Created by salazar on 15/02/17.
 */

public class MaskEditText extends MaterialEditText {

    private String mask;

    public MaskEditText(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        initialize(context, attrs);
    }

    public MaskEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs);
    }

    public MaskEditText(Context context) {
        super(context);
    }

    private void initialize(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MaskEditText);
        mask = typedArray.getString(R.styleable.MaskEditText_mask);
        if (Util.isNotBlank(mask)){
            final MaskedTextChangedListener maskListener = new MaskedTextChangedListener(
                    mask, true, this, null, null);
            this.addTextChangedListener(maskListener);
            this.setOnFocusChangeListener(maskListener);
            this.setHelperText(maskListener.placeholder());
        }
    }

}
