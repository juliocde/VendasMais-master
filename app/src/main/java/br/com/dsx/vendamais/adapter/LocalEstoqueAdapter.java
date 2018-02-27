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
import br.com.dsx.vendamais.domain.LocalEstoque;


public class LocalEstoqueAdapter extends RecyclerView.Adapter<LocalEstoqueAdapter.LocalEstoqueViewHolder>
                                 implements Serializable {

    private LocalEstoqueClickListener clickListener;
    private List<LocalEstoque> locaisEstoque;

    public LocalEstoqueAdapter(LocalEstoqueClickListener clickListener) {
        this.clickListener = clickListener;
        this.locaisEstoque = new ArrayList<>();
    }

    @Override
    public LocalEstoqueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.local_estoque_fragment_row, parent, false);
        return new LocalEstoqueViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(final LocalEstoqueViewHolder holder, int position) {
        LocalEstoque localEstoque = locaisEstoque.get(position);
        holder.localEstoque = localEstoque;
        holder.localEstoqueView.setText(localEstoque.getDescricao());
    }

    @Override
    public int getItemCount() {
        return locaisEstoque.size();
    }

    public void setLocalEstoques(@NonNull List<LocalEstoque> locaisEstoque) {
        this.locaisEstoque = locaisEstoque;
        notifyDataSetChanged();
    }


    public LocalEstoque getLocalEstoque(int position){
        return (locaisEstoque!=null)?locaisEstoque.get(position):null;
    }

    static class LocalEstoqueViewHolder extends RecyclerView.ViewHolder {

        public TextView localEstoqueView;
        public LocalEstoque localEstoque;

        public LocalEstoqueViewHolder(View itemView, final LocalEstoqueClickListener clickListener) {
            super(itemView);
            localEstoqueView = (TextView) itemView.findViewById(R.id.localEstoqueDescricaoCardView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        clickListener.onLocalEstoqueClick(localEstoque);
                    }
                }
            });
        }
    }

    public interface LocalEstoqueClickListener extends Serializable {
        void onLocalEstoqueClick(LocalEstoque localEstoque);
    }

}
