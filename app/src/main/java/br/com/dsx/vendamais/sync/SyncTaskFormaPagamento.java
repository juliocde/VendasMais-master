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
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.domain.Erro;
import br.com.dsx.vendamais.domain.FormaPagamento;

public class SyncTaskFormaPagamento extends SyncTask {

    public SyncTaskFormaPagamento(ProgressBar progressBar, TextView status, DaoSession daoSession) {
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
                .append("   CODTIPVENDA,")
                .append("   DESCRTIPVENDA, ")
                .append("   TAXA ")
                .append(" FROM AD_FORPAG");

            JSONArray linhas = SankhyaUtil.execSelect(serviceInvoker, sql.toString());

            //Este comando coloca o maxValue da progressBar para a quantidade de formas de pagamento
            publishProgress(0, linhas.length());

            jsonFormaPagamento(linhas);

        } catch (Exception e1) {
            e1.printStackTrace();
            messages.add(new Message(Message.Type.ERROR, e1.toString()));
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            return messages;
        }
        messages.add(new Message(Message.Type.SUCESS, "Formas de pagamento sincronizadas."));
        return messages;
    }



    private void jsonFormaPagamento(JSONArray linhas) {
        try {
            List<FormaPagamento> formas = new ArrayList<>();
            for (int i = 0; i < linhas.length(); i++) {
                JSONArray linha = linhas.getJSONArray(i);

                //Atualiza a progressBar na interface do usuário
                publishProgress(i+1);

                FormaPagamento formaPagamento = new FormaPagamento();
                formaPagamento.setId(linha.getLong(0));
                formaPagamento.setDescricao(JSONUtil.getString(linha, 1));
                formaPagamento.setTaxa(Util.stringToDouble(JSONUtil.getString(linha, 2), 0.0));
                formas.add(formaPagamento);
                System.out.print(formaPagamento);
            }
            daoSession.getFormaPagamentoDao().insertOrReplaceInTx(formas);
        } catch (JSONException e1) {
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            messages.add(new Message(Message.Type.ERROR, e1.toString()));
            e1.printStackTrace();
        }

    }

}
