package com.buschmais.cdo.neo4j.impl.datastore.metadata;

public class PrimitivePropertyMetadata {

    private final String name;

    public PrimitivePropertyMetadata(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
