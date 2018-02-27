package br.com.dsx.vendamais.common;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.redmadrobot.inputmask.helper.Mask;
import com.redmadrobot.inputmask.model.CaretString;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;

import br.com.dsx.vendamais.domain.Perfil;

/**
 * Created by salazar on 2/12/17.
 */

public class Util {

    public static Perfil perfil;

    public static String STRING_EMPTY = "";

    public static Drawable changeDrawableTint(Drawable drawable, int color){
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, ColorStateList.valueOf(color));
        return wrappedDrawable;
    }

    public static boolean isBlank(String value){
        int strLen;
        if (value == null || (strLen = value.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (Character.isWhitespace(value.charAt(i)) == false) {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotBlank(String value){
        return !isBlank(value);
    }

    public static String applyMask(String mask, String value) {
        if (isBlank(value)) return value;
        final Mask m = new Mask(mask);
        final Mask.Result result = m.apply(
                new CaretString(
                        value,
                        value.length()
                ),
                false
        );
        return result.getFormattedText().getString();
    }

    public static void hideKeyboard(Activity activity) {
        if (activity != null) {
            Window window = activity.getWindow();
            if (window != null) {
                View v = window.getCurrentFocus();
                if (v != null) {
                    InputMethodManager imm = (InputMethodManager)
                            activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm!=null){
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
            }
        }
    }

    public static String produtoFotoToURL(Long produtoId) {
        StringBuilder url = new StringBuilder();

        url.append(perfil.getUrlSankhya());

        if (!url.toString().endsWith("/"))
            url.append("/");

        if (!url.toString().endsWith("mge/"))
            url.append("mge/");

        url.append("Produto@IMAGEM@CODPROD=")
           .append(produtoId)
           .append(".dbimage");

        return url.toString();

    }

    public static String dateToString(String pattern, Date data) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        if (data!=null) {
            return format.format(data);
        }
        return STRING_EMPTY;
    }

    public static String doubleToValorMonetaria(Double number) {
        Locale locate = new Locale( "pt", "BR" );
        DecimalFormat decimalFormat = new DecimalFormat("¤ #,##0.00");
        decimalFormat.setCurrency(Currency.getInstance(locate));
        if (number!=null) {
            return decimalFormat.format(number);
        }
        return STRING_EMPTY;
    }

    public static String doubleToString(Double number) {
        Locale locate = new Locale( "pt", "BR" );
        DecimalFormat decimalFormat = new DecimalFormat("###0.00");
        decimalFormat.setCurrency(Currency.getInstance(locate));
        if (number!=null) {
            return decimalFormat.format(number);
        }
        return STRING_EMPTY;
    }

    public static Double textViewToDouble(TextView value, Double defaultValue) {
        return stringToDouble(value.getText().toString(), defaultValue);
    }

    public static Double stringToDouble(String number) {
        return stringToDouble(number, null);
    }

    public static Double stringToDouble(String number, Double defaultValue) {
        Double value = defaultValue;
        Locale locate = new Locale( "pt", "BR" );
        DecimalFormat decimalFormat = new DecimalFormat("###0.00");
        decimalFormat.setCurrency(Currency.getInstance(locate));
        try {
            value = decimalFormat.parse(number).doubleValue();
        }catch (Exception e2){
            try {
                value = Double.valueOf(number);
            }catch (Exception e1){
            }
        }
        return value;
    }


    public static boolean isValidEmail(CharSequence target) {
        if (target == null) {
            return false;
        }
        return Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    public static Integer textViewToNumber(TextView value) {
        return textViewToNumber(value, null);
    }

    public static Integer textViewToNumber(TextView value, Integer defaultValue) {
        String valueString = value.getText().toString();
        try {
            return Integer.valueOf(valueString);
        }catch (Exception e) {
        }
        return defaultValue;
    }

    public static Long stringToId(String value) {
        return stringToId(value, 0L);
    }

    public static Long stringToId(String value, Long defaultValue) {
        try {
            return Long.valueOf(value);
        }catch (Exception e) {
        }
        return defaultValue;
    }


    public static Integer stringToNumber(String value, Integer defaultValue) {
        try {
            return Integer.valueOf(value);
        }catch (Exception e) {
        }
        return defaultValue;
    }

    public static String toString(Object value) {
        String string = STRING_EMPTY;
        if (value!=null) {
            return string+value;
        }
        return string;
    }

    public static String cleanMessage(String message) {
        String[] parts = message.split("ORA-[0-9]{5}:");
        if (isBlank(parts[0]) && parts.length>1){
            return parts[1].trim();
        }
        return message;
    }
}

