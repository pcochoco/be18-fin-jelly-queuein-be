package com.beyond.qiin.domain.iam.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserProfile is a Querydsl query type for UserProfile
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserProfile extends EntityPathBase<UserProfile> {

    private static final long serialVersionUID = -1851985545L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserProfile userProfile = new QUserProfile("userProfile");

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

    public final StringPath imageKey = createString("imageKey");

    public final StringPath imageUrl = createString("imageUrl");

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public final QUser user;

    public QUserProfile(String variable) {
        this(UserProfile.class, forVariable(variable), INITS);
    }

    public QUserProfile(Path<? extends UserProfile> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserProfile(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserProfile(PathMetadata metadata, PathInits inits) {
        this(UserProfile.class, metadata, inits);
    }

    public QUserProfile(Class<? extends UserProfile> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

