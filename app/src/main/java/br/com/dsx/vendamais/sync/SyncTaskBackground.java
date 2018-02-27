package br.com.dsx.vendamais.sync;

import android.os.AsyncTask;

import java.io.Serializable;

import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.domain.Configuracao;


public abstract class SyncTaskBackground extends AsyncTask<String, Integer, String>
                                         implements Serializable {

    protected SyncTaskCallback<String> delegate;
    protected DaoSession daoSession;
    protected SWServiceInvoker serviceInvoker;
    protected Configuracao configuracao;

    public SyncTaskBackground(DaoSession daoSession) {
        this.daoSession = daoSession;
    }

    @Override
    protected void onPostExecute(String value) {
        if (delegate!=null) {
            delegate.processFinish(value);
        }
    }

    @Override
    protected void onCancelled(String reason) {
        super.onCancelled(reason);
        cancelled(reason);
    }

    protected void cancelled(String reason) {
        if (delegate!=null) {
            delegate.processCanceled(reason);
        }
    }

    public void setDelegate(SyncTaskCallback<String> delegate){
        this.delegate = delegate;
    }

}
