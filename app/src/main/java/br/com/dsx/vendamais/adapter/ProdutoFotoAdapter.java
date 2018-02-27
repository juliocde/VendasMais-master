package br.com.dsx.vendamais.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.common.ImageUtil;
import br.com.dsx.vendamais.domain.FotoProduto;


public class ProdutoFotoAdapter extends RecyclerView.Adapter<ProdutoFotoAdapter.FotoProdutoViewHolder>
                                implements Serializable {

    private FotoProdutoClickListener clickListener;
    private List<FotoProduto> fotos;

    public ProdutoFotoAdapter(FotoProdutoClickListener clickListener) {
        this.clickListener = clickListener;
        this.fotos = new ArrayList<>();
    }

    @Override
    public FotoProdutoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.produto_foto_fragment, parent, false);
        return new FotoProdutoViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(final FotoProdutoViewHolder holder, int position) {
        FotoProduto foto = fotos.get(position);
        holder.foto = foto;
        ImageUtil.handleMedia(foto.getConteudo(), holder.fotoView);
    }

    @Override
    public int getItemCount() {
        return fotos.size();
    }

    public void setFotos(@NonNull List<FotoProduto> fotos) {
        this.fotos = fotos;
        notifyDataSetChanged();
    }

    public FotoProduto getFotoProduto(int position){
        return (fotos!=null)?fotos.get(position):null;
    }

    static class FotoProdutoViewHolder extends RecyclerView.ViewHolder {
        public ImageView fotoView;
        public FotoProduto foto;

        public FotoProdutoViewHolder(View itemView, final FotoProdutoClickListener clickListener) {
            super(itemView);
            fotoView = (ImageView) itemView.findViewById(R.id.produtoFotoCardView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        clickListener.onFotoProdutoClick(getAdapterPosition());
                    }
                }
            });
        }
    }

    public interface FotoProdutoClickListener extends Serializable {
        void onFotoProdutoClick(int position);
    }

}
