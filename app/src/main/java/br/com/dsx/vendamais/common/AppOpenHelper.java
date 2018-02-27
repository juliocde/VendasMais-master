package br.com.dsx.vendamais.common;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import br.com.dsx.vendamais.dao.ClienteDao;
import br.com.dsx.vendamais.dao.ConfiguracaoDao;
import br.com.dsx.vendamais.dao.DaoMaster;
import br.com.dsx.vendamais.dao.EmpresaDao;
import br.com.dsx.vendamais.dao.EstoqueDao;
import br.com.dsx.vendamais.dao.FormaPagamentoDao;
import br.com.dsx.vendamais.dao.ItemPedidoDao;
import br.com.dsx.vendamais.dao.LocalEstoqueDao;
import br.com.dsx.vendamais.dao.PedidoDao;
import br.com.dsx.vendamais.dao.ProdutoDao;
import br.com.dsx.vendamais.dao.TipoOperacaoDao;

/**
 * Created by salazar on 25/05/17.
 */

public class AppOpenHelper extends DaoMaster.OpenHelper {

    public AppOpenHelper(Context context, String name) {
        super(context, name);
    }

    public AppOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("AppOpenHelper", "Atualizando o esquema da versão " + oldVersion
                + " para " + newVersion);
        MigrationHelper.migradWithClean(db,
                ClienteDao.class,
                ConfiguracaoDao.class,
                EmpresaDao.class,
                EstoqueDao.class,
                FormaPagamentoDao.class,
                ItemPedidoDao.class,
                LocalEstoqueDao.class,
                PedidoDao.class,
                ProdutoDao.class,
                TipoOperacaoDao.class
        );
    }
}



