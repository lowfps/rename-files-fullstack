package com.bank.filerenamer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuración del almacenamiento S3. Apunta a LocalStack en local y al S3 real en AWS sin
 * cambiar el código del dominio (solo propiedades/endpoint).
 */
@ConfigurationProperties(prefix = "app.s3")
public class S3Properties {

    /** Bucket de origen de los archivos. */
    private String bucket = "incoming-files";
    /** Archivos de muestra que siembra la simulación de carga batch. */
    private List<String> sampleFiles = List.of(
            "PHO_CD_DES_20260430",
            "PHO_SV_20260430",
            "PHO_CK_20260430.txt",
            "PHO_ML_UTIL_20260430.txt",
            "cuotas_bdb_20260430.txt",
            "garantias_solo_firma_20260430.txt",
            "activos_inmob_bdb_20260430.txt",
            "PrendasPajaro.txt",
            // --- Lote adicional para probar los 4 estados del contador ---
            "PHO_CD_DES_20260615",
            "PHO_SV_20260601",
            "garantias_finca_rural_20260501.txt",
            "PHO_CD_DES_20261301",
            "PHO_SV_20260231",
            "PHO_CK_20269999.txt",
            "PHO_XX_20260430.txt",
            "reporte_consolidado_mensual.txt");
    /** Endpoint S3; nulo para usar el endpoint real de AWS. */
    private String endpoint;
    private String region = "us-east-1";
    private String accessKey = "test";
    private String secretKey = "test";
    /** LocalStack requiere acceso path-style. */
    private boolean pathStyleAccess = true;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public List<String> getSampleFiles() {
        return sampleFiles;
    }

    public void setSampleFiles(List<String> sampleFiles) {
        this.sampleFiles = sampleFiles;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean isPathStyleAccess() {
        return pathStyleAccess;
    }

    public void setPathStyleAccess(boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }
}
