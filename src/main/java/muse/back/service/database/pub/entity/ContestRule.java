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
@Table(name = "contest_rule")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContestRule extends CommonDateEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "contest_rule_id")
    private Long contestRuleId;

    @Column(name = "contest_id", nullable = false)
    private Long contestId;

    @Column(name = "rule_text", length = 255, nullable = false)
    private String ruleText;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public ContestRule(Long contestId, String ruleText, int sortOrder) {
        this.contestId = contestId;
        this.ruleText = ruleText;
        this.sortOrder = sortOrder;
    }
}
