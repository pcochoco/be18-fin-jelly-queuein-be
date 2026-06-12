package com.beyond.qiin.domain.inventory.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.dsl.StringTemplate;

import com.querydsl.core.types.PathMetadata;
import com.querydsl.core.annotations.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAssetClosure is a Querydsl query type for AssetClosure
 */
@SuppressWarnings("this-escape")
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAssetClosure extends EntityPathBase<AssetClosure> {

    private static final long serialVersionUID = -570717191L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QAssetClosure assetClosure = new QAssetClosure("assetClosure");

    public final QAsset ancestor;

    public final QAssetClosureId assetClosureId;

    public final NumberPath<Integer> depth = createNumber("depth", Integer.class);

    public final QAsset descendant;

    public QAssetClosure(String variable) {
        this(AssetClosure.class, forVariable(variable), INITS);
    }

    public QAssetClosure(Path<? extends AssetClosure> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QAssetClosure(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QAssetClosure(PathMetadata metadata, PathInits inits) {
        this(AssetClosure.class, metadata, inits);
    }

    public QAssetClosure(Class<? extends AssetClosure> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.ancestor = inits.isInitialized("ancestor") ? new QAsset(forProperty("ancestor"), inits.get("ancestor")) : null;
        this.assetClosureId = inits.isInitialized("assetClosureId") ? new QAssetClosureId(forProperty("assetClosureId")) : null;
        this.descendant = inits.isInitialized("descendant") ? new QAsset(forProperty("descendant"), inits.get("descendant")) : null;
    }

}

