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
@Table(name = "profile_stat")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileStat extends CommonDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "profile_stat_id")
    private Long profileStatId;

    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @Column(name = "total_works", nullable = false)
    private int totalWorks;

    @Column(name = "total_awards", nullable = false)
    private int totalAwards;

    @Column(name = "total_earnings", nullable = false)
    private int totalEarnings;

    @Column(name = "followers", nullable = false)
    private int followers;

    public ProfileStat(Long artistId, int totalWorks, int totalAwards, int totalEarnings, int followers) {
        this.artistId = artistId;
        this.totalWorks = totalWorks;
        this.totalAwards = totalAwards;
        this.totalEarnings = totalEarnings;
        this.followers = followers;
    }

    public void addAwards(int amount) {
        if (amount > 0) {
            this.totalAwards += amount;
        }
    }

    public void addEarnings(int amount) {
        if (amount > 0) {
            this.totalEarnings += amount;
        }
    }

    public void addWorks(int amount) {
        if (amount > 0) {
            this.totalWorks += amount;
        }
    }

    public void removeWorks(int amount) {
        if (amount > 0) {
            this.totalWorks = Math.max(0, this.totalWorks - amount);
        }
    }

    public void updateFollowers(int followers) {
        this.followers = Math.max(0, followers);
    }
}
