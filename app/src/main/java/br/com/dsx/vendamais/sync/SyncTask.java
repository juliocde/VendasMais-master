package br.com.dsx.vendamais.sync;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import br.com.dsx.vendamais.common.Message;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.dao.PerfilDao;
import br.com.dsx.vendamais.domain.Configuracao;
import br.com.dsx.vendamais.domain.Perfil;


public abstract class SyncTask extends AsyncTask<String, Integer, List<Message>>  {

    protected int progressoGeral;
    protected SyncTaskCallback<List<Message>> delegate;
    protected DaoSession daoSession;
    protected SWServiceInvoker serviceInvoker;
    protected ProgressBar progressBar;
    protected TextView status;
    protected Configuracao configuracao;
    protected String ultimaSincronizacao = null;
    protected List<Message> messages;

    public SyncTask(ProgressBar progressBar,
                    TextView status,
                    DaoSession daoSession) {
        this.progressBar = progressBar;
        this.status = status;
        this.daoSession = daoSession;
        this.messages = new ArrayList<>();
        Context context = progressBar.getContext();

        PerfilDao perfilDao = daoSession.getPerfilDao();
        List<Perfil> list =  perfilDao.loadAll();
        if (!list.isEmpty()) {
            Perfil perfil = list.get(0);
            String url  = perfil.getUrlSankhya();
            String usuario = perfil.getLoginSankhya();
            String senha = perfil.getSenhaSankhya();

            if (url == null){
                messages.add(new Message(Message.Type.ERROR,
                        "Não foi possível iniciar conexão com o SankhyaW"));
                //throw new Exception("Não foi possível iniciar conexão com o SankhyaW. IP em branco.");
                System.out.println("Não foi possível iniciar conexão com o SankhyaW. IP em branco. Usuário "
                        + perfil.getEmail());
                //throw new Exception("Não foi possível iniciar conexão com o SankhyaW. IP em branco. Usuário " + perfil.getEmail());
            }

            serviceInvoker = new SWServiceInvoker(url, usuario, senha);
        }
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressoGeral=0;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (progressBar != null && values != null) {
            if (values.length == 2) {
                status.setText("sincronizando " + values[1] + " cadastro(s).");
                progressBar.setMax(values[1]);
            }
            progressBar.setProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(List<Message> values) {
        if (status != null)
            status.setText("sincronizado.");

        if (delegate!=null) {
            delegate.processFinish(values);
        }
    }

    @Override
    protected void onCancelled(List<Message> reason) {
        super.onCancelled(reason);
        cancelled(reason);
    }

    protected void cancelled(List<Message> reasons) {
        if (delegate!=null) {
            delegate.processCanceled(reasons);
        }
    }

    public void setDelegate(SyncTaskCallback<List<Message>> delegate){
        this.delegate = delegate;
    }

    public List<Message> getMessages(){
        return messages;
    }

}
