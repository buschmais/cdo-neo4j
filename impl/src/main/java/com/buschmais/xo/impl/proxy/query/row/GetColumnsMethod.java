package com.buschmais.xo.impl.proxy.query.row;

import java.util.ArrayList;
import java.util.Map;

import com.buschmais.xo.impl.proxy.query.RowProxyMethod;

public class GetColumnsMethod implements RowProxyMethod {

    @Override
    public Object invoke(Map<String, Object> entity, Object instance, Object[] args) {
        return new ArrayList<>(entity.keySet());
    }

}
