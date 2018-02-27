package br.com.dsx.vendamais.fragment;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
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

import org.greenrobot.greendao.database.Database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.VendaMaisApplication;
import br.com.dsx.vendamais.adapter.ProdutoAdapter;
import br.com.dsx.vendamais.common.MenuConfig;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.dao.EstoqueDao;
import br.com.dsx.vendamais.dao.ProdutoDao;
import br.com.dsx.vendamais.domain.ItemPedido;
import br.com.dsx.vendamais.domain.Perfil;
import br.com.dsx.vendamais.domain.Produto;

import static br.com.dsx.vendamais.common.Constants.Key;
import static br.com.dsx.vendamais.common.Constants.Modo;
import static br.com.dsx.vendamais.common.Constants.Query;

public class ProdutoFragmentList extends BaseFragment {

    private transient TextView semProduto;
    private transient MenuItem searchMenuItem;

    private ProdutoAdapter produtoAdapter;
    private ProdutoSelectedListener listener;
    private String modo = Modo.EDICAO;
    private List<ItemPedido> itens;
    private Long idEmpresa;
    private Long idLocal;
    private Double taxa;
    private MenuConfig menuConfig;

    public static ProdutoFragmentList newInstance(
            String modo,
            ProdutoSelectedListener listener,
            List<ItemPedido> itens) {
        ProdutoFragmentList fragment = new ProdutoFragmentList();
        Bundle args = new Bundle();
        args.putSerializable(Key.MODO, modo);
        args.putSerializable(Key.PRODUTO_LISTENER, listener);
        args.putSerializable(Key.ITENS_PEDIDO, (Serializable) itens);
        fragment.setArguments(args);
        return fragment;
    }

    public static ProdutoFragmentList newInstance(
            String modo,
            ProdutoSelectedListener listener,
            FragmentListener callback,
            List<ItemPedido> itens,
            Long idEmpresa,
            Long idLocal,
            Double taxa) {
        ProdutoFragmentList fragment = new ProdutoFragmentList();
        Bundle args = new Bundle();
        args.putSerializable(Key.MODO, modo);
        args.putSerializable(Key.PRODUTO_LISTENER, listener);
        args.putSerializable(Key.FRAGMENT_LISTENER, callback);
        args.putSerializable(Key.ITENS_PEDIDO, (Serializable) itens);
        args.putLong(Key.ID_EMPRESA, idEmpresa);
        args.putLong(Key.ID_LOCAL_ESTOQUE, idLocal);
        args.putDouble(Key.TAXA, taxa);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            modo = getArguments().getString(Key.MODO);
            listener = (ProdutoSelectedListener)getArguments()
                    .getSerializable(Key.PRODUTO_LISTENER);
            callback = (FragmentListener) getArguments().
                    getSerializable(Key.FRAGMENT_LISTENER);
            itens = (ArrayList<ItemPedido>)getArguments()
                    .getSerializable(Key.ITENS_PEDIDO);
            idEmpresa = getArguments().getLong(Key.ID_EMPRESA);
            idLocal = getArguments().getLong(Key.ID_LOCAL_ESTOQUE);
            taxa = getArguments().getDouble(Key.TAXA);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("Produtos");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.produto_fragment_list, container, false);
        semProduto = (TextView) view.findViewById(R.id.semProduto);

