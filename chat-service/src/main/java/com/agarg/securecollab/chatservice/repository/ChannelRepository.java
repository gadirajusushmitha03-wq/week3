package com.agarg.securecollab.chatservice.repository;

import com.agarg.securecollab.chatservice.entity.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChannelRepository extends JpaRepository<ChannelEntity, String> {
}
