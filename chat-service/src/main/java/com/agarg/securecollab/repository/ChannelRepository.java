package com.agarg.securecollab.repository;

import com.agarg.securecollab.chatservice.entity.ChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChannelRepository extends JpaRepository<ChannelEntity, Long> {
}