package br.com.dsx.vendamais.domain;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;

import br.com.dsx.vendamais.common.Constants;
import br.com.dsx.vendamais.common.Util;

@Entity(nameInDb="CLIENTE")
public class Cliente implements Serializable {

    private static final long serialVersionUID = 1571130157037390317L;

    @Id
    @Property(nameInDb = "ID_CLIENTE")
    private Long id;

    @Property(nameInDb = "NOME_RAZAO_SOCIAL")
    private String nomeRazaoSocial;

    @Property(nameInDb = "NOME_FANTASIA")
    private String nomeFantasia;

    @Property(nameInDb = "INSCRICAO_ESTADUAL")
    private String inscricaoEstadual;

    @Property(nameInDb = "CPF_CNPJ")
    private String cpfCnpj;

    @Property(nameInDb = "EMAIL")
    private String email;

    @Property(nameInDb = "CELULAR")
    private String celular;

    @Property(nameInDb = "TIPO")
    private int tipo;

    @Property(nameInDb = "CEP")
    private String cep;

    @Property(nameInDb = "NUMERO")
    private String numero;

    @Property(nameInDb = "COMPLEMENTO")
    private String complemento;

    @Property(nameInDb = "LOGRADOURO")
    private String logradouro;

    @Property(nameInDb = "TIPOLOGRADOURO")
    private String tipoLogradouro;

    @Property(nameInDb = "BAIRRO")
    private String bairro;

    @Property(nameInDb = "CIDADE")
    private String cidade;

    @Property(nameInDb = "UF")
    private String uf;

    @Property(nameInDb = "CODPARC_SK")
    private int codParcSk;

    @Property(nameInDb = "CODUF")
    private int codUf;

    @Property(nameInDb = "LOGRADOURO_AUTOCOMPLETE")
    private boolean logradouroAutocomplete;

    @Transient
    private String cepAux;

    @Transient
    private String celularAux;

    @Transient
    private String cpfCnpjAux;

    public Cliente() {
    }

    @Generated(hash = 422290136)
    public Cliente(Long id, String nomeRazaoSocial, String nomeFantasia,
            String inscricaoEstadual, String cpfCnpj, String email, String celular,
            int tipo, String cep, String numero, String complemento,
            String logradouro, String tipoLogradouro, String bairro, String cidade,
            String uf, int codParcSk, int codUf, boolean logradouroAutocomplete) {
        this.id = id;
        this.nomeRazaoSocial = nomeRazaoSocial;
        this.nomeFantasia = nomeFantasia;
        this.inscricaoEstadual = inscricaoEstadual;
        this.cpfCnpj = cpfCnpj;
        this.email = email;
        this.celular = celular;
        this.tipo = tipo;
        this.cep = cep;
        this.numero = numero;
        this.complemento = complemento;
        this.logradouro = logradouro;
        this.tipoLogradouro = tipoLogradouro;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf;
        this.codParcSk = codParcSk;
        this.codUf = codUf;
        this.logradouroAutocomplete = logradouroAutocomplete;
    }

