package com.buschmais.cdo.impl.test.bootstrap.provider;

import com.buschmais.cdo.impl.test.bootstrap.provider.metadata.TestEntityMetadata;
import com.buschmais.cdo.api.bootstrap.CdoUnit;
import com.buschmais.cdo.spi.datastore.*;
import com.buschmais.cdo.spi.metadata.RelationMetadata;
import com.buschmais.cdo.spi.metadata.TypeMetadata;
import com.buschmais.cdo.spi.reflection.AnnotatedMethod;
import com.buschmais.cdo.spi.reflection.PropertyMethod;
import com.buschmais.cdo.spi.reflection.AnnotatedType;

import java.util.Collection;
import java.util.Map;

public class TestCdoDatastore<D extends DatastoreSession> implements Datastore<D, TestEntityMetadata, String> {

    private final CdoUnit cdoUnit;

    public TestCdoDatastore(CdoUnit cdoUnit) {
        this.cdoUnit = cdoUnit;
    }

    @Override
    public DatastoreMetadataFactory<TestEntityMetadata, String> getMetadataFactory() {
        return new DatastoreMetadataFactory<TestEntityMetadata, String>() {
            @Override
            public TestEntityMetadata createEntityMetadata(AnnotatedType annotatedType, Map<Class<?>, TypeMetadata<TestEntityMetadata>> metadataByType) {
                return new TestEntityMetadata(annotatedType.getAnnotatedElement().getName());
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
            public <PrimitivePropertyMetadata> PrimitivePropertyMetadata createPrimitivePropertyMetadata(PropertyMethod propertyMethod) {
                return null;
            }

            @Override
            public <EnumPropertyMetadata> EnumPropertyMetadata createEnumPropertyMetadata(PropertyMethod propertyMethod) {
                return null;
            }

            @Override
            public <IndexedPropertyMetadata> IndexedPropertyMetadata createIndexedPropertyMetadata(PropertyMethod propertyMethod) {
                return null;
            }

            @Override
            public <RelationMetadata> RelationMetadata createRelationMetadata(PropertyMethod propertyMethod) {
                return null;
            }

            @Override
            public RelationMetadata.Direction getRelationDirection(PropertyMethod propertyMethod) {
                return null;
            }
        };
    }

    @Override
    public D createSession() {
        return null;
    }

    @Override
    public void close() {
    }

    @Override
    public void init(Collection<TypeMetadata<TestEntityMetadata>> registeredMetadata) {
    }

    public CdoUnit getCdoUnit() {
        return cdoUnit;
    }
}
