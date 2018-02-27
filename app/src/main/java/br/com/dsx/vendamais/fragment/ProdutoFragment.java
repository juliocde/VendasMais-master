package br.com.dsx.vendamais.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rengwuxian.materialedittext.MaterialEditText;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.ImageUtil;
import br.com.dsx.vendamais.databinding.ProdutoFragmentBinding;
import br.com.dsx.vendamais.domain.Produto;
import butterknife.BindView;
import butterknife.ButterKnife;


public class ProdutoFragment extends BaseFragment {

    @BindView(R.id.imgProdutoFoto) ImageView imageProdutoFoto;
    @BindView(R.id.edtProdutoCodigo) MaterialEditText editCodigo;
    @BindView(R.id.edtProdutoPreco) MaterialEditText editPreco;
    @BindView(R.id.edtProdutoEstoque) MaterialEditText editEstoque;
    @BindView(R.id.edtProdutoComplemento) MaterialEditText editComplemento;
    @BindView(R.id.edtProdutoMarca) MaterialEditText editMarca;
    @BindView(R.id.edtProdutoCategoria) MaterialEditText editCategoria;
    @BindView(R.id.edtProdutoDescricao) MaterialEditText editDescricao;
    @BindView(R.id.edtProdutoRefFornecedor) MaterialEditText editReferenciaForncedor;

    private Produto produto;


    public static ProdutoFragment newInstance(@NonNull Produto produto,
                                              @NonNull FragmentListener callback) {
        ProdutoFragment fragment = new ProdutoFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.Key.PRODUTO, produto);
        args.putSerializable(Constants.Key.FRAGMENT_LISTENER, callback);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            produto = (Produto) getArguments().getSerializable(Constants.Key.PRODUTO);
            callback = (FragmentListener) getArguments().
                    getSerializable(Constants.Key.FRAGMENT_LISTENER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        produto = (produto==null)?new Produto():produto;

        ProdutoFragmentBinding binding = DataBindingUtil.inflate(
                inflater, R.layout.produto_fragment, container, false);
        binding.setProduto(produto);
        View view = binding.getRoot();

        unbinder = ButterKnife.bind(this, view);

        setTitle("Produto");

        ImageUtil.handleMedia(produto.getFotoPrincipal(), imageProdutoFoto);

        coloreCampos(view);

        return view;
    }

    private void coloreCampos(View view) {
        tintField(editCodigo);
        tintField(editPreco);
        tintField(editEstoque);
        tintField(editComplemento);
        tintField(editMarca);
        tintField(editCategoria);
        tintField(editDescricao);
        tintField(editComplemento);
        tintField(editReferenciaForncedor);
    }

}