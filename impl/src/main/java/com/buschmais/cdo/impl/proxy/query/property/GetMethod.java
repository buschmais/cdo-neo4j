package com.buschmais.cdo.impl.proxy.query.property;

import com.buschmais.cdo.api.CdoException;
import com.buschmais.cdo.impl.proxy.query.RowProxyMethod;

import java.util.Map;

public class GetMethod implements RowProxyMethod {

    private final String name;
    private final Class<?> type;

    public GetMethod(String name, Class<?> type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public Object invoke(Map<String, Object> entity, Object instance, Object[] args) {
        if (!entity.containsKey(name)) {
            throw new CdoException("Query result does not contain column '" + name + "'");
        }
        Object value = entity.get(name);
        return value != null ? type.cast(value) : null;
    }
}
