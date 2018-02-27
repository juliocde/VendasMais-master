package br.com.dsx.vendamais.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.google.android.gms.common.api.GoogleApiClient;

import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.VendaMaisApplication;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.dao.PerfilDao;
import br.com.dsx.vendamais.domain.Perfil;
import br.com.dsx.vendamais.sync.SyncTaskBackground;
import br.com.dsx.vendamais.sync.SyncTaskBackgroundPerfil;

/**
 * Created by salazar on 04/04/17.
 */

public class MainActivity extends AppCompatActivity {

    protected PerfilDao perfilDao;
    protected GoogleApiClient googleApiClient;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaoSession daoSession = ((VendaMaisApplication)getApplication()).getDaoSession();
        perfilDao = daoSession.getPerfilDao();
    }

    protected void startApp(Perfil perfil){

        DaoSession daoSession = ((VendaMaisApplication)getApplication()).getDaoSession();
        SyncTaskBackground sync = new SyncTaskBackgroundPerfil(perfil, getBaseContext(), daoSession);
        sync.execute();

        Intent intent = new Intent(this, PrincipalActivity.class);
        intent.putExtra(Constants.Key.PERFIL, perfil);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    protected boolean isLoggedFacebookIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    protected void toLoginApp(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    protected Perfil getPerfil(){
        return ((VendaMaisApplication)this.getApplication()).getPerfil();
    }

    protected void setPerfil(Perfil perfil) {
        ((VendaMaisApplication)this.getApplication()).setPerfil(perfil);
    }
}
