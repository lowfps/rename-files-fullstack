package com.bank.filerenamer.domain.service;

import com.bank.filerenamer.domain.model.DateFormat;
import com.bank.filerenamer.domain.model.RenameResult;
import com.bank.filerenamer.domain.model.RenameRule;
import com.bank.filerenamer.domain.model.RenameStatus;
import com.bank.filerenamer.domain.model.S3File;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEngineTest {

    private final RuleEngine engine = new RuleEngine(new DateNormalizer(), new TemplateRenderer());

    // --- Catálogo de reglas que refleja los ejemplos del reto ---
    private final List<RenameRule> rules = List.of(
            rule("01", "^PHO_CD_DES_(?<date>\\d{8})$", "01_Estructura CDT Desmaterializado_{date}", DateFormat.YYYYMMDD, 10),
            rule("03", "^PHO_SV_(?<date>\\d{8})$", "03_Estructura Cuenta Ahorros_{date}", DateFormat.YYYYMMDD, 20),
            rule("04", "^PHO_CK_(?<date>\\d{8})$", "04_Estructura Cuenta Corriente_{date}", DateFormat.YYYYMMDD, 30),
            rule("13", "^PHO_ML_UTIL_(?<date>\\d{8})$", "13_CUOTAS Activos", DateFormat.NONE, 40),
            rule("13", "^cuotas_bdb_(?<date>\\d{8})$", "13_CUOTAS Activos", DateFormat.NONE, 41),
            rule("14", "^garantias_.*$", "14_Hipotecaria", DateFormat.NONE, 50),
            rule("37", "^activos_inmob_bdb_(?<date>\\d{8})$", "37_Leasing_Vehículo", DateFormat.NONE, 60),
            rule("03A", "^PHO_SV2_(?<date>\\d{8})$", "03_Estructura Cuenta Ahorros_{date}", DateFormat.YYYYDDMM, 25)
    );

    @Test
    void transformsCdtWithDate() {
        RenameResult r = engine.apply(new S3File("PHO_CD_DES_20260430"), rules);
        assertThat(r.status()).isEqualTo(RenameStatus.TRANSFORMED);
        assertThat(r.targetFileName()).isEqualTo("01_Estructura CDT Desmaterializado_20260430");
        assertThat(r.appliedRuleCode()).isEqualTo("01");
    }

    @Test
    void transformsSavingsAccount() {
        RenameResult r = engine.apply(new S3File("PHO_SV_20260430"), rules);
        assertThat(r.targetFileName()).isEqualTo("03_Estructura Cuenta Ahorros_20260430");
    }

    @Test
    void stripsTxtExtensionBeforeMatching() {
        RenameResult r = engine.apply(new S3File("cuotas_bdb_20260430.txt"), rules);
        assertThat(r.status()).isEqualTo(RenameStatus.TRANSFORMED);
        assertThat(r.targetFileName()).isEqualTo("13_CUOTAS Activos");
    }

    @Test
    void transformsLeasingWithoutDateInOutput() {
        RenameResult r = engine.apply(new S3File("activos_inmob_bdb_20260430.txt"), rules);
        assertThat(r.targetFileName()).isEqualTo("37_Leasing_Vehículo");
    }

    @Test
    void normalizesYyyyddmmEmbeddedDate() {
        RenameResult r = engine.apply(new S3File("PHO_SV2_20263004"), rules);
        assertThat(r.targetFileName()).isEqualTo("03_Estructura Cuenta Ahorros_20260430");
    }

    @Test
    void marksUnknownFileAsNoMapeado() {
        RenameResult r = engine.apply(new S3File("PrendasPajaro.txt"), rules);
        assertThat(r.status()).isEqualTo(RenameStatus.NO_MAPEADO);
        assertThat(r.targetFileName()).isNull();
    }

    @Test
    void marksInvalidEmbeddedDateAsError() {
        RenameResult r = engine.apply(new S3File("PHO_CD_DES_20261345"), rules);
        assertThat(r.status()).isEqualTo(RenameStatus.ERROR);
        assertThat(r.appliedRuleCode()).isEqualTo("01");
    }

    @Test
    void resolvesMultipleMatchesByPriority() {
        // Regla genérica (prioridad alta=menos preferente) frente a la específica "01".
        RenameRule generic = rule("99", "^PHO_.*$", "99_Generico", DateFormat.NONE, 1000);
        List<RenameRule> withGeneric = new java.util.ArrayList<>(rules);
        withGeneric.add(generic);
        RenameResult r = engine.apply(new S3File("PHO_CD_DES_20260430"), withGeneric);
        assertThat(r.appliedRuleCode()).isEqualTo("01");
    }

    @Test
    void inactiveRulesAreIgnored() {
        RenameRule inactive = new RenameRule(null, "77", "Inactiva",
                "^PrendasPajaro$", "77_NoDeberia", DateFormat.NONE, 1, false, 1);
        List<RenameRule> withInactive = new java.util.ArrayList<>(rules);
        withInactive.add(inactive);
        RenameResult r = engine.apply(new S3File("PrendasPajaro.txt"), withInactive);
        assertThat(r.status()).isEqualTo(RenameStatus.NO_MAPEADO);
    }

    private static RenameRule rule(String code, String pattern, String template, DateFormat fmt, int priority) {
        return new RenameRule(null, code, code + " desc", pattern, template, fmt, priority, true, 1);
    }
}
