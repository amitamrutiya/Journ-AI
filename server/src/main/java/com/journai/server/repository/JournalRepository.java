package com.journai.server.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.journai.server.model.Journal;
import com.journai.server.model.Mood;

@Repository
public interface JournalRepository extends JpaRepository<Journal, String> {

        List<Journal> findByUser_IdOrderByCreatedAtDesc(String userId);

        Page<Journal> findByUser_IdOrderByCreatedAtDesc(String userId, Pageable pageable);

        Optional<Journal> findByIdAndUser_Id(String id, String userId);

        void deleteByIdAndUser_Id(String id, String userId);

        @Query("SELECT j FROM Journal j WHERE j.user.id = :userId AND j.createdAt >= :startDate AND j.createdAt <= :endDate ORDER BY j.createdAt DESC")
        List<Journal> findByUserIdAndDateRange(@Param("userId") String userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        // findByUserIdAndMoodAndDateRange
        @Query("SELECT j FROM Journal j WHERE j.user.id = :userId AND j.mood = :mood AND j.createdAt >= :startDate AND j.createdAt <= :endDate ORDER BY j.createdAt DESC")
        List<Journal> findByUserIdAndMoodAndDateRange(@Param("userId") String userId,
                        @Param("mood") Mood mood,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT j FROM Journal j WHERE j.user.id = :userId AND j.mood = :mood ORDER BY j.createdAt DESC")
        List<Journal> findByUserIdAndMood(@Param("userId") String userId, @Param("mood") Mood mood);

        @Query("SELECT COUNT(j) FROM Journal j WHERE j.user.id = :userId")
        long countByUserId(@Param("userId") String userId);

        @Query("SELECT COUNT(j) FROM Journal j WHERE j.user.id = :userId AND j.createdAt >= :startDate AND j.createdAt <= :endDate")
        long countByUserIdAndDateRange(@Param("userId") String userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        @Query("SELECT j.mood, COUNT(j) FROM Journal j WHERE j.user.id = :userId AND j.createdAt >= :startDate AND j.createdAt <= :endDate GROUP BY j.mood")
        List<Object[]> getMoodDistribution(@Param("userId") String userId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);
}
