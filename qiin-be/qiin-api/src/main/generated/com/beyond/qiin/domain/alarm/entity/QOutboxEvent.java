package com.beyond.qiin.domain.alarm.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QOutboxEvent is a Querydsl query type for OutboxEvent
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOutboxEvent extends EntityPathBase<OutboxEvent> {

    private static final long serialVersionUID = -3178822L;

    public static final QOutboxEvent outboxEvent = new QOutboxEvent("outboxEvent");

    public final NumberPath<Long> aggregateId = createNumber("aggregateId", Long.class);

    public final StringPath aggregateType = createString("aggregateType");

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final StringPath eventType = createString("eventType");

    public final ComparablePath<java.util.UUID> id = createComparable("id", java.util.UUID.class);

    public final BooleanPath isPublished = createBoolean("isPublished");

    public final StringPath payload = createString("payload");

    public QOutboxEvent(String variable) {
        super(OutboxEvent.class, forVariable(variable));
    }

    public QOutboxEvent(Path<? extends OutboxEvent> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOutboxEvent(PathMetadata metadata) {
        super(OutboxEvent.class, metadata);
    }

}

