package com.cladd.entities.api;

public class BaseConfigApi {
    private int configid;
    private String type;
    private String name;
    private String value;

    public int getConfigid() {
        return configid;
    }

    public void setConfigid(int configid) {
        this.configid = configid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
