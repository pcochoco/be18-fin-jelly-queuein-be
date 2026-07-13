package com.beyond.qiin.common;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QCreatedBaseEntity is a Querydsl query type for CreatedBaseEntity
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultSupertypeSerializer")
public class QCreatedBaseEntity extends EntityPathBase<CreatedBaseEntity> {

    private static final long serialVersionUID = -1475713544L;

    public static final QCreatedBaseEntity createdBaseEntity = new QCreatedBaseEntity("createdBaseEntity");

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public QCreatedBaseEntity(String variable) {
        super(CreatedBaseEntity.class, forVariable(variable));
    }

    public QCreatedBaseEntity(Path<? extends CreatedBaseEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCreatedBaseEntity(PathMetadata metadata) {
        super(CreatedBaseEntity.class, metadata);
    }

}

