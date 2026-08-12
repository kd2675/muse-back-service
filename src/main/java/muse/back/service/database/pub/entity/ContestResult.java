package muse.back.service.database.pub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muse.back.service.common.jpa.CommonDateEntity;

import java.time.LocalDateTime;

@Entity
@Table(name = "contest_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContestResult extends CommonDateEntity {

    @Id
    @Column(name = "contest_id")
    private Long contestId;

    @Column(name = "finalized_at", nullable = false)
    private LocalDateTime finalizedAt;

    @Column(name = "finalized_by", length = 64, nullable = false)
    private String finalizedBy;

    public ContestResult(Long contestId, LocalDateTime finalizedAt, String finalizedBy) {
        this.contestId = contestId;
        this.finalizedAt = finalizedAt;
        this.finalizedBy = finalizedBy;
    }
}
