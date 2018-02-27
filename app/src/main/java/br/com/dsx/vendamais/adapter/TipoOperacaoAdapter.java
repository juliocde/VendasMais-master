package br.com.dsx.vendamais.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.Serializable;

import br.com.dsx.vendamais.R;
import br.com.dsx.vendamais.domain.TipoOperacao;


public class TipoOperacaoAdapter extends ArrayAdapter<TipoOperacao>
                                 implements Serializable {

    private TipoOperacao[] tipos;
    private Context context;
    private int layoutResourceId;
    private TipoOperacaoClickListener clickListener;

    public TipoOperacaoAdapter(Context context, int layoutResourceId,
                               TipoOperacao[] tipos, TipoOperacaoClickListener clickListener) {
        super(context, layoutResourceId, tipos);
        this.tipos = tipos;
        this.context = context;
        this.layoutResourceId = layoutResourceId;
        this.clickListener = clickListener;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        TipoOperacaoViewHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new TipoOperacaoViewHolder(row, clickListener);
            row.setTag(holder);
        } else {
            holder = (TipoOperacaoViewHolder) row.getTag();
        }

        TipoOperacao tipo = tipos[position];
        holder.tipoOperacao = tipo;
        holder.txtTitle.setText(tipo.toString());
        return row;
    }

    static class TipoOperacaoViewHolder {
        public TextView txtTitle;
        public TipoOperacao tipoOperacao;

        public TipoOperacaoViewHolder(final View view, final TipoOperacaoClickListener clickListener) {
            txtTitle = (TextView) view.findViewById(R.id.tipoOperacaoDescricao);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (clickListener != null) {
                        clickListener.onTipoOperacaoClick(tipoOperacao);
                    }
                }
            });
        }
    }

    public interface TipoOperacaoClickListener extends Serializable {
        void onTipoOperacaoClick(TipoOperacao tipoOperacao);
    }

}