package br.com.dsx.vendamais.domain;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;
import java.util.Date;

import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.Util;

@Entity(nameInDb="PEDIDO")
public class Pedido implements Serializable {

    private static final long serialVersionUID = -357832620454057448L;

    @Id
    @Property(nameInDb = "ID_PEDIDO")
    private Long id;

    @Property(nameInDb = "ID_CLIENTE")
    private Long idCliente;

    @Property(nameInDb = "NUMERO_UNICO")
    private int numeroUnico;

    @Property(nameInDb = "DATA_INCLUSAO")
    private Date dataInclusao;

    @Property(nameInDb = "ID_FORMA_PAGAMENTO")
    private Long idFormaPagamento;

    @Property(nameInDb = "ID_EMPRESA")
    private Long idEmpresa;

    @Property(nameInDb = "ID_LOCAL_ESTOQUE")
    private Long idLocalEstoque;

    @Property(nameInDb = "ID_TIPO_OPERACAO")
    private Long idTipoOperacao;

    @Property(nameInDb = "OBSERVACAO")
    private String observacao;

    @Property(nameInDb = "VALOR_TOTAL")
    private Double valorTotal;

    @Property(nameInDb = "QUANTIDADE_ITENS")
    private Integer quantidadeItens;

    @Property(nameInDb = "DADOS")
    private String dados;

    @Transient
    private Cliente cliente;

    public Pedido() {
    }



    @Generated(hash = 1090531539)
    public Pedido(Long id, Long idCliente, int numeroUnico, Date dataInclusao,
            Long idFormaPagamento, Long idEmpresa, Long idLocalEstoque,
            Long idTipoOperacao, String observacao, Double valorTotal,
            Integer quantidadeItens, String dados) {
        this.id = id;
        this.idCliente = idCliente;
        this.numeroUnico = numeroUnico;
        this.dataInclusao = dataInclusao;
        this.idFormaPagamento = idFormaPagamento;
        this.idEmpresa = idEmpresa;
        this.idLocalEstoque = idLocalEstoque;
        this.idTipoOperacao = idTipoOperacao;
        this.observacao = observacao;
        this.valorTotal = valorTotal;
        this.quantidadeItens = quantidadeItens;
        this.dados = dados;
    }

    @Keep
    public boolean isNew(){
        return getId()==null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }

    public Date getDataInclusao() {
        return dataInclusao;
    }

    public void setDataInclusao(Date dataInclusao) {
        this.dataInclusao = dataInclusao;
    }

    public Long getIdFormaPagamento() {
        return idFormaPagamento;
    }

    public void setIdFormaPagamento(Long idFormaPagamento) {
        this.idFormaPagamento = idFormaPagamento;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getDados() {
        return dados;
    }

    public void setDados(String dados) {
        this.dados = dados;
    }

    @Keep
    public Cliente getCliente() {
        if (cliente==null){
            cliente = new Cliente();
        }
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    @Keep
    public Double getValorTotal() {
        if (valorTotal==null) {
            valorTotal = 0.0;
        }
        return valorTotal;
    }

    public void setValorTotal(Double valorTotal) {
        this.valorTotal = valorTotal;
    }

    public Integer getQuantidadeItens() {
        return quantidadeItens;
    }

    public void setQuantidadeItens(Integer quantidadeItens) {
        this.quantidadeItens = quantidadeItens;
    }

    public int getNumeroUnico() {
        return numeroUnico;
    }

    public void setNumeroUnico(int numeroUnico) {
        this.numeroUnico = numeroUnico;
    }

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    @Keep
    public Long getIdLocalEstoque() {
        if (idLocalEstoque==null) {
            idLocalEstoque = 0L;
        }
        return idLocalEstoque;
    }

    public void setIdLocalEstoque(Long idLocalEstoque) {
        this.idLocalEstoque = idLocalEstoque;
    }

    public Long getIdTipoOperacao() {
        return idTipoOperacao;
    }

    public void setIdTipoOperacao(Long idTipoOperacao) {
        this.idTipoOperacao = idTipoOperacao;
    }

    public String toValidationString() {
        StringBuilder string = new StringBuilder();
        string.append("Pedido ["+Util.dateToString("dd/MM/yyyy", dataInclusao)+"]");
        return string.toString();
    }

    public String toString(){
        return "Cliente: " + idCliente
                    + "\tNro Único: " + numeroUnico
                    + "\tVlr Total: " + valorTotal;
    }

    public void configuraDados(){
        StringBuilder sb = new StringBuilder();
        sb.append(Util.dateToString(Constants.DateFormat.DATA,dataInclusao)+" ");
        sb.append(getCliente().getNomeRazaoSocial());
        setDados(sb.toString());
    }
}
