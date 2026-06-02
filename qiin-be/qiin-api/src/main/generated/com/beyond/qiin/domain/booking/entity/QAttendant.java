package com.beyond.qiin.domain.booking.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAttendant is a Querydsl query type for Attendant
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAttendant extends EntityPathBase<Attendant> {

    private static final long serialVersionUID = 537603682L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAttendant attendant = new QAttendant("attendant");

    public final com.beyond.qiin.common.QBaseEntity _super = new com.beyond.qiin.common.QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.Instant> deletedAt = _super.deletedAt;

    //inherited
    public final NumberPath<Long> deletedBy = _super.deletedBy;

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final QReservation reservation;

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public final com.beyond.qiin.domain.iam.entity.QUser user;

    public QAttendant(String variable) {
        this(Attendant.class, forVariable(variable), INITS);
    }

    public QAttendant(Path<? extends Attendant> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAttendant(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAttendant(PathMetadata metadata, PathInits inits) {
        this(Attendant.class, metadata, inits);
    }

    public QAttendant(Class<? extends Attendant> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.reservation = inits.isInitialized("reservation") ? new QReservation(forProperty("reservation"), inits.get("reservation")) : null;
        this.user = inits.isInitialized("user") ? new com.beyond.qiin.domain.iam.entity.QUser(forProperty("user")) : null;
    }

}

