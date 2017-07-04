package com.buschmais.xo.impl.transaction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.buschmais.xo.api.ResultIterator;
import com.buschmais.xo.api.XOTransaction;

public class TransactionalResultIterator<E> implements ResultIterator<E>, XOTransaction.Synchronization {

    private ResultIterator<E> delegateIterator;
    private XOTransaction xoTransaction;

    public TransactionalResultIterator(ResultIterator<E> delegateIterator, XOTransaction xoTransaction) {
        this.xoTransaction = xoTransaction;
        this.delegateIterator = delegateIterator;
        if (isTransactional()) {
            xoTransaction.registerSynchronization(this);
        }
    }

    @Override
    public void close() {
        unregisterSynchronization();
        delegateIterator.close();
    }

    @Override
    public boolean hasNext() {
        boolean hasNext = delegateIterator.hasNext();
        if (!hasNext) {
            unregisterSynchronization();
        }
        return hasNext;
    }

    @Override
    public E next() {
        return delegateIterator.next();
    }

    @Override
    public void remove() {
        delegateIterator.remove();
    }

    @Override
    public void beforeCompletion() {
        final List<E> detachedList = new ArrayList<>();
        while (delegateIterator.hasNext()) {
            detachedList.add(delegateIterator.next());
        }
        final Iterator<E> iterator = detachedList.iterator();
        this.delegateIterator = new ResultIterator<E>() {
            @Override
            public void close() {
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public E next() {
                return iterator.next();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Remove is not supported for this iterator.");
            }
        };
    }

    @Override
    public void afterCompletion(boolean committed) {
    }

    private void unregisterSynchronization() {
        if (isTransactional()) {
            this.xoTransaction.unregisterSynchronization(this);
            this.xoTransaction = null;
        }
    }

    private boolean isTransactional() {
        return this.xoTransaction != null && this.xoTransaction.isActive();
    }
}
