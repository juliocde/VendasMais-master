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
import android.view.MenuInflater;
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
import br.com.dsx.vendamais.adapter.FormaPagamentoAdapter;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.dao.FormaPagamentoDao;
import br.com.dsx.vendamais.domain.FormaPagamento;


public class FormaPagamentoFragmentList extends BaseFragment {

    private transient TextView semFormaPagamento;
    private transient MenuItem searchMenuItem;

    private FormaPagamentoAdapter formaPagamentoAdapter;
    private FormaPagamentoSelectedListener listener;

    public static FormaPagamentoFragmentList newInstance(
            @NonNull FormaPagamentoSelectedListener listener,
            @NonNull FragmentListener callback) {
        FormaPagamentoFragmentList fragment = new FormaPagamentoFragmentList();
        Bundle args = new Bundle();
        args.putSerializable(Constants.Key.FORMA_PAGMENTO_LISTENER, listener);
        args.putSerializable(Constants.Key.FRAGMENT_LISTENER, callback);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            listener = (FormaPagamentoSelectedListener)getArguments()
                    .getSerializable(Constants.Key.FORMA_PAGMENTO_LISTENER);
            callback = (FragmentListener) getArguments().
                    getSerializable(Constants.Key.FRAGMENT_LISTENER);
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.forma_pagamento_fragment_list, container, false);
        semFormaPagamento = (TextView) view.findViewById(R.id.semFormaPagamento);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.formaPagamentoListRecyclerView);

        formaPagamentoAdapter = new FormaPagamentoAdapter(formaPagamentoClickListener);
        recyclerView.setAdapter(formaPagamentoAdapter);

        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        setTitle("Formas de Pagamento");

        buscaFormaPagamentos();

        return view;
    }

    /**
     * Configura as opções do menu.
     */
    @Override
    public void configuraMenu(Menu menu) {
        final String key = Constants.Key.FORMA_PAGMENTO;
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
                    buscaFormaPagamentos(dados);
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

    FormaPagamentoAdapter.FormaPagamentoClickListener formaPagamentoClickListener = new FormaPagamentoAdapter.FormaPagamentoClickListener() {
        @Override
        public void onFormaPagamentoClick(FormaPagamento formaPagamento) {
            MenuItemCompat.collapseActionView(searchMenuItem);
            if (listener!=null) {
                listener.onFormaPagamentoSelected(formaPagamento);
            }
            getActivity().onBackPressed();
        }
    };


    /**
     * Recupera todos dos produtos.
     */
    private void buscaFormaPagamentos() {
        buscaFormaPagamentos(null);
    }


    /**
     * Recupera todos dos produtos iniciados pelo parametro <arg>descricao</arg> e
     * e inicializa o adapter com os produto encontrados.
     * @param descricao
     */
    private void buscaFormaPagamentos(String descricao) {

        DaoSession daoSession = ((VendaMaisApplication)getActivity().getApplication()).getDaoSession();
        FormaPagamentoDao dao = daoSession.getFormaPagamentoDao();

        QueryBuilder<FormaPagamento> queryBuilderFormaPagamento = dao.queryBuilder();
        if (Util.isNotBlank(descricao)){
            StringBuilder arg = new StringBuilder().append("%").append(descricao).append("%");
            queryBuilderFormaPagamento.where(FormaPagamentoDao.Properties.Descricao.like(arg.toString()));
        }
        Query<FormaPagamento> queryFormaPagamento = queryBuilderFormaPagamento.orderAsc(FormaPagamentoDao.Properties.Descricao).build();
        List<FormaPagamento> formas = queryFormaPagamento.list();
        formaPagamentoAdapter.setFormaPagamentos(formas);

        if (formas.isEmpty() &&  Util.isBlank(descricao)) {
            semFormaPagamento.setVisibility(View.VISIBLE);
        }else {
            semFormaPagamento.setVisibility(View.GONE);
        }
    }

    public interface FormaPagamentoSelectedListener extends Serializable {
        void onFormaPagamentoSelected(FormaPagamento formaPagamento);
    }

}
