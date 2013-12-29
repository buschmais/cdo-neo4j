package com.buschmais.cdo.impl.query;

import com.buschmais.cdo.api.Query;
import com.buschmais.cdo.api.ResultIterator;
import com.buschmais.cdo.impl.AbstractResultIterable;
import com.buschmais.cdo.impl.InstanceManager;
import com.buschmais.cdo.impl.proxy.query.RowInvocationHandler;
import com.buschmais.cdo.impl.proxy.query.RowProxyMethodService;
import com.buschmais.cdo.spi.datastore.DatastoreSession;

import java.io.IOException;
import java.util.*;

class QueryResultIterableImpl<T> extends AbstractResultIterable<T> implements Query.Result<T> {

    private final InstanceManager instanceManager;
    private final DatastoreSession datastoreSession;
    private final ResultIterator<Map<String, Object>> iterator;
    private final SortedSet<Class<?>> types;
    private final RowProxyMethodService rowProxyMethodService;

    QueryResultIterableImpl(InstanceManager instanceManager, DatastoreSession datastoreSession,
                            ResultIterator<Map<String, Object>> iterator, SortedSet<Class<?>> types) {
        this.instanceManager = instanceManager;
        this.datastoreSession = datastoreSession;
        this.iterator = iterator;
        this.types = types;
        this.rowProxyMethodService = new RowProxyMethodService(types, instanceManager);
    }

    @Override
    public ResultIterator<T> iterator() {
        return new ResultIterator<T>() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                Map<String, Object> next = iterator.next();
                Map<String, Object> row = new LinkedHashMap<>();
                for (Map.Entry<String, Object> entry : next.entrySet()) {
                    String column = entry.getKey();
                    Object value = entry.getValue();
                    Object decodedValue = decodeValue(value);
                    row.put(column, decodedValue);
                }
                RowInvocationHandler invocationHandler = new RowInvocationHandler(row, rowProxyMethodService);
                return (T) instanceManager.createInstance(invocationHandler, types, CompositeRowObject.class);
            }

            @Override
            public void remove() {
                iterator.remove();
            }

            private Object decodeValue(Object value) {
                if (value == null) {
                    return null;
                }
                Object decodedValue;
                if (datastoreSession.isEntity(value)) {
                    return instanceManager.getInstance(value);
                } else if (value instanceof List<?>) {
                    List<?> listValue = (List<?>) value;
                    List<Object> decodedList = new ArrayList<>();
                    for (Object o : listValue) {
                        decodedList.add(decodeValue(o));
                    }
                    decodedValue = decodedList;
                } else {
                    decodedValue = value;
                }
                return decodedValue;
            }

            @Override
            public void close() {
                iterator.close();
            }
        };
    }

    @Override
    public void close() throws IOException {
        iterator.close();
    }
}
