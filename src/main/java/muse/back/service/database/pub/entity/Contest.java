package muse.back.service.database.pub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muse.back.service.common.jpa.CommonDateEntity;
import java.time.LocalDateTime;

@Entity
@Table(name = "contest")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contest extends CommonDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contest_id")
    private Long contestId;

    @Column(name = "theme", length = 200, nullable = false)
    private String theme;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "period", length = 50, nullable = false)
    private String period;

    @Column(name = "entry_fee", nullable = false)
    private int entryFee;

    @Column(name = "prize_pool", nullable = false)
    private int prizePool;

    @Column(name = "days_left", nullable = false)
    private int daysLeft;

    @Column(name = "submission_start_at")
    private LocalDateTime submissionStartAt;

    @Column(name = "submission_end_at")
    private LocalDateTime submissionEndAt;

    @Column(name = "voting_start_at")
    private LocalDateTime votingStartAt;

    @Column(name = "voting_end_at")
    private LocalDateTime votingEndAt;

    @Column(name = "participation_count", nullable = false)
    private int participationCount;

    public Contest(
            String theme,
            String description,
            String period,
            int entryFee,
            int prizePool,
            int daysLeft,
            LocalDateTime submissionStartAt,
            LocalDateTime submissionEndAt,
            LocalDateTime votingStartAt,
            LocalDateTime votingEndAt,
            int participationCount
    ) {
        this(
                null,
                theme,
                description,
                period,
                entryFee,
                prizePool,
                daysLeft,
                submissionStartAt,
                submissionEndAt,
                votingStartAt,
                votingEndAt,
                participationCount
        );
    }

    public Contest(
            Long contestId,
            String theme,
            String description,
            String period,
            int entryFee,
            int prizePool,
            int daysLeft,
            LocalDateTime submissionStartAt,
            LocalDateTime submissionEndAt,
            LocalDateTime votingStartAt,
            LocalDateTime votingEndAt,
            int participationCount
    ) {
        this.contestId = contestId;
        this.theme = theme;
        this.description = description;
        this.period = period;
        this.entryFee = entryFee;
        this.prizePool = prizePool;
        this.daysLeft = daysLeft;
        this.submissionStartAt = submissionStartAt;
        this.submissionEndAt = submissionEndAt;
        this.votingStartAt = votingStartAt;
        this.votingEndAt = votingEndAt;
        this.participationCount = participationCount;
    }

    public void increaseParticipationCount() {
        this.participationCount += 1;
    }

    public void decreaseParticipationCount() {
        if (this.participationCount > 0) {
            this.participationCount -= 1;
        }
    }

    public void updateContestInfo(
            String theme,
            String description,
            String period,
            int entryFee,
            int prizePool,
            int daysLeft,
            LocalDateTime submissionStartAt,
            LocalDateTime submissionEndAt,
            LocalDateTime votingStartAt,
            LocalDateTime votingEndAt
    ) {
        this.theme = theme;
        this.description = description;
        this.period = period;
        this.entryFee = entryFee;
        this.prizePool = prizePool;
        this.daysLeft = daysLeft;
        this.submissionStartAt = submissionStartAt;
        this.submissionEndAt = submissionEndAt;
        this.votingStartAt = votingStartAt;
        this.votingEndAt = votingEndAt;
    }
}
