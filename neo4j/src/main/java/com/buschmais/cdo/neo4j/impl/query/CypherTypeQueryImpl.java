package com.buschmais.cdo.neo4j.impl.query;

import com.buschmais.cdo.api.CdoException;
import com.buschmais.cdo.neo4j.api.annotation.Cypher;
import com.buschmais.cdo.neo4j.impl.node.InstanceManager;
import com.buschmais.cdo.neo4j.spi.DatastoreSession;

import java.util.ArrayList;
import java.util.List;

public class CypherTypeQueryImpl extends AbstractCypherQueryImpl<Class<?>> {

    public CypherTypeQueryImpl(Class<?> expression, DatastoreSession datastoreSession, InstanceManager instanceManager, List<Class<?>> types) {
        super(expression, datastoreSession, instanceManager, types);
    }

    @Override
    protected String getQuery() {
        Class<?> expression = getExpression();
        Cypher cypher = expression.getAnnotation(Cypher.class);
        if (cypher == null) {
            throw new CdoException("Type '" + expression.getName() + "' is not annotated with '" + Cypher.class.getName() + "'");
        }
        return cypher.value();
    }

    @Override
    protected List<Class<?>> getResultTypes(Class<?> expression, List<Class<?>> types) {
        List<Class<?>> resultTypes = new ArrayList<>(types);
        resultTypes.add(expression);
        return resultTypes;
    }
}
