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
import br.com.dsx.vendamais.domain.Empresa;


public class EmpresaAdapter extends RecyclerView.Adapter<EmpresaAdapter.EmpresaViewHolder>
                            implements Serializable {

    private EmpresaClickListener clickListener;
    private List<Empresa> empresas;

    public EmpresaAdapter(EmpresaClickListener clickListener) {
        this.clickListener = clickListener;
        this.empresas = new ArrayList<>();
    }

    @Override
    public EmpresaViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.empresa_fragment_row, parent, false);
        return new EmpresaViewHolder(view, clickListener);
    }

    @Override
    public void onBindViewHolder(final EmpresaViewHolder holder, int position) {
        Empresa empresa = empresas.get(position);
        holder.empresa = empresa;
        holder.nomeFantasiaView.setText(empresa.getNomeFantasia());
    }

    @Override
    public int getItemCount() {
        return empresas.size();
    }

    public void setEmpresas(@NonNull List<Empresa> empresas) {
        this.empresas = empresas;
        notifyDataSetChanged();
    }


    public Empresa getEmpresa(int position){
        return (empresas!=null)?empresas.get(position):null;
    }

    static class EmpresaViewHolder extends RecyclerView.ViewHolder {

        public TextView nomeFantasiaView;
        public Empresa empresa;

        public EmpresaViewHolder(View itemView, final EmpresaClickListener clickListener) {
            super(itemView);
            nomeFantasiaView = (TextView) itemView.findViewById(R.id.empresaDescricaoCardView);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (clickListener != null) {
                        clickListener.onEmpresaClick(empresa);
                    }
                }
            });
        }
    }

    public interface EmpresaClickListener extends Serializable {
        void onEmpresaClick(Empresa empresa);
    }

}
