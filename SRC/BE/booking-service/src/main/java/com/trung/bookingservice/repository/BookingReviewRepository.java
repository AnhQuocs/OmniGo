package com.trung.bookingservice.repository;

import com.trung.bookingservice.entity.BookingReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingReviewRepository extends JpaRepository<BookingReview, Long> {

    Optional<BookingReview> findByBookingId(Long bookingId);

    boolean existsByBookingId(Long bookingId);

    List<BookingReview> findByDriverIdOrderByCreatedAtDesc(Long driverId);

    List<BookingReview> findByDriverIdAndRatingOrderByCreatedAtDesc(Long driverId, Integer rating);

    long countByDriverId(Long driverId);

    long countByDriverIdAndRating(Long driverId, Integer rating);

    @Query("SELECT COALESCE(AVG(r.rating), 5.0) FROM BookingReview r WHERE r.driverId = :driverId")
    Double calculateAverageDriverRating(@Param("driverId") Long driverId);
}

