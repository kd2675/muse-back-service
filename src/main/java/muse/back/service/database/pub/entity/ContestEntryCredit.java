package muse.back.service.database.pub.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import muse.back.service.common.jpa.CommonDateEntity;

@Entity
@Table(
        name = "contest_entry_credit",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_contest_entry_credit_artist_contest", columnNames = {
                        "artist_id",
                        "contest_id"
                })
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContestEntryCredit extends CommonDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contest_entry_credit_id")
    private Long id;

    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @Column(name = "contest_id", nullable = false)
    private Long contestId;

    @Column(name = "balance", nullable = false)
    private int balance;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    public ContestEntryCredit(Long artistId, Long contestId, int balance) {
        this.artistId = artistId;
        this.contestId = contestId;
        this.balance = balance;
    }

    public void increase(int amount) {
        this.balance += amount;
    }

    public void decrease(int amount) {
        this.balance -= amount;
    }
}
