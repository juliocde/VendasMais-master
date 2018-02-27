package br.com.dsx.vendamais.sync;

import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import br.com.dsx.vendamais.common.JSONUtil;
import br.com.dsx.vendamais.common.Message;
import br.com.dsx.vendamais.common.SankhyaUtil;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.domain.Empresa;
import br.com.dsx.vendamais.domain.Erro;

public class SyncTaskEmpresa extends SyncTask {

    public SyncTaskEmpresa(ProgressBar progressBar, TextView status, DaoSession daoSession) {
        super(progressBar, status, daoSession);
    }

    @Override
    protected List<Message> doInBackground(String... params) {

        try{
            if (params!=null && params.length>0){
                ultimaSincronizacao = params[0];
            }

            StringBuilder sql = new StringBuilder()
                    .append(" SELECT ")
                    .append("   CODEMP,")
                    .append("   CODEMP || ' - ' || NOMEFANTASIA AS NOMEFANTASIA")
                    .append(" FROM TSIEMP ")
                    .append(" WHERE AD_USAAPP='S' ");

            JSONArray linhas = SankhyaUtil.execSelect(serviceInvoker, sql.toString());

            //Este comando coloca o maxValue da progressBar para a quantidade de formas de pagamento
            publishProgress(0, linhas.length());

            jsonEmpresas(linhas);

        } catch (Exception e1) {
            e1.printStackTrace();
            messages.add(new Message(Message.Type.ERROR, e1.toString()));
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            return messages;
        }
        messages.add(new Message(Message.Type.SUCESS, "Empresas sincronizadas."));
        return messages;
    }



    private void jsonEmpresas(JSONArray linhas) {
        try {
            List<Empresa> empresas = new ArrayList<>();
            for (int i = 0; i < linhas.length(); i++) {
                JSONArray linha = linhas.getJSONArray(i);
                publishProgress(i+1);

                Empresa empresa = new Empresa();
                empresa.setId(linha.getLong(0));
                empresa.setNomeFantasia(JSONUtil.getString(linha, 1));
                empresas.add(empresa);
            }
            daoSession.getEmpresaDao().insertOrReplaceInTx(empresas);
        } catch (JSONException e1) {
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            messages.add(new Message(Message.Type.ERROR, e1.toString()));
            e1.printStackTrace();
        }

    }

}
