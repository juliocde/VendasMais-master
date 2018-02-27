package br.com.dsx.vendamais.sync;

import android.content.Context;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONArray;
import org.w3c.dom.Document;

import java.util.List;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.common.Message;
import br.com.dsx.vendamais.common.SankhyaUtil;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.domain.Erro;
import br.com.dsx.vendamais.domain.Perfil;


public class SyncTaskLogErro extends SyncTask {

    private String emailUsuario;

    public SyncTaskLogErro(ProgressBar progressBar, TextView status, DaoSession daoSession, Perfil perfil, Context context) {
        super(progressBar, status, daoSession);

        String url  = context.getResources().getString(R.string.urlDSX);
        String user = context.getResources().getString(R.string.usuarioDSX);
        String pass = context.getResources().getString(R.string.senhaDSX);
        serviceInvoker = new SWServiceInvoker(url, user, pass);
        emailUsuario = perfil.getEmail();
    }

    @Override
    protected List<Message> doInBackground(String... params) {

        try {

            //Busca todos os logs do BD. E vamos excluir após sincronizar.
            List<Erro> list =  daoSession.getErroDao().loadAll();

            if (list.isEmpty()){
                messages.add(new Message(Message.Type.SUCESS, "Logs sincronizados."));
                return messages;
            }

            StringBuilder sql = new StringBuilder()
                    .append(" SELECT" +
                            "    CODUSU" +
                            "    FROM AD_USUARIOS" +
                            "    WHERE EMAIL = '")
                    .append(emailUsuario)
                    .append("'");

            int codUsuario;

            JSONArray linha = SankhyaUtil.execSelect(serviceInvoker, sql.toString());
            if (linha.length()>0) {
                linha = linha.getJSONArray(0);

                codUsuario = linha.getInt(0);
            }
            else {
                return messages;
            }


            for (Erro logErro : list) {

                String xmlAsString = "<entity name='APPLOG'>"
                        + "<campo nome='CODUSU'>" + codUsuario + "</campo>"
                        + "<campo nome='DT'>" + Util.dateToString("dd/MM/yyyy HH:mm", logErro.getData())+ "</campo>"
                        + "<campo nome='LOG'>" + logErro.getDescricao() + "</campo>"
                        + "<campo nome='LOGIN'>" + Util.perfil.getEmail() + "</campo>"
                        + "<campo nome='OS_VERSION'>" + logErro.getOsVersion() + "</campo>"
                        + "<campo nome='OS_API_LEVEL'>" + logErro.getOsAPILevel() + "</campo>"
                        + "<campo nome='DEVICE'>" + logErro.getDevice() + "</campo>"
                        + "<campo nome='MODEL'>" + logErro.getModel() + "</campo>"
                        + "</entity>";

                try {
                    Document docRet = serviceInvoker.call("crud.save", "mge", xmlAsString);
                    //SWServiceInvoker.printDocument(docRet);

                } catch (Exception e1) {
                    e1.printStackTrace();
                    /*Message msg = new Message(Message.Type.ERROR,
                            p.toValidationString()+"\n"+ e1.toString());
                    messages.add(msg);*/
                    daoSession.getErroDao().insert(new Erro(e1.toString()));
                }
            } //for

            //Apaga todos os logs que foram enviados
            daoSession.getErroDao().deleteInTx(list);

        } catch (Exception e1) {
            e1.printStackTrace();
        }

        if (messages.isEmpty()) {
            messages.add(new Message(Message.Type.SUCESS, "Logs sincronizados."));
        }
        return messages;
    }



}
