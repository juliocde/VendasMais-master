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
import br.com.dsx.vendamais.domain.TipoOperacao;

public class SyncTaskTipoOperacao extends SyncTask {

    public SyncTaskTipoOperacao(ProgressBar progressBar, TextView status, DaoSession daoSession) {
        super(progressBar, status, daoSession);
    }

    @Override
    protected List<Message>  doInBackground(String... params) {

        try{
            if (params!=null && params.length>0){
                ultimaSincronizacao = params[0];
            }

            StringBuilder sql = new StringBuilder()
                .append(" SELECT ")
                .append("   CODTIPOPER,")
                .append("   DESCROPER, ")
                .append("   RESERVA ")
                .append(" FROM AD_APPTOP ");

            JSONArray linhas = SankhyaUtil.execSelect(serviceInvoker, sql.toString());

            publishProgress(0, linhas.length());

            jsonTipoOperacao(linhas);

        } catch (Exception e1) {
            e1.printStackTrace();
            messages.add(new Message(Message.Type.ERROR, e1.toString()));
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            return messages;
        }
        messages.add(new Message(Message.Type.SUCESS, "Tipos de operação sincronizados."));
        return messages;
    }



    private void jsonTipoOperacao(JSONArray linhas) {
        try {
            List<TipoOperacao> tipos = new ArrayList<>();
            for (int i = 0; i < linhas.length(); i++) {
                JSONArray linha = linhas.getJSONArray(i);

                publishProgress(i+1);

                TipoOperacao tipoOperacao = new TipoOperacao();
                tipoOperacao.setId(linha.getLong(0));
                tipoOperacao.setDescricao(JSONUtil.getString(linha, 1));
                tipoOperacao.setReserva(JSONUtil.getString(linha, 2));

                tipos.add(tipoOperacao);
            }
            daoSession.getTipoOperacaoDao().insertOrReplaceInTx(tipos);
        } catch (JSONException e1) {
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            messages.add(new Message(Message.Type.ERROR, e1.toString()));
            e1.printStackTrace();
        }
    }
}
