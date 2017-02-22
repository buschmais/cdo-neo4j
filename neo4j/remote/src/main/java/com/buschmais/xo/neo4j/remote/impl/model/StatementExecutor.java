package com.buschmais.xo.neo4j.remote.impl.model;

import java.util.Map;

import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.exceptions.Neo4jException;
import org.neo4j.driver.v1.exceptions.NoSuchRecordException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.xo.api.XOException;
import com.buschmais.xo.neo4j.remote.impl.datastore.RemoteDatastoreTransaction;

public class StatementExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatementExecutor.class);

    private RemoteDatastoreTransaction transaction;

    public StatementExecutor(RemoteDatastoreTransaction transaction) {
        this.transaction = transaction;
    }

    public Record getSingleResult(String statement, Value parameters) {
        return getSingleResult(statement, parameters.asMap());
    }

    public Record getSingleResult(String statement, Map<String, Object> parameters) {
        return getSingleResult(execute(statement, parameters));
    }

    public StatementResult execute(String statement, Value parameters) {
        return execute(statement, parameters.asMap());
    }

    public StatementResult execute(String statement, Map<String, Object> parameters) {
        LOGGER.info("'" + statement + "': " + parameters);
        try {
            return transaction.getStatementRunner().run(statement, parameters);
        } catch (Neo4jException e) {
            throw new XOException("Cannot execute statement '" + statement + "', " + parameters, e);
        }
    }

    private Record getSingleResult(StatementResult result) {
        try {
            return result.single();
        } catch (NoSuchRecordException e) {
            throw new XOException("Query returned no result.");
        } finally {
            result.consume();
        }
    }
}