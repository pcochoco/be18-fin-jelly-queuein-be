package com.beyond.qiin.domain.accounting.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSettlement is a Querydsl query type for Settlement
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSettlement extends EntityPathBase<Settlement> {

    private static final long serialVersionUID = 330772582L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSettlement settlement = new QSettlement("settlement");

    public final com.beyond.qiin.common.QCreatedBaseEntity _super = new com.beyond.qiin.common.QCreatedBaseEntity(this);

    public final NumberPath<java.math.BigDecimal> actualUsageCost = createNumber("actualUsageCost", java.math.BigDecimal.class);

    public final com.beyond.qiin.domain.inventory.entity.QAsset asset;

    public final NumberPath<java.math.BigDecimal> costPerHourSnapshot = createNumber("costPerHourSnapshot", java.math.BigDecimal.class);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<java.math.BigDecimal> totalUsageCost = createNumber("totalUsageCost", java.math.BigDecimal.class);

    public final NumberPath<java.math.BigDecimal> usageGapCost = createNumber("usageGapCost", java.math.BigDecimal.class);

    public final QUsageHistory usageHistory;

    public final NumberPath<Long> usageTargetId = createNumber("usageTargetId", Long.class);

    public QSettlement(String variable) {
        this(Settlement.class, forVariable(variable), INITS);
    }

    public QSettlement(Path<? extends Settlement> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSettlement(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSettlement(PathMetadata metadata, PathInits inits) {
        this(Settlement.class, metadata, inits);
    }

    public QSettlement(Class<? extends Settlement> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.asset = inits.isInitialized("asset") ? new com.beyond.qiin.domain.inventory.entity.QAsset(forProperty("asset"), inits.get("asset")) : null;
        this.usageHistory = inits.isInitialized("usageHistory") ? new QUsageHistory(forProperty("usageHistory"), inits.get("usageHistory")) : null;
    }

}

