package com.buschmais.xo.neo4j.impl.datastore;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;

import com.buschmais.xo.neo4j.api.annotation.Indexed;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.impl.datastore.metadata.IndexedPropertyMetadata;
import com.buschmais.xo.neo4j.impl.datastore.metadata.Neo4jRelationshipType;
import com.buschmais.xo.neo4j.impl.datastore.metadata.NodeMetadata;
import com.buschmais.xo.neo4j.impl.datastore.metadata.PropertyMetadata;
import com.buschmais.xo.neo4j.impl.datastore.metadata.RelationshipMetadata;
import com.buschmais.xo.spi.datastore.DatastoreMetadataFactory;
import com.buschmais.xo.spi.metadata.method.IndexedPropertyMethodMetadata;
import com.buschmais.xo.spi.metadata.type.TypeMetadata;
import com.buschmais.xo.spi.reflection.AnnotatedElement;
import com.buschmais.xo.spi.reflection.AnnotatedMethod;
import com.buschmais.xo.spi.reflection.AnnotatedType;
import com.buschmais.xo.spi.reflection.PropertyMethod;

/**
 * {@link com.buschmais.xo.spi.datastore.DatastoreMetadataFactory} implementation for Neo4j datastores.
 */
public class Neo4jMetadataFactory implements DatastoreMetadataFactory<NodeMetadata, org.neo4j.graphdb.Label, RelationshipMetadata, Neo4jRelationshipType> {

    @Override
    public NodeMetadata createEntityMetadata(AnnotatedType annotatedType, Map<Class<?>, TypeMetadata> metadataByType) {
        Label labelAnnotation = annotatedType.getAnnotation(Label.class);
        org.neo4j.graphdb.Label label = null;
        IndexedPropertyMethodMetadata<?> indexedProperty = null;
        if (labelAnnotation != null) {
            String value = labelAnnotation.value();
            if (Label.DEFAULT_VALUE.equals(value)) {
                value = annotatedType.getName();
            }
            label = DynamicLabel.label(value);
            Class<?> usingIndexOf = labelAnnotation.usingIndexedPropertyOf();
            if (!Object.class.equals(usingIndexOf)) {
                TypeMetadata typeMetadata = metadataByType.get(usingIndexOf);
                indexedProperty = typeMetadata.getIndexedProperty();
            }
        }
        return new NodeMetadata(label, indexedProperty);
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
    public RelationshipMetadata createRelationMetadata(AnnotatedElement<?> annotatedElement, Map<Class<?>, TypeMetadata> metadataByType) {
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
            name = StringUtils.capitalize(annotatedElement.getName());
        }
        Neo4jRelationshipType relationshipType = new Neo4jRelationshipType(DynamicRelationshipType.withName(name));
        return new RelationshipMetadata(relationshipType);
    }
}
