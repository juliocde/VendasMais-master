package br.com.dsx.vendamais.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.greendao.query.Join;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.VendaMaisApplication;
import br.com.dsx.vendamais.adapter.PedidoAdapter;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.ClienteDao;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.dao.PedidoDao;
import br.com.dsx.vendamais.domain.Cliente;
import br.com.dsx.vendamais.domain.Pedido;
import br.com.dsx.vendamais.domain.Perfil;


public class PedidoFragmentList extends BaseFragment {

    private transient MenuItem searchMenuItem;
    private transient RecyclerView recyclerView;
    private transient TextView semPedido;

    private PedidoAdapter pedidoAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.pedido_fragment_list, container, false);
        semPedido = (TextView) view.findViewById(R.id.semPedido);
        recyclerView = (RecyclerView) view.findViewById(R.id.pedidoListRecyclerView);
        pedidoAdapter = new PedidoAdapter(pedidoClickListener);
        recyclerView.setAdapter(pedidoAdapter);
        setTitle("Pedidos");

        Perfil perfil = getPerfil();
        if (perfil.getIdEmpresa()==null) {
            StringBuilder msg = new StringBuilder();
            msg.append("Vá em Configuração e cadastre uma Empresa e um ").
                    append("Local de Estoque ante de lançar um pedido.");
            semPedido.setVisibility(View.VISIBLE);
            semPedido.setText(msg.toString());
            return view;
        } else {
            semPedido.setVisibility(View.GONE);
        }

        FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.button_add_pedido);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PedidoFragment pedidoFragment = PedidoFragment.newInstance(new Pedido(), fragmentListener);
                addFragment(pedidoFragment);
            }
        });

        buscaPedidos();

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
                if (!getMenuConfig(Constants.Key.PEDIDO).isSearchCollapse() &&
                        !getMenuConfig(Constants.Key.PEDIDO).isSearchViewVisible()) {
                    getMenuConfig(Constants.Key.PEDIDO).setSearchFilter(dados);
                    buscaPedidos(dados);
                }
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery(getMenuConfig(Constants.Key.PEDIDO).getSearchFilter(), false);
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                getMenuConfig(Constants.Key.PEDIDO).setSearchCollapse(false);
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                getMenuConfig(Constants.Key.PEDIDO).setSearchCollapse(true);
                return true;
            }
        });
        searchMenuItem.setVisible(true);
    }

    PedidoAdapter.PedidoClickListener pedidoClickListener = new PedidoAdapter.PedidoClickListener() {
        @Override
        public void onPedidoClick(Pedido pedido) {
            PedidoFragment pedidoFragment = PedidoFragment.newInstance(pedido, fragmentListener);
            addFragment(pedidoFragment);
        }
    };

    FragmentListener fragmentListener = new FragmentListener() {
        @Override
        public void onFragmentEdit() {
            if (MenuItemCompat.isActionViewExpanded(searchMenuItem)) {
                MenuItemCompat.collapseActionView(searchMenuItem);
                getMenuConfig(Constants.Key.PEDIDO).setSearchViewVisible(true);
            }
        }
        @Override
        public void onFragmentFinish() {
            configuraMenu(true);
            if (getMenuConfig(Constants.Key.PEDIDO).isSearchViewVisible()) {
                MenuItemCompat.expandActionView(searchMenuItem);
                getMenuConfig(Constants.Key.PEDIDO).setSearchViewVisible(false);
            }
            String filter = getMenuConfig(Constants.Key.PEDIDO).getSearchFilter();
            if (Util.isNotBlank(filter)) {
                buscaPedidos(filter);
            }else {
                buscaPedidos();
            }

        }
    };

    /**
     * Recupera todos dos pedidos.
     */
    private void buscaPedidos() {
        buscaPedidos(null);
    }


    /**
     * Recupera todos dos pedidos iniciados pelo parametro <arg>dados</arg> e
     * e inicializa o adapter com os pedidos encontrados.
     * @param nome
     */
    private void buscaPedidos(String nome) {

        DaoSession daoSession = ((VendaMaisApplication)getActivity().getApplication()).getDaoSession();
        PedidoDao pedidoDao = daoSession.getPedidoDao();

        QueryBuilder<Pedido> qbPedido = pedidoDao.queryBuilder();
        Join join = qbPedido.join(PedidoDao.Properties.IdCliente,Cliente.class);
        if (Util.isNotBlank(nome)){
            join.where(ClienteDao.Properties.NomeRazaoSocial.like("%" + nome + "%"));
        }
        Query<Pedido> queryPedido = qbPedido.orderDesc(PedidoDao.Properties.DataInclusao).build();
        List<Pedido> pedidos = queryPedido.list();

        ClienteDao clienteDao = daoSession.getClienteDao();
        for(Pedido p:pedidos) {
            Cliente cliente = clienteDao.load(p.getIdCliente());
            p.setCliente(cliente);
        }
        pedidoAdapter.setPedidos(pedidos);

        if (pedidos.isEmpty() &&  Util.isBlank(nome)) {
            semPedido.setVisibility(View.VISIBLE);
        }else {
            semPedido.setVisibility(View.GONE);
        }
    }

}

