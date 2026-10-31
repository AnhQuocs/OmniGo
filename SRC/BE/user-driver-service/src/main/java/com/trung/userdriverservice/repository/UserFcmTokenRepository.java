package com.trung.userdriverservice.repository;

import com.trung.userdriverservice.entity.UserFcmToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFcmTokenRepository extends JpaRepository<UserFcmToken, Long> {

    Optional<UserFcmToken> findByFcmToken(String fcmToken);

    List<UserFcmToken> findByUserId(Long userId);

    void deleteByUserIdAndFcmToken(Long userId, String fcmToken);

    void deleteByUserId(Long userId);
}

