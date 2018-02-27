package br.com.dsx.vendamais.adapter;

import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.common.Message;
import br.com.dsx.vendamais.dao.ClienteDao;
import br.com.dsx.vendamais.dao.DaoSession;
import br.com.dsx.vendamais.dao.PedidoDao;
import br.com.dsx.vendamais.domain.Cliente;
import br.com.dsx.vendamais.domain.Pedido;
import br.com.dsx.vendamais.fragment.ClienteFragment;
import br.com.dsx.vendamais.fragment.PedidoFragment;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
                            implements Serializable {

    public static FragmentManager fragmentManager;
    public transient static DaoSession dao;
    private List<Message> messages;

    public MessageAdapter(FragmentManager fm, DaoSession dao) {
        this.messages = new ArrayList<>();
        this.fragmentManager = fm;
        this.dao = dao;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.empresa_fragment_row, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.message = message;
        holder.msgView.setText(message.getValue());

        int color = Color.TRANSPARENT;
        if (message.getType()==Message.Type.ERROR){
            color = ContextCompat.getColor(holder.view.getContext(), R.color.colorError);
        } else if (message.getType()==Message.Type.BUSINESS){
            color = ContextCompat.getColor(holder.view.getContext(), R.color.colorBusiness);
        }
        holder.view.setBackgroundColor(color);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(@NonNull List<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }


    public Message getMessage(int position){
        return (messages!=null)?messages.get(position):null;
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.empresaDescricaoCardView) TextView msgView;
        public Message message;
        public View view;

        public MessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            view = itemView;
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                if (message.getType()==Message.Type.BUSINESS){
                    if (Cliente.class.getSimpleName().equals(message.getEntityName())) {
                        ClienteDao clienteDao = dao.getClienteDao();
                        Cliente cliente = clienteDao.load(message.getEntityId());
                        ClienteFragment fragment = ClienteFragment.newInstance(cliente);
                        if (!fragment.isAdded()) {
                            FragmentTransaction ft = fragmentManager.beginTransaction();
                            ft.add(R.id.content_frame, fragment);
                            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                            ft.addToBackStack(null);
                            ft.commit();
                        }
                    } else if (Pedido.class.getSimpleName().equals(message.getEntityName())) {
                        PedidoDao pedidoDao = dao.getPedidoDao();
                        Pedido pedido = pedidoDao.load(message.getEntityId());
                        PedidoFragment fragment = PedidoFragment.newInstance(pedido);
                        if (!fragment.isAdded()) {
                            FragmentTransaction ft = fragmentManager.beginTransaction();
                            ft.add(R.id.content_frame, fragment);
                            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                            ft.addToBackStack(null);
                            ft.commit();
                        }
                    }
                }
                }
            });
        }
    }

    public interface MessageClickListener extends Serializable {
        void onMessageClick(Cliente cliente);
    }

}
