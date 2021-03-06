package com.buschmais.xo.api.metadata.type;

import java.util.Collection;

import com.buschmais.xo.api.metadata.method.MethodMetadata;
import com.buschmais.xo.api.metadata.reflection.AnnotatedType;

public class RepositoryTypeMetadata extends AbstractTypeMetadata {

    public RepositoryTypeMetadata(AnnotatedType annotatedType, Collection<TypeMetadata> superTypes, Collection<MethodMetadata<?, ?>> properties) {
        super(annotatedType, superTypes, properties, null);
    }
}
