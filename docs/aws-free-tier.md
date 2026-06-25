# Despliegue en AWS Free Tier (opcional)

La solución está diseñada *cloud-native* sobre LocalStack para desarrollo. Migrar a AWS real no
requiere cambios en el dominio: gracias a `FileStoragePort` (AWS SDK v2) y a la configuración por
propiedades, solo cambian **endpoint y credenciales**.

## Mapeo de servicios

| Local (Docker Compose) | AWS Free Tier                                   |
|------------------------|-------------------------------------------------|
| LocalStack S3          | **Amazon S3** (bucket real)                     |
| PostgreSQL (contenedor)| **Amazon RDS PostgreSQL** (db.t3.micro, 12 meses)|
| backend (contenedor)   | **EC2 t2.micro** o **Elastic Beanstalk**        |
| frontend (nginx)       | **S3 + CloudFront** (hosting estático) o nginx en la EC2 |

## Cambios de configuración (sin tocar código)

El backend lee estas propiedades/variables de entorno:

```properties
# Quitar el endpoint para usar el S3 real de AWS
app.s3.endpoint=            # vacío → SDK usa el endpoint regional de AWS
app.s3.bucket=mi-bucket-real
app.s3.region=us-east-1
app.s3.path-style-access=false   # S3 real usa virtual-hosted style

# RDS
DB_URL=jdbc:postgresql://<rds-endpoint>:5432/filerenamer
DB_USERNAME=...
DB_PASSWORD=...
```

Las credenciales en AWS se resuelven con la **cadena por defecto** del SDK (rol IAM de la instancia
EC2 / variables de entorno), evitando llaves embebidas.

## Pasos resumidos

1. Crear bucket S3 y rol IAM con permisos `s3:ListBucket` y `s3:PutObject` sobre el bucket.
2. Crear RDS PostgreSQL Free Tier; aplicar Flyway al arrancar el backend (automático).
3. Empaquetar el backend (`mvn -DskipTests package`) y desplegar el JAR en EC2/Beanstalk con las
   variables de entorno anteriores y el rol IAM adjunto.
4. Publicar el build de Angular (`npm run build`) en S3 + CloudFront, o servirlo con nginx en la EC2.

## Evolución asíncrona (fuera de alcance)

Para grandes volúmenes, el procesamiento podría desacoplarse con **SQS** (S3 event → SQS → worker).
El diseño hexagonal lo permite añadiendo un adaptador de entrada (listener SQS) que invoque el mismo
`ProcessFilesUseCase`, sin cambiar dominio ni aplicación.
