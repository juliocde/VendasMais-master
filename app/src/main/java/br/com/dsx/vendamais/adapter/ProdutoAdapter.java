package br.com.dsx.vendamais.adapter;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.ImageUtil;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.domain.Produto;


public class ProdutoAdapter extends RecyclerView.Adapter<ProdutoAdapter.ProdutoViewHolder>
                            implements Serializable {

    private ProdutoClickListener clickListener;
    private List<Produto> produtos;
    private String modo = Constants.Modo.EDICAO;

    public ProdutoAdapter(ProdutoClickListener clickListener, String modo) {
        this.clickListener = clickListener;
        this.produtos = new ArrayList<>();
        this.modo = modo;
    }

    @Override
    public ProdutoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.produto_fragment_row, parent, false);
        return new ProdutoViewHolder(view, clickListener, modo);
    }

    @Override
    public void onBindViewHolder(final ProdutoViewHolder holder, int position) {
        Produto produto = produtos.get(position);
        holder.produto = produto;
        holder.complementoView.setText(String.valueOf(produto.getId())+" - " +
                produto.getComplemento());
        holder.precoView.setText(produto.getPrecoAux());
        holder.quantidadeWatcher.setEnable(false);
        String qtd = String.valueOf(produto.getQuantidade());
        holder.quantidadeView.setText(qtd.replace(".",","));
        holder.quantidadeWatcher.setEnable(true);
        holder.estoqueView.setText("ESTOQUE: "+produto.getDescricaoEstoque());
        holder.marcaView.setText(produto.getMarca());

        //Baixa a imagem da internet (se não existir no cache)
        Context context = holder.fotoView.getContext();
        Picasso pic = Picasso.with(context);
        pic.load(Util.produtoFotoToURL(produto.getId())).into(holder.fotoView);
    }

    @Override
    public int getItemCount() {
        return produtos.size();
    }

    public void setProdutos(@NonNull List<Produto> produtos) {
        this.produtos = produtos;
        notifyDataSetChanged();
    }


    public Produto getProduto(int position){
        return (produtos!=null)?produtos.get(position):null;
    }

    static class ProdutoViewHolder extends RecyclerView.ViewHolder {

        public TextView complementoView;
        public TextView precoView;
        public ImageView fotoView;
        public LinearLayout layout;
        public TextView quantidadeView;
        public TextView estoqueView;
        public TextView marcaView;
        public Produto produto;
        private ProdutoClickListener clickListener;

        public ProdutoQuantidadeWatcher quantidadeWatcher = new ProdutoQuantidadeWatcher();

        public ProdutoViewHolder(View itemView, final ProdutoClickListener clickListener, String modo) {
            super(itemView);
            this.clickListener = clickListener;

            complementoView = (TextView) itemView.findViewById(R.id.produtoComplementoCardView);
            precoView = (TextView) itemView.findViewById(R.id.produtoPrecoCardView);
            fotoView = (ImageView) itemView.findViewById(R.id.produtoFotoCardView);
            layout = (LinearLayout) itemView.findViewById(R.id.produtoAddRemoveItem);
            quantidadeView = (TextView) itemView.findViewById(R.id.produtoQtdCardView);
            estoqueView = (TextView) itemView.findViewById(R.id.produtoEstoqueCardView);
            marcaView = (TextView) itemView.findViewById(R.id.produtoMarcaCardView);

            if (modo.equals(Constants.Modo.EDICAO)) {
                estoqueView.setVisibility(View.GONE);
                layout.setVisibility(View.GONE);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (clickListener != null) {
                            BitmapDrawable foto = (BitmapDrawable)fotoView.getDrawable();
                            if (foto != null) {
                                produto.setFotoPrincipal(ImageUtil.bitmapToBytes(foto.getBitmap()));
                            }
                            clickListener.onProdutoClick(produto);
                        }
                    }
                });
            } else {
                estoqueView.setVisibility(View.VISIBLE);
                layout.setVisibility(View.VISIBLE);
            }

            quantidadeView.addTextChangedListener(quantidadeWatcher);

            ImageView imgAddItem = (ImageView) itemView.findViewById(R.id.icon_add_item);
            imgAddItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adicionaProduto();
                }
            });
            ImageView imgRemoveItem = (ImageView) itemView.findViewById(R.id.icon_remove_item);
            imgRemoveItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeProduto();
                }
            });

        }

        void setProduto(){
            Double quantidade = Util.textViewToDouble(quantidadeView, 0.0);
            produto.setQuantidade(quantidade);
            if (clickListener != null) {
                clickListener.onProdutoSelected(produto);
            }
        }

        void adicionaProduto(){
            Double quantidade = Util.textViewToDouble(quantidadeView, 0.0);
            quantidadeWatcher.setEnable(false);
            String qtd = String.valueOf(++quantidade);
            quantidadeView.setText(qtd.replace(".",","));
            quantidadeWatcher.setEnable(true);
            produto.setQuantidade(quantidade);
            if (clickListener != null) {
                clickListener.onProdutoSelected(produto);
            }
        }

        void removeProduto(){
            Double quantidade = Util.textViewToDouble(quantidadeView, 0.0);
            if (quantidade>0) {
                quantidadeWatcher.setEnable(false);
                String qtd = String.valueOf(--quantidade);
                quantidadeView.setText(qtd.replace(".",","));
                quantidadeWatcher.setEnable(true);
            }
            produto.setQuantidade(quantidade);
            if (clickListener != null) {
                clickListener.onProdutoSelected(produto);
            }
        }

        class ProdutoQuantidadeWatcher implements TextWatcher {

            private boolean enable=true;

            public void setEnable(boolean enable){
                this.enable = enable;
            }

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (!enable) return;
                setEnable(false);
                String valueString = s.toString().replace(".",",");
                setEnable(false);
                if (!quantidadeView.getText().toString().equals(valueString)) {
                    quantidadeView.setTextKeepState(valueString);
                }
                setEnable(true);
                setProduto();
            }
        }
    }

    public interface ProdutoClickListener extends Serializable {
        void onProdutoClick(Produto produto);
        void onProdutoSelected(Produto produto);
    }

}

