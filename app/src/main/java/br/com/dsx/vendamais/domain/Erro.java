package br.com.dsx.vendamais.domain;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.Property;

import java.io.Serializable;
import java.util.Date;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by salazar on 27/04/17.
 */

@Entity(nameInDb = "ERRO")
public class Erro implements Serializable {

    private static final long serialVersionUID = -6742681108956978825L;


    @Id
    @Property(nameInDb = "ID_ERRO")
    private Long id;

    @Property(nameInDb = "DESCRICAO")
    private String descricao;

    @Property(nameInDb = "LOGIN")
    private String login;

    @Property(nameInDb = "OS_VERSION")
    private String osVersion;

    @Property(nameInDb = "OS_API_LEVEL")
    private int osAPILevel;

    @Property(nameInDb = "DEVICE")
    private String device;

    @Property(nameInDb = "MODEL")
    private String model;

    @Property(nameInDb = "DATA")
    private Date data;

    @Keep
    public Erro() {
        osVersion = System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
        osAPILevel = android.os.Build.VERSION.SDK_INT;
        device =  android.os.Build.DEVICE;
        model = android.os.Build.MODEL + " ("+ android.os.Build.PRODUCT + ")";
    }

    @Keep
    public Erro(String erro) {
        this();
        this.descricao = erro;
        this.data = new Date();
    }

    @Generated(hash = 661176661)
    public Erro(Long id, String descricao, String login, String osVersion, int osAPILevel,
            String device, String model, Date data) {
        this.id = id;
        this.descricao = descricao;
        this.login = login;
        this.osVersion = osVersion;
        this.osAPILevel = osAPILevel;
        this.device = device;
        this.model = model;
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

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public int getOsAPILevel() {
        return osAPILevel;
    }

    public void setOsAPILevel(int osAPILevel) {
        this.osAPILevel = osAPILevel;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "Log{" +
                "id=" + id +
                ", descricao='" + descricao + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Erro log = (Erro) o;
        return getId() != null ? getId().equals(log.getId()) : log.getId() == null;

    }

}
