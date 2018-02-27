package br.com.dsx.vendamais.domain;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;


@Entity(nameInDb = "ESTOQUE")
public class Estoque implements Serializable {

    private static final long serialVersionUID = -2575849285565519499L;

    @Id
    @Property(nameInDb = "ID_PRODUTO")
    private Long idProduto;

    @Property(nameInDb = "ID_EMPRESA")
    private Long idEmpresa;

    @Property(nameInDb = "ID_LOCAL_ESTOQUE")
    private Long idLocal;

    @Property(nameInDb = "QUANTIDADE")
    private Long quantidade;

    @Generated(hash = 548028489)
    public Estoque(Long idProduto, Long idEmpresa, Long idLocal, Long quantidade) {
        this.idProduto = idProduto;
        this.idEmpresa = idEmpresa;
        this.idLocal = idLocal;
        this.quantidade = quantidade;
    }

    @Generated(hash = 2063022435)
    public Estoque() {
    }

    public Long getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Long idProduto) {
        this.idProduto = idProduto;
    }

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public Long getIdLocal() {
        return idLocal;
    }

    public void setIdLocal(Long idLocal) {
        this.idLocal = idLocal;
    }

    public Long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Long quantidade) {
        this.quantidade = quantidade;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Estoque estoque = (Estoque) o;
        return getIdProduto().equals(estoque.getIdProduto());
    }

    @Override
    public int hashCode() {
        return getIdProduto().hashCode();
    }

    @Override
    public String toString() {
        return "Estoque{" +
                "idProduto=" + idProduto +
                ", idEmpresa=" + idEmpresa +
                ", idLocal=" + idLocal +
                ", quantidade=" + quantidade +
                '}';
    }
}