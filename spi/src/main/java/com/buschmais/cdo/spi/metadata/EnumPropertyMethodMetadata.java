package com.buschmais.cdo.spi.metadata;


import com.buschmais.cdo.spi.reflection.PropertyMethod;

public class EnumPropertyMethodMetadata<DatastoreMetadata> extends AbstractPropertyMethodMetadata<DatastoreMetadata> {

    private final Class<? extends Enum<?>> enumerationType;

    public EnumPropertyMethodMetadata(PropertyMethod propertyMethod, Class<? extends Enum<?>> enumerationType, DatastoreMetadata datastoreMetadata) {
        super(propertyMethod, datastoreMetadata);
        this.enumerationType = enumerationType;
    }

    public Class<? extends Enum<?>> getEnumerationType() {
        return enumerationType;
    }
}
