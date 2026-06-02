package com.beyond.qiin.domain.booking.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReservation is a Querydsl query type for Reservation
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReservation extends EntityPathBase<Reservation> {

    private static final long serialVersionUID = 988394465L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReservation reservation = new QReservation("reservation");

    public final com.beyond.qiin.common.QBaseEntity _super = new com.beyond.qiin.common.QBaseEntity(this);

    public final DateTimePath<java.time.Instant> actualEndAt = createDateTime("actualEndAt", java.time.Instant.class);

    public final DateTimePath<java.time.Instant> actualStartAt = createDateTime("actualStartAt", java.time.Instant.class);

    public final com.beyond.qiin.domain.iam.entity.QUser applicant;

    public final com.beyond.qiin.domain.inventory.entity.QAsset asset;

    public final ListPath<Attendant, QAttendant> attendants = this.<Attendant, QAttendant>createList("attendants", Attendant.class, QAttendant.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.Instant> deletedAt = _super.deletedAt;

    //inherited
    public final NumberPath<Long> deletedBy = _super.deletedBy;

    public final StringPath description = createString("description");

    public final DateTimePath<java.time.Instant> endAt = createDateTime("endAt", java.time.Instant.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final BooleanPath isApplied = createBoolean("isApplied");

    public final BooleanPath isApproved = createBoolean("isApproved");

    public final StringPath reason = createString("reason");

    public final com.beyond.qiin.domain.iam.entity.QUser respondent;

    public final DateTimePath<java.time.Instant> startAt = createDateTime("startAt", java.time.Instant.class);

    public final NumberPath<Integer> status = createNumber("status", Integer.class);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public final NumberPath<Long> version = createNumber("version", Long.class);

    public QReservation(String variable) {
        this(Reservation.class, forVariable(variable), INITS);
    }

    public QReservation(Path<? extends Reservation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReservation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReservation(PathMetadata metadata, PathInits inits) {
        this(Reservation.class, metadata, inits);
    }

    public QReservation(Class<? extends Reservation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.applicant = inits.isInitialized("applicant") ? new com.beyond.qiin.domain.iam.entity.QUser(forProperty("applicant")) : null;
        this.asset = inits.isInitialized("asset") ? new com.beyond.qiin.domain.inventory.entity.QAsset(forProperty("asset"), inits.get("asset")) : null;
        this.respondent = inits.isInitialized("respondent") ? new com.beyond.qiin.domain.iam.entity.QUser(forProperty("respondent")) : null;
    }

}

