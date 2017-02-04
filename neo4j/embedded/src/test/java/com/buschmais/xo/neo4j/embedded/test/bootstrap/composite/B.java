package com.buschmais.xo.neo4j.embedded.test.bootstrap.composite;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("B")
public interface B {

    @Outgoing
    B2B getOutgoingB2B();

    @Incoming
    B2B getIncomingB2B();
}
