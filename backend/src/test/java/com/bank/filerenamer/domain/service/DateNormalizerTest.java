package com.bank.filerenamer.domain.service;

import com.bank.filerenamer.domain.exception.InvalidDateException;
import com.bank.filerenamer.domain.model.DateFormat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DateNormalizerTest {

    private final DateNormalizer normalizer = new DateNormalizer();

    @Test
    void normalizesYyyymmddUnchanged() {
        assertThat(normalizer.normalize("20260430", DateFormat.YYYYMMDD)).isEqualTo("20260430");
    }

    @Test
    void normalizesYyyyddmmToCanonical() {
        // 2026, día 30, mes 04 -> canónico 20260430
        assertThat(normalizer.normalize("20263004", DateFormat.YYYYDDMM)).isEqualTo("20260430");
    }

    @Test
    void rejectsInvalidMonth() {
        assertThatThrownBy(() -> normalizer.normalize("20261345", DateFormat.YYYYMMDD))
                .isInstanceOf(InvalidDateException.class);
    }

    @Test
    void rejectsImpossibleDay() {
        assertThatThrownBy(() -> normalizer.normalize("20260230", DateFormat.YYYYMMDD))
                .isInstanceOf(InvalidDateException.class);
    }

    @Test
    void rejectsBlankToken() {
        assertThatThrownBy(() -> normalizer.normalize("", DateFormat.YYYYMMDD))
                .isInstanceOf(InvalidDateException.class);
    }
}
