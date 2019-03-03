package com.buschmais.xo.impl.proxy.common.composite;

import com.buschmais.xo.api.XOException;
import com.buschmais.xo.api.proxy.ProxyMethod;

public abstract class AbstractAsMethod<DatastoreType> implements ProxyMethod<DatastoreType> {

    @Override
    public Object invoke(DatastoreType datastoreType, Object instance, Object[] args) {
        Class<?> targetType = (Class<?>) args[0];
        if (targetType.isAssignableFrom(instance.getClass())) {
            return getInstance(instance, datastoreType);
        }
        throw new XOException(instance + " cannot be cast to " + targetType.getName());
    }

    protected abstract Object getInstance(Object instance, DatastoreType datastoreType);
}
