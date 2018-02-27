package br.com.dsx.vendamais.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.Email;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.component.MaskEditText;
import br.com.dsx.vendamais.dao.ConfiguracaoDao;
import br.com.dsx.vendamais.dao.EmpresaDao;
import br.com.dsx.vendamais.dao.LocalEstoqueDao;
import br.com.dsx.vendamais.domain.Configuracao;
import br.com.dsx.vendamais.domain.Empresa;
import br.com.dsx.vendamais.domain.LocalEstoque;
import br.com.dsx.vendamais.domain.Perfil;
import br.com.dsx.vendamais.sync.SyncTaskBackground;
import br.com.dsx.vendamais.sync.SyncTaskBackgroundEstoque;
import br.com.dsx.vendamais.sync.SyncTaskCallback;
import butterknife.BindView;


public class ConfiguracaoFragment extends BaseFragment implements
        EmpresaFragmentList.EmpresaSelectedListener,
        LocalEstoqueFragmentList.LocalEstoqueSelectedListener {

    private transient MaskEditText empresaNome;
    private transient MaskEditText localEstoqueNome;
    private transient TextView msgConfiguracao;
    private transient LinearLayout enviarLayout;

    private Perfil perfil;
    private SyncTaskBackground estoqueSyncTask;

    public static ConfiguracaoFragment newInstance() {
        ConfiguracaoFragment fragment = new ConfiguracaoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    /**
     * Configura as opções do menu.
     */
    @Override
    public void configuraMenu(Menu menu) {

        MenuItem salvarMenuItem = menu.findItem(R.id.action_salvar);
        salvarMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                salvar();
                return false;
            }
        });
        salvarMenuItem.setVisible(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.configuracao_fragment, container, false);
        setTitle("Configurar");

        perfil = getPerfil();

        initCampos(view);

        verificaSincronizacao();

        initEmpresaLocalEstoque();

        return view;
    }

    private FragmentListener fragmentListener = new FragmentListener() {
        @Override
        public void onFragmentEdit() {
        }
        @Override
        public void onFragmentFinish() {
            configuraMenu(true);
        }
    };

    private void verificaSincronizacao() {
        ConfiguracaoDao dao = getDaoSession().getConfiguracaoDao();
        Configuracao configuracao = dao.load(Constants.ConfiguracaoIds.DATA_ULT_SINC);
        if (configuracao==null) {
            msgConfiguracao.setVisibility(View.VISIBLE);
        } else {
            msgConfiguracao.setVisibility(View.GONE);
        }
    }


    private void initCampos(View view){
        final ConfiguracaoFragment that = this;
        empresaNome = (MaskEditText) view.findViewById(R.id.empresaNome);
        empresaNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmpresaFragmentList empresaFragmentList = EmpresaFragmentList.newInstance(that, fragmentListener);
                addFragment(empresaFragmentList);
            }
        });
        localEstoqueNome = (MaskEditText) view.findViewById(R.id.localEstoque);
        localEstoqueNome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if (perfil.getIdEmpresa()==134) {
                LocalEstoqueFragmentList fragmentList = LocalEstoqueFragmentList.newInstance(that, fragmentListener);
                addFragment(fragmentList);
               /* }else {
                    StringBuilder msg = new StringBuilder();
                    msg.append("A empresa ");
                    msg.append(empresaNome.getText().toString());
                    msg.append(" deve utiizar o local ");
                    msg.append(localEstoqueNome.getText().toString());
                    Toast.makeText(this.getContext(), msg.toString(), Toast.LENGTH_LONG).show();
                }*/
                atualizaEstoque();
            }
        });
        msgConfiguracao = (TextView) view.findViewById(R.id.msgConfiguracao);

        enviarLayout = (LinearLayout) view.findViewById(R.id.enviarLayout);
        enviarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(msgConfiguracao.getContext())
                    .setTitle("Envido de Log")
                    .setMessage("O envio dos logs pode demorar um pouco. Deseja continar?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //enviarLog();
                            String email = "salazar.wagner@gmail.com";
                            String subject = "Logs Venda Mais - Ceitel";
                            String message = "O arquivo anexo representa o banco de dados SQLite.";
                            String popupMessage = "Enviando logs para o suporte ...";
                            String filename= "/data/data/br.com.dsx.vendamais/databases/venda-mais-db";
                            final Context ctx = empresaNome.getContext();
                            Email sendEmail = new Email(ctx, email, subject, message, popupMessage, filename,
                                    new Email.EmailResponse() {
                                        @Override
                                        public void processFinish(String erroMsg) {
                                            if (Util.isNotBlank(erroMsg)) {
                                                Toast.makeText(ctx,
                                                        "Verifique sua conexão com a internet e tente novamente.",Toast.LENGTH_LONG).show();
                                            }else {
                                                Toast.makeText(ctx,
                                                        "Logs enviados com sucesso.",Toast.LENGTH_LONG).show();
                                            }
                                        }
                                    });
                            sendEmail.execute();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            }
        });
    }

    private void enviarLog() {

        String email = "salazar.wagner@gmail.com";
        String subject = "Logs Venda Mais - Ceitel";
        String message = "O arquivo anexo representa o banco de dados SQLite.";
        String popupMessage = "Enviando logs para o suporte ...";
        String filename= "/data/data/br.com.dsx.vendamais/databases/venda-mais-db";
        final Context ctx = empresaNome.getContext();
        Email sendEmail = new Email(ctx, email, subject, message, popupMessage, filename,
                new Email.EmailResponse() {
                    @Override
                    public void processFinish(String erroMsg) {
                        if (Util.isNotBlank(erroMsg)) {
                            Toast.makeText(ctx,
                                    "Verifique sua conexão com a internet e tente novamente.",Toast.LENGTH_LONG).show();
                        }else {
                            Toast.makeText(ctx,
                                    "Logs enviados com sucesso.",Toast.LENGTH_LONG).show();
                        }
                    }
                });
        sendEmail.execute();
    }

    private void initEmpresaLocalEstoque() {
        if (perfil.getIdEmpresa()!=null && getDaoSession()!=null) {
            EmpresaDao empresaDao = getDaoSession().getEmpresaDao();
            Empresa empresa = empresaDao.load(perfil.getIdEmpresa());
            onEmpresaSelected(empresa);
        }
        if (perfil.getIdLocalEstoque()!=null && getDaoSession()!=null) {
            LocalEstoqueDao localDao = getDaoSession().getLocalEstoqueDao();
            LocalEstoque local = localDao.load(perfil.getIdLocalEstoque());
            onLocalEstoqueSelected(local);
        }
    }

    private void atualizaEstoque() {
        if (perfil.getIdEmpresa()!=null && perfil.getIdLocalEstoque()!=null) {
            String idEmpresa = String.valueOf(perfil.getIdEmpresa());
            String idLocal = String.valueOf(perfil.getIdLocalEstoque());
            estoqueSyncTask = new SyncTaskBackgroundEstoque(perfil, getDaoSession());
            estoqueSyncTask.setDelegate(new SyncTaskCallback<String>() {
                @Override
                public void processFinish(String values) {
                }
                @Override
                public void processCanceled(String reason) {
                }
            });
            estoqueSyncTask.execute(idEmpresa, idLocal);
        }
    }


    @Override
    public void onEmpresaSelected(Empresa empresa) {
        empresaNome.setText(empresa.getNomeFantasia());
        perfil.setIdEmpresa(empresa.getId());
       /* Long idEmpresa = perfil.getIdEmpresa();
        Long idLocalEstoque = perfil.getIdLocalEstoque();

        if (perfil.getIdEmpresa()!=134 && perfil.getIdLocalEstoque()!=1000){
            DaoSession daoSession = ((VendaMaisApplication)getActivity().getApplication()).getDaoSession();
            LocalEstoqueDao dao = daoSession.getLocalEstoqueDao();
            LocalEstoque local = dao.load(1000L);
            onLocalEstoqueSelected(local);
        }*/
        atualizaEstoque();
    }


    @Override
    public void onLocalEstoqueSelected(LocalEstoque localEstoque) {
        localEstoqueNome.setText(localEstoque.getDescricao());
        perfil.setIdLocalEstoque(localEstoque.getId());
    }



    /**
     * Os dados dos campos já forma setados diretamente no objeto devido ao data binding,
     * assim, o método recupera o objeto com os dados preenchidos da tela e salva no banco.
     */
    public void salvar(){
        if (perfil==null) return;
        if (validaCampos()) {
            getDaoSession().getPerfilDao().save(perfil);
            Util.hideKeyboard(getActivity());
            Toast.makeText(this.getContext(), R.string.configuracao_salvo, Toast.LENGTH_LONG).show();
        }
    }


    /**
     * @return True dados validados false caso contrário.
     */
    private boolean validaCampos() {

        boolean valido = true;
        if (perfil.getIdEmpresa()==null){
            empresaNome.setError("Empresa é obrigatório.");
            valido = false;
        }
        if (perfil.getIdLocalEstoque()==null){
            localEstoqueNome.setError("Local de estoque é obrigatório.");
            valido = false;
        }
        return valido;
    }


}