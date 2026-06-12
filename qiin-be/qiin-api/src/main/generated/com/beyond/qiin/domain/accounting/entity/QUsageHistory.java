package com.beyond.qiin.domain.accounting.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUsageHistory is a Querydsl query type for UsageHistory
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUsageHistory extends EntityPathBase<UsageHistory> {

    private static final long serialVersionUID = -1302568016L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUsageHistory usageHistory = new QUsageHistory("usageHistory");

    public final com.beyond.qiin.common.QCreatedBaseEntity _super = new com.beyond.qiin.common.QCreatedBaseEntity(this);

    public final DateTimePath<java.time.Instant> actualEndAt = createDateTime("actualEndAt", java.time.Instant.class);

    public final DateTimePath<java.time.Instant> actualStartAt = createDateTime("actualStartAt", java.time.Instant.class);

    public final NumberPath<Integer> actualUsageTime = createNumber("actualUsageTime", Integer.class);

    public final com.beyond.qiin.domain.inventory.entity.QAsset asset;

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    public final DateTimePath<java.time.Instant> endAt = createDateTime("endAt", java.time.Instant.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.beyond.qiin.domain.booking.entity.QReservation reservation;

    public final DateTimePath<java.time.Instant> startAt = createDateTime("startAt", java.time.Instant.class);

    public final NumberPath<java.math.BigDecimal> usageRatio = createNumber("usageRatio", java.math.BigDecimal.class);

    public final NumberPath<Integer> usageTime = createNumber("usageTime", Integer.class);

    public QUsageHistory(String variable) {
        this(UsageHistory.class, forVariable(variable), INITS);
    }

    public QUsageHistory(Path<? extends UsageHistory> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUsageHistory(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUsageHistory(PathMetadata metadata, PathInits inits) {
        this(UsageHistory.class, metadata, inits);
    }

    public QUsageHistory(Class<? extends UsageHistory> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.asset = inits.isInitialized("asset") ? new com.beyond.qiin.domain.inventory.entity.QAsset(forProperty("asset"), inits.get("asset")) : null;
        this.reservation = inits.isInitialized("reservation") ? new com.beyond.qiin.domain.booking.entity.QReservation(forProperty("reservation"), inits.get("reservation")) : null;
    }

}

