package br.com.dsx.vendamais.sync;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.SankhyaUtil;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.domain.Erro;
import br.com.dsx.vendamais.domain.Estoque;
import br.com.dsx.vendamais.domain.Perfil;

public class SyncTaskBackgroundEstoque extends SyncTaskBackground {

    private String idEmpresa;
    private String idLocal;

    public SyncTaskBackgroundEstoque(Perfil perfil, DaoSession daoSession) {
        super(daoSession);

        if (Util.isNotBlank(perfil.getUrlSankhya()) ) {
            String url  = perfil.getUrlSankhya();
            String usuario = perfil.getLoginSankhya();
            String senha = perfil.getSenhaSankhya();
            serviceInvoker = new SWServiceInvoker(url, usuario, senha);

        }
    }

    @Override
    protected String doInBackground(String... params) {

        try{
            if (params!=null && params.length>0){
                idEmpresa = params[0];
                idLocal = params[1];
            }

            StringBuilder sql = new StringBuilder()
                .append(" SELECT ")
                .append("   CODPROD, ")
                .append("   CODEMP, ")
                .append("   CODLOCAL, ")
                .append("   ESTOQUE ")
                .append(" FROM AD_APPEST ")
                .append(" WHERE  1=1 ");
            if (Util.isNotBlank(idEmpresa)) {
                sql.append(" AND CODEMP = "+idEmpresa);
            }
            if (Util.isNotBlank(idEmpresa)) {
                sql.append(" AND CODLOCAL = "+idLocal);
            }

            JSONArray linhas = SankhyaUtil.execSelect(serviceInvoker, sql.toString());
            jsonLocalEstoque(linhas);

        } catch (Exception e1) {
            e1.printStackTrace();
            daoSession.getErroDao().insert(new Erro(e1.toString()));
            return e1.getMessage();
        }
        return Constants.SyncStatus.SUCESSO;
    }



    private void jsonLocalEstoque(JSONArray linhas) {
        try {
            List<Estoque> estoques = new ArrayList<>();
            for (int i = 0; i < linhas.length(); i++) {
                JSONArray linha = linhas.getJSONArray(i);
                Estoque estoque = new Estoque();
                estoque.setIdProduto(linha.getLong(0));
                estoque.setIdEmpresa(linha.getLong(1));
                estoque.setIdLocal(linha.getLong(2));
                estoque.setQuantidade(linha.getLong(3));
                estoques.add(estoque);
            }
            daoSession.getEstoqueDao().insertOrReplaceInTx(estoques);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
