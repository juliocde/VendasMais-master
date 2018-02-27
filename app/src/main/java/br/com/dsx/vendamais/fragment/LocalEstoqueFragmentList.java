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
import br.com.dsx.vendamais.adapter.LocalEstoqueAdapter;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.dao.LocalEstoqueDao;
import br.com.dsx.vendamais.domain.LocalEstoque;


public class LocalEstoqueFragmentList extends BaseFragment {

    private transient TextView semLocalEstoque;
    private transient MenuItem searchMenuItem;

    private LocalEstoqueAdapter localEstoqueAdapter;
    private LocalEstoqueSelectedListener listener;

    public static LocalEstoqueFragmentList newInstance(
            @NonNull LocalEstoqueSelectedListener listener) {
        LocalEstoqueFragmentList fragment = new LocalEstoqueFragmentList();
        Bundle args = new Bundle();
        args.putSerializable(Constants.Key.LOCAL_ESTOQUE_LISTENER, listener);
        fragment.setArguments(args);
        return fragment;
    }

    public static LocalEstoqueFragmentList newInstance(
            @NonNull LocalEstoqueSelectedListener listener,
            @NonNull FragmentListener callback) {
        LocalEstoqueFragmentList fragment = new LocalEstoqueFragmentList();
        Bundle args = new Bundle();
        args.putSerializable(Constants.Key.LOCAL_ESTOQUE_LISTENER, listener);
        args.putSerializable(Constants.Key.FRAGMENT_LISTENER, callback);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listener = (LocalEstoqueSelectedListener)getArguments()
                    .getSerializable(Constants.Key.LOCAL_ESTOQUE_LISTENER);
            callback = (FragmentListener) getArguments().
                    getSerializable(Constants.Key.FRAGMENT_LISTENER);
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.local_estoque_fragment_list, container, false);
        semLocalEstoque = (TextView) view.findViewById(R.id.semLocalEstoque);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.localEstoqueListRecyclerView);
        localEstoqueAdapter = new LocalEstoqueAdapter(localEstoqueClickListener);
        recyclerView.setAdapter(localEstoqueAdapter);

        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        setTitle("Locais de Estoque");

        buscaLocalEstoques();

        return view;
    }

    /**
     * Configura as opções do menu.
     */
    @Override
    public void configuraMenu(Menu menu) {
        final String key = Constants.Key.LOCAL_ESTOQUE;
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
                    buscaLocalEstoques(dados);
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

    LocalEstoqueAdapter.LocalEstoqueClickListener localEstoqueClickListener = new LocalEstoqueAdapter.LocalEstoqueClickListener() {
        @Override
        public void onLocalEstoqueClick(LocalEstoque localEstoque) {
            MenuItemCompat.collapseActionView(searchMenuItem);
            if (listener!=null) {
                listener.onLocalEstoqueSelected(localEstoque);
            }
            getActivity().onBackPressed();
        }
    };


    /**
     * Recupera todos dos produtos.
     */
    private void buscaLocalEstoques() {
        buscaLocalEstoques(null);
    }


    /**
     * Recupera todos dos produtos iniciados pelo parametro <arg>descricao</arg> e
     * e inicializa o adapter com os produto encontrados.
     * @param descricao
     */
    private void buscaLocalEstoques(String descricao) {

        DaoSession daoSession = ((VendaMaisApplication)getActivity().getApplication()).getDaoSession();
        LocalEstoqueDao dao = daoSession.getLocalEstoqueDao();

        QueryBuilder<LocalEstoque> queryBuilderLocalEstoque = dao.queryBuilder();
        if (Util.isNotBlank(descricao)){
            StringBuilder arg = new StringBuilder().append("%").append(descricao).append("%");
            queryBuilderLocalEstoque.where(LocalEstoqueDao.Properties.Descricao.like(arg.toString()));
        }
        Query<LocalEstoque> queryLocalEstoque = queryBuilderLocalEstoque
                .orderAsc(LocalEstoqueDao.Properties.Descricao)
                .build();
        List<LocalEstoque> localEstoques = queryLocalEstoque.list();
        localEstoqueAdapter.setLocalEstoques(localEstoques);

        if (localEstoques.isEmpty() &&  Util.isBlank(descricao)) {
            semLocalEstoque.setVisibility(View.VISIBLE);
        }else {
            semLocalEstoque.setVisibility(View.GONE);
        }

    }

    public interface LocalEstoqueSelectedListener extends Serializable {
        void onLocalEstoqueSelected(LocalEstoque localEstoque);
    }

}
