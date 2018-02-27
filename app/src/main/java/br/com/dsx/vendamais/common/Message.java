package br.com.dsx.vendamais.common;

/**
 * Created by salazar on 04/05/17.
 */

public class Message {

    public interface Type {
        int ERROR = 1;
        int BUSINESS = 2;
        int SUCESS = 3;
    }

    private int type;
    private String value;
    private Long entityId;
    private String entityName;

    public Message(int type, String value) {
        this.value = value;
        this.type = type;
    }

    public Message(int type, String value, Long entityId, String entityName) {
        this.value = value;
        this.type = type;
        this.entityId = entityId;
        this.entityName = entityName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
}
