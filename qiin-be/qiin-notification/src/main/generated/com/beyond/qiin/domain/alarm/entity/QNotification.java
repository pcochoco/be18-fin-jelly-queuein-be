package com.beyond.qiin.domain.alarm.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QNotification is a Querydsl query type for Notification
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QNotification extends EntityPathBase<Notification> {

    private static final long serialVersionUID = -191253842L;

    public static final QNotification notification = new QNotification("notification");

    public final NumberPath<Long> aggregateId = createNumber("aggregateId", Long.class);

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final DateTimePath<java.time.Instant> deletedAt = createDateTime("deletedAt", java.time.Instant.class);

    public final DateTimePath<java.time.Instant> deliveredAt = createDateTime("deliveredAt", java.time.Instant.class);

    public final ComparablePath<java.util.UUID> eventOutboxId = createComparable("eventOutboxId", java.util.UUID.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isRead = createBoolean("isRead");

    public final StringPath message = createString("message");

    public final StringPath payload = createString("payload");

    public final DateTimePath<java.time.Instant> readAt = createDateTime("readAt", java.time.Instant.class);

    public final NumberPath<Long> receiverId = createNumber("receiverId", Long.class);

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    public final NumberPath<Integer> type = createNumber("type", Integer.class);

    public QNotification(String variable) {
        super(Notification.class, forVariable(variable));
    }

    public QNotification(Path<? extends Notification> path) {
        super(path.getType(), path.getMetadata());
    }

    public QNotification(PathMetadata metadata) {
        super(Notification.class, metadata);
    }

}