        itens = (itens==null)?new ArrayList<ItemPedido>():itens;
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.produtoListRecyclerView);
        produtoAdapter = new ProdutoAdapter(produtoClickListener, modo);
        recyclerView.setAdapter(produtoAdapter);

        Context context = recyclerView.getContext();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        ProdutoDao produtoDao = getDaoSession().getProdutoDao();
        produtoDao.detachAll();

        setTitle("Produtos");

        Perfil perfil = getPerfil();
        if (perfil.getIdEmpresa()==null) {
            StringBuilder msg = new StringBuilder();
            msg.append("Vá em Configuração e cadastre uma Empresa e um ").
                    append("Local de Estoque ante de visualizar os produtos.");
            semProduto.setVisibility(View.VISIBLE);
            semProduto.setText(msg.toString());
            return view;
        } else {
            semProduto.setVisibility(View.GONE);
        }

        if (!modo.equals(Modo.SELECAO)) {
            idEmpresa = perfil.getIdEmpresa();
            idLocal = perfil.getIdLocalEstoque();
        }
        buscaProdutos(null, idEmpresa, idLocal);

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
            public boolean onQueryTextChange(String descricao) {
                if (!getMenuConfig(Key.PRODUTO).isSearchCollapse() &&
                        !getMenuConfig(Key.PRODUTO).isSearchViewVisible()) {
                    getMenuConfig(Key.PRODUTO).setSearchFilter(descricao);
                    buscaProdutos(descricao, idEmpresa, idLocal);
                }
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setQuery(getMenuConfig(Key.PRODUTO).getSearchFilter(), false);
            }
        });
        MenuItemCompat.setOnActionExpandListener(searchMenuItem, new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                getMenuConfig(Key.PRODUTO).setSearchCollapse(false);
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                getMenuConfig(Key.PRODUTO).setSearchCollapse(true);
                return true;
            }
        });
        searchMenuItem.setVisible(true);

        if (modo.equals(Modo.SELECAO)) {
            MenuItem selecionarMenuItem = menu.findItem(R.id.action_selecionar);
            selecionarMenuItem.setVisible(true);
            selecionarMenuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (listener!=null) {
                        listener.onProdutoSelected(itens);
                    }
                    getActivity().onBackPressed();
                    return false;
                }
            });
        }
    }


    FragmentListener fragmentListener = new FragmentListener() {
        @Override
        public void onFragmentEdit() {
            if (MenuItemCompat.isActionViewExpanded(searchMenuItem)) {
                MenuItemCompat.collapseActionView(searchMenuItem);
                getMenuConfig(Key.PRODUTO).setSearchViewVisible(true);
            }
        }
        @Override
        public void onFragmentFinish() {
            Menu menu = getMenu();
            menu.findItem(R.id.action_salvar).setVisible(false);
            menu.findItem(R.id.action_remover).setVisible(false);
            searchMenuItem.setVisible(true);
            if (getMenuConfig(Key.PRODUTO).isSearchViewVisible()) {
                MenuItemCompat.expandActionView(searchMenuItem);
                getMenuConfig(Key.PRODUTO).setSearchViewVisible(false);
            }
        }
    };

    ProdutoAdapter.ProdutoClickListener produtoClickListener = new ProdutoAdapter.ProdutoClickListener() {
        @Override
        public void onProdutoClick(Produto produto) {
            DaoSession daoSession = ((VendaMaisApplication)getActivity().getApplication()).getDaoSession();
            ProdutoDao dao = daoSession.getProdutoDao();
            Produto produtoFromDB = dao.load(produto.getId());
            produtoFromDB.setFotoPrincipal(produto.getFotoPrincipal());
            produtoFromDB.setEstoque(produto.getEstoque());

            ProdutoFragment produtoFragment = ProdutoFragment.newInstance(produtoFromDB, fragmentListener);
            addFragment(produtoFragment);
        }

        @Override
        public void onProdutoSelected(Produto produto) {

            boolean find=false;
            ItemPedido item = null;
            Iterator<ItemPedido> i = itens.iterator();
            while(!find && i.hasNext()) {
                item = i.next();
                if (produto.getId().equals(item.getIdProduto())) {
                    item.setQuantidade(produto.getQuantidade());
                    find=true;
                }
            }
            if (!find && produto.getQuantidade()>0) {
                item = new ItemPedido();
                item.setQuantidade(produto.getQuantidade());
                item.setIdProduto(produto.getId());
                item.setProduto(produto);
                item.setDesconto(0.0);
                item.setTaxa(taxa);
                item.setValorUnitario(produto.getPreco());
                item.setValorTotal(item.getQuantidade()*item.getValorTaxado());
                itens.add(item);
            }else if (find && produto.getQuantidade().equals(0)){
                itens.remove(item);
                listener.onProdutoRemoved(item);
            }
            listener.onProdutoSelected(itens);
        }
    };

    private Produto cursorToProduto(Cursor cursor) {
        Produto produto = new Produto();
        produto.setId(cursor.getLong(0));
        produto.setComplemento(cursor.getString(1));
        produto.setPreco(cursor.getDouble(2));
        produto.setDescricaoVolume(cursor.getString(3));
        produto.setMarca(cursor.getString(4));
        produto.setEstoque(cursor.getLong(5));
        return produto;
    }


    /**
     * Recupera todos dos produtos iniciados pelo parametro <arg>descricao</arg> e
     * e inicializa o adapter com os produto encontrados.
     * @param dados
     */
    private List<Produto> buscaProdutos(String dados, Long idEmp, Long idLoc) {

        DaoSession daoSession = ((VendaMaisApplication)getActivity().getApplication()).getDaoSession();
        Database db = daoSession.getDatabase();

        StringBuilder query = new StringBuilder();
        query.append(" SELECT ");
        query.append("P."+ProdutoDao.Properties.Id.columnName);
        query.append(", P."+ProdutoDao.Properties.Complemento.columnName);
        query.append(", P."+ProdutoDao.Properties.Preco.columnName);
        query.append(", P."+ProdutoDao.Properties.DescricaoVolume.columnName);
        query.append(", P."+ProdutoDao.Properties.Marca.columnName);
        query.append(", IFNULL (( SELECT ");
        query.append(" E."+EstoqueDao.Properties.Quantidade.columnName);
        query.append(" FROM ");
        query.append(EstoqueDao.TABLENAME+" E ");
        query.append(" WHERE E."+EstoqueDao.Properties.IdProduto.columnName);
        query.append("= P."+ProdutoDao.Properties.Id.columnName);
        query.append(" AND E."+EstoqueDao.Properties.IdLocal.columnName+" = ?");
        query.append(" AND E."+EstoqueDao.Properties.IdEmpresa.columnName+" = ?");
        query.append("), 0) AS ESTOQUE FROM ");
        query.append(ProdutoDao.TABLENAME+" P ");

        String[] arg = null;
        String codLocal = (idLoc == null) ? "0" : idLoc.toString();
        String codEmp   = (idEmp == null) ? "0" : idEmp.toString();

        if (Util.isNotBlank(dados)) {
            query.append(" WHERE P."+ProdutoDao.Properties.Id.columnName+" = ? ");
            query.append("  OR P."+ProdutoDao.Properties.Complemento.columnName+" like ? ");
            query.append("  OR P."+ProdutoDao.Properties.Marca.columnName+" like ? ");
            query.append("  OR P."+ProdutoDao.Properties.ReferenciaFornecedor.columnName+" = ? ");
            arg = new String[]{codLocal, codEmp, dados, "%"+dados+"%", "%"+dados+"%", dados};
        }else {
            arg = new String[]{codLocal, codEmp};
        }
        query.append(" ORDER BY P."+ProdutoDao.Properties.Id.columnName);
        query.append(" LIMIT "+Query.LIMITE_PRODUTOS);

        Cursor cursor = db.rawQuery(query.toString(), arg);
        List<Produto> produtos = new ArrayList<>();
        for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
            Produto produto = cursorToProduto(cursor);
            produtos.add(produto);
        }
        produtoAdapter.setProdutos(produtos);
        if (semProduto!=null) {
            if (produtos.isEmpty() && Util.isBlank(dados)) {
                semProduto.setVisibility(View.VISIBLE);
            } else {
                semProduto.setVisibility(View.GONE);
            }
        }

        for (ItemPedido item : itens) {
            int index = produtos.indexOf(new Produto(item.getIdProduto()));
            if (index >= 0) {
                Double qtd = item.getQuantidade();
                produtos.get(index).setQuantidade(qtd);
            }
        }
        return produtos;
    }


    public interface ProdutoSelectedListener extends Serializable {
        void onProdutoSelected(List<ItemPedido> itens);
        void onProdutoRemoved(ItemPedido item);
    }

}
