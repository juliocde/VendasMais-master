package br.com.dsx.vendamais.fragment;

import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import org.greenrobot.greendao.query.QueryBuilder;
import org.json.JSONArray;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.VendaMaisApplication;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.JSONUtil;
import br.com.dsx.vendamais.common.SankhyaUtil;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.ClienteDao;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.dao.PedidoDao;
import br.com.dsx.vendamais.databinding.ClienteFragmentBinding;
import br.com.dsx.vendamais.domain.Cliente;
import br.com.dsx.vendamais.domain.Erro;
import br.com.dsx.vendamais.domain.Pedido;
import br.com.dsx.vendamais.sync.SWServiceInvoker;
import butterknife.BindView;
import butterknife.ButterKnife;


public class ClienteFragment extends BaseFragment {

    private static String LOGRADOURO_BAIRRO = "LOGRADOURO_BAIRRO";
    private static String TEM_VARIOS = "TEM_VARIOS";
    private static String NAO_TEM = "NAO_TEM";
    private ClienteFragmentBinding binding;
    private ClienteFragmentList.ClienteFragmentListListener listener;

    @BindView(R.id.edit_codigo) MaterialEditText editCodigo;
    @BindView(R.id.edit_inscricao_estadual) MaterialEditText editInscricaoEstadual;
    @BindView(R.id.edit_nome_razao_social) MaterialEditText editNomeRazaoSocial;
    @BindView(R.id.edit_nome_fantasia) MaterialEditText editNomeFantasia;
    @BindView(R.id.edit_cpf) MaterialEditText editCpf;
    @BindView(R.id.edit_cnpj) MaterialEditText editCnpj;
    @BindView(R.id.edit_celular) MaterialEditText editCelular;
    @BindView(R.id.edit_email) MaterialEditText editEmail;

    @BindView(R.id.edit_cep)  MaterialEditText editCep;
    @BindView(R.id.cliente_endereco_progress_bar)  ProgressBar progressBarCep;
    @BindView(R.id.edit_logradouro)  MaterialEditText editLogradouro;
    @BindView(R.id.edit_numero)  MaterialEditText editNumero;
    @BindView(R.id.edit_complemento)  MaterialEditText editComplemento;
    @BindView(R.id.edit_bairro)  MaterialEditText editBairro;
    @BindView(R.id.edit_cidade)  MaterialEditText editCidade;
    @BindView(R.id.edit_uf)  MaterialEditText editUf;
    @BindView(R.id.spinner_tipo) MaterialBetterSpinner spinnerTipo;
    @BindView(R.id.cliente_endereco_cep_buscar) ImageView imageViewCep;

    private MaterialBetterSpinner txtTipoLogradouro;

    private String[] tipoPessoaItens = new String[]{"PESSOA JURÍDICA","PESSOA FÍSICA"};
    private SWServiceInvoker sankhyaService;
    private Cliente cliente;
    private boolean editable;

