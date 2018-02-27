package br.com.dsx.vendamais.domain;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.com.dsx.vendamais.common.Util;


@Entity(nameInDb = "PRODUTO")
public class Produto implements Serializable {

    private static final long serialVersionUID = -2575849285565519499L;

    @Id
    @Property(nameInDb = "ID_PRODUTO")
    private Long id;

    @Property(nameInDb = "DESCRICAO")
    private String descricao;

    @Property(nameInDb = "COMPLEMENTO")
    private String complemento;

    @Property(nameInDb = "PRECO")
    private Double preco;

    @Property(nameInDb = "CODIGO_VOLUME")
    private String codigoVolume;

    @Property(nameInDb = "DESCRICAO_VOLUME")
    private String descricaoVolume;

    @Property(nameInDb = "CATEGORIA")
    private String categoria;

    @Property(nameInDb = "MARCA")
    private String marca;

    @Property(nameInDb = "FOTO_PRINCIPAL")
    private byte[] fotoPrincipal;

    @Property(nameInDb = "REFERENCIA_FORNECEDOR")
    private String referenciaFornecedor;

    @Transient
    private List<FotoProduto> fotos;

    @Transient
    private Double quantidade;

    @Transient
    private Long estoque;

    public Produto() {
    }

    @Keep
    public Produto(Long id) {
        this.id = id;
    }

    @Generated(hash = 1966212031)
    public Produto(Long id, String descricao, String complemento, Double preco,
            String codigoVolume, String descricaoVolume, String categoria,
            String marca, byte[] fotoPrincipal, String referenciaFornecedor) {
        this.id = id;
        this.descricao = descricao;
        this.complemento = complemento;
        this.preco = preco;
        this.codigoVolume = codigoVolume;
        this.descricaoVolume = descricaoVolume;
        this.categoria = categoria;
        this.marca = marca;
        this.fotoPrincipal = fotoPrincipal;
        this.referenciaFornecedor = referenciaFornecedor;
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

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public Double getPreco() {
        return preco;
    }

    @Keep
    public String getPrecoAux(){
        return Util.doubleToValorMonetaria(preco);
    }

    public void setPreco(Double preco) {
        this.preco = preco;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public byte[] getFotoPrincipal() {
        return fotoPrincipal;
    }

    public void setFotoPrincipal(byte[] fotoPrincipal) {
        this.fotoPrincipal = fotoPrincipal;
    }

    public String getCodigoVolume() {
        return codigoVolume;
    }

    public void setCodigoVolume(String codigoVolume) {
        this.codigoVolume = codigoVolume;
    }

    public String getDescricaoVolume() {
        return descricaoVolume;
    }

    public void setDescricaoVolume(String descricaoVolume) {
        this.descricaoVolume = descricaoVolume;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public void setFotos(List<FotoProduto> fotos) {
        this.fotos = fotos;
    }

    public List<FotoProduto> getFotos() {
        if (fotos == null) {
            fotos = new ArrayList<>();
        }
        return fotos;
    }

    public Double getQuantidade() {
        if (quantidade==null) {
            quantidade = 0.0;
        }
        return quantidade;
    }

    public void setQuantidade(Double quantidade) {
        this.quantidade = quantidade;
    }

    public String getReferenciaFornecedor() {
        return referenciaFornecedor;
    }

    public void setReferenciaFornecedor(String referenciaFornecedor) {
        this.referenciaFornecedor = referenciaFornecedor;
    }

    @Keep
    public String getDescricaoEstoque(){
        return getEstoque()+" "+getDescricaoVolume()+"(S)";
    }

    public Long getEstoque() {
        if (estoque==null) {
            estoque = 0L;
        }
        return estoque;
    }

    public void setEstoque(Long estoque) {
        this.estoque = estoque;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        return getId().equals(produto.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "Produto{" +
                "categoria='" + categoria + '\'' +
                ", preco='" + preco + '\'' +
                ", estoque='" + estoque + '\'' +
                ", complemento='" + complemento + '\'' +
                ", descricao='" + descricao + '\'' +
                ", id=" + id +
                '}';
    }
}

