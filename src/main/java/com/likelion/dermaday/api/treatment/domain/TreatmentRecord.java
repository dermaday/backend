package com.likelion.dermaday.api.treatment.domain;

import com.likelion.dermaday.api.member.domain.Member;
import com.likelion.dermaday.common.entity.BaseTimeEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "treatment_records")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TreatmentRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TreatmentRecordSource source;

    @OneToMany(mappedBy = "treatmentRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("treatedOn DESC, id ASC")
    private List<TreatmentItem> items = new ArrayList<>();

    private TreatmentRecord(Member member, TreatmentRecordSource source, List<TreatmentItem> items) {
        this.member = Objects.requireNonNull(member, "Member must not be null");
        this.source = Objects.requireNonNull(source, "Treatment record source must not be null");
        addItems(items);
    }

    public static TreatmentRecord create(
            Member member,
            TreatmentRecordSource source,
            List<TreatmentItem> items
    ) {
        return new TreatmentRecord(member, source, items);
    }

    public LocalDate latestTreatedOn() {
        return items.stream()
                .map(TreatmentItem::getTreatedOn)
                .max(LocalDate::compareTo)
                .orElseThrow(() -> new IllegalStateException("Treatment record must contain at least one item"));
    }

    private void addItems(List<TreatmentItem> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("At least one treatment item is required");
        }

        Set<TreatmentType> uniqueTypes = new HashSet<>();
        for (TreatmentItem item : items) {
            Objects.requireNonNull(item, "Treatment item must not be null");
            if (!uniqueTypes.add(item.getTreatmentType())) {
                throw new IllegalArgumentException("Duplicate treatment types are not allowed");
            }
            item.attachTo(this);
            this.items.add(item);
        }
    }
}