    @Keep
    public boolean isEditable(){
        return getCodParcSk()==0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeRazaoSocial() {
        return nomeRazaoSocial;
    }

    public void setNomeRazaoSocial(String nomeRazaoSocial) {
        this.nomeRazaoSocial = nomeRazaoSocial;
    }

    public String getInscricaoEstadual() {
        return inscricaoEstadual;
    }

    public void setInscricaoEstadual(String inscricaoEstadual) {
        this.inscricaoEstadual = inscricaoEstadual;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public int getTipo() {
        return this.tipo;
    }

    public void setTipo(int tipo) {
        this.tipo = tipo;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getCidade() {
        return cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cliente cliente = (Cliente) o;
        return getId().equals(cliente.getId());

    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "Cliente{" +
                "uf='" + uf + '\'' +
                ", cidade='" + cidade + '\'' +
                ", tipo=" + tipo +
                ", celular='" + celular + '\'' +
                ", email='" + email + '\'' +
                ", cep='" + cep + '\'' +
                ", cpfCnpj='" + cpfCnpj + '\'' +
                ", nomeFantasia='" + nomeFantasia + '\'' +
                ", nomeRazaoSocial='" + nomeRazaoSocial + '\'' +
                ", cofUF='" + codUf + '\'' +
                ", tipoLogradouro='" + tipoLogradouro + '\'' +
                ", id=" + id +
                '}';
    }

    public String toValidationString() {
        StringBuilder string = new StringBuilder();
        string.append("Cliente ["+nomeRazaoSocial+"] ");
        return string.toString();
    }

    public String getCelularAux() {
        if (Util.isBlank(celularAux) && Util.isNotBlank(celular)) {
            if (celular.length()==11) {
                celularAux = Util.applyMask(Constants.Mask.TELEFONE, celular);
            } else if (celular.length()>9) {
                celularAux = Util.applyMask(Constants.Mask.CELULAR, celular);
            } else {
                celularAux = celular;
            }
        }
        return celularAux;
    }

    public void setCelularAux(String celularAux) {
        this.celularAux = celularAux;
        if (celularAux!=null) {
            if (celularAux.startsWith("9969")) {
                System.out.print("1");
            } else if (celularAux.startsWith("99 69")) {
                System.out.print("1");
            }
        }
        if (celularAux == null) setCelular(Util.STRING_EMPTY);
        else setCelular(celularAux.replaceAll("\\D+", Util.STRING_EMPTY));
    }

    public String getCepAux() {
        if (Util.isBlank(cepAux)) {
            cepAux = cep;
        }
        return cepAux;
    }

    public void setCepAux(String cepAux) {
        this.cepAux = cepAux;
        if (cepAux == null) setCep(Util.STRING_EMPTY);
        else setCep(cepAux.replaceAll("\\D+", Util.STRING_EMPTY));
    }

    public String getCpfCnpjAux() {
        if (Util.isBlank(cpfCnpjAux) && Util.isNotBlank(cpfCnpj)) {
            if (cpfCnpj.length()==14) {
                cpfCnpjAux = Util.applyMask(Constants.Mask.CNPJ, cpfCnpj);
            } else if (cpfCnpj.length()==11) {
                cpfCnpjAux = Util.applyMask(Constants.Mask.CPF, cpfCnpj);
            } else {
                cpfCnpjAux = cpfCnpj;
            }
        }
        return cpfCnpjAux;
    }

    public void setCpfCnpjAux(String cpfCnpjAux) {
        this.cpfCnpjAux = cpfCnpjAux;
        if (cpfCnpjAux == null) setCpfCnpj(Util.STRING_EMPTY);
        else setCpfCnpj(cpfCnpjAux.replaceAll("\\D+", Util.STRING_EMPTY));
    }

    public int getCodParcSk() {
        return this.codParcSk;
    }

    public void setCodParcSk(int codParcSk) {
        this.codParcSk = codParcSk;
    }

    public String getTipoLogradouro() {
        return this.tipoLogradouro;
    }

    public void setTipoLogradouro(String tipoLogradouro) {
        this.tipoLogradouro = tipoLogradouro;
    }

    public int getCodUf() {
        return this.codUf;
    }

    public void setCodUf(int codUf) {
        this.codUf = codUf;
    }

    public void setCodParcSk(Integer codParcSk) {
        this.codParcSk = codParcSk;
    }

    public boolean isLogradouroAutocomplete() {
        return logradouroAutocomplete;
    }

    public void setLogradouroAutocomplete(boolean logradouroAutocomplete) {
        this.logradouroAutocomplete = logradouroAutocomplete;
    }

    public boolean getLogradouroAutocomplete() {
        return this.logradouroAutocomplete;
    }
}
