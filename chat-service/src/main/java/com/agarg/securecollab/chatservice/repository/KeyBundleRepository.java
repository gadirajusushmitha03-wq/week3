package com.agarg.securecollab.chatservice.repository;

import com.agarg.securecollab.chatservice.entity.KeyBundleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KeyBundleRepository extends JpaRepository<KeyBundleEntity, String> {
    List<KeyBundleEntity> findByUserId(String userId);
    KeyBundleEntity findByUserIdAndDeviceId(String userId, String deviceId);
}
