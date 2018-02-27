package br.com.dsx.vendamais.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.VendaMaisApplication;
import br.com.dsx.vendamais.activity.PrincipalActivity;
import br.com.dsx.vendamais.adapter.ItemPedidoAdapter;
import br.com.dsx.vendamais.adapter.TipoOperacaoAdapter;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.component.MaskEditText;
import br.com.dsx.vendamais.dao.ClienteDao;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.dao.EmpresaDao;
import br.com.dsx.vendamais.dao.FormaPagamentoDao;
import br.com.dsx.vendamais.dao.ItemPedidoDao;
import br.com.dsx.vendamais.dao.LocalEstoqueDao;
import br.com.dsx.vendamais.dao.PedidoDao;
import br.com.dsx.vendamais.dao.ProdutoDao;
import br.com.dsx.vendamais.dao.TipoOperacaoDao;
import br.com.dsx.vendamais.databinding.PedidoFragmentBinding;
import br.com.dsx.vendamais.domain.Cliente;
import br.com.dsx.vendamais.domain.Empresa;
import br.com.dsx.vendamais.domain.FormaPagamento;
import br.com.dsx.vendamais.domain.ItemPedido;
import br.com.dsx.vendamais.domain.LocalEstoque;
import br.com.dsx.vendamais.domain.Pedido;
import br.com.dsx.vendamais.domain.Perfil;
import br.com.dsx.vendamais.domain.Produto;
import br.com.dsx.vendamais.domain.TipoOperacao;
import br.com.dsx.vendamais.sync.SyncTaskBackground;
import br.com.dsx.vendamais.sync.SyncTaskBackgroundEstoque;
import br.com.dsx.vendamais.sync.SyncTaskCallback;


