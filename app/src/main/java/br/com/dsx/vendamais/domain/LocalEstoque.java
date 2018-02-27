package br.com.dsx.vendamais.domain;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;


@Entity(nameInDb = "LOCAL_ESTOQUE")
public class LocalEstoque implements Serializable {

    private static final long serialVersionUID = 2014328512858816076L;

    @Id
    @Property(nameInDb = "ID_LOCAL_ESTOQUE")
    private Long id;

    @Property(nameInDb = "DESCRICAO")
    private String descricao;

    public LocalEstoque() {
    }

    @Generated(hash = 841438873)
    public LocalEstoque(Long id, String descricao) {
        this.id = id;
        this.descricao = descricao;
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

        LocalEstoque that = (LocalEstoque) o;
        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "LocalEstoque{" +
                "id=" + id +
                ", descricao='" + descricao + '\'' +
                '}';
    }
}