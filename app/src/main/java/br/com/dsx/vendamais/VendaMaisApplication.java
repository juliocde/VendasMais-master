package br.com.dsx.vendamais;

import android.app.Application;
import android.view.Menu;

import com.facebook.drawee.backends.pipeline.Fresco;

import org.greenrobot.greendao.database.Database;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.dsx.vendamais.common.AppOpenHelper;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.MenuConfig;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.DaoMaster;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.dao.PerfilDao;
import br.com.dsx.vendamais.domain.Perfil;
import br.com.dsx.vendamais.sync.SWServiceInvoker;

public class VendaMaisApplication extends Application {
    /**
     * A flag to show how easily you can switch from standard SQLite to the encrypted SQLCipher.
     */
    public static final boolean ENCRYPTED = false;
    private DaoSession daoSession;
    private DaoMaster daoMaster;
    private Map<String, MenuConfig> menuConfig;
    private Menu menu;
    private SWServiceInvoker sankhyaService;

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this);

        menuConfig = new HashMap<>();
        menuConfig.put(Constants.Key.PRODUTO,new MenuConfig());
        menuConfig.put(Constants.Key.CLIENTE,new MenuConfig());
        menuConfig.put(Constants.Key.PEDIDO,new MenuConfig());
        menuConfig.put(Constants.Key.EMPRESA,new MenuConfig());
        menuConfig.put(Constants.Key.LOCAL_ESTOQUE,new MenuConfig());
        menuConfig.put(Constants.Key.FORMA_PAGMENTO,new MenuConfig());

        AppOpenHelper helper = new AppOpenHelper(this, ENCRYPTED ? "notes-db-encrypted" : "venda-mais-db");
        Database db = ENCRYPTED ? helper.getEncryptedWritableDb("super-secret") : helper.getWritableDb();
        daoMaster = new DaoMaster(db);
        daoSession = daoMaster.newSession();

        String url  = getResources().getString(R.string.urlDSX);
        String user = getResources().getString(R.string.usuarioDSX);
        String pass = getResources().getString(R.string.senhaDSX);
        sankhyaService = new SWServiceInvoker(url, user, pass);

    }

    public DaoSession getDaoSession() {
        return daoSession;
    }

    public DaoSession getDaoSession(boolean newSession) {
        if (newSession) {
            return daoMaster.newSession();
        }
        return daoSession;
    }

    public Perfil getPerfil() {
        if (Util.perfil == null) {
            PerfilDao dao = getDaoSession().getPerfilDao();
            List<Perfil> list = dao.loadAll();
            if (!list.isEmpty()) {
                Util.perfil = list.get(0);
            } else {
                Util.perfil = new Perfil();
            }
        }
        return Util.perfil;
    }

    public void setPerfil(Perfil perfil) {
        Util.perfil = perfil;
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        this.menu = menu;
    }

    public MenuConfig getMenuConfig(String fragmentKey) {
        MenuConfig menuConfig = null;
        if (this.menuConfig.containsKey(fragmentKey)) {
            menuConfig = this.menuConfig.get(fragmentKey);
        }
        return menuConfig;
    }

    public SWServiceInvoker getSankhyaService() {
        return sankhyaService;
    }

}
