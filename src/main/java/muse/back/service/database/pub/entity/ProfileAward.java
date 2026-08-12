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
@Table(name = "profile_award")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileAward extends CommonDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "award_id")
    private Long awardId;

    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @Column(name = "contest_id")
    private Long contestId;

    @Column(name = "entry_id", length = 64)
    private String entryId;

    @Column(name = "contest", length = 200, nullable = false)
    private String contest;

    @Column(name = "rank_label", length = 20, nullable = false)
    private String rankLabel;

    @Column(name = "prize", length = 50, nullable = false)
    private String prize;

    @Column(name = "period", length = 20, nullable = false)
    private String period;

    public ProfileAward(
            Long artistId,
            Long contestId,
            String entryId,
            String contest,
            String rankLabel,
            String prize,
            String period
    ) {
        this.artistId = artistId;
        this.contestId = contestId;
        this.entryId = entryId;
        this.contest = contest;
        this.rankLabel = rankLabel;
        this.prize = prize;
        this.period = period;
    }
}
