package br.com.dsx.vendamais.domain;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;


@Entity(nameInDb = "TIPO_OPERACAO")
public class TipoOperacao implements Serializable {

    private static final long serialVersionUID = 2014328512858816076L;

    @Id
    @Property(nameInDb = "ID_TIPO_OPERACAO")
    private Long id;

    @Property(nameInDb = "DESCRICAO")
    private String descricao;

    @Property(nameInDb = "RESERVA")
    private String reserva;

    public TipoOperacao() {
    }

    @Generated(hash = 218288714)
    public TipoOperacao(Long id, String descricao, String reserva) {
        this.id = id;
        this.descricao = descricao;
        this.reserva = reserva;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TipoOperacao that = (TipoOperacao) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return id +" - " + descricao;
    }

    public String getReserva() {
        return this.reserva;
    }

    public void setReserva(String reserva) {
        this.reserva = reserva;
    }
}