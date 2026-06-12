package com.beyond.qiin.domain.iam.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPermission is a Querydsl query type for Permission
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPermission extends EntityPathBase<Permission> {

    private static final long serialVersionUID = 550738070L;

    public static final QPermission permission = new QPermission("permission");

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

    public final StringPath permissionDescription = createString("permissionDescription");

    public final StringPath permissionName = createString("permissionName");

    public final ListPath<RolePermission, QRolePermission> rolePermissions = this.<RolePermission, QRolePermission>createList("rolePermissions", RolePermission.class, QRolePermission.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.Instant> updatedAt = _super.updatedAt;

    //inherited
    public final NumberPath<Long> updatedBy = _super.updatedBy;

    public QPermission(String variable) {
        super(Permission.class, forVariable(variable));
    }

    public QPermission(Path<? extends Permission> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPermission(PathMetadata metadata) {
        super(Permission.class, metadata);
    }

}

