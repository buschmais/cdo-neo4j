package com.buschmais.xo.neo4j.spi;

import java.util.Map;

import com.buschmais.xo.neo4j.api.annotation.Indexed;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.model.Neo4jLabel;
import com.buschmais.xo.neo4j.api.model.Neo4jRelationshipType;
import com.buschmais.xo.neo4j.spi.metadata.IndexedPropertyMetadata;
import com.buschmais.xo.neo4j.spi.metadata.NodeMetadata;
import com.buschmais.xo.neo4j.spi.metadata.PropertyMetadata;
import com.buschmais.xo.neo4j.spi.metadata.RelationshipMetadata;
import com.buschmais.xo.spi.datastore.DatastoreMetadataFactory;
import com.buschmais.xo.spi.metadata.method.IndexedPropertyMethodMetadata;
import com.buschmais.xo.spi.metadata.type.TypeMetadata;
import com.buschmais.xo.spi.reflection.AnnotatedElement;
import com.buschmais.xo.spi.reflection.AnnotatedMethod;
import com.buschmais.xo.spi.reflection.AnnotatedType;
import com.buschmais.xo.spi.reflection.PropertyMethod;
import com.google.common.base.CaseFormat;

/**
 * {@link com.buschmais.xo.spi.datastore.DatastoreMetadataFactory}
 * implementation for Neo4j datastores.
 */
public abstract class AbstractNeo4jMetadataFactory<L extends Neo4jLabel, R extends Neo4jRelationshipType> implements DatastoreMetadataFactory<NodeMetadata<L>, L, RelationshipMetadata<R>, R> {

    @Override
    public NodeMetadata createEntityMetadata(AnnotatedType annotatedType, Map<Class<?>, TypeMetadata> metadataByType) {
        Label labelAnnotation = annotatedType.getAnnotation(Label.class);
        L label = null;
        IndexedPropertyMethodMetadata<IndexedPropertyMetadata> indexedProperty = null;
        if (labelAnnotation != null) {
            String value = labelAnnotation.value();
            if (Label.DEFAULT_VALUE.equals(value)) {
                value = annotatedType.getName();
            }
            label = createLabel(value);
            Class<?> usingIndexOf = labelAnnotation.usingIndexedPropertyOf();
            if (!Object.class.equals(usingIndexOf)) {
                TypeMetadata typeMetadata = metadataByType.get(usingIndexOf);
                indexedProperty = typeMetadata.getIndexedProperty();
            }
        }
        return new NodeMetadata<L>(label, indexedProperty);
    }



    @Override
    public <ImplementedByMetadata> ImplementedByMetadata createImplementedByMetadata(AnnotatedMethod annotatedMethod) {
        return null;
    }

    @Override
    public <CollectionPropertyMetadata> CollectionPropertyMetadata createCollectionPropertyMetadata(PropertyMethod propertyMethod) {
        return null;
    }

    @Override
    public <ReferencePropertyMetadata> ReferencePropertyMetadata createReferencePropertyMetadata(PropertyMethod propertyMethod) {
        return null;
    }

    @Override
    public PropertyMetadata createPropertyMetadata(PropertyMethod propertyMethod) {
        Property property = propertyMethod.getAnnotationOfProperty(Property.class);
        String name = property != null ? property.value() : propertyMethod.getName();
        return new PropertyMetadata(name);
    }

    @Override
    public IndexedPropertyMetadata createIndexedPropertyMetadata(PropertyMethod propertyMethod) {
        Indexed indexed = propertyMethod.getAnnotation(Indexed.class);
        return new IndexedPropertyMetadata(indexed.create(), indexed.unique());
    }

    @Override
    public RelationshipMetadata<R> createRelationMetadata(AnnotatedElement<?> annotatedElement, Map<Class<?>, TypeMetadata> metadataByType) {
        Relation relationAnnotation;
        if (annotatedElement instanceof PropertyMethod) {
            relationAnnotation = ((PropertyMethod) annotatedElement).getAnnotationOfProperty(Relation.class);
        } else {
            relationAnnotation = annotatedElement.getAnnotation(Relation.class);
        }
        String name = null;
        if (relationAnnotation != null) {
            String value = relationAnnotation.value();
            if (!Relation.DEFAULT_VALUE.equals(value)) {
                name = value;
            }
        }
        if (name == null) {
            name = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, annotatedElement.getName());
        }
        return new RelationshipMetadata<R>(createRelationshipType(name));
    }

    protected abstract R createRelationshipType(String name);

    protected abstract L createLabel(String name);

}