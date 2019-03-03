package com.buschmais.xo.impl.proxy.relation.object;

import com.buschmais.xo.api.proxy.ProxyMethod;
import com.buschmais.xo.impl.SessionContext;
import com.buschmais.xo.spi.datastore.DatastoreEntityMetadata;
import com.buschmais.xo.spi.datastore.DatastoreRelationMetadata;
import com.buschmais.xo.spi.datastore.DatastoreSession;
import com.buschmais.xo.spi.session.InstanceManager;

public class EqualsMethod<Relation> implements ProxyMethod<Relation> {

    private final SessionContext<?, ?, ?, ?, ?, Relation, ?, ?, ?> sessionContext;

    public EqualsMethod(SessionContext<?, ?, ?, ?, ?, Relation, ?, ?, ?> sessionContext) {
        this.sessionContext = sessionContext;
    }

    @Override
    public Object invoke(Relation relation, Object instance, Object[] args) {
        Object other = args[0];
        InstanceManager<?, Relation> instanceManager = sessionContext.getRelationInstanceManager();
        if (instanceManager.isInstance(other)) {
            Relation otherRelation = instanceManager.getDatastoreType(other);
            DatastoreSession<?, ?, ? extends DatastoreEntityMetadata<?>, ?, ?, Relation, ? extends DatastoreRelationMetadata<?>, ?, ?> datastoreSession = sessionContext
                    .getDatastoreSession();
            return datastoreSession.getDatastoreRelationManager().getRelationId(otherRelation)
                    .equals(datastoreSession.getDatastoreRelationManager().getRelationId(relation));
        }
        return Boolean.FALSE;
    }
}
