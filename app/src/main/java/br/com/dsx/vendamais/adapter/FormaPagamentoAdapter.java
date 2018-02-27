package br.com.dsx.vendamais.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.domain.FormaPagamento;


public class FormaPagamentoAdapter  extends RecyclerView.Adapter<FormaPagamentoAdapter.FormaPagamentoViewHolder>
                                    implements Serializable {

    private FormaPagamentoClickListener clickListener;
    private List<FormaPagamento> formasPagamento;

    public FormaPagamentoAdapter(FormaPagamentoClickListener clickListener) {
        this.clickListener = clickListener;
        this.formasPagamento = new ArrayList<>();
    }

    @Override
    public FormaPagamentoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.forma_pagamento_fragment_row, parent, false);
        return new FormaPagamentoViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(final FormaPagamentoViewHolder holder, int position) {
        FormaPagamento formaPagamento = formasPagamento.get(position);
        holder.formaPagamento = formaPagamento;
        holder.descricaoView.setText(formaPagamento.getDescricao());
    }

    @Override
    public int getItemCount() {
        return formasPagamento.size();
    }

    public void setFormaPagamentos(@NonNull List<FormaPagamento> formasPagamento) {
        this.formasPagamento = formasPagamento;
        notifyDataSetChanged();
    }


    public FormaPagamento getFormaPagamento(int position){
        return (formasPagamento!=null)?formasPagamento.get(position):null;
    }

    static class FormaPagamentoViewHolder extends RecyclerView.ViewHolder {
        public TextView descricaoView;
        public FormaPagamento formaPagamento;

        public FormaPagamentoViewHolder(View itemView, final FormaPagamentoClickListener clickListener) {
            super(itemView);
            descricaoView = (TextView) itemView.findViewById(R.id.formaPagamentoDescricaoCardView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        clickListener.onFormaPagamentoClick(formaPagamento);
                    }
                }
            });
        }
    }

    public interface FormaPagamentoClickListener extends Serializable {
        void onFormaPagamentoClick(FormaPagamento formaPagamento);
    }

}
