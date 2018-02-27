package br.com.dsx.vendamais.common;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by salazar on 2/24/17.
 */

public class JSONUtil {

    public static String getString(JSONArray linha, int index){
        String value = Util.STRING_EMPTY;
        try {
            value = linha.getString(index);
            if (value.equalsIgnoreCase("null")) {
                value = Util.STRING_EMPTY;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return value;
    }
}