public class PedidoFragment extends BaseFragment implements
        ClienteFragmentList.ClienteSelectedListener,
        FormaPagamentoFragmentList.FormaPagamentoSelectedListener,
        EmpresaFragmentList.EmpresaSelectedListener,
        LocalEstoqueFragmentList.LocalEstoqueSelectedListener,
        ProdutoFragmentList.ProdutoSelectedListener,
        ItemPedidoAdapter.ItemPedidoClickListener,
        TipoOperacaoAdapter.TipoOperacaoClickListener {

    private transient MaskEditText pedidoData;
    private transient MaskEditText clienteNome;
    private transient MaskEditText empresaNome;
    private transient MaterialBetterSpinner spnTipoOperacao;
    private transient MaskEditText localEstoqueNome;
    private transient MaskEditText frmPgDescricao;
    private transient MaskEditText produtos;
    private transient MaskEditText observacao;
    private transient MaskEditText totalPedido;
    private transient RecyclerView recyclerView;
    private transient SyncTaskBackground estoqueSyncTask;

    private Pedido pedido;
    private List<ItemPedido> itens;
    private List<ItemPedido> itensParaRemover;
    private ItemPedidoAdapter itemPedidoAdapter;
    private String title = "Pedidos";
    private boolean editable;
    private FormaPagamento formaPagamento;

    public static PedidoFragment newInstance(@NonNull Pedido pedido) {
        PedidoFragment fragment = new PedidoFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.Key.PEDIDO, pedido);
        fragment.setArguments(args);
        return fragment;
    }

    public static PedidoFragment newInstance(@NonNull Pedido pedido,
                                             @NonNull FragmentListener callback) {
        PedidoFragment fragment = new PedidoFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.Key.PEDIDO, pedido);
        args.putSerializable(Constants.Key.FRAGMENT_LISTENER, callback);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pedido = (Pedido) getArguments().getSerializable(Constants.Key.PEDIDO);
            callback = (FragmentListener) getArguments().
                    getSerializable(Constants.Key.FRAGMENT_LISTENER);
        }

        pedido = (pedido==null)?new Pedido():pedido;
        itens = new ArrayList<>();
        if (pedido.getId()==null) {
            pedido.setDataInclusao(new Date());
        }
        editable = pedido.getNumeroUnico()==0;
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
        salvarMenuItem.setVisible(editable);
        MenuItem removerMenuItem = menu.findItem(R.id.action_remover);
        removerMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                excluir();
                return false;
            }
        });
        removerMenuItem.setVisible(pedido!=null && pedido.getId()!=null && editable);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        PedidoFragmentBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.pedido_fragment, container, false);
        binding.setPedido(pedido);
        View view = binding.getRoot();

        setTitle("Pedido");

        initCampos(view);

        initTipoOperacao();

        if (pedido.isNew()) {
            initEmpresaLocalEstoque();
        } else {
            complementaDadosPedido(pedido);
        }

        configuraCampos();
        if (pedido.getNumeroUnico()>0) {
            coloreCampos();
        }

        itensParaRemover = new ArrayList<>();

        return view;
    }

    FragmentListener fragmentListener = new FragmentListener() {
        @Override
        public void onFragmentEdit() {
        }
        @Override
        public void onFragmentFinish() {
            configuraMenu(true);
        }
    };

    /**
     * Recupera os dados da view e inicializa-os na classe.
     */
    private void initCampos(View view) {

        pedidoData = (MaskEditText) view.findViewById(R.id.pedidoData);
        clienteNome = (MaskEditText) view.findViewById(R.id.pedidoClienteNome);
        empresaNome = (MaskEditText) view.findViewById(R.id.pedidoEmpresaNome);
        spnTipoOperacao = (MaterialBetterSpinner) view.findViewById(R.id.pedidoTop);
        localEstoqueNome = (MaskEditText) view.findViewById(R.id.pedidoLocalEstoque);
        frmPgDescricao = (MaskEditText) view.findViewById(R.id.pedidoFrmPgDescricao);
        produtos = (MaskEditText) view.findViewById(R.id.selecionarProdutos);
        observacao = (MaskEditText) view.findViewById(R.id.pedidoObservacao);
        totalPedido = (MaskEditText) view.findViewById(R.id.totalItemPedido);
        recyclerView = (RecyclerView) view.findViewById(R.id.itemPedidoListRecyclerView);
        pedidoData.setText(Util.dateToString(Constants.DateFormat.DATA, pedido.getDataInclusao()));
        itemPedidoAdapter = new ItemPedidoAdapter(this);
        itemPedidoAdapter.setItemPedidos(itens);
        recyclerView.setAdapter(itemPedidoAdapter);
        recyclerView.setNestedScrollingEnabled(false);

        view.findViewById(R.id.pedidoEmpresaNome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selecinarEmpresa();
            }
        });
        view.findViewById(R.id.pedidoClienteNome).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selecinarCliente();
            }
        });

        view.findViewById(R.id.pedidoLocalEstoque).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selecinarLocalEstoque();
            }
        });
        view.findViewById(R.id.pedidoFrmPgDescricao).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selecinarFormaPagamento();
            }
        });
        view.findViewById(R.id.selecionarProdutos).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selecionarProdutos();
            }
        });
    }

    private void initTipoOperacao() {
        DaoSession daoSession = getDaoSession();
        TipoOperacaoDao dao = daoSession.getTipoOperacaoDao();
        List<TipoOperacao> operacoes = dao.loadAll();
        TipoOperacaoAdapter arrayAdapter = new TipoOperacaoAdapter(
                this.getContext(), R.layout.tipo_operacao_fragment_row,
                operacoes.toArray(new TipoOperacao[]{}), this);
        spnTipoOperacao.setAdapter(arrayAdapter);
    }

    private void initEmpresaLocalEstoque() {
        DaoSession daoSession = getDaoSession();
        Perfil perfil = getPerfil();

        EmpresaDao empresaDao = daoSession.getEmpresaDao();
        Empresa empresa = empresaDao.load(perfil.getIdEmpresa());

        LocalEstoqueDao localDao = daoSession.getLocalEstoqueDao();
        LocalEstoque local = localDao.load(perfil.getIdLocalEstoque());
        onEmpresaSelected(empresa);
        onLocalEstoqueSelected(local);
    }

    public void selecinarEmpresa(){
        if (pedido.getNumeroUnico()>0) return;
        ((PrincipalActivity)getActivity()).getTitles().push(title);
        EmpresaFragmentList empresaFragmentList = EmpresaFragmentList.newInstance(this, fragmentListener);
        addFragment(empresaFragmentList);
    }

    @Override
    public void onEmpresaSelected(Empresa empresa) {
        empresaNome.setText(empresa.getNomeFantasia());
        pedido.setIdEmpresa(empresa.getId());
        if (pedido.getIdEmpresa()!=134 && pedido.getIdLocalEstoque()!=1000){
            DaoSession daoSession = ((VendaMaisApplication)getActivity().getApplication()).getDaoSession();
            LocalEstoqueDao dao = daoSession.getLocalEstoqueDao();
            LocalEstoque local = dao.load(1000L);
            onLocalEstoqueSelected(local);
        }
        atualizaEstoque();
    }

    public void selecinarCliente(){
        if (pedido.getNumeroUnico()>0) return;
        ((PrincipalActivity)getActivity()).getTitles().push(title);
        ClienteFragmentList clienteFragmentList =
                ClienteFragmentList.newInstance(Constants.Modo.SELECAO, this, fragmentListener);
        addFragment(clienteFragmentList);
    }

    @Override
    public void onTipoOperacaoClick(TipoOperacao tipo) {
        pedido.setIdTipoOperacao(tipo.getId());
        spnTipoOperacao.setText(tipo.toString(), false);
        spnTipoOperacao.dismissDropDown();
    }

    @Override
    public void onClienteSelected(Cliente cliente) {
        if (cliente.getTipo() == Constants.TipoCliente.PESSOA_FISICA) {
            clienteNome.setFloatingLabelText(getResources().getString(R.string.nome));
        }else{
            clienteNome.setFloatingLabelText(getResources().getString(R.string.razao_social));
        }
        clienteNome.setText(cliente.getNomeRazaoSocial());
        pedido.setIdCliente(cliente.getId());
    }

    public void selecinarLocalEstoque(){
        if (pedido.getNumeroUnico()>0) return;
        if (pedido.getIdEmpresa()==134) {
            ((PrincipalActivity)getActivity()).getTitles().push(title);
            LocalEstoqueFragmentList fragmentList =
                    LocalEstoqueFragmentList.newInstance(this, fragmentListener);
            addFragment(fragmentList);
        }else {
            StringBuilder msg = new StringBuilder();
            msg.append("A empresa ");
            msg.append(empresaNome.getText().toString());
            msg.append(" deve utiizar o local ");
            msg.append(localEstoqueNome.getText().toString());
            Toast.makeText(this.getContext(), msg.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onLocalEstoqueSelected(LocalEstoque localEstoque) {
        localEstoqueNome.setText(localEstoque.getDescricao());
        pedido.setIdLocalEstoque(localEstoque.getId());
        atualizaEstoque();
    }

    public void selecinarFormaPagamento(){
        if (pedido.getNumeroUnico()>0) return;
        ((PrincipalActivity)getActivity()).getTitles().push(title);
        FormaPagamentoFragmentList fragment = FormaPagamentoFragmentList.newInstance(this, fragmentListener);
        addFragment(fragment);
    }

    @Override
    public void onFormaPagamentoSelected(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
        frmPgDescricao.setText(formaPagamento.getDescricao());
        pedido.setIdFormaPagamento(formaPagamento.getId());
        if (!itens.isEmpty()) {
            aplicaNovaTaxa(formaPagamento.getTaxa());
        }
    }

    public void selecionarProdutos(){
        if (pedido.getNumeroUnico()>0) return;
        StringBuilder msg = new StringBuilder();
        boolean virgula = false;
        if (pedido.getIdEmpresa()==null) {
            msg.append("Empresa");
            virgula=true;
        }
        if (pedido.getIdLocalEstoque()==null) {
            if(virgula) msg.append(", ");
            msg.append("Local de Estoque");
            virgula=true;
        }
        if (pedido.getIdFormaPagamento()==null) {
            if(virgula) msg.append(", ");
            msg.append("Forma de Pagamento");
        }
        if (Util.isNotBlank(msg.toString())) {
            produtos.setError("Selecione: "+msg.toString()+" primeiro.");
            return;
        }

        List<ItemPedido> removerItens = new ArrayList<>();
        for (ItemPedido item : itens) {
            if (Integer.valueOf(0).equals(item.getQuantidade())) {
                removerItens.add(item);
            }
        }
        itens.removeAll(removerItens);
        ((PrincipalActivity)getActivity()).getTitles().push(title);
        getDaoSession().clear();
        ProdutoFragmentList fragmentoList = ProdutoFragmentList.newInstance(
                Constants.Modo.SELECAO, this, fragmentListener,itens, pedido.getIdEmpresa(),
                pedido.getIdLocalEstoque(),formaPagamento.getTaxa());
        addFragment(fragmentoList);
    }

    @Override
    public void onProdutoSelected(List<ItemPedido> itens) {
        if (!itens.isEmpty()) {
            produtos.setError(null);
        }
        itemPedidoAdapter.setItemPedidos(itens);
        calcularValorTotal();
    }

    @Override
    public void onProdutoRemoved(ItemPedido itemPedido) {
        if (itemPedido.getId()!=null) {
            itensParaRemover.add(itemPedido);
        }
    }

    @Override
    public void onItemPedidoRemoved(ItemPedido itemPedido) {
        if (itemPedido.getId()!=null) {
            itensParaRemover.add(itemPedido);
        }
    }

    @Override
    public void onItemPedidoClick(ItemPedido itemPedido) {

    }

    @Override
    public void onItemPedidoChanged(ItemPedido itemPedido) {
        calcularValorTotal();
    }

    /**
     * Aplica a nova taxa nos itens de pedidos já selecionados.
     * @param taxa
     */
    public void aplicaNovaTaxa(Double taxa) {
        for (ItemPedido item: itens) {
            item.setTaxa(taxa);
        }
        itemPedidoAdapter.setItemPedidos(itens);
        calcularValorTotal();
    }

    /**
     * Soma o valor de todos os itens do pedido
     */
    public void calcularValorTotal(){
        Double total = 0.0;
        for (ItemPedido item: itens) {
            total += item.getValorTotal();
        }
        pedido.setValorTotal(total);
        totalPedido.setText("Total do Pedido: "+Util.doubleToValorMonetaria(total));
    }

    /**
     * Excluir o cliente que está sem editado do banco.
     */
    public void excluir(){
        if (pedido==null || pedido.getId()==null) {
            StringBuilder msg = new StringBuilder("Pedido não existe no aplicativo.");
            Toast.makeText(this.getContext(), msg.toString(), Toast.LENGTH_LONG).show();
            return;
        }
        DaoSession daoSession = ((VendaMaisApplication) getActivity().getApplication()).getDaoSession();
        PedidoDao pedidoDao = daoSession.getPedidoDao();
        pedidoDao.delete(pedido);

        ItemPedidoDao itemDao = daoSession.getItemPedidoDao();
        itemDao.deleteInTx(itens);

        getActivity().onBackPressed();
    }


    /**
     * Os dados dos campos já forma setados diretamente no objeto devido ao data binding,
     * assim, o método recupera o objeto com os dados preenchidos da tela e salva no banco.
     */
    public void salvar(){
        if (pedido==null) return;

        DaoSession daoSession = ((VendaMaisApplication)getActivity().getApplication()).getDaoSession();
        PedidoDao pedidoDao = daoSession.getPedidoDao();
        if (validaCampos(pedidoDao)) {
            daoSession.clear();

            pedido.setObservacao(observacao.getText().toString());
            pedido.setQuantidadeItens(itens.size());
            pedido.configuraDados();
            pedidoDao.save(pedido);
            for (ItemPedido item: itens) {
                item.setIdPedido(pedido.getId());
            }
            ItemPedidoDao itemPedidoDao = daoSession.getItemPedidoDao();
            itemPedidoDao.saveInTx(itens);
            if (!itensParaRemover.isEmpty()) {
                itemPedidoDao.deleteInTx(itensParaRemover);
                itensParaRemover = new ArrayList<>();
            }

            Util.hideKeyboard(getActivity());
            getActivity().onBackPressed();
            Toast.makeText(this.getContext(), R.string.pedido_salvo, Toast.LENGTH_LONG).show();
        }
    }


    /**
     * Recuperar os dados das entidades relacionadas ao pedido.
     *
     * @param pedido Registro que o usuário seleciou na tela anterior
     */
    private void complementaDadosPedido(Pedido pedido){

        DaoSession daoSession = ((VendaMaisApplication)getActivity().getApplication()).getDaoSession();

        if (pedido.getIdCliente()!=null) {
            ClienteDao clienteDao = daoSession.getClienteDao();
            Cliente cliente = clienteDao.load(pedido.getIdCliente());
            clienteNome.setText(cliente.getNomeRazaoSocial());
        }

        if (pedido.getId()==null) return;

        EmpresaDao empresaDao = daoSession.getEmpresaDao();
        Empresa empresa = empresaDao.load(pedido.getIdEmpresa());
        empresaNome.setText(empresa.getNomeFantasia());

        LocalEstoqueDao localEstoqueDao = daoSession.getLocalEstoqueDao();
        LocalEstoque localEstoque = localEstoqueDao.load(pedido.getIdLocalEstoque());
        localEstoqueNome.setText(localEstoque.getDescricao());

        TipoOperacaoDao tipoOperacaoDao = daoSession.getTipoOperacaoDao();
        TipoOperacao tipoOperacao = tipoOperacaoDao.load(pedido.getIdTipoOperacao());
        spnTipoOperacao.setText(tipoOperacao.toString(), false);

        FormaPagamentoDao formaPagamentoDao = daoSession.getFormaPagamentoDao();
        formaPagamento = formaPagamentoDao.load(pedido.getIdFormaPagamento());
        frmPgDescricao.setText(formaPagamento.getDescricao());

        ItemPedidoDao itemPedidodDao = daoSession.getItemPedidoDao();
        QueryBuilder<ItemPedido> queryBuilderItem = itemPedidodDao.queryBuilder();
        queryBuilderItem.where(ItemPedidoDao.Properties.IdPedido.eq(pedido.getId()));
        Query<ItemPedido> queryPedido = queryBuilderItem.build();
        List<ItemPedido> list = queryPedido.list();

        ProdutoDao produtoDao = daoSession.getProdutoDao();
        for (ItemPedido item: list) {
            Produto produto = produtoDao.load(item.getIdProduto());
            item.setProduto(produto);
        }

        itens = list;
        itemPedidoAdapter.setItemPedidos(itens);
        observacao.setText(pedido.getObservacao());
        calcularValorTotal();
    }

    private void atualizaEstoque() {
        if (pedido.getIdEmpresa()!=null && pedido.getIdLocalEstoque()!=null) {
            String idEmpresa = String.valueOf(pedido.getIdEmpresa());
            String idLocal = String.valueOf(pedido.getIdLocalEstoque());
            DaoSession daoSession = ((VendaMaisApplication)getActivity().getApplication()).getDaoSession();
            estoqueSyncTask = new SyncTaskBackgroundEstoque(getPerfil(), daoSession);
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

    /**
     *
     *
     * @param pedidoDao Dao para acessar o banco de ados.
     * @return True pedido validado false caso contrário.
     */
    private boolean validaCampos(PedidoDao pedidoDao ) {

        boolean valido = true;
        if (pedido.getIdCliente()==null){
            clienteNome.setError("Cliente é obrigatório.");
            valido = false;
        }
        if (itens.isEmpty()){
            produtos.setError("Adicione pelo menos um item.");
            valido = false;
        }
        if (pedido.getIdFormaPagamento()==null){
            frmPgDescricao.setError("Forma de pagamento é obrigatória.");
            valido = false;
        }
        if (pedido.getIdEmpresa()==null){
            empresaNome.setError("Empresa é obrigatório.");
            valido = false;
        }
        if (pedido.getIdLocalEstoque()==null){
            localEstoqueNome.setError("Local de estoque é obrigatório.");
            valido = false;
        }
        if (pedido.getIdTipoOperacao()==null){
            spnTipoOperacao.setError("Tipo de operação é obrigatório.");
            valido = false;
        }

        return valido;
    }

    private void configuraCampos() {
        pedidoData.setFocusable(editable);
        pedidoData.setEnabled(editable);
        clienteNome.setFocusable(editable);
        clienteNome.setEnabled(editable);
        empresaNome.setFocusable(editable);
        empresaNome.setEnabled(editable);
        localEstoqueNome.setFocusable(editable);
        localEstoqueNome.setEnabled(editable);
        frmPgDescricao.setFocusable(editable);
        frmPgDescricao.setEnabled(editable);
        observacao.setFocusable(editable);
        observacao.setEnabled(editable);
        totalPedido.setFocusable(editable);
        totalPedido.setEnabled(editable);
        spnTipoOperacao.setFocusable(editable);
        spnTipoOperacao.setEnabled(editable);
    }

    private void coloreCampos() {
        tintField(pedidoData);
        tintField(clienteNome);
        tintField(empresaNome);
        tintField(localEstoqueNome);
        tintField(frmPgDescricao);
        tintField(observacao);
        tintField(totalPedido);
        tintField(spnTipoOperacao);
    }

}