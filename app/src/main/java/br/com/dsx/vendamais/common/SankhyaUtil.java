package br.com.dsx.vendamais.common;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import br.com.dsx.vendamais.sync.SWServiceInvoker;

/**
 * Created by salazar on 09/03/17.
 */

public class SankhyaUtil {

    public static JSONArray execSelect(SWServiceInvoker service, String sql) throws Exception {
        String json = service.buildRequestQueryJSON(sql);
        String jsonResponse = service.callJSON("DbExplorerSP.executeQuery", "mge", json);
        JSONObject rootObject = new JSONObject(jsonResponse);
        JSONObject responseBody;
        try {
            responseBody = rootObject.getJSONObject("responseBody");
            JSONArray array = responseBody.getJSONArray("rows");
            return array;
        } catch (JSONException jsonException){
            jsonException.printStackTrace();
            String msgErro = rootObject.getString("statusMessage");
            throw new Exception(msgErro);
        }

    }

}
