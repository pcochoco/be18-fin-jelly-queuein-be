package com.beyond.qiin.domain.iam.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 1078992850L;

    public static final QUser user = new QUser("user");

    public final com.beyond.qiin.common.QBaseEntity _super = new com.beyond.qiin.common.QBaseEntity(this);

    public final StringPath birth = createString("birth");

    //inherited
    public final DateTimePath<java.time.Instant> createdAt = _super.createdAt;

    //inherited
    public final NumberPath<Long> createdBy = _super.createdBy;

    //inherited
    public final DateTimePath<java.time.Instant> deletedAt = _super.deletedAt;

    //inherited
    public final NumberPath<Long> deletedBy = _super.deletedBy;

    public final NumberPath<Long> dptId = createNumber("dptId", Long.class);

    public final StringPath email = createString("email");

    public final DateTimePath<java.time.Instant> hireDate = createDateTime("hireDate", java.time.Instant.class);

    //inherited
    public final NumberPath<Long> id = _super.id;

    public final DateTimePath<java.time.Instant> lastLoginAt = createDateTime("lastLoginAt", java.time.Instant.class);

    public final StringPath password = createString("password");

    public final BooleanPath passwordExpired = createBoolean("passwordExpired");

    public final StringPath phone = createString("phone");

    public final DateTimePath<java.time.Instant> retireDate = createDateTime("retireDate", java.time.Instant.class);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public final StringPath userName = createString("userName");

    public final StringPath userNo = createString("userNo");

    public final ListPath<UserRole, QUserRole> userRoles = this.<UserRole, QUserRole>createList("userRoles", UserRole.class, QUserRole.class, PathInits.DIRECT2);

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

