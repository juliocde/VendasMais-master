package br.com.dsx.vendamais.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.common.Util;
import br.com.dsx.vendamais.domain.Pedido;


public class PedidoAdapter  extends RecyclerView.Adapter<PedidoAdapter.PedidoViewHolder>
                            implements Serializable {

    private PedidoClickListener clickListener;
    private List<Pedido> pedidos;

    public PedidoAdapter(PedidoClickListener clickListener) {
        this.clickListener = clickListener;
        this.pedidos = new ArrayList<>();
    }

    @Override
    public PedidoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pedido_fragment_row, parent, false);
        return new PedidoViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(final PedidoViewHolder holder, int position) {
        Pedido pedido = pedidos.get(position);
        holder.pedido = pedido;
        Date data = pedido.getDataInclusao();
        holder.pedidoDia.setText(Util.dateToString("dd",data));
        holder.pedidoMes.setText(Util.dateToString("MMM",data).toUpperCase());
        holder.pedidoAno.setText(Util.dateToString("yyyy",data));
        holder.pedidoNomeCliente.setText(pedido.getCliente().getNomeRazaoSocial());
        holder.pedidoValorTotal.setText(Util.doubleToValorMonetaria(pedido.getValorTotal()));
        if (pedido.getNumeroUnico()==0) {
            holder.pedidoNumeroUnico.setText("-");
            holder.pedidoStatus.setText("NÃO ENVIADO");
            holder.pedidoNaoEnviado.setVisibility(View.VISIBLE);
            holder.pedidoEnviado.setVisibility(View.GONE);

        } else {
            holder.pedidoNumeroUnico.setText(String.valueOf(pedido.getNumeroUnico()));
            holder.pedidoStatus.setText("ENVIADO");
            holder.pedidoEnviado.setVisibility(View.VISIBLE);
            holder.pedidoNaoEnviado.setVisibility(View.GONE);
        }
        holder.pedidoQtd.setText(String.valueOf(pedido.getQuantidadeItens())+ " PRODUTO(S)");
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    public void setPedidos(@NonNull List<Pedido> pedidos) {
        this.pedidos = pedidos;
        notifyDataSetChanged();
    }


    public Pedido getPedido(int position){
        return (pedidos!=null)?pedidos.get(position):null;
    }

    static class PedidoViewHolder extends RecyclerView.ViewHolder {
        public TextView pedidoDia;
        public TextView pedidoMes;
        public TextView pedidoAno;
        public TextView pedidoNomeCliente;
        public TextView pedidoValorTotal;
        public TextView pedidoNumeroUnico;
        public TextView pedidoStatus;
        public ImageView pedidoNaoEnviado;
        public ImageView pedidoEnviado;
        public TextView pedidoQtd;
        public Pedido pedido;

        public PedidoViewHolder(View itemView, final PedidoClickListener clickListener) {
            super(itemView);

            pedidoDia = (TextView) itemView.findViewById(R.id.pedidoDia);
            pedidoMes = (TextView) itemView.findViewById(R.id.pedidoMes);
            pedidoAno = (TextView) itemView.findViewById(R.id.pedidoAno);
            pedidoNomeCliente = (TextView) itemView.findViewById(R.id.pedidoNomeCliente);
            pedidoValorTotal = (TextView) itemView.findViewById(R.id.pedidoValorTotalValue);
            pedidoNumeroUnico = (TextView) itemView.findViewById(R.id.pedidoNumeroUnicoValue);
            pedidoStatus = (TextView) itemView.findViewById(R.id.pedidoStatusValue);
            pedidoNaoEnviado = (ImageView) itemView.findViewById(R.id.pedidoNaoEnviado);
            pedidoEnviado = (ImageView) itemView.findViewById(R.id.pedidoEnviado);
            pedidoQtd = (TextView) itemView.findViewById(R.id.pedidoQtdValue);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        clickListener.onPedidoClick(pedido);
                    }
                }
            });
        }
    }

    public interface PedidoClickListener extends Serializable {
        void onPedidoClick(Pedido pedido);
    }

}
