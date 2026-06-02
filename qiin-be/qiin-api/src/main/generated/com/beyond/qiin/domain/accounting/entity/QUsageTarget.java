package com.beyond.qiin.domain.accounting.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QUsageTarget is a Querydsl query type for UsageTarget
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUsageTarget extends EntityPathBase<UsageTarget> {

    private static final long serialVersionUID = 294100725L;

    public static final QUsageTarget usageTarget = new QUsageTarget("usageTarget");

    public final com.beyond.qiin.common.QCreatedBaseEntity _super = new com.beyond.qiin.common.QCreatedBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    public final NumberPath<Long> createdBy = createNumber("createdBy", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<java.math.BigDecimal> targetRate = createNumber("targetRate", java.math.BigDecimal.class);

    public final NumberPath<Integer> year = createNumber("year", Integer.class);

    public QUsageTarget(String variable) {
        super(UsageTarget.class, forVariable(variable));
    }

    public QUsageTarget(Path<? extends UsageTarget> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUsageTarget(PathMetadata metadata) {
        super(UsageTarget.class, metadata);
    }

}

