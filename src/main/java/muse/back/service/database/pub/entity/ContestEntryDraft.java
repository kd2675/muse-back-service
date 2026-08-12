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
@Table(name = "contest_entry_draft")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContestEntryDraft extends CommonDateEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contest_entry_draft_id")
    private Long contestEntryDraftId;
    @Column(name = "artist_id", nullable = false)
    private Long artistId;
    @Column(name = "contest_id", nullable = false)
    private Long contestId;
    @Column(name = "title", length = 200)
    private String title;
    @Column(name = "description", length = 2000)
    private String description;

    public ContestEntryDraft(Long artistId, Long contestId, String title, String description) {
        this.artistId = artistId;
        this.contestId = contestId;
        update(title, description);
    }

    public void update(String title, String description) {
        this.title = title;
        this.description = description;
    }
}
