package br.com.dsx.vendamais.domain;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;


@Entity(nameInDb = "FORMA_PAGAMENTO")
public class FormaPagamento implements Serializable {

    private static final long serialVersionUID = -2575849285565519499L;

    @Id
    @Property(nameInDb = "ID_FORMA_PAGAMENTO")
    private Long id;

    @Property(nameInDb = "DESCRICAO")
    private String descricao;

    @Property(nameInDb = "TAXA")
    private Double taxa = 0.0;

    public FormaPagamento() {
    }

    @Generated(hash = 295952133)
    public FormaPagamento(Long id, String descricao, Double taxa) {
        this.id = id;
        this.descricao = descricao;
        this.taxa = taxa;
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

    public Double getTaxa() {
        return taxa;
    }

    public void setTaxa(Double taxa) {
        this.taxa = taxa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FormaPagamento that = (FormaPagamento) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "FormaPagamento{" +
                "id=" + id +
                ", descricao='" + descricao + '\'' +
                ", taxa='" + taxa + '\'' +
                '}';
    }
}