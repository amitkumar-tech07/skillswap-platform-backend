package com.backend.skillswap.repository;

import com.backend.skillswap.entity.Booking;
import com.backend.skillswap.entity.UserEntity;
import com.backend.skillswap.entity.UserSkill;
import com.backend.skillswap.entity.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    // ------------------------- FIND BOOKINGS BY USER -------------------------

    List<Booking> findByRequester(UserEntity requester);  // All bookings made by a user

    List<Booking> findByProvider(UserEntity provider);    // All bookings where user is provider

    List<Booking> findByProviderAndStatus(UserEntity provider, BookingStatus status); // Filtered by status

    List<Booking> findByRequesterAndStatus(UserEntity requester, BookingStatus status);

    // ------------------------- FIND BOOKINGS BY SKILL -------------------------

    List<Booking> findBySkill(UserSkill skill);

    List<Booking> findBySkillAndStatus(UserSkill skill, BookingStatus status);

    // ------------------------- UPCOMING BOOKINGS -------------------------
    @Query("""
SELECT b FROM Booking b
WHERE b.provider = :provider
AND (
     (b.status = com.backend.skillswap.entity.enums.BookingStatus.CONFIRMED
      AND b.startTime > :now)
  OR (b.status = com.backend.skillswap.entity.enums.BookingStatus.IN_PROGRESS
      AND b.endTime > :now)
)
ORDER BY b.startTime ASC
""")
    List<Booking> findUpcomingBookingsForProvider(
            @Param("provider") UserEntity provider,
            @Param("now") LocalDateTime now
    );

    @Query("""
SELECT b FROM Booking b
WHERE b.requester = :requester
AND (
      b.status = com.backend.skillswap.entity.enums.BookingStatus.PENDING
   OR (b.status = com.backend.skillswap.entity.enums.BookingStatus.CONFIRMED
       AND b.startTime > :now)
   OR (b.status = com.backend.skillswap.entity.enums.BookingStatus.IN_PROGRESS
       AND b.endTime > :now)
)
ORDER BY b.startTime ASC
""")
    List<Booking> findUpcomingBookingsForRequester(
            @Param("requester") UserEntity requester,
            @Param("now") LocalDateTime now
    );

    // ------------------------- PAST BOOKINGS -------------------------

    @Query("""
SELECT b FROM Booking b
WHERE b.provider = :provider
AND b.status IN (
    com.backend.skillswap.entity.enums.BookingStatus.COMPLETED,
    com.backend.skillswap.entity.enums.BookingStatus.CANCELLED
)
ORDER BY b.updatedAt DESC
""")
    List<Booking> findPastBookingsForProvider(
            @Param("provider") UserEntity provider
    );


    @Query("""
SELECT b FROM Booking b
WHERE b.requester = :requester
AND b.status IN (
    com.backend.skillswap.entity.enums.BookingStatus.COMPLETED,
    com.backend.skillswap.entity.enums.BookingStatus.CANCELLED
)
ORDER BY b.updatedAt DESC
""")
    List<Booking> findPastBookingsForRequester(
            @Param("requester") UserEntity requester
    );

    // ------------------------- FIND BY STATUS -------------------------
    List<Booking> findByStatus(BookingStatus status);

    // ------------------------- CUSTOM QUERY EXAMPLE -------------------------
    @Query("SELECT b FROM Booking b WHERE b.provider = :provider AND b.status = :status AND b.startTime BETWEEN :start AND :end")
    List<Booking> findProviderBookingsWithinDateRange(UserEntity provider, BookingStatus status, LocalDateTime start, LocalDateTime end);

    // ------------------------- OPTIONAL SINGLE RESULT -------------------------
    Optional<Booking> findByIdAndProvider(Long bookingId, UserEntity provider);

    Optional<Booking> findByIdAndRequester(Long bookingId, UserEntity requester);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.request WHERE b.id = :id")
    Optional<Booking> findByIdWithRequest(@Param("id") Long id);

    @Query("SELECT b FROM Booking b LEFT JOIN FETCH b.request WHERE b.id = :id AND b.provider = :provider")
    Optional<Booking> findByIdWithRequestAndProvider(@Param("id") Long id, @Param("provider") UserEntity provider);


    // ===================== OVERLAPPING SLOT VALIDATION (Provider) ================
    // Provider ke paas pehle se booking to nahi?
    @Query("""
    SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
    FROM Booking b
    WHERE b.provider.id = :providerId
      AND b.status NOT IN (com.backend.skillswap.entity.enums.BookingStatus.CANCELLED,
                           com.backend.skillswap.entity.enums.BookingStatus.COMPLETED)
      AND (
            (b.startTime <= :endTime AND b.endTime >= :startTime)
          )
""")
    boolean existsOverlappingBookingForProvider(Long providerId,
                                                LocalDateTime startTime,
                                                LocalDateTime endTime);


    // ================ OVERLAPPING SLOT VALIDATION (User) =========================
    // User ne same slot me dusri booking to nahi kari?
    @Query("""
    SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END
    FROM Booking b
    WHERE b.requester.id = :userId
      AND b.status NOT IN (com.backend.skillswap.entity.enums.BookingStatus.CANCELLED,
                           com.backend.skillswap.entity.enums.BookingStatus.COMPLETED)
      AND (
            (b.startTime <= :endTime AND b.endTime >= :startTime)
          )
""")
    boolean existsOverlappingBookingForUser(Long userId,
                                            LocalDateTime startTime,
                                            LocalDateTime endTime);


    // ===================== COOLDOWN CHECK ===========================
    // Same user baar baar provider ko spam na kare
    @Query("""
    SELECT COUNT(b) > 0
    FROM Booking b
    WHERE b.requester.id = :userId
      AND b.provider.id = :providerId
      AND b.createdAt >= :cooldownTime
""")
    boolean hasRecentBooking(Long userId, Long providerId, LocalDateTime cooldownTime);
}
