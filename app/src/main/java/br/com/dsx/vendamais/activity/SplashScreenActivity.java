package br.com.dsx.vendamais.activity;

import android.os.Bundle;

import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.domain.Perfil;

/**
 * Created by salazar on 14/05/17.
 */

public class SplashScreenActivity extends MainActivity {

    private Perfil perfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPerfilAndStart();
    }

    private void initPerfilAndStart() {
        perfil = getPerfil();
        switch (perfil.getTipo()) {
            case Constants.TipopPerfil.FACEBOOK:
                if (isLoggedFacebookIn()) {
                    startApp(perfil);
                } else {
                    toLoginApp();
                    finish();
                }
                break;
            default:
                if (perfil.isLogado()) {
                    startApp(perfil);
                } else {
                    toLoginApp();
                    finish();
                }
        }
    }
}
