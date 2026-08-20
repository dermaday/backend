package com.likelion.dermaday.api.treatment.domain;

import com.likelion.dermaday.api.member.domain.Member;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TreatmentRecordTest {

    private final Member member = Member.createUser("회원");

    @Test
    void createsRecordWithMultipleItemsAndFindsLatestDate() {
        TreatmentItem older = item(TreatmentType.ULTHERA, LocalDate.of(2026, 7, 1));
        TreatmentItem latest = item(TreatmentType.OLIGIO, LocalDate.of(2026, 8, 10));

        TreatmentRecord record = TreatmentRecord.create(
                member,
                TreatmentRecordSource.MANUAL,
                List.of(older, latest)
        );

        assertEquals(LocalDate.of(2026, 8, 10), record.latestTreatedOn());
        assertSame(record, older.getTreatmentRecord());
        assertSame(record, latest.getTreatmentRecord());
        assertThrows(UnsupportedOperationException.class, () -> record.getItems().clear());
    }

    @Test
    void rejectsEmptyNullOrDuplicateItems() {
        assertThrows(IllegalArgumentException.class, () -> TreatmentRecord.create(
                member,
                TreatmentRecordSource.MANUAL,
                List.of()
        ));
        assertThrows(IllegalArgumentException.class, () -> TreatmentRecord.create(
                member,
                TreatmentRecordSource.MANUAL,
                null
        ));

        TreatmentItem first = item(TreatmentType.ULTHERA, LocalDate.of(2026, 7, 1));
        TreatmentItem duplicate = item(TreatmentType.ULTHERA, LocalDate.of(2026, 8, 1));
        assertThrows(IllegalArgumentException.class, () -> TreatmentRecord.create(
                member,
                TreatmentRecordSource.MANUAL,
                List.of(first, duplicate)
        ));
    }

    @Test
    void rejectsMissingAggregateValues() {
        TreatmentItem item = item(TreatmentType.ULTHERA, LocalDate.of(2026, 7, 1));

        assertThrows(NullPointerException.class, () -> TreatmentRecord.create(
                null,
                TreatmentRecordSource.MANUAL,
                List.of(item)
        ));
        assertThrows(NullPointerException.class, () -> TreatmentRecord.create(member, null, List.of(item)));
        assertThrows(NullPointerException.class, () -> TreatmentItem.create(
                null,
                LocalDate.of(2026, 7, 1),
                TreatmentReaction.COMFORTABLE
        ));
        assertThrows(NullPointerException.class, () -> TreatmentItem.create(
                TreatmentType.ULTHERA,
                null,
                TreatmentReaction.COMFORTABLE
        ));
        assertThrows(NullPointerException.class, () -> TreatmentItem.create(
                TreatmentType.ULTHERA,
                LocalDate.of(2026, 7, 1),
                null
        ));
    }

    private TreatmentItem item(TreatmentType type, LocalDate date) {
        return TreatmentItem.create(type, date, TreatmentReaction.COMFORTABLE);
    }
}
