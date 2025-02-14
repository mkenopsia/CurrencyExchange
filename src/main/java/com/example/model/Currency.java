package com.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Currency {
    private int id;
    private String code;
    private String name;
    private String sign;

    public Currency() {
    }

    @JsonCreator
    public Currency(@JsonProperty("id") int id, @JsonProperty("code") String code, @JsonProperty("name") String name, @JsonProperty("sign") String sign) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.sign = sign;
    }

//    public Currency(String code, String fullname, String sign) {
//        this.code = code;
//        this.fullname = fullname;
//        this.sign = sign;
//    }

//    public Currency(int id, String code, String fullname, String sign) {
//        this.id = id;
//        this.code = code;
//        this.fullname = fullname;
//        this.sign = sign;
//    }

    @Override
    public String toString() {
        return "Currency{" +
                "id=" + id +
                ", code='" + code + '\'' +
                ", fullname='" + name + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String fullname) {
        this.name = fullname;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

}
