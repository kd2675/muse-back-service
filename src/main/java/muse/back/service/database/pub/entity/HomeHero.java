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
@Table(name = "home_hero")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HomeHero extends CommonDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "home_hero_id")
    private Long homeHeroId;

    @Column(name = "badge", length = 60, nullable = false)
    private String badge;

    @Column(name = "headline", length = 255, nullable = false)
    private String headline;

    @Column(name = "subheadline", length = 255, nullable = false)
    private String subheadline;

    @Column(name = "description", length = 500, nullable = false)
    private String description;
}
