package com.foo.processor;

import java.io.Serializable;

import java.util.Collection;
import java.util.Set;

// this is the pojo which will be used to represent your data
public class TProcDefaultOutput implements Serializable {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}