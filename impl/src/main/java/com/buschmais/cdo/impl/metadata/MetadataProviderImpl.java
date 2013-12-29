package com.buschmais.cdo.impl.metadata;

import com.buschmais.cdo.api.CdoException;
import com.buschmais.cdo.api.CompositeObject;
import com.buschmais.cdo.api.annotation.ImplementedBy;
import com.buschmais.cdo.api.annotation.ResultOf;
import com.buschmais.cdo.impl.MetadataProvider;
import com.buschmais.cdo.impl.reflection.BeanMethodProvider;
import com.buschmais.cdo.spi.annotation.EntityDefinition;
import com.buschmais.cdo.spi.annotation.IndexDefinition;
import com.buschmais.cdo.spi.annotation.RelationDefinition;
import com.buschmais.cdo.spi.datastore.*;
import com.buschmais.cdo.spi.metadata.*;
import com.buschmais.cdo.spi.reflection.AnnotatedMethod;
import com.buschmais.cdo.spi.reflection.AnnotatedType;
import com.buschmais.cdo.spi.reflection.PropertyMethod;
import com.buschmais.cdo.spi.reflection.UserMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 * Implementation of the {@link MetadataProvider}.
 *
 * @param <EntityMetadata>        The type of datastore specific entity metadata.
 * @param <EntityDiscriminator>   The type of datastore specific entity type discriminators.
 * @param <RelationMetadata>      The type of datastore specific relation metadata.
 * @param <RelationDiscriminator> The type of datastore specific relationtype discriminators.
 */
