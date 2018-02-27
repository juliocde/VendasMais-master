package br.com.dsx.vendamais.domain;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;

import br.com.dsx.vendamais.common.Util;


@Entity(nameInDb="ITEM_PRODUTO")
public class ItemPedido implements Serializable {

    private static final long serialVersionUID = -3827118323028722668L;

    @Id
    @Property(nameInDb = "ID_ITEM_PEDIDO")
    private Long id;

    @Property(nameInDb = "ID_PEDIDO")
    private Long idPedido;

    @Property(nameInDb = "ID_PRODUTO")
    private Long idProduto;

    @Property(nameInDb = "QUANTIDADE")
    private Double quantidade;

    @Property(nameInDb = "VALOR_UNITARIO")
    private Double valorUnitario;

    @Property(nameInDb = "VALOR_TAXA")
    private Double taxa;

    @Property(nameInDb = "DESCONTO")
    private Double desconto;

    @Transient
    private Produto produto;

    @Transient
    private Double valorTotal;

    public ItemPedido(){

    }

    @Keep
    public ItemPedido(Long id, Long idPedido, Long idProduto, Double quantidade, Double valorUnitario) {
        this.id = id;
        this.idPedido = idPedido;
        this.idProduto = idProduto;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
    }

    @Generated(hash = 1151304037)
    public ItemPedido(Long id, Long idPedido, Long idProduto, Double quantidade, Double valorUnitario,
            Double taxa, Double desconto) {
        this.id = id;
        this.idPedido = idPedido;
        this.idProduto = idProduto;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.taxa = taxa;
        this.desconto = desconto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(Long idPedido) {
        this.idPedido = idPedido;
    }

    public Long getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Long idProduto) {
        this.idProduto = idProduto;
    }

    public Double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Double quantidade) {
        this.quantidade = quantidade;
    }

    public Double getValorUnitario() {
        return valorUnitario;
    }

    public Double getDesconto() {
        return desconto;
    }

    public void setDesconto(Double desconto) {
        this.desconto = desconto;
    }

    @Keep
    public String getValorTaxadoAux(){
        return Util.doubleToValorMonetaria(getValorTaxado());
    }

    @Keep
    public Double getValorTaxado(){
        return getValorUnitario()+(getValorUnitario()*getTaxa()/100);
    }

    public void setValorUnitario(Double valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public Double getValorTotal() {
        if (valorTotal==null) {
            valorTotal=0.0;
        }
        return valorTotal;
    }

    public Double getTaxa() {
        if (taxa==null) {
            taxa=0.0;
        }
        return taxa;
    }

    public void setTaxa(Double taxa) {
        this.taxa = taxa;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemPedido that = (ItemPedido) o;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        return idProduto != null ? idProduto.equals(that.idProduto) : that.idProduto == null;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "ItemPedido{" +
                "id=" + id +
                ", valorUnitario=" + valorUnitario +
                ", quantidade=" + quantidade +
                ", idPedido=" + idPedido +
                ", idProduto=" + idProduto +
                '}';
    }

    public String toValidationString() {
        StringBuilder string = new StringBuilder();
        string.append("Item ["+idProduto+"]");
        return string.toString();
    }
}

