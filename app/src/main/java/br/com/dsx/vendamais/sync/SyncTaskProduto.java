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
import br.com.dsx.vendamais.domain.Produto;


public class SyncTaskProduto extends SyncTask {

    public SyncTaskProduto(ProgressBar progressBar, TextView status, DaoSession daoSession) {
        super(progressBar, status, daoSession);
    }

    @Override
    protected List<Message> doInBackground(String... params) {

        //Pega a quantidade de clientes que vamos sincronizar.
        try {
            if (params!=null && params.length>0){
                ultimaSincronizacao = params[0];
            }

            //Baixa dos produtos cadastrados no sistema Sankhya
            baixarProdutos();

        } catch (Exception e1) {
            e1.printStackTrace();
            messages.add(new Message(Message.Type.ERROR, e1.toString()));
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            return messages;
        }
        messages.add(new Message(Message.Type.SUCESS, "Produtos sincronizados."));
        return messages;
    }

    public void baixarProdutos() throws Exception {

        StringBuilder sql =  new StringBuilder("SELECT COUNT(1) FROM AD_APPPRO");
        if (Util.isNotBlank(ultimaSincronizacao)) {
            sql.append(" WHERE DTALTER > TRUNC(TO_DATE('")
                    .append(ultimaSincronizacao)
                    .append("', 'YYYY/MM/DD HH24:MI'))");
        }

        JSONArray linhas = SankhyaUtil.execSelect(serviceInvoker, sql.toString());
        JSONArray linha = linhas.getJSONArray(0);
        int qtdProdutos = linha.getInt(0);
        if (qtdProdutos==0) {
            publishProgress(0, 10);
            for (int i=0;i<=10;i++) {
                publishProgress(i);
            }
            return;
        }

        //Atualiza o TextView STATUS com o texto "Atualizando XX cadastros"
        publishProgress(0, qtdProdutos);

        sql =  new StringBuilder()
                .append(" SELECT")
                .append(" CODPROD,")
                .append(" DESCRPROD,")
                .append(" COMPLDESC,")
                .append(" ESTOQUE, ")
                .append(" PRECO, ")
                .append(" CATEGORIA, ")
                .append(" MARCA, ")
                .append(" CODVOL, ")
                .append(" DESCRVOL, ")
                .append(" REFFORN ")
                .append("FROM AD_APPPRO");
        if (Util.isNotBlank(ultimaSincronizacao)) {
            sql.append(" WHERE DTALTER > TRUNC(TO_DATE('")
                    .append(ultimaSincronizacao)
                    .append("', 'YYYY/MM/DD HH24:MI'))");
        }
        linhas = SankhyaUtil.execSelect(serviceInvoker, sql.toString());
        jsonToProduto(linhas);
    }


    private void jsonToProduto(JSONArray linhas) {

        try {
            List<Produto> produtos = new ArrayList<>();
            for (int i = 0; i < linhas.length(); i++) {
                JSONArray linha = linhas.getJSONArray(i);
                publishProgress(i);

                Produto produto = new Produto();
                produto.setId(linha.getLong(0));
                produto.setDescricao(JSONUtil.getString(linha, 1));
                produto.setComplemento(JSONUtil.getString(linha, 2));

                String preco = JSONUtil.getString(linha, 4);
                produto.setPreco(Double.parseDouble(preco));
                produto.setCategoria(JSONUtil.getString(linha, 5));
                produto.setMarca(JSONUtil.getString(linha, 6));
                produto.setCodigoVolume(JSONUtil.getString(linha, 7));
                produto.setDescricaoVolume(JSONUtil.getString(linha, 8));
                produto.setReferenciaFornecedor(JSONUtil.getString(linha, 9));

                produtos.add(produto);
            }
            daoSession.getProdutoDao().insertOrReplaceInTx(produtos);
        } catch (JSONException e1) {
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            messages.add(new Message(Message.Type.ERROR, e1.toString()));
            e1.printStackTrace();
        }

    }

}
