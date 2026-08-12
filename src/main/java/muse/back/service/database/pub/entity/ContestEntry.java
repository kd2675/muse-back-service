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
@Table(name = "contest_entry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContestEntry extends CommonDateEntity {

    @Id
    @Column(name = "entry_id", length = 64)
    private String entryId;

    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @Column(name = "contest_id", nullable = false)
    private Long contestId;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "description", length = 2000)
    private String description;

    @Column(name = "file_name", length = 255, nullable = false)
    private String fileName;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "status", length = 20, nullable = false)
    private String status;

    public ContestEntry(
            String entryId,
            Long artistId,
            Long contestId,
            String title,
            String description,
            String fileName,
            String status
    ) {
        this(entryId, artistId, contestId, title, description, fileName, null, status);
    }

    public ContestEntry(
            String entryId,
            Long artistId,
            Long contestId,
            String title,
            String description,
            String fileName,
            String imageUrl,
            String status
    ) {
        this.entryId = entryId;
        this.artistId = artistId;
        this.contestId = contestId;
        this.title = title;
        this.description = description;
        this.fileName = fileName;
        this.imageUrl = imageUrl;
        this.status = status;
    }

    public void updateStatus(String status) {
        this.status = status;
    }
}
