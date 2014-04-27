package com.buschmais.xo.neo4j.test.issues.not_a_proxy.composite;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

@Label("B")
public interface B {

    @Relation("B")
    @Incoming
    List<A> getA();

}
