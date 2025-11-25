package com.ssafy.group.infrastructure.read.persistence.jpa.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QGroupReadEntity is a Querydsl query type for GroupReadEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QGroupReadEntity extends EntityPathBase<GroupReadEntity> {

    private static final long serialVersionUID = -1496376718L;

    public static final QGroupReadEntity groupReadEntity = new QGroupReadEntity("groupReadEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath inviteCode = createString("inviteCode");

    public final StringPath name = createString("name");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public QGroupReadEntity(String variable) {
        super(GroupReadEntity.class, forVariable(variable));
    }

    public QGroupReadEntity(Path<? extends GroupReadEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QGroupReadEntity(PathMetadata metadata) {
        super(GroupReadEntity.class, metadata);
    }

}

