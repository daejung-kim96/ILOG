package com.ssafy.user.infrastructure.read.persistence.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QUserReadEntity is a Querydsl query type for UserReadEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserReadEntity extends EntityPathBase<UserReadEntity> {

    private static final long serialVersionUID = -1031008828L;

    public static final QUserReadEntity userReadEntity = new QUserReadEntity("userReadEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath name = createString("name");

    public final StringPath picture = createString("picture");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QUserReadEntity(String variable) {
        super(UserReadEntity.class, forVariable(variable));
    }

    public QUserReadEntity(Path<? extends UserReadEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserReadEntity(PathMetadata metadata) {
        super(UserReadEntity.class, metadata);
    }

}

