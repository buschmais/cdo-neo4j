package com.buschmais.xo.neo4j.test.relation.implicit.composite;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

import com.buschmais.xo.neo4j.api.annotation.Relation;

@Relation
@Retention(RUNTIME)
public @interface ImplicitOneToOne {
}
