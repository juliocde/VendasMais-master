package br.com.dsx.vendamais.domain;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;
import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by salazar on 14/03/17.
 */
@Entity(nameInDb = "CONFIGURACAO")
public class Configuracao implements Serializable {

    private static final long serialVersionUID = 4329545543892352218L;

    @Id
    @Property(nameInDb = "ID_CONFIGURACAO")
    private Long id;

    @Property(nameInDb = "DESCRICAO")
    private String descricao;

    @Property(nameInDb = "TEXTO")
    private String texto;

    @Property(nameInDb = "NUMERO")
    private Long numero;

    @Property(nameInDb = "DATA")
    private Date data;

    public Configuracao() {
    }

    @Generated(hash = 437103357)
    public Configuracao(Long id, String descricao, String texto, Long numero,
            Date data) {
        this.id = id;
        this.descricao = descricao;
        this.texto = texto;
        this.numero = numero;
        this.data = data;
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

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Configuracao that = (Configuracao) o;
        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "Configuracao{" +
                "id='" + id + '\'' +
                ", descricao='" + descricao + '\'' +
                ", texto='" + texto + '\'' +
                ", numero=" + numero +
                ", data=" + data +
                '}';
    }
}
