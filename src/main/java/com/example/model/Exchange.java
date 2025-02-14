package com.example.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Exchange {
    int id;
    Currency baseCurrency;
    Currency targetCurrency;
    double rate;

    @JsonCreator
    public Exchange(@JsonProperty("id") int id, @JsonProperty("baseCurrency") Currency base, @JsonProperty("targetCurrency") Currency target, @JsonProperty("rate") double rate) {
        this.id = id;
        this.baseCurrency = base;
        this.targetCurrency = target;
        this.rate = rate;
    }

    public Exchange() {}
//
//    public Exchange(int id, Currency baseCurrency, Currency targetCurrency, double rate) {
//        this.id = id;
//        this.baseCurrency = baseCurrency;
//        this.targetCurrency = targetCurrency;
//        this.rate = rate;
//    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Currency getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(Currency baseCurrency) {
        this.baseCurrency = baseCurrency;
    }

    public Currency getTargetCurrency() {
        return targetCurrency;
    }

    public void setTargetCurrency(Currency targetCurrency) {
        this.targetCurrency = targetCurrency;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }
}
