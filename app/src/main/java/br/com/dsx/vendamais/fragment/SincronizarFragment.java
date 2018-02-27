package br.com.dsx.vendamais.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.VendaMaisApplication;
import br.com.dsx.vendamais.adapter.MessageAdapter;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.Message;
import br.com.dsx.vendamais.common.MessageUtil;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.ConfiguracaoDao;
import br.com.dsx.vendamais.domain.Configuracao;
import br.com.dsx.vendamais.domain.Perfil;
import br.com.dsx.vendamais.sync.SyncTask;
import br.com.dsx.vendamais.sync.SyncTaskLogErro;
import br.com.dsx.vendamais.sync.SyncTaskCallback;
import br.com.dsx.vendamais.sync.SyncTaskCliente;
import br.com.dsx.vendamais.sync.SyncTaskEmpresa;
import br.com.dsx.vendamais.sync.SyncTaskEstoque;
import br.com.dsx.vendamais.sync.SyncTaskFormaPagamento;
import br.com.dsx.vendamais.sync.SyncTaskLocalEstoque;
import br.com.dsx.vendamais.sync.SyncTaskPedido;
import br.com.dsx.vendamais.sync.SyncTaskProduto;
import br.com.dsx.vendamais.sync.SyncTaskTipoOperacao;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class SincronizarFragment extends BaseFragment {

    @BindView(R.id.data_ult_sync) TextView dataLabel;
    @BindView(R.id.sync_label) TextView label;
    @BindView(R.id.sync_status) TextView status;
    @BindView(R.id.sync_progress_bar) ProgressBar progressBar;
    @BindView(R.id.apagarLayout) LinearLayout apagarLayout;
    @BindView(R.id.baixarLayout) LinearLayout baixarLayout;
    @BindView(R.id.enviarLayout) LinearLayout enviarLayout;
    @BindView(R.id.imgApagar) ImageView imgApagar;
    @BindView(R.id.imgBaixar) ImageView imgBaixar;
    @BindView(R.id.imgEnviar) ImageView imgEnviar;
    @BindView(R.id.txtApagar) TextView txtApagar;
    @BindView(R.id.txtBaixar) TextView txtBaixar;
    @BindView(R.id.txtEnviar) TextView txtEnviar;

    @BindView(R.id.logLayout) RelativeLayout logLayout;


    private SyncTask estoqueSyncTask;
    private SyncTask clienteSyncTask;
    private SyncTask produtoSyncTask;
    private SyncTask formaPagamentoSyncTask;
    private SyncTask empresaSyncTask;
    private SyncTask localEstoqueSyncTask;
    private SyncTask tipoOperacaoSyncTask;
    private SyncTask pedidoSyncTask;
    private SyncTask logSyncTask;

    private String ultimaSincronizacao;
    private Configuracao configuracao;
    private MessageAdapter messageAdapter;
    private List<Message> messages;
    private boolean isButtonsEnabled;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messages = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sincronizar_fragment, container, false);
        unbinder = ButterKnife.bind(this, view);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.messagesListRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        messageAdapter = new MessageAdapter(getFragmentManager(), getDaoSession());
        recyclerView.setAdapter(messageAdapter);
        messageAdapter.setMessages(messages);
        if (!messages.isEmpty()) {
            logLayout.setVisibility(View.VISIBLE);
        }

        configuracao = recuperaConfiguracao();
        enabledButtons(true);
        setConfiguracaoCampos();
        setTitle("Sincronizar");
        return view;
    }

    @OnClick(R.id.apagarLayout)
    public void apagaTodosRegistros(){

        new AlertDialog.Builder(apagarLayout.getContext())
            .setTitle("Apagar todos os dados")
            .setMessage("Os clientes e pedidos não enviados serão perdidos. Deseja continar?")
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    getDaoSession().getClienteDao().deleteAll();
                    getDaoSession().getConfiguracaoDao().deleteAll();
                    getDaoSession().getEmpresaDao().deleteAll();
                    //Os erros são pagar somente quando são enviados para o Sankhya
                    getDaoSession().getEstoqueDao().deleteAll();
                    getDaoSession().getFormaPagamentoDao().deleteAll();
                    //FotoProduto por enquanto não tem dado gravado.
                    getDaoSession().getItemPedidoDao().deleteAll();
                    getDaoSession().getLocalEstoqueDao().deleteAll();
                    getDaoSession().getPedidoDao().deleteAll();
                    //Perfil nunca é apagado.
                    getDaoSession().getProdutoDao().deleteAll();
                    getDaoSession().getTipoOperacaoDao().deleteAll();

                    getDaoSession().getPerfilDao().deleteAll();

                    Perfil perfil = getPerfil();
                    perfil.setIdEmpresa(null);
                    perfil.setIdLocalEstoque(null);
                    getDaoSession().getPerfilDao().insertOrReplace(perfil);

                    messages.clear();
                    messageAdapter.setMessages(messages);
                    label.setVisibility(View.GONE);
                    status.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    logLayout.setVisibility(View.GONE);

                    dataLabel.setText("Ainda não foi realizada nenhuma sincronização.");
                    ultimaSincronizacao = Util.STRING_EMPTY;
                    Toast.makeText(getContext(), "Registros excluídos", Toast.LENGTH_LONG).show();
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


    @OnClick(R.id.enviarLayout)
    public void enviarRegistros() {
        if (!isButtonsEnabled) return;

        enabledButtons(false);
        messages.clear();
        messageAdapter.setMessages(messages);
        logLayout.setVisibility(View.GONE);

        initSincronizacaoCliente(SyncTaskCliente.Tipo.ENVIAR);
    }

    @OnClick(R.id.baixarLayout)
    public void baixarRegistros() {
        if (!isButtonsEnabled) return;

        enabledButtons(false);
        messages.clear();
        messageAdapter.setMessages(messages);
        logLayout.setVisibility(View.GONE);

        initSincronizacaoCliente(SyncTaskCliente.Tipo.BAIXAR);
    }


    /**
     *
     */
    public void initSincronizacaoCliente(final int tipo) {
        label.setText("Clientes: ");
        status.setText(" ");
        clienteSyncTask = new SyncTaskCliente(tipo, progressBar, status, getDaoSession());
        clienteSyncTask.setDelegate(new SyncTaskCallback<List<Message>>() {
            @Override
            public void processFinish(List<Message> messages) {
                addMessages("Clientes",messages);
                boolean hasError = MessageUtil.hasError(messages);
                if (!hasError) {
                    if (tipo == SyncTaskCliente.Tipo.ENVIAR) {
                        initSincronizacaoPedido(tipo);
                    } else {
                        initSincronizacaoEstoque();
                    }

                }else {
                    finalizaSincronizacao();
                }
            }
            @Override
            public void processCanceled(List<Message> messages) {
                addMessages("Clientes", messages);
                enabledButtons(true);
            }
        });
        label.setVisibility(View.VISIBLE);
        status.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        clienteSyncTask.execute(ultimaSincronizacao);
    }


    public void initSincronizacaoPedido(final int tipo){
        label.setText("Pedidos: ");
        status.setText(" ");
        pedidoSyncTask = new SyncTaskPedido(progressBar, status, getDaoSession());
        pedidoSyncTask.setDelegate(new SyncTaskCallback<List<Message>>() {
            @Override
            public void processFinish(List<Message> messages) {
                addMessages("Pedidos", messages);
                if (tipo == SyncTaskCliente.Tipo.BAIXAR) {
                    initSincronizacaoEstoque();
                }else{
                    initSincronizacaoLogErro();
                }
            }
            @Override
            public void processCanceled(List<Message> messages) {
                addMessages("Pedidos", messages);
                enabledButtons(true);
            }
        });
        pedidoSyncTask.execute(ultimaSincronizacao);
    }

    /**
     * A sincronização do log de erros executará de forma silenciosa.
     * Ou seja se tiver sucesso ou erros, não emitirá mensagens ao usuário. Pois ele não tem
     * interesse nisso.
     */
    public void initSincronizacaoLogErro(){
        label.setText("Enviando Log de Erros: ");
        status.setText(" ");
        Perfil p = ((VendaMaisApplication) this.getActivity().getApplication()).getPerfil();

        logSyncTask = new SyncTaskLogErro(progressBar, status, getDaoSession(), p, getContext());
        logSyncTask.setDelegate(new SyncTaskCallback<List<Message>>() {
            @Override
            public void processFinish(List<Message> messages) {
                addMessages("Logs", messages);
                finalizaSincronizacao();
            }
            @Override
            public void processCanceled(List<Message> messages) {
                addMessages("Logs", messages);
                enabledButtons(true);
            }
        });
        logSyncTask.execute(ultimaSincronizacao);
    }

    public void initSincronizacaoEstoque(){
        label.setText("Estoques: ");
        status.setText(" ");
        estoqueSyncTask = new SyncTaskEstoque(progressBar, status, getDaoSession());
        estoqueSyncTask.setDelegate(new SyncTaskCallback<List<Message>>() {
            @Override
            public void processFinish(List<Message> messages) {
                addMessages("Estoques", messages);
                initSincronizacaoProduto();
            }
            @Override
            public void processCanceled(List<Message> messages) {
                addMessages("Estoques", messages);
                enabledButtons(true);
            }
        });
        estoqueSyncTask.execute(ultimaSincronizacao);
    }


    public void initSincronizacaoProduto() {
        label.setText("Produtos: ");
        status.setText(" ");
        produtoSyncTask = new SyncTaskProduto(progressBar, status, getDaoSession());
        produtoSyncTask.setDelegate(new SyncTaskCallback<List<Message>>() {
            @Override
            public void processFinish(List<Message> messages) {
                addMessages("Produtos", messages);
                initSincronizacaoFormaPagamento();
            }
            @Override
            public void processCanceled(List<Message> messages) {
                addMessages("Produtos", messages);
                enabledButtons(true);
            }
        });
        produtoSyncTask.execute(ultimaSincronizacao);
    }


    public void initSincronizacaoFormaPagamento() {
        label.setText("Formas de pagamento: ");
        status.setText(" ");
        formaPagamentoSyncTask = new SyncTaskFormaPagamento(progressBar, status, getDaoSession());
        formaPagamentoSyncTask.setDelegate(new SyncTaskCallback<List<Message>>() {
            @Override
            public void processFinish(List<Message> messages) {
                addMessages("Formas de pagamento", messages);
                initSincronizacaoEmpresa();
            }
            @Override
            public void processCanceled(List<Message> messages) {
                addMessages("Formas de pagamento", messages);
                enabledButtons(true);
            }
        });
        formaPagamentoSyncTask.execute(ultimaSincronizacao);
    }


    public void initSincronizacaoEmpresa() {
        label.setText("Empresas: ");
        status.setText(" ");
        empresaSyncTask = new SyncTaskEmpresa(progressBar, status, getDaoSession());
        empresaSyncTask.setDelegate(new SyncTaskCallback<List<Message>>() {
            @Override
            public void processFinish(List<Message> messages) {
                addMessages("Empresas", messages);
                initSincronizacaoLocalEstoque();
            }
            @Override
            public void processCanceled(List<Message> messages) {
                addMessages("Empresas", messages);
                enabledButtons(true);
            }
        });
        empresaSyncTask.execute(ultimaSincronizacao);
    }


    public void initSincronizacaoLocalEstoque() {
        label.setText("Locais de estoque: ");
        status.setText(" ");
        localEstoqueSyncTask = new SyncTaskLocalEstoque(progressBar, status, getDaoSession());
        localEstoqueSyncTask.setDelegate(new SyncTaskCallback<List<Message>>() {
            @Override
            public void processFinish(List<Message> messages) {
                addMessages("Locais de estoque", messages);
                initSincronizacaoTipoOperacao();
            }
            @Override
            public void processCanceled(List<Message> messages) {
                addMessages("Locais de estoque", messages);
                enabledButtons(true);
            }
        });
        localEstoqueSyncTask.execute(ultimaSincronizacao);
    }

    /**
     *
     */
    public void initSincronizacaoTipoOperacao() {
        label.setText("Tipos de operação: ");
        status.setText(" ");
        tipoOperacaoSyncTask = new SyncTaskTipoOperacao(progressBar, status, getDaoSession());
        tipoOperacaoSyncTask.setDelegate(new SyncTaskCallback<List<Message>>() {
            @Override
            public void processFinish(List<Message> messages) {
                addMessages("Tipos de operação", messages);
                finalizaSincronizacao();
            }
            @Override
            public void processCanceled(List<Message> messages) {
                addMessages("Tipos de operação", messages);
                enabledButtons(true);
            }
        });
        tipoOperacaoSyncTask.execute(ultimaSincronizacao);
    }


    private void finalizaSincronizacao() {
        enabledButtons(true);
        status.setVisibility(View.GONE);

        boolean sucess = MessageUtil.onlySucess(messages);
        if (sucess){
            label.setText("Sincronização concluída com sucesso.");
        }else{
            label.setText("Sincronização concluída com PENDÊNCIAS.");
        }

        atualizaConfiguracao(configuracao);
    }

    /**
     * Recupera o registro que contém a data da última sincronização
     * @return
     */
    public Configuracao recuperaConfiguracao() {
        ConfiguracaoDao dao = getDaoSession().getConfiguracaoDao();
        Configuracao configuracao = dao.load(Constants.ConfiguracaoIds.DATA_ULT_SINC);
        if (configuracao==null) {
            configuracao = new Configuracao();
            configuracao.setId(Constants.ConfiguracaoIds.DATA_ULT_SINC);
        } else {
            Date dataUltimaSincronizacao = configuracao.getData();
        }
        return configuracao;
    }

    /**
     * Atualiza a data da última sincronização.
     * @param configuracao
     */
    public void atualizaConfiguracao(Configuracao configuracao) {
        configuracao.setData(new Date());
        ConfiguracaoDao dao = getDaoSession().getConfiguracaoDao();
        dao.insertOrReplace(configuracao);
        setConfiguracaoCampos();
    }

    public void setConfiguracaoCampos(){

        if (configuracao.getData()!=null) {
            ultimaSincronizacao = Util.dateToString(Constants.DateFormat.DATA_HORA_BANCO, configuracao.getData());
            dataLabel.setText("Última atualização: "+
                    Util.dateToString(Constants.DateFormat.DATA_HORA, configuracao.getData()));
        }else {
            ultimaSincronizacao = Util.STRING_EMPTY;
            dataLabel.setText("Ainda não foi realizada nenhuma sincronização.");
        }
    }

    private void addMessages(String key, List<Message> msg){
        messages.addAll(msg);
        messageAdapter.setMessages(messages);
        logLayout.setVisibility(View.VISIBLE);
    }

    private void enabledButtons(boolean value){
        isButtonsEnabled = value;
        int color = ContextCompat.getColor(imgApagar.getContext(), R.color.colorDivider);
        if (!isButtonsEnabled) {
            color = ContextCompat.getColor(imgApagar.getContext(), R.color.lineColor);
        }
        imgApagar.setImageDrawable(Util.changeDrawableTint(imgApagar.getDrawable(), color));
        imgBaixar.setImageDrawable(Util.changeDrawableTint(imgBaixar.getDrawable(), color));
        imgEnviar.setImageDrawable(Util.changeDrawableTint(imgEnviar.getDrawable(), color));
        txtApagar.setTextColor(color);
        txtBaixar.setTextColor(color);
        txtEnviar.setTextColor(color);
    }
}
