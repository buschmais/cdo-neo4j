package com.buschmais.xo.neo4j.api.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.buschmais.xo.spi.annotation.EntityDefinition;

/**
 * Defines the label to be used on a node representing a composite object.
 */
@EntityDefinition
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Label {

    String DEFAULT_VALUE = "";

    /**
     * @return The name of the label.
     */
    String value() default "";

    /**
     * @return The (super) type containing an indexed property ({@link Indexed}).
     * <p>An index will be created for this label and the indexed property and used by {@link com.buschmais.xo.api.XOManager#find(Class, Object)}.</p>
     */
    Class<?> usingIndexedPropertyOf() default Object.class;
}
