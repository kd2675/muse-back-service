package muse.back.service.database.pub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muse.back.service.common.jpa.CommonDateEntity;

@Entity
@Table(name = "contest")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contest extends CommonDateEntity {

    @Id
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

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @Column(name = "participation_count", nullable = false)
    private int participationCount;

    public void increaseParticipationCount() {
        this.participationCount += 1;
    }
}
