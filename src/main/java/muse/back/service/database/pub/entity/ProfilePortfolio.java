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
@Table(name = "profile_portfolio")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfilePortfolio extends CommonDateEntity {

    @Id
    @Column(name = "portfolio_id")
    private Long portfolioId;

    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "color_from", length = 20)
    private String colorFrom;

    @Column(name = "color_to", length = 20)
    private String colorTo;
}
