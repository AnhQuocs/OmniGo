package com.trung.userdriverservice.repository;

import com.trung.userdriverservice.entity.UserSavedAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSavedAddressRepository extends JpaRepository<UserSavedAddress, Long> {

    List<UserSavedAddress> findByUserIdOrderByIsDefaultDescCreatedAtDesc(Long userId);

    Optional<UserSavedAddress> findByIdAndUserId(Long id, Long userId);

    @Modifying
    @Query("UPDATE UserSavedAddress a SET a.isDefault = false WHERE a.userId = :userId")
    void resetDefaultAddressForUser(@Param("userId") Long userId);
}

