package br.com.dsx.vendamais.adapter;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.domain.Cliente;
import br.com.dsx.vendamais.domain.Pedido;
import br.com.dsx.vendamais.fragment.PedidoFragment;


public class ClienteAdapter extends RecyclerView.Adapter<ClienteAdapter.ClienteViewHolder>
                            implements Serializable {

    private ClienteClickListener clickListener;
    private List<Cliente> clientes;
    private String modo = Constants.Modo.EDICAO;
    public static FragmentManager fragmentManager;

    public ClienteAdapter(FragmentManager fm, ClienteClickListener clickListener, String modo) {
        this.clickListener = clickListener;
        this.clientes = new ArrayList<>();
        this.modo = modo;
        this.fragmentManager = fm;
    }

    @Override
    public ClienteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cliente_fragment_row, parent, false);
        return new ClienteViewHolder(view, clickListener, modo);
    }

    @Override
    public void onBindViewHolder(final ClienteViewHolder holder, int position) {
        Cliente cliente = clientes.get(position);
        holder.cliente = cliente;
        holder.razaoSocialView.setText(String.valueOf(cliente.getId())+" - "
                +cliente.getNomeRazaoSocial());
        holder.cidadeView.setText(cliente.getCidade());
        holder.celularView.setText(cliente.getCelularAux());
        holder.cnpjCpfView.setText(cliente.getCpfCnpjAux());
        holder.inscEstView.setText(cliente.getInscricaoEstadual());
    }

    @Override
    public int getItemCount() {
        return clientes.size();
    }

    public void setClientes(@NonNull List<Cliente> clientes) {
        this.clientes = clientes;
        notifyDataSetChanged();
    }


    public Cliente getCliente(int position){
        return (clientes!=null)?clientes.get(position):null;
    }

    static class ClienteViewHolder extends RecyclerView.ViewHolder {
        public TextView razaoSocialView;
        public TextView cidadeView;
        public TextView celularView;
        public TextView cnpjCpfView;
        public TextView inscEstView;
        public ImageView imgClientePedido;
        public View view;
        public Cliente cliente;

        public ClienteViewHolder(View itemView, final ClienteClickListener clickListener, String modo) {
            super(itemView);
            view = itemView;
            razaoSocialView = (TextView) view.findViewById(R.id.razaosocialCardView);
            cidadeView = (TextView) view.findViewById(R.id.cidadeCardView);
            celularView = (TextView) view.findViewById(R.id.celularCardView);
            cnpjCpfView = (TextView) view.findViewById(R.id.cnpjCpfCardView);
            inscEstView = (TextView) view.findViewById(R.id.inscEstCardView);
            imgClientePedido = (ImageView) view.findViewById(R.id.imgClientePedido);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        clickListener.onClienteClick(cliente);
                    }
                }
            });
            if (modo.equals(Constants.Modo.SELECAO)) {
                imgClientePedido.setVisibility(View.GONE);
            }
            this.view = itemView;
        }

        //@OnClick(R.id.imgClientePedido)
        void pedido(){
            PopupMenu popup = new PopupMenu(view.getContext(), imgClientePedido);
            popup.getMenuInflater().inflate(R.menu.cliente_list_item, popup.getMenu());
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(MenuItem item) {
                    Pedido pedido = new Pedido();
                    pedido.setIdCliente(cliente.getId());
                    PedidoFragment pedidoFragment = PedidoFragment.newInstance(pedido);
                    if (!pedidoFragment.isAdded()) {
                        FragmentTransaction ft = fragmentManager.beginTransaction();
                        ft.add(R.id.content_frame, pedidoFragment);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft.addToBackStack(null);
                        ft.commit();
                    }
                    return true;
                }
            });
            popup.show();
        }
    }

    public interface ClienteClickListener extends Serializable {
        void onClienteClick(Cliente cliente);
    }

}