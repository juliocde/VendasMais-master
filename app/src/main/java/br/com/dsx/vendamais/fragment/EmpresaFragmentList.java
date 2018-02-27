package br.com.dsx.vendamais.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import br.com.dsx.vendamais.VendaMaisApplication;
import br.com.dsx.vendamais.adapter.EmpresaAdapter;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.dao.EmpresaDao;
import br.com.dsx.vendamais.domain.Empresa;


public class EmpresaFragmentList extends BaseFragment {

    private transient MenuItem searchMenuItem;
    private transient TextView semEmpresa;

    private EmpresaAdapter empresaAdapter;
    private EmpresaSelectedListener empresaListener;

    public static EmpresaFragmentList newInstance(EmpresaSelectedListener listener) {
        EmpresaFragmentList fragment = new EmpresaFragmentList();
        Bundle args = new Bundle();
        args.putSerializable(Constants.Key.EMPRESA_LISTENER, listener);
        fragment.setArguments(args);
        return fragment;
    }

    public static EmpresaFragmentList newInstance(EmpresaSelectedListener listener,
                                                  @NonNull FragmentListener callback) {
        EmpresaFragmentList fragment = new EmpresaFragmentList();
        Bundle args = new Bundle();
        args.putSerializable(Constants.Key.EMPRESA_LISTENER, listener);
        args.putSerializable(Constants.Key.FRAGMENT_LISTENER, callback);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            empresaListener = (EmpresaSelectedListener)getArguments()
                    .getSerializable(Constants.Key.EMPRESA_LISTENER);
            callback = (FragmentListener) getArguments().
                    getSerializable(Constants.Key.FRAGMENT_LISTENER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.empresa_fragment_list, container, false);
        semEmpresa = (TextView) view.findViewById(R.id.semEmpresa);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.empresaListRecyclerView);
        empresaAdapter = new EmpresaAdapter(empresaClickListener);
        recyclerView.setAdapter(empresaAdapter);

        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        setTitle("Empresas");

        buscaEmpresas();

        return view;
    }

    /**
     * Configura as opções do menu.
     */
    @Override
    public void configuraMenu(Menu menu) {
        final String key = Constants.Key.EMPRESA;
        searchMenuItem = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenuItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String dados) {
                if (!getMenuConfig(key).isSearchCollapse() &&
                        !getMenuConfig(key).isSearchViewVisible()) {
                    getMenuConfig(key).setSearchFilter(dados);
                    buscaEmpresas(dados);
                }
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery(getMenuConfig(key).getSearchFilter(), false);
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                getMenuConfig(key).setSearchCollapse(false);
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                getMenuConfig(key).setSearchCollapse(true);
                return true;
            }
        });
        searchMenuItem.setVisible(true);
    }

    EmpresaAdapter.EmpresaClickListener empresaClickListener = new EmpresaAdapter.EmpresaClickListener() {
        @Override
        public void onEmpresaClick(Empresa empresa) {
            MenuItemCompat.collapseActionView(searchMenuItem);
            if (empresaListener!=null) {
                empresaListener.onEmpresaSelected(empresa);
            }
            getActivity().onBackPressed();
        }
    };


    /**
     * Recupera todos dos produtos.
     */
    private void buscaEmpresas() {
        buscaEmpresas(null);
    }


    /**
     * Recupera todos dos produtos iniciados pelo parametro <arg>nome</arg> e
     * e inicializa o adapter com os produto encontrados.
     * @param nome
     */
    private void buscaEmpresas(String nome) {

        DaoSession daoSession = ((VendaMaisApplication)getActivity().getApplication()).getDaoSession();
        EmpresaDao dao = daoSession.getEmpresaDao();

        QueryBuilder<Empresa> queryBuilderEmpresa = dao.queryBuilder();
        if (Util.isNotBlank(nome)){
            StringBuilder arg = new StringBuilder().append("%").append(nome).append("%");
            queryBuilderEmpresa.where(EmpresaDao.Properties.NomeFantasia.like(arg.toString()));
        }
        Query<Empresa> queryEmpresa = queryBuilderEmpresa
                .orderAsc(EmpresaDao.Properties.NomeFantasia)
                .build();
        List<Empresa> empresas = queryEmpresa.list();
        empresaAdapter.setEmpresas(empresas);

        if (empresas.isEmpty() &&  Util.isBlank(nome)) {
            semEmpresa.setVisibility(View.VISIBLE);
        }else {
            semEmpresa.setVisibility(View.GONE);
        }

    }

    public interface EmpresaSelectedListener extends Serializable {
        void onEmpresaSelected(Empresa empresa);
    }

}
