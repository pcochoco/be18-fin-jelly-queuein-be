package com.beyond.qiin.domain.booking.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReservationSlot is a Querydsl query type for ReservationSlot
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReservationSlot extends EntityPathBase<ReservationSlot> {

    private static final long serialVersionUID = -2057160321L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReservationSlot reservationSlot = new QReservationSlot("reservationSlot");

    public final com.beyond.qiin.domain.inventory.entity.QAsset asset;

    public final DateTimePath<java.time.Instant> createdAt = createDateTime("createdAt", java.time.Instant.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QReservation reservation;

    public final DateTimePath<java.time.Instant> startAt = createDateTime("startAt", java.time.Instant.class);

    public QReservationSlot(String variable) {
        this(ReservationSlot.class, forVariable(variable), INITS);
    }

    public QReservationSlot(Path<? extends ReservationSlot> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReservationSlot(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReservationSlot(PathMetadata metadata, PathInits inits) {
        this(ReservationSlot.class, metadata, inits);
    }

    public QReservationSlot(Class<? extends ReservationSlot> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.asset = inits.isInitialized("asset") ? new com.beyond.qiin.domain.inventory.entity.QAsset(forProperty("asset"), inits.get("asset")) : null;
        this.reservation = inits.isInitialized("reservation") ? new QReservation(forProperty("reservation"), inits.get("reservation")) : null;
    }

}

