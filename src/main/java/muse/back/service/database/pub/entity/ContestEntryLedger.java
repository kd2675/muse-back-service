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

@Entity
@Table(name = "contest_entry_ledger")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContestEntryLedger extends CommonDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contest_entry_ledger_id")
    private Long id;

    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @Column(name = "contest_id", nullable = false)
    private Long contestId;

    @Column(name = "delta", nullable = false)
    private int delta;

    @Column(name = "reason", length = 30, nullable = false)
    private String reason;

    @Column(name = "ref_id", length = 100)
    private String refId;

    public ContestEntryLedger(
            Long artistId,
            Long contestId,
            int delta,
            String reason,
            String refId
    ) {
        this.artistId = artistId;
        this.contestId = contestId;
        this.delta = delta;
        this.reason = reason;
        this.refId = refId;
    }
}
