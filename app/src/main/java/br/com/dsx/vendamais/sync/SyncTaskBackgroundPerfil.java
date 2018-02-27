package br.com.dsx.vendamais.sync;

import android.content.Context;

import org.json.JSONArray;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.SankhyaUtil;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.dao.PerfilDao;
import br.com.dsx.vendamais.domain.Perfil;

public class SyncTaskBackgroundPerfil extends SyncTaskBackground {

    private Perfil perfil;
    private String url;
    private String user;
    private String pass;
    private String urlDemo;

    public SyncTaskBackgroundPerfil(Perfil perfil, Context context, DaoSession daoSession){
        super(daoSession);
        this.perfil = perfil;
        this.urlDemo = context.getResources().getString(R.string.urlDEMO);
        this.url  = context.getResources().getString(R.string.urlDSX);
        this.user = context.getResources().getString(R.string.usuarioDSX);
        this.pass = context.getResources().getString(R.string.senhaDSX);
        serviceInvoker = new SWServiceInvoker(url, user, pass);
    }

    @Override
    protected String doInBackground(String... params) {

        PerfilDao perfilDao = daoSession.getPerfilDao();

        StringBuilder sql = new StringBuilder()
                .append(" SELECT ")
                .append("   IP,") //0
                .append("   USUARIOSANKHYA,") //1
                .append("   SENHASANKHYA") //2
                .append(" FROM AD_USUARIOS ")
                .append(" WHERE EMAIL = '")
                .append(perfil.getEmail())
                .append("'");

        try {
            JSONArray linhas = SankhyaUtil.execSelect(serviceInvoker, sql.toString());

            if (linhas.length() == 0) {
                perfil.setUrlSankhya(urlDemo);
                perfil.setLoginSankhya(user);
                perfil.setSenhaSankhya(pass);

                /*
                Se não retornar nenhum linha, significa que o e-mail utilizado para login
                não está cadastrado no nosso modulo da DSX.
                Com isso, devemos setar um ambiente de demonstração para o usuário.
                Como ainda não temos um ambiente de demostração, vou lançar uma exceção.
                throw new Exception("E-mail não está liberado para utilizar o aplicativo. E-mail: " + perfil.getEmail());
                */

            } else {
                //Usuário existe e está cadastrado no nosso modulo.
                JSONArray linha = linhas.getJSONArray(0);

                perfil.setUrlSankhya(linha.getString(0));
                perfil.setLoginSankhya(linha.getString(1));
                perfil.setSenhaSankhya(linha.getString(2));

                perfilDao.insertOrReplace(perfil);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }

        return Constants.SyncStatus.SUCESSO;
    }

}
