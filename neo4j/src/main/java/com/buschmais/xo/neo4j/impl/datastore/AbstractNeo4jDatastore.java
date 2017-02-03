package com.buschmais.xo.neo4j.impl.datastore;

import com.buschmais.xo.neo4j.api.Neo4jDatastore;
import com.buschmais.xo.neo4j.api.Neo4jDatastoreSession;
import com.buschmais.xo.neo4j.impl.datastore.metadata.NodeMetadata;
import com.buschmais.xo.neo4j.impl.datastore.metadata.RelationshipMetadata;
import com.buschmais.xo.neo4j.impl.model.EmbeddedLabel;
import com.buschmais.xo.neo4j.impl.model.EmbeddedRelationshipType;
import com.buschmais.xo.spi.datastore.DatastoreMetadataFactory;

/**
 * Abstract base class for Neo4j datastores.
 * 
 * @param <DS>
 *            The datastore session type.
 */
public abstract class AbstractNeo4jDatastore<DS extends Neo4jDatastoreSession> implements Neo4jDatastore<DS> {

    private final Neo4jMetadataFactory metadataFactory = new Neo4jMetadataFactory();

    @Override
    public DatastoreMetadataFactory<NodeMetadata, EmbeddedLabel, RelationshipMetadata, EmbeddedRelationshipType> getMetadataFactory() {
        return metadataFactory;
    }

}
