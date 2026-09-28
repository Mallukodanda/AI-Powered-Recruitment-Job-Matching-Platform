package com.recruitment.platform.repository;

import com.recruitment.platform.model.Interview;
import com.recruitment.platform.model.InterviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface InterviewRepository extends JpaRepository<Interview, Long> {

    List<Interview> findByCandidateIdOrderByScheduledTimeAsc(Long candidateId);

    List<Interview> findByJobIdOrderByScheduledTimeAsc(Long jobId);

    List<Interview> findByInterviewerIdOrderByScheduledTimeAsc(Long interviewerId);

    List<Interview> findByStatus(InterviewStatus status);

    long countByStatus(InterviewStatus status);

    List<Interview> findByInterviewerIdAndStatus(Long interviewerId, InterviewStatus status);

    List<Interview> findByApplicationId(Long applicationId);

    @Query("SELECT i.status, COUNT(i) FROM Interview i GROUP BY i.status")
    List<Object[]> countInterviewsGroupedByStatus();

    /**
     * Checks if the interviewer already has an active SCHEDULED interview at the designated time slot.
     */
    @Query("SELECT i FROM Interview i WHERE i.interviewer.id = :interviewerId " +
           "AND i.status = :status " +
           "AND i.scheduledTime < :slotEnd " +
           "AND i.scheduledTime >= :slotStart")
    List<Interview> findInterviewerConflictingSlots(
            @Param("interviewerId") Long interviewerId,
            @Param("status") InterviewStatus status,
            @Param("slotStart") ZonedDateTime slotStart,
            @Param("slotEnd") ZonedDateTime slotEnd
    );
}
