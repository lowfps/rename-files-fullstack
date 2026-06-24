package com.bank.filerenamer.domain.service;

import com.bank.filerenamer.domain.exception.TemplateRenderException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TemplateRendererTest {

    private final TemplateRenderer renderer = new TemplateRenderer();

    @Test
    void rendersDatePlaceholder() {
        String out = renderer.render("01_Estructura CDT Desmaterializado_{date}",
                Map.of("date", "20260430"));
        assertThat(out).isEqualTo("01_Estructura CDT Desmaterializado_20260430");
    }

    @Test
    void rendersTemplateWithoutPlaceholders() {
        assertThat(renderer.render("13_CUOTAS Activos", Map.of())).isEqualTo("13_CUOTAS Activos");
    }

    @Test
    void rendersMultipleNamedGroups() {
        String out = renderer.render("37_Leasing_{tipo}", Map.of("tipo", "Vehículo"));
        assertThat(out).isEqualTo("37_Leasing_Vehículo");
    }

    @Test
    void failsWhenPlaceholderHasNoValue() {
        assertThatThrownBy(() -> renderer.render("01_{date}", Map.of()))
                .isInstanceOf(TemplateRenderException.class);
    }
}