    public static ClienteFragment newInstance(@NonNull Cliente cliente) {
        ClienteFragment fragment = new ClienteFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.Key.CLIENTE, cliente);
        fragment.setArguments(args);
        return fragment;
    }

    public static ClienteFragment newInstance(
            @NonNull Cliente cliente,
            @NonNull FragmentListener callback) {
        ClienteFragment fragment = new ClienteFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.Key.CLIENTE, cliente);
        args.putSerializable(Constants.Key.FRAGMENT_LISTENER, callback);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            cliente = (Cliente) getArguments().getSerializable(Constants.Key.CLIENTE);
            callback = (FragmentListener) getArguments().
                    getSerializable(Constants.Key.FRAGMENT_LISTENER);
        }
        String url  = getResources().getString(R.string.urlDSX);
        String user = getResources().getString(R.string.usuarioDSX);
        String pass = getResources().getString(R.string.senhaDSX);
        sankhyaService = new SWServiceInvoker(url, user, pass);

        cliente = (cliente==null)?new Cliente():cliente;
        editable = cliente.isEditable();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(
                inflater, R.layout.cliente_fragment, container, false);

        binding.setCliente(cliente);
        View view = binding.getRoot();

        unbinder = ButterKnife.bind(this, view);

        inicializaCampos(view);

        coloreCampos();

        configuraTipoPessoa(view);

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setTitle("Cliente");
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
        removerMenuItem.setVisible(cliente!=null && cliente.getId()!=null && editable);
    }


    /**
     * Associa os campos da view com as variáveis de instância da classe.
     *
     * @param view Tela.
     */
    void inicializaCampos(View view){
        txtTipoLogradouro = (MaterialBetterSpinner)view.findViewById(R.id.txtTipoLogradouro);

        if (cliente.getId()==null) {
            editCodigo.setVisibility(View.GONE);
        }else {
            editCodigo.setText(cliente.getId().toString());
            editCodigo.setVisibility(View.VISIBLE);
            editCodigo.setEnabled(editable);
        }

        editNomeRazaoSocial.setEnabled(editable);
        if (editable) {
            editNomeRazaoSocial.requestFocus();
        }else{
            editNomeRazaoSocial.setFloatingLabel(MaterialEditText.FLOATING_LABEL_NORMAL);
        }
        editNomeFantasia.setEnabled(editable);
        editInscricaoEstadual.setEnabled(editable);
        spinnerTipo.setEnabled(editable);
        editCpf.setEnabled(editable);
        editCnpj.setEnabled(editable);
        editCelular.setEnabled(editable);
        editEmail.setEnabled(editable);

        editCep.setEnabled(editable);
        imageViewCep.setEnabled(editable);
        imageViewCep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buscaEndereco();
            }
        });
        editCep.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    buscaEndereco();
                    return true;
                }
                return false;
            }
        });
        editNumero.setEnabled(editable);
        editComplemento.setEnabled(editable);

        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(view.getContext(),
                        android.R.layout.simple_list_item_1, tipoPessoaItens);
        spinnerTipo.setAdapter(itemsAdapter);
        spinnerTipo.setEnabled(editable);
        spinnerTipo.setFocusable(editable);
        spinnerTipo.setClickable(editable);

        String[] tipos = getResources().getStringArray(R.array.tipo_logradouro);
        ArrayAdapter<String> tiposAdapter =
                new ArrayAdapter<String>(view.getContext(),
                        android.R.layout.simple_list_item_1, tipos);
        txtTipoLogradouro.setAdapter(tiposAdapter);

        verificaCamposEndereco();
    }

    /**
     *
     */
    private void verificaCamposEndereco() {
        txtTipoLogradouro.setEnabled(false);
        txtTipoLogradouro.setFocusable(false);
        txtTipoLogradouro.setClickable(false);
        editLogradouro.setEnabled(false);
        editBairro.setEnabled(false);
        if (editable && !cliente.isLogradouroAutocomplete()) {
            txtTipoLogradouro.setEnabled(true);
            txtTipoLogradouro.setFocusable(true);
            txtTipoLogradouro.setClickable(true);
            editLogradouro.setEnabled(true);
            editBairro.setEnabled(true);
        }
    }


    private void coloreCampos() {
        tintField(editCodigo);
        tintField(editNomeRazaoSocial);
        tintField(editNomeFantasia);
        tintField(editCpf);
        tintField(editCnpj);
        tintField(editEmail);
        tintField(editCelular);
        tintField(editCep);
        tintField(editNumero);
        tintField(editComplemento);
        tintField(editLogradouro);
        tintField(editBairro);
        tintField(editCidade);
        tintField(editUf);
        tintField(spinnerTipo);
        tintField(txtTipoLogradouro);
    }

    /**
     *
     */
    void buscaEndereco() {
        editLogradouro.setText(Util.STRING_EMPTY);
        editBairro.setText(Util.STRING_EMPTY);
        editCidade.setText(Util.STRING_EMPTY);
        editUf.setText(Util.STRING_EMPTY);
        txtTipoLogradouro.setText(Util.STRING_EMPTY);
        String cep = cliente.getCep();
        new ClienteAsyncTask(new ClienteAsyncTaskResponse() {
            @Override
            public void processFinish(Map<String, String> values) {
                imageViewCep.setVisibility(View.VISIBLE);
                progressBarCep.setVisibility(View.GONE);
                editCep.setEnabled(true);
                if (Util.isNotBlank(values.get("ERRO"))) {
                    editCep.setError(values.get("ERRO"));
                    editCep.setFocusable(true);
                    editCep.requestFocus();
                    return;
                } else if (Util.isBlank(values.get("uf"))) {
                    editCep.setError("CEP não encontrado.");
                    editCep.setFocusable(true);
                    editCep.requestFocus();
                    return;
                }
                String logradouro = values.get("logradouro");
                String tipoLogradouro = values.get("tipologradouro");

                txtTipoLogradouro.setText(tipoLogradouro,false);
                editLogradouro.setText(logradouro);
                editBairro.setText(values.get("bairro"));
                editCidade.setText(values.get("cidade"));
                editUf.setText(values.get("uf"));

                cliente.setTipoLogradouro(tipoLogradouro);
                cliente.setCodUf( Integer.parseInt( values.get("coduf") ) );
                cliente.setLogradouroAutocomplete(true);

                if (Util.isBlank(logradouro)) {
                    editComplemento.setImeOptions(EditorInfo.IME_ACTION_NEXT);
                    editBairro.setImeOptions(EditorInfo.IME_ACTION_DONE);
                    editLogradouro.setEnabled(true);
                    editBairro.setEnabled(true);
                    txtTipoLogradouro.setEnabled(true);
                    cliente.setLogradouroAutocomplete(false);
                } else {
                    editComplemento.setImeOptions(EditorInfo.IME_ACTION_DONE);
                }
                editNumero.setFocusable(true);
                editNumero.requestFocus();
                editNumero.setEnabled(true);

                verificaCamposEndereco();

                coloreCampos();

            }
        }).execute(cep);
        editCep.setEnabled(false);
        imageViewCep.setVisibility(View.GONE);
        progressBarCep.setVisibility(View.VISIBLE);
        Util.hideKeyboard(getActivity());
    }

    /**
     * Configura os campos que devem aparecer e sumir de acordo com o tipo da pessoa.
     *
     * @param view
     */
    void configuraTipoPessoa(View view){

        int tipoPessoa = cliente.getTipo();
        spinnerTipo.setText(tipoPessoaItens[tipoPessoa],false);
        if (tipoPessoa == Constants.TipoCliente.PESSOA_FISICA) {
            editCnpj.setVisibility(View.GONE);
            editCpf.setVisibility(View.VISIBLE);
            editNomeRazaoSocial.setFloatingLabelText("Nome:");
            editNomeRazaoSocial.setHint("Nome");
            editNomeFantasia.setText(Util.STRING_EMPTY);
            editNomeFantasia.setVisibility(View.GONE);
            editInscricaoEstadual.setText(Util.STRING_EMPTY);
            editInscricaoEstadual.setVisibility(View.GONE);
        }

        if (spinnerTipo.isEnabled()) {
            spinnerTipo.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (count == 15) {
                        cliente.setTipo(0);
                        editCnpj.setVisibility(View.VISIBLE);
                        editCpf.setVisibility(View.GONE);
                        editNomeRazaoSocial.setFloatingLabelText("Razão Social:");
                        editNomeRazaoSocial.setHint("Razão Social");
                        if (editNomeRazaoSocial.getError() != null) {
                            editNomeRazaoSocial.setError("Razão social é obrigatória");
                        }
                        editNomeFantasia.setVisibility(View.VISIBLE);
                        editInscricaoEstadual.setVisibility(View.VISIBLE);
                    } else {
                        cliente.setTipo(1);
                        editCnpj.setVisibility(View.GONE);
                        editCpf.setVisibility(View.VISIBLE);
                        editNomeRazaoSocial.setFloatingLabelText("Nome:");
                        editNomeRazaoSocial.setHint("Nome");
                        if (editNomeRazaoSocial.getError() != null) {
                            editNomeRazaoSocial.setError("Nome é obrigatório");
                        }
                        editNomeFantasia.setText(Util.STRING_EMPTY);
                        editNomeFantasia.setVisibility(View.GONE);
                        editInscricaoEstadual.setText(Util.STRING_EMPTY);
                        editInscricaoEstadual.setVisibility(View.GONE);
                    }
                    editNomeRazaoSocial.requestFocusFromTouch();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        } else {
            spinnerTipo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

        String tipoLogradouro = cliente.getTipoLogradouro();
        if (Util.isNotBlank(tipoLogradouro)){
            tipoLogradouro = tipoLogradouro.toUpperCase();
        }
        txtTipoLogradouro.setText(tipoLogradouro,false);

        if (txtTipoLogradouro.isEnabled()) {
            txtTipoLogradouro.addTextChangedListener(new TextWatcher() {

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    cliente.setTipoLogradouro(s.toString());
                }
            });
        }else {
            txtTipoLogradouro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }


    /**
     * Excluir o cliente que está sem editado do banco.
     */
    public void excluir(){
        if (cliente==null || cliente.getId()==null) {
            StringBuilder msg = new StringBuilder("Cliente não existe no aplicativo.");
            Toast.makeText(this.getContext(), msg.toString(), Toast.LENGTH_LONG).show();
            return;
        }
        DaoSession daoSession = ((VendaMaisApplication) getActivity().getApplication()).getDaoSession();
        PedidoDao pedidoDao = daoSession.getPedidoDao();
        QueryBuilder<Pedido> qb = pedidoDao.queryBuilder();
        qb.where(PedidoDao.Properties.IdCliente.eq(cliente.getId()));
        List<Pedido> list = qb.list();
        if (!list.isEmpty()) {
            StringBuilder msg = new StringBuilder("Existem pedidos (");
            boolean first = true;
            for (Pedido pedido: list) {
                if (!first) msg.append(", ");
                msg.append(Util.dateToString(Constants.DateFormat.DATA,pedido.getDataInclusao()));
                first = false;
            }
            msg.append(")associados a este cliente, remova-os primeiro.");
            Toast.makeText(this.getContext(), msg.toString(), Toast.LENGTH_LONG).show();
        }else {
            ClienteDao clienteDao = daoSession.getClienteDao();
            clienteDao.delete(cliente);
            getActivity().onBackPressed();
        }
    }


    /**
     * Os dados dos campos já forma setados diretamente no objeto devido ao data binding,
     * assim, o método recupera o objeto com os dados preenchidos da tela e salva no banco.
     */
    public void salvar(){
        if (cliente==null) return;

        Util.hideKeyboard(getActivity());

        DaoSession daoSession = ((VendaMaisApplication)getActivity().getApplication()).getDaoSession();
        ClienteDao clienteDao = daoSession.getClienteDao();

        if (validaCampos(clienteDao)) {
            daoSession.clear();
            String nome = cliente.getNomeRazaoSocial();
            cliente.setNomeRazaoSocial((nome!=null)?nome.toUpperCase():nome);
            cliente.setTipoLogradouro(txtTipoLogradouro.getText().toString());

            clienteDao.save(cliente);
            getActivity().onBackPressed();
            Toast.makeText(this.getContext(), R.string.cliente_salvo, Toast.LENGTH_LONG).show();
        }

    }


    /**
     * Por enquanto os dados estão sendo gravados no banco de dados com mascára.
     * Estou olhando com o cara que dev a biblioteca para arrumar. Se ele não mexer
     * mudo depois.
     *
     * @param clienteDao Dao para acessar o banco de ados.
     * @return True cliente validado false caso contrário.
     */
    private boolean validaCampos(ClienteDao clienteDao ) {

        boolean valido = true;
        if (Util.isBlank(cliente.getNomeRazaoSocial())){
            editNomeRazaoSocial.setError(cliente.getTipo()==0?
                    "Razão social é obrigatória":"Nome é obrigatório");
            valido = false;
        }
        if (Util.isBlank(cliente.getCpfCnpj())){
            if (cliente.getTipo()==0) {
                editCnpj.setError("CNPJ social é obrigatório");
            }else {
                editCpf.setError("CPF é obrigatório");
            }
            valido = false;
        } else if ((cliente.getTipo()==0 && cliente.getCpfCnpj().length()<13)||
                (cliente.getTipo()==1 && cliente.getCpfCnpj().length()<11)) {
            if (cliente.getTipo()==0) {
                editCnpj.setError("CNPJ inválido");
            }else {
                editCpf.setError("CPF inválido");
            }
            valido = false;
        } else {
            QueryBuilder<Cliente> queryBuilder = clienteDao.queryBuilder();
            queryBuilder.where(ClienteDao.Properties.CpfCnpj.eq(cliente.getCpfCnpj()));
            List<Cliente> clientes = queryBuilder.list();
            if (!clientes.isEmpty() && (cliente.getId()==null ||
                    !clientes.get(0).getId().equals(cliente.getId()))) {
                if (cliente.getTipo()==0) {
                    editCnpj.setError("CNPJ já cadastrado");
                }else {
                    editCpf.setError("CPF já cadastrado");
                }
                valido = false;
            }
        }

        if (Util.isNotBlank(cliente.getCep()) && cliente.getCep().length()<8){
            editCep.setError("CEP inválido");
            valido = false;
        }

        if (Util.isNotBlank(cliente.getCelular()) && cliente.getCelular().length()<10){
            editCelular.setError("Telefone inválido");
            valido = false;
        }

        if (Util.isBlank(cliente.getCep())){
            editCep.setError("CEP é obrigatório");
            valido = false;
        }

        if (Util.isNotBlank(cliente.getEmail()) && !Util.isValidEmail(editEmail.getText())) {
            editEmail.setError("Email inválido");
            valido = false;
        }

        return valido;

    }




    public class ClienteAsyncTask extends AsyncTask<String, Void, Map<String,String>> {

        private ClienteAsyncTaskResponse delegate;

        public ClienteAsyncTask(ClienteAsyncTaskResponse delegate){
            this.delegate = delegate;
        }

        @Override
        protected Map<String, String> doInBackground(String... params) {

            Map<String, String> values = new HashMap<>();
            String tipoLogradouro = Util.STRING_EMPTY;
            String logradouro = Util.STRING_EMPTY;
            String bairro = Util.STRING_EMPTY;
            String cidade = Util.STRING_EMPTY;
            String uf = Util.STRING_EMPTY;
            String cep = params[0];
            String codUf = Util.STRING_EMPTY;

            StringBuilder sql = new StringBuilder()
                    .append(" SELECT ")
                    .append("     TIPO,") //0
                    .append("     NOMEEND,") //1
                    .append("     BAIRRO,") //3
                    .append("     CIDADE,") //3
                    .append("     ESTADO,") //4
                    .append("     CODUF ") //5
                    .append(" FROM AD_CEPEND")
                    .append(" WHERE CEP = ").append(cep);
            try {
                JSONArray resultado = SankhyaUtil.execSelect(sankhyaService, sql.toString());

                //TODO: o que fazer com o resultado vir NULL
                if (resultado!=null && resultado.length()>1) {
                    JSONArray linha = resultado.getJSONArray(0);
                    cidade = JSONUtil.getString(linha, 3);
                    uf = JSONUtil.getString(linha, 4);
                    codUf = JSONUtil.getString(linha, 5);
                } else if (resultado!=null && resultado.length()==1) {
                    JSONArray linha = resultado.getJSONArray(0);
                    tipoLogradouro = JSONUtil.getString(linha, 0);
                    logradouro = JSONUtil.getString(linha, 1);
                    bairro = JSONUtil.getString(linha, 2);
                    cidade = JSONUtil.getString(linha, 3);
                    uf = JSONUtil.getString(linha, 4);
                    codUf = JSONUtil.getString(linha, 5);
                }
            } catch (UnknownHostException net) {
                values.put("ERRO", "Verifique sua conexão com a internet.");
                return values;
            } catch (java.net.ConnectException e) {
                values.put("ERRO", "Verifique sua conexão com a internet.");
                return values;
            } catch (Exception e) {
                e.printStackTrace();
                getDaoSession().getErroDao().insert(new Erro(e.toString()));
            }
            values.put("tipologradouro",tipoLogradouro.trim());
            values.put("logradouro",logradouro.trim());
            values.put("bairro",bairro);
            values.put("cidade",cidade);
            values.put("uf",uf);
            values.put("coduf",codUf);
            return values;
        }

        @Override
        protected void onPostExecute(Map<String, String> values) {
            delegate.processFinish(values);
        }
    }

    public interface ClienteAsyncTaskResponse {
        void processFinish(Map<String, String> values);
    }

}

