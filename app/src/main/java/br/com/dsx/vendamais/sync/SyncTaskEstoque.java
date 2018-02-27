package br.com.dsx.vendamais.sync;

import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import br.com.dsx.vendamais.common.Message;
import br.com.dsx.vendamais.common.SankhyaUtil;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.domain.Erro;
import br.com.dsx.vendamais.domain.Estoque;

public class SyncTaskEstoque extends SyncTask {

    public SyncTaskEstoque(ProgressBar progressBar, TextView status, DaoSession daoSession) {
        super(progressBar, status, daoSession);
    }

    @Override
    protected List<Message> doInBackground(String... params) {

        try{
            if (params!=null && params.length>0){
                ultimaSincronizacao = params[0];
            }

            //Pega a quantidade de clientes que vamos sincronizar.
            StringBuilder sql = new StringBuilder();
            sql.append(" SELECT COUNT(1)")
                    .append(" FROM AD_APPEST");

            JSONArray linhas = SankhyaUtil.execSelect(serviceInvoker, sql.toString());
            JSONArray linha = linhas.getJSONArray(0);
            int qtdEstoques = linha.getInt(0);
            if (qtdEstoques == 0) {
                publishProgress(0, 1);
                publishProgress(1);
                messages.add(new Message(Message.Type.SUCESS, "Estoque sincronizados."));
                return messages;
            }

            publishProgress(0, qtdEstoques);

            //Armazena o código do último produto sincronizado. Será utilizado no SELECT
            int ultimoProdutoId = 0;
            //Cada consulta jSON retorna até 5000 parceiros
            for (int k = 0; qtdEstoques > 0 && k <= Math.ceil(qtdEstoques / 5000); k++) {

                sql = new StringBuilder()
                        .append(" SELECT ")
                        .append("   CODPROD, ")
                        .append("   CODEMP, ")
                        .append("   CODLOCAL, ")
                        .append("   ESTOQUE ")
                        .append(" FROM AD_APPEST ")
                        .append(" WHERE CODPROD > ").append(ultimoProdutoId)
                        .append(" ORDER BY CODPROD");

                linhas = SankhyaUtil.execSelect(serviceInvoker, sql.toString());
                if (linhas != null && linhas.length() > 0) {
                    ultimoProdutoId = jsonLocalEstoque(linhas);
                } else {
                    break;
                }

            }

        } catch (Exception e1) {
            e1.printStackTrace();
            messages.add(new Message(Message.Type.ERROR, e1.toString()));
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            return messages;
        }
        messages.add(new Message(Message.Type.SUCESS, "Estoque sincronizados."));
        return messages;
    }



    private int jsonLocalEstoque(JSONArray linhas) {
        try {
            JSONArray linha = null;
            List<Estoque> estoques = new ArrayList<>();
            for (int i = 0; i < linhas.length(); i++) {
                linha = linhas.getJSONArray(i);

                //Atualiza a progressBar na interface do usuário
                progressoGeral++;
                publishProgress(progressoGeral);

                Estoque estoque = new Estoque();
                estoque.setIdProduto(linha.getLong(0));
                estoque.setIdEmpresa(linha.getLong(1));
                estoque.setIdLocal(linha.getLong(2));
                estoque.setQuantidade(linha.getLong(3));
                estoques.add(estoque);
            }
            daoSession.getEstoqueDao().insertOrReplaceInTx(estoques);
            if (linha != null) {
                return linha.getInt(0);
            }
        } catch (JSONException e) {
            daoSession.getErroDao().insert(new Erro(e.toString()));
            e.printStackTrace();
        }
        return 0;
    }

}
