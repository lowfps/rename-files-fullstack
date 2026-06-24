package com.bank.filerenamer.domain.model;

/**
 * Resultado de aplicar el motor de reglas a un único archivo de origen.
 *
 * @param sourceFileName    nombre original tal cual llegó al bucket
 * @param targetFileName    nombre estandarizado generado (nulo si ERROR o NO_MAPEADO)
 * @param status            estado del procesamiento
 * @param appliedRuleCode   código de la regla aplicada (nulo si NO_MAPEADO)
 * @param appliedRuleVersion versión de la regla aplicada (nulo si NO_MAPEADO)
 * @param message           detalle del error o de la regla aplicada
 */
public record RenameResult(
        String sourceFileName,
        String targetFileName,
        RenameStatus status,
        String appliedRuleCode,
        Integer appliedRuleVersion,
        String message
) {

    public static RenameResult transformed(String source, String target, RenameRule rule) {
        return new RenameResult(source, target, RenameStatus.TRANSFORMED,
                rule.code(), rule.version(), "Regla " + rule.code() + " (v" + rule.version() + ")");
    }

    public static RenameResult error(String source, RenameRule rule, String message) {
        return new RenameResult(source, null, RenameStatus.ERROR,
                rule.code(), rule.version(), message);
    }

    public static RenameResult noMapeado(String source) {
        return new RenameResult(source, null, RenameStatus.NO_MAPEADO,
                null, null, "No existe regla activa que aplique");
    }
}
