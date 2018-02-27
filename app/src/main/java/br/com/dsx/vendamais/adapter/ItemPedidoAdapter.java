package br.com.dsx.vendamais.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.dao.ItemPedidoDao;
import br.com.dsx.vendamais.domain.ItemPedido;
import br.com.dsx.vendamais.domain.Produto;


public class ItemPedidoAdapter  extends RecyclerView.Adapter<ItemPedidoAdapter.ItemPedidoViewHolder>
        implements Serializable {

    private ItemPedidoClickListener clickListener;
    private List<ItemPedido> itens;
    private transient ItemPedidoDao dao;

    public ItemPedidoAdapter(ItemPedidoClickListener clickListener) {
        this.clickListener = clickListener;
        this.itens = new ArrayList<>();
    }

    @Override
    public ItemPedidoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido_fragment_row, parent, false);
        return new ItemPedidoViewHolder(view, this);
    }

    @Override
    public void onBindViewHolder(final ItemPedidoViewHolder holder, int position) {
        ItemPedido itemPedido = itens.get(position);
        holder.itemPedido = itemPedido;
        holder.precoView.setText(itemPedido.getValorTaxadoAux());
        holder.quantidadeWatcher.setEnable(false);
        String qtd = String.valueOf(itemPedido.getQuantidade());
        holder.quantidadeView.setText(qtd.replace(".",","));
        holder.quantidadeWatcher.setEnable(true);
        holder.valorWatcher.setEnable(false);
        holder.descontoValor.setText(Util.doubleToString(itemPedido.getDesconto()));
        holder.valorWatcher.setEnable(true);
        holder.calculaSubTotal("valor");

        //TODO: não sei pq o produto conseguiu ficar nulo em um registro.
        Produto produto = itemPedido.getProduto();
        if (produto!=null) {
            String estoque = itemPedido.getProduto().getDescricaoEstoque();
            holder.estoqueView.setText("Estoque: "+estoque.toLowerCase());
            holder.descricaoView.setText(itemPedido.getProduto().getComplemento());

            Context context = holder.fotoView.getContext();
            Picasso pic = Picasso.with(context);
            pic.load(Util.produtoFotoToURL(itemPedido.getProduto().getId())).into(holder.fotoView);
        }

    }


    @Override
    public int getItemCount() {
        return itens.size();
    }

    public void setItemPedidos(@NonNull List<ItemPedido> itens) {
        this.itens = itens;
        notifyDataSetChanged();
    }

    public ItemPedido getItemPedido(int position){
        return (itens!=null)?itens.get(position):null;
    }

    static class ItemPedidoViewHolder extends RecyclerView.ViewHolder {
        public TextView descricaoView;
        public TextView precoView;
        public ImageView fotoView;
        public LinearLayout layout;
        public TextView quantidadeView;
        public TextView estoqueView;
        public TextView descontoPorcent;
        public TextView descontoValor;
        public TextView subtotalView;
        public TextView totalCardView;
        public ImageView imgItemMenu;

        public ItemPedido itemPedido;
        private ItemPedidoAdapter adapter;
        private View view;

        public DescontoWatcher valorWatcher = new DescontoWatcher("valor");
        public DescontoWatcher porcentagemWatcher = new DescontoWatcher("%");
        public QuantidadeWatcher quantidadeWatcher = new QuantidadeWatcher();

        public ItemPedidoViewHolder(View itemView, final ItemPedidoAdapter adapter) {
            super(itemView);
            this.adapter = adapter;
            this.view = itemView;
            descricaoView = (TextView) view.findViewById(R.id.produtoDescricaoCardView);
            precoView = (TextView) view.findViewById(R.id.produtoPrecoCardView);
            fotoView = (ImageView) view.findViewById(R.id.produtoFotoCardView);
            layout = (LinearLayout) view.findViewById(R.id.produtoAddRemoveItem);
            quantidadeView = (TextView) view.findViewById(R.id.produtoQtdCardView);
            estoqueView = (TextView) view.findViewById(R.id.produtoEstoqueCardView);
            descontoPorcent = (TextView) view.findViewById(R.id.descontoPorcent);
            descontoValor = (TextView) view.findViewById(R.id.descontoValor);
            subtotalView = (TextView) view.findViewById(R.id.produtoSubtotalCardView);
            totalCardView = (TextView) view.findViewById(R.id.totalCardView);
            imgItemMenu = (ImageView) view.findViewById(R.id.itemMenuCardView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (adapter.clickListener != null) {
                        adapter.clickListener.onItemPedidoClick(itemPedido);
                    }
                }
            });

            quantidadeView.addTextChangedListener(quantidadeWatcher);
            descontoPorcent.addTextChangedListener(porcentagemWatcher);
            descontoValor.addTextChangedListener(valorWatcher);

            imgItemMenu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    menuItem();
                }
            });
            ImageView imgAddItem = (ImageView) view.findViewById(R.id.icon_add_item);
            imgAddItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adicionaItemPedido();
                }
            });
            ImageView imgRemoveItem = (ImageView) view.findViewById(R.id.icon_remove_item);
            imgRemoveItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeItemPedido();
                }
            });
        }

        void menuItem(){
            PopupMenu popup = new PopupMenu(view.getContext(), imgItemMenu);
            popup.getMenuInflater().inflate(R.menu.item_pedido_list_item, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    adapter.itens.remove(itemPedido);
                    adapter.notifyDataSetChanged();
                    if (adapter.clickListener != null) {
                        adapter.clickListener.onItemPedidoRemoved(itemPedido);
                    }
                    calculaSubTotal("valor");
                    return true;
                }
            });
            popup.show();
        }

        void setProduto(){
            Double quantidade = Util.textViewToDouble(quantidadeView, 0.0);
            itemPedido.setQuantidade(quantidade);
            calculaSubTotal("valor");
        }

        void adicionaItemPedido(){
            Double quantidade = Util.textViewToDouble(quantidadeView, 0.0);
            quantidadeWatcher.setEnable(false);
            String qtd = String.valueOf(++quantidade);
            quantidadeView.setText(qtd.replace(".",","));
            quantidadeWatcher.setEnable(true);
            itemPedido.setQuantidade(quantidade);
            calculaSubTotal("valor");
        }

        void removeItemPedido(){
            Double quantidade = Util.textViewToDouble(quantidadeView, 0.0);
            if (quantidade>0) {
                quantidadeWatcher.setEnable(false);
                String qtd = String.valueOf(--quantidade);
                quantidadeView.setText(qtd.replace(".",","));
                quantidadeWatcher.setEnable(true);
            }
            itemPedido.setQuantidade(quantidade);
            calculaSubTotal("valor");
        }

        void calculaSubTotal(String campo){
            Double subtotal = itemPedido.getQuantidade() * itemPedido.getValorTaxado();
            subtotalView.setText(Util.doubleToValorMonetaria(subtotal));
            Double desconto = 0.0, porcent =0.0;
            if ("%".equals(campo)) {
                porcent = Util.textViewToDouble(descontoPorcent, 0.0);
                desconto = porcent*0.01*subtotal;
                valorWatcher.setEnable(false);
                descontoValor.setText(Util.doubleToString(desconto));
                valorWatcher.setEnable(true);
            } else {
                desconto = Util.textViewToDouble(descontoValor, 0.0);
                if (subtotal != 0) {
                    porcent = desconto * 100 / subtotal;
                }
                porcentagemWatcher.setEnable(false);
                descontoPorcent.setText(Util.doubleToString(porcent));
                porcentagemWatcher.setEnable(true);
            }
            itemPedido.setDesconto(desconto);
            itemPedido.setValorTotal((desconto>subtotal)?0:subtotal-desconto);
            totalCardView.setText(Util.doubleToValorMonetaria(itemPedido.getValorTotal()));
            if (adapter.clickListener != null) {
                adapter.clickListener.onItemPedidoChanged(itemPedido);
            }
        }

        class QuantidadeWatcher implements TextWatcher {

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

        class DescontoWatcher implements TextWatcher {

            private String id;
            private boolean enable=true;

            public DescontoWatcher(String id) {
                this.id = id;
            }

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
                String valueString = s.toString().replace(".",",");
                setEnable(false);
                if (id.equals("valor")) {
                    if (!descontoValor.getText().toString().equals(valueString)) {
                        descontoValor.setTextKeepState(valueString);
                    }
                }else {
                    if (!descontoPorcent.getText().toString().equals(valueString)) {
                        descontoPorcent.setTextKeepState(valueString);
                    }
                }
                setEnable(true);
                calculaSubTotal(id);
            }
        }
    }

    public interface ItemPedidoClickListener extends Serializable {
        void onItemPedidoClick(ItemPedido itemPedido);
        void onItemPedidoChanged(ItemPedido itemPedido);
        void onItemPedidoRemoved(ItemPedido itemPedido);
    }

}

