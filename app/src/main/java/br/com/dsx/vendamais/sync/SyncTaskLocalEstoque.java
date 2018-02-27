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
import br.com.dsx.vendamais.domain.Erro;
import br.com.dsx.vendamais.domain.LocalEstoque;

public class SyncTaskLocalEstoque extends SyncTask {

    public SyncTaskLocalEstoque(ProgressBar progressBar, TextView status, DaoSession daoSession) {
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
                .append("   CODLOCAL,")
                .append("   CODLOCAL || ' - ' || DESCRLOCAL AS DESCRLOCAL")
                .append(" FROM TGFLOC ")
                .append(" WHERE AD_USAAPP='S' ");

            JSONArray linhas = SankhyaUtil.execSelect(serviceInvoker, sql.toString());

            //Este comando coloca o maxValue da progressBar para a quantidade de formas de pagamento
            publishProgress(0, linhas.length());

            jsonLocalEstoque(linhas);

        } catch (Exception e1) {
            e1.printStackTrace();
            messages.add(new Message(Message.Type.ERROR, e1.toString()));
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            return messages;
        }
        messages.add(new Message(Message.Type.SUCESS, "Locais de estoque sincronizados."));
        return messages;
    }



    private void jsonLocalEstoque(JSONArray linhas) {
        try {
            List<LocalEstoque> locais = new ArrayList<>();
            for (int i = 0; i < linhas.length(); i++) {
                JSONArray linha = linhas.getJSONArray(i);

                //Atualiza a progressBar na interface do usuário
                publishProgress(i+1);

                LocalEstoque localEstoque = new LocalEstoque();
                localEstoque.setId(linha.getLong(0));
                localEstoque.setDescricao(JSONUtil.getString(linha, 1));
                locais.add(localEstoque);
            }
            daoSession.getLocalEstoqueDao().insertOrReplaceInTx(locais);
        } catch (JSONException e1) {
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            messages.add(new Message(Message.Type.ERROR, e1.toString()));
            e1.printStackTrace();
        }

    }

}
