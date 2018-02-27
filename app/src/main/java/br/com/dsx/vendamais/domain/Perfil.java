package br.com.dsx.vendamais.domain;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by salazar on 29/03/17.
 */

@Entity(nameInDb = "PROFILE")
public class Perfil implements Serializable {

    private static final long serialVersionUID = -2948717045506848260L;

    @Id
    @Property(nameInDb = "ID_PROFILE")
    private Long id;

    @Property(nameInDb = "NOME")
    private String nome;

    @Property(nameInDb = "LOGIN")
    private String login;

    @Property(nameInDb = "SENHA")
    private String senha;

    @Property(nameInDb = "EMAIL")
    private String email;

    @Property(nameInDb = "FOTO")
    private String fotoUri;

    @Property(nameInDb = "TIPO")
    private Integer tipo;

    @Property(nameInDb = "URL_SANKHYA")
    private String urlSankhya;

    @Property(nameInDb = "LOGIN_SANKHYA")
    private String loginSankhya;

    @Property(nameInDb = "SENHA_SANKHYA")
    private String senhaSankhya;

    @Property(nameInDb = "LEMBRAR_SENHA")
    private boolean lembrarSenha;

    @Property(nameInDb = "LOGRADO")
    private boolean logado;

    @Property(nameInDb = "ID_EMPRESA")
    private Long idEmpresa;

    @Property(nameInDb = "ID_LOCAL_ESTOQUE")
    private Long idLocalEstoque;

    public Perfil() {
    }

    @Generated(hash = 671821446)
    public Perfil(Long id, String nome, String login, String senha, String email,
            String fotoUri, Integer tipo, String urlSankhya, String loginSankhya,
            String senhaSankhya, boolean lembrarSenha, boolean logado,
            Long idEmpresa, Long idLocalEstoque) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senha = senha;
        this.email = email;
        this.fotoUri = fotoUri;
        this.tipo = tipo;
        this.urlSankhya = urlSankhya;
        this.loginSankhya = loginSankhya;
        this.senhaSankhya = senhaSankhya;
        this.lembrarSenha = lembrarSenha;
        this.logado = logado;
        this.idEmpresa = idEmpresa;
        this.idLocalEstoque = idLocalEstoque;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFotoUri() {
        return fotoUri;
    }

    public void setFotoUri(String fotoUri) {
        this.fotoUri = fotoUri;
    }

    public String getUrlSankhya() {
        return urlSankhya;
    }

    public void setUrlSankhya(String urlSankhya) {
        this.urlSankhya = urlSankhya;
    }

    public String getLoginSankhya() {
        return loginSankhya;
    }

    public void setLoginSankhya(String loginSankhya) {
        this.loginSankhya = loginSankhya;
    }

    public String getSenhaSankhya() {
        return senhaSankhya;
    }

    public void setSenhaSankhya(String senhaSankhya) {
        this.senhaSankhya = senhaSankhya;
    }

    public boolean isLembrarSenha() {
        return lembrarSenha;
    }

    public void setLembrarSenha(boolean lembrarSenha) {
        this.lembrarSenha = lembrarSenha;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

    public boolean getLembrarSenha() {
        return this.lembrarSenha;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public Integer getTipo() {
        if (tipo==null) {
            tipo = 0;
        }
        return tipo;
    }

    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }

    public boolean isLogado() {
        return logado;
    }

    public void setLogado(boolean logado) {
        this.logado = logado;
    }

    public boolean getLogado() {
        return this.logado;
    }

    public Long getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    public Long getIdLocalEstoque() {
        return idLocalEstoque;
    }

    public void setIdLocalEstoque(Long idLocalEstoque) {
        this.idLocalEstoque = idLocalEstoque;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Perfil perfil = (Perfil) o;
        return id != null ? id.equals(perfil.id) : perfil.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