public class MetadataProviderImpl<EntityMetadata extends DatastoreEntityMetadata<EntityDiscriminator>, EntityDiscriminator, RelationMetadata extends DatastoreRelationMetadata<RelationDiscriminator>, RelationDiscriminator> implements MetadataProvider<EntityMetadata, EntityDiscriminator, RelationMetadata, RelationDiscriminator> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityTypeMetadata.class);
    private DatastoreMetadataFactory<EntityMetadata, EntityDiscriminator> metadataFactory;
    private EntityTypeMetadataResolver<EntityMetadata, EntityDiscriminator> entityTypeMetadataResolver;

    private Map<Class<?>, EntityTypeMetadata<EntityMetadata>> entityMetadataByType = new HashMap<>();
    private Map<Class<?>, RelationTypeMetadata<RelationMetadata>> relationMetadataByType = new HashMap<>();

    /**
     * Constructor.
     *
     * @param types     All classes as provided by the CDO unit.
     * @param datastore The datastore.
     */
    public MetadataProviderImpl(Collection<Class<?>> types, Datastore<?, EntityMetadata, EntityDiscriminator> datastore) {
        this.metadataFactory = datastore.getMetadataFactory();
        DependencyResolver.DependencyProvider<Class<?>> classDependencyProvider = new DependencyResolver.DependencyProvider<Class<?>>() {
            @Override
            public Set<Class<?>> getDependencies(Class<?> dependent) {
                return new HashSet<>(Arrays.asList(dependent.getInterfaces()));
            }
        };
        List<Class<?>> allClasses = DependencyResolver.newInstance(types, classDependencyProvider).resolve();
        LOGGER.debug("Processing types {}", allClasses);
        Map<Class<?>, Collection<AnnotatedMethod>> annotatedMethodsByClass = new HashMap<>();
        for (Class<?> currentClass : allClasses) {
            if (!currentClass.isInterface()) {
                throw new CdoException("Type " + currentClass.getName() + " is not an interface.");
            }
            annotatedMethodsByClass.put(currentClass, BeanMethodProvider.newInstance().getMethods(currentClass));
        }
        List<EntityTypeMetadata> entityTypeMetadata = new ArrayList<>();
        for (Class<?> currentClass : allClasses) {
            AnnotatedType annotatedType = new AnnotatedType(currentClass);
            Annotation entityDefinition = annotatedType.getByMetaAnnotation(EntityDefinition.class);
            Annotation relationDefinition = annotatedType.getByMetaAnnotation(RelationDefinition.class);
            if (entityDefinition != null) {
                EntityTypeMetadata metadata = createEntityTypeMetadata(annotatedType, annotatedMethodsByClass.get(currentClass), annotatedMethodsByClass.keySet());
                entityMetadataByType.put(currentClass, metadata);
                entityTypeMetadata.add(metadata);
            } else {

            }
        }
        entityTypeMetadataResolver = new EntityTypeMetadataResolver(entityMetadataByType);
        entityMetadataByType.put(CompositeObject.class, new EntityTypeMetadata(new AnnotatedType(CompositeObject.class), Collections.emptyList(), Collections.<AbstractMethodMetadata>emptyList(), null, null));

    }

    @Override
    public TypeMetadataSet getTypes(Set<EntityDiscriminator> entityDiscriminators) {
        return entityTypeMetadataResolver.getTypes(entityDiscriminators);
    }

    @Override
    public Set<EntityDiscriminator> getDiscriminators(TypeMetadataSet<EntityMetadata> types) {
        Set<EntityDiscriminator> entityDiscriminators = new HashSet<>();
        for (EntityTypeMetadata<EntityMetadata> entityTypeMetadata : types) {
            Set<EntityDiscriminator> discriminatorsOfType = entityTypeMetadataResolver.getDiscriminators(entityTypeMetadata);
            entityDiscriminators.addAll(discriminatorsOfType);
        }
        return entityDiscriminators;
    }

    @Override
    public Collection<EntityTypeMetadata<EntityMetadata>> getRegisteredMetadata() {
        return entityMetadataByType.values();
    }

    @Override
    public EntityTypeMetadata getEntityMetadata(Class<?> type) {
        EntityTypeMetadata entityTypeMetadata = entityMetadataByType.get(type);
        if (entityTypeMetadata == null) {
            throw new CdoException("Cannot resolve metadata for type " + type.getName() + ".");
        }
        return entityTypeMetadata;
    }

    /**
     * Create the {@link EntityTypeMetadata} for the given {@link AnnotatedType}.
     *
     * @param annotatedType    The {@link AnnotatedType}.
     * @param annotatedMethods The determined annotated methods of the type.
     * @param types            A set of all registered classes and their super types.
     * @return The {@link EntityTypeMetadata} instance representing the annotated type.
     */
    private EntityTypeMetadata createEntityTypeMetadata(AnnotatedType annotatedType, Collection<AnnotatedMethod> annotatedMethods, Set<Class<?>> types) {
        LOGGER.debug("Processing class {}", annotatedType.getAnnotatedElement().getName());
        Collection<MethodMetadata> methodMetadataOfType = getMethodMetadataOfType(annotatedMethods, types);
        IndexedPropertyMethodMetadata indexedProperty = getIndexedPropertyMethodMetadata(methodMetadataOfType);
        List<EntityTypeMetadata<EntityMetadata>> superTypes = new ArrayList<>();
        for (Class<?> i : annotatedType.getAnnotatedElement().getInterfaces()) {
            superTypes.add(this.entityMetadataByType.get(i));
        }
        DatastoreEntityMetadata<EntityDiscriminator> datastoreEntityMetadata = metadataFactory.createEntityMetadata(annotatedType, entityMetadataByType);
        EntityTypeMetadata entityTypeMetadata = new EntityTypeMetadata(annotatedType, superTypes, methodMetadataOfType, indexedProperty, datastoreEntityMetadata);
        return entityTypeMetadata;
    }

    /**
     * Determine the indexed property from a list of method metadata.
     *
     * @param methodMetadataOfType The list of method metadata.
     * @return The {@link IndexedPropertyMethodMetadata}.
     */
    private IndexedPropertyMethodMetadata getIndexedPropertyMethodMetadata(Collection<MethodMetadata> methodMetadataOfType) {
        IndexedPropertyMethodMetadata indexedProperty = null;
        for (MethodMetadata methodMetadata : methodMetadataOfType) {
            AnnotatedMethod annotatedMethod = methodMetadata.getAnnotatedMethod();
            Annotation indexedAnnotation = annotatedMethod.getByMetaAnnotation(IndexDefinition.class);
            if (indexedAnnotation != null) {
                if (!(methodMetadata instanceof PrimitivePropertyMethodMetadata)) {
                    throw new CdoException("Only primitive properties are allowed to be used for indexing.");
                }
                indexedProperty = new IndexedPropertyMethodMetadata((PropertyMethod) annotatedMethod, (PrimitivePropertyMethodMetadata) methodMetadata, metadataFactory.createIndexedPropertyMetadata((PropertyMethod) annotatedMethod));
            }
        }
        return indexedProperty;
    }

    /**
     * Return the collection of method metadata from the given collection of annotateed methods.
     *
     * @param annotatedMethods The collection of annotated methods.
     * @param types            The set of all registered classes and their super types.
     * @return The collection of method metadata.
     */
    private Collection<MethodMetadata> getMethodMetadataOfType(Collection<AnnotatedMethod> annotatedMethods, Set<Class<?>> types) {
        Collection<MethodMetadata> methodMetadataOfType = new ArrayList<>();
        // Collect the getter methods as they provide annotations holding meta information also to be applied to setters
        for (AnnotatedMethod annotatedMethod : annotatedMethods) {
            MethodMetadata methodMetadata;
            ResultOf resultOf = annotatedMethod.getAnnotation(ResultOf.class);
            ImplementedBy implementedBy = annotatedMethod.getAnnotation(ImplementedBy.class);
            if (implementedBy != null) {
                methodMetadata = new ImplementedByMethodMetadata(annotatedMethod, implementedBy.value(), metadataFactory.createImplementedByMetadata(annotatedMethod));
            } else if (resultOf != null) {
                methodMetadata = createResultOfMetadata(annotatedMethod, resultOf);
            } else if (annotatedMethod instanceof PropertyMethod) {
                methodMetadata = createPropertyMethodMetadata(types, (PropertyMethod) annotatedMethod);
            } else {
                methodMetadata = new UnsupportedOperationMethodMetadata((UserMethod) annotatedMethod);
            }
            methodMetadataOfType.add(methodMetadata);
        }
        return methodMetadataOfType;
    }

    private MethodMetadata createPropertyMethodMetadata(Set<Class<?>> types, PropertyMethod beanPropertyMethod) {
        MethodMetadata methodMetadata;
        if (Collection.class.isAssignableFrom(beanPropertyMethod.getType())) {
            methodMetadata = new CollectionPropertyMethodMetadata(beanPropertyMethod, new RelationTypeMetadata(metadataFactory.createRelationMetadata(beanPropertyMethod)), metadataFactory.getRelationDirection(beanPropertyMethod), metadataFactory.createCollectionPropertyMetadata(beanPropertyMethod));
        } else if (types.contains(beanPropertyMethod.getType())) {
            methodMetadata = new ReferencePropertyMethodMetadata(beanPropertyMethod, new RelationTypeMetadata(metadataFactory.createRelationMetadata(beanPropertyMethod)), metadataFactory.getRelationDirection(beanPropertyMethod), metadataFactory.createReferencePropertyMetadata(beanPropertyMethod));
        } else {
            if (Enum.class.isAssignableFrom(beanPropertyMethod.getType())) {
                methodMetadata = new EnumPropertyMethodMetadata(beanPropertyMethod, beanPropertyMethod.getType(), metadataFactory.createEnumPropertyMetadata(beanPropertyMethod));
            } else {
                methodMetadata = new PrimitivePropertyMethodMetadata(beanPropertyMethod, metadataFactory.createPrimitivePropertyMetadata(beanPropertyMethod));
            }
        }
        return methodMetadata;
    }

    private MethodMetadata createResultOfMetadata(AnnotatedMethod annotatedMethod, ResultOf resultOf) {
        Method method = annotatedMethod.getAnnotatedElement();
        // Determine query type
        Class<?> queryType = resultOf.query();
        Class<?> returnType = method.getReturnType();
        if (Object.class.equals(queryType)) {
            if (Iterable.class.isAssignableFrom(returnType)) {
                java.lang.reflect.Type genericReturnType = method.getGenericReturnType();
                if (genericReturnType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
                    queryType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
                }
            } else {
                queryType = returnType;
            }
        }
        // Determine parameter bindings
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        List<ResultOf.Parameter> parameters = new ArrayList<>();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            ResultOf.Parameter parameter = null;
            for (Annotation annotation : parameterAnnotations[i]) {
                if (ResultOf.Parameter.class.equals(annotation.annotationType())) {
                    parameter = (ResultOf.Parameter) annotation;
                }
            }
            if (parameter == null) {
                throw new CdoException("Cannot determine parameter names for '" + method.getName() + "', all parameters must be annotated with '" + ResultOf.Parameter.class.getName() + "'.");
            }
            parameters.add(parameter);
        }
        boolean singleResult = !Iterable.class.isAssignableFrom(returnType);
        return new ResultOfMethodMetadata(annotatedMethod, queryType, resultOf.usingThisAs(), parameters, singleResult);
    }
}
