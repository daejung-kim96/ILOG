package com.ssafy.group.infrastructure.write.persistence.jpa.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.ssafy.group.domain.model.aggregate.Group;
import com.ssafy.group.domain.repository.GroupRepository;
import com.ssafy.group.infrastructure.write.persistence.jpa.entity.GroupEntity;
import com.ssafy.group.infrastructure.write.persistence.jpa.mapper.GroupEntityMapper;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class GroupRepositoryImpl implements GroupRepository {

    private final GroupJpaRepository groupJpaRepository;
    private final GroupEntityMapper groupEntityMapper;

    @Override
    public Group save(Group group) {
        GroupEntity groupEntity;
        if (group.getId() != 0) {
            // ID가 있으면 기존 엔티티를 찾아서 업데이트 (Dirty Checking 활용)
            groupEntity = groupJpaRepository.findById(group.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Group not found with id: " + group.getId()));
            groupEntityMapper.updateJpaEntity(group, groupEntity);
        } else {
            // ID가 없으면 새로운 엔티티 생성
            groupEntity = groupEntityMapper.toJpaEntity(group);
        }

        GroupEntity savedGroupEntity = groupJpaRepository.save(groupEntity);
        return groupEntityMapper.toDomain(savedGroupEntity);
    }

    @Override
    public Optional<Group> findById(long groupId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    @Override
    public Optional<Group> findByInviteCode(String inviteCode) {
        return groupJpaRepository.findByInviteCode(inviteCode).map(groupEntityMapper::toDomain);
    }

}
