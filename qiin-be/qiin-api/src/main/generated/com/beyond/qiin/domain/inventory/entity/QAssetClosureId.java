package com.beyond.qiin.domain.inventory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;


/**
 * QAssetClosureId is a Querydsl query type for AssetClosureId
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEmbeddableSerializer")
public class QAssetClosureId extends BeanPath<AssetClosureId> {

    private static final long serialVersionUID = 1296595700L;

    public static final QAssetClosureId assetClosureId = new QAssetClosureId("assetClosureId");

    public final NumberPath<Long> ancestorId = createNumber("ancestorId", Long.class);

    public final NumberPath<Long> descendantId = createNumber("descendantId", Long.class);

    public QAssetClosureId(String variable) {
        super(AssetClosureId.class, forVariable(variable));
    }

    public QAssetClosureId(Path<? extends AssetClosureId> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAssetClosureId(PathMetadata metadata) {
        super(AssetClosureId.class, metadata);
    }

}

