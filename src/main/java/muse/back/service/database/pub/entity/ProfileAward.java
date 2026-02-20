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
@Table(name = "profile_award")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileAward extends CommonDateEntity {

    @Id
    @Column(name = "award_id")
    private Long awardId;

    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @Column(name = "contest", length = 200, nullable = false)
    private String contest;

    @Column(name = "rank_label", length = 20, nullable = false)
    private String rankLabel;

    @Column(name = "prize", length = 50, nullable = false)
    private String prize;

    @Column(name = "period", length = 20, nullable = false)
    private String period;

    public ProfileAward(
            Long awardId,
            Long artistId,
            String contest,
            String rankLabel,
            String prize,
            String period
    ) {
        this.awardId = awardId;
        this.artistId = artistId;
        this.contest = contest;
        this.rankLabel = rankLabel;
        this.prize = prize;
        this.period = period;
    }
}
