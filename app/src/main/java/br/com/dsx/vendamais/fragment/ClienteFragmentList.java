package br.com.dsx.vendamais.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import java.io.Serializable;
import java.util.List;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.adapter.ClienteAdapter;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.ClienteDao;
import br.com.dsx.vendamais.domain.Cliente;

public class ClienteFragmentList extends BaseFragment {

    private transient TextView semCliente;
    private transient MenuItem searchMenuItem;

    private ClienteAdapter clienteAdapter;
    private static ClienteSelectedListener listener;
    private String modo = Constants.Modo.EDICAO;

    public static ClienteFragmentList newInstance(
            @NonNull String modo,
            @NonNull ClienteSelectedListener listener,
            @NonNull FragmentListener callback) {
        ClienteFragmentList fragment = new ClienteFragmentList();
        Bundle args = new Bundle();
        args.putSerializable(Constants.Key.MODO, modo);
        args.putSerializable(Constants.Key.CLIENTE_LISTENER, listener);
        args.putSerializable(Constants.Key.FRAGMENT_LISTENER, callback);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            modo = getArguments().getString(Constants.Key.MODO);
            listener = (ClienteSelectedListener)getArguments()
                    .getSerializable(Constants.Key.CLIENTE_LISTENER);
            callback = (FragmentListener) getArguments().
                    getSerializable(Constants.Key.FRAGMENT_LISTENER);
        }
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.cliente_fragment_list, container, false);
        semCliente = (TextView) view.findViewById(R.id.semCliente);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.clienteListRecyclerView);

        FragmentManager fm = getActivity().getSupportFragmentManager();
        clienteAdapter = new ClienteAdapter(fm, clienteClickListener, modo);
        recyclerView.setAdapter(clienteAdapter);

        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.button_add_cliente);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClienteFragment clienteFragment = ClienteFragment.newInstance(new Cliente(), fragmentListener);
                addFragment(clienteFragment);
            }
        });

        setTitle("Clientes");

        if (modo.equals(Constants.Modo.SELECAO)) {
            button.setVisibility(View.GONE);
        }

        buscaClientes();

        return view;
    }


    /**
     * Configura as opções do menu.
     */
    @Override
    public void configuraMenu(Menu menu) {

        searchMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String dados) {
                if (!getMenuConfig(Constants.Key.CLIENTE).isSearchCollapse() &&
                        !getMenuConfig(Constants.Key.CLIENTE).isSearchViewVisible()) {
                    getMenuConfig(Constants.Key.CLIENTE).setSearchFilter(dados);
                    buscaClientes(dados);
                }
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery(getMenuConfig(Constants.Key.CLIENTE).getSearchFilter(), false);
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                getMenuConfig(Constants.Key.CLIENTE).setSearchCollapse(false);
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                getMenuConfig(Constants.Key.CLIENTE).setSearchCollapse(true);
                return true;
            }
        });
        searchMenuItem.setVisible(true);
    }


    FragmentListener fragmentListener = new FragmentListener() {
        @Override
        public void onFragmentEdit() {
            if (MenuItemCompat.isActionViewExpanded(searchMenuItem)) {
                MenuItemCompat.collapseActionView(searchMenuItem);
                getMenuConfig(Constants.Key.CLIENTE).setSearchViewVisible(true);
            }
        }
        @Override
        public void onFragmentFinish() {
            configuraMenu(true);
            if (getMenuConfig(Constants.Key.CLIENTE).isSearchViewVisible()) {
                MenuItemCompat.expandActionView(searchMenuItem);
                getMenuConfig(Constants.Key.CLIENTE).setSearchViewVisible(false);
            }
            String filter = getMenuConfig(Constants.Key.CLIENTE).getSearchFilter();
            if (Util.isNotBlank(filter)) {
                buscaClientes(filter);
            }else {
                buscaClientes();
            }
        }
    };

    ClienteAdapter.ClienteClickListener clienteClickListener = new ClienteAdapter.ClienteClickListener() {
        @Override
        public void onClienteClick(Cliente cliente) {
            if (modo.equals(Constants.Modo.SELECAO)) {
                MenuItemCompat.collapseActionView(searchMenuItem);
                if (listener!=null) {
                    listener.onClienteSelected(cliente);
                }
                getActivity().onBackPressed();
            } else {
                ClienteFragment clienteFragment = ClienteFragment.newInstance(cliente, fragmentListener);
                addFragment(clienteFragment);
            }
        }
    };


    /**
     * Recupera todos dos clientes.
     */
    private void buscaClientes() {
        buscaClientes(null);
    }


    /**
     * Recupera todos dos clientes iniciados pelo parametro <arg>nome</arg> e
     * e inicializa o adapter com os cliente encontrados.
     * @param dados
     */
    private void buscaClientes(String dados) {

        ClienteDao clienteDao = getDaoSession().getClienteDao();
        QueryBuilder<Cliente> queryBuilder = clienteDao.queryBuilder();
        if (Util.isNotBlank(dados)){
            queryBuilder.whereOr(
                    ClienteDao.Properties.CpfCnpj.like(dados+ "%"),
                    ClienteDao.Properties.CodParcSk.eq(dados),
                    ClienteDao.Properties.NomeRazaoSocial.like("%" + dados + "%"));
        }
        queryBuilder.limit(Constants.Query.LIMITE_CLIENTES);
        Query<Cliente> query = queryBuilder.orderAsc(ClienteDao.Properties.Id).build();
        List<Cliente> clientes = query.list();

        clienteAdapter.setClientes(clientes);
        if (clientes.isEmpty() &&  Util.isBlank(dados)) {
            semCliente.setVisibility(View.VISIBLE);
        }else {
            semCliente.setVisibility(View.GONE);
        }
    }


    public interface ClienteSelectedListener extends Serializable {
        void onClienteSelected(Cliente cliente);
    }

    public interface ClienteFragmentListListener extends Serializable {
        void onFragmentEdit();
        void onFragmentFinish();
    }
}
