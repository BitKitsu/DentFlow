# DentFlow Deployment Guide

## Requirements

- Java 21
- Maven 3.9+
- Docker + Docker Compose
- SendGrid account (for emails)
- S3-compatible storage account (e.g. AWS S3, MinIO, Railway Object Storage)

## 1. Environment Configuration

### Copy Environment Variables

```bash
cd DentFlow-PZ
cp .env.example .env
```

### Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `POSTGRES_DB` | Database name | `dentflow` |
| `POSTGRES_USER` | Database user | `dentflow` |
| `POSTGRES_PASSWORD` | Database password | `securepassword123` |
| `JWT_SECRET` | JWT secret (min. 32 chars) | `openssl rand -hex 32` |
| `JWT_EXPIRATION` | JWT expiration (ms) | `3600000` (1h) |
| `SENDGRID_API_KEY` | SendGrid API key | `SG.xxx` |
| `MAIL_HOST` | SMTP server | `smtp.sendgrid.net` |
| `MAIL_PORT` | SMTP port | `587` |
| `MAIL_USERNAME` | SMTP user | `apikey` |
| `AWS_ACCESS_KEY_ID` | S3 access key | `xxx` |
| `AWS_SECRET_ACCESS_KEY` | S3 secret key | `xxx` |
| `AWS_ENDPOINT_URL` | S3 endpoint URL | `https://s3.amazonaws.com` |
| `AWS_REGION` | S3 region | `us-east-1` |
| `S3_BUCKET_NAME` | Bucket name | `dentflow-files` |

### Generate JWT_SECRET

```bash
openssl rand -hex 32
```

## 2. Local Development

### Start Database and MinIO

```bash
docker-compose up -d postgres minio
```

### Build and Start Services

```bash
# Install dependencies
mvn install -DskipTests

# Terminal 1 - Identity Service
mvn spring-boot:run -pl identity-service -Dspring-boot.run.profiles=dev

# Terminal 2 - Core Service
mvn spring-boot:run -pl core-service/core-app -Dspring-boot.run.profiles=dev
```

### Verification

```bash
# Health check
curl http://localhost:8081/swagger-ui.html  # Identity Service
curl http://localhost:8080/swagger-ui.html  # Core Service
```

## 3. Docker Deployment

```bash
# Start all services
docker-compose up -d

# Check status
docker-compose ps

# Logs
docker-compose logs -f
```

## 4. Database Initialization

### Schema (automatic via Flyway)

Flyway automatically creates and updates the database schema on service startup.

### Test Data

```bash
# After starting the database
docker-compose exec -T postgres psql -U dentflow -d dentflow < ../DB/init_schema_with_data.sql
```

## 5. SendGrid Configuration

1. Create account at [SendGrid](https://sendgrid.com)
2. Generate API Key
3. Add environment variables:
   ```
   SENDGRID_API_KEY=SG.your-api-key
   MAIL_HOST=smtp.sendgrid.net
   MAIL_PORT=587
   MAIL_USERNAME=apikey
   ```

## 6. S3 Configuration

### Local Development (MinIO)

MinIO is included in `docker-compose.yml` and starts automatically with `docker-compose up`.
The bucket is created automatically by the `minio-init` container.

- API: http://localhost:9000
- Console: http://localhost:9001 (login: `minioadmin` / `minioadmin`)

### Production (AWS S3 / Railway)

1. Create an S3-compatible storage bucket
2. Generate access keys
3. Add environment variables:
   ```
   AWS_ACCESS_KEY_ID=your-access-key
   AWS_SECRET_ACCESS_KEY=your-secret-key
   AWS_ENDPOINT_URL=https://your-s3-endpoint.com
   AWS_REGION=us-east-1
   S3_BUCKET_NAME=dentflow-files
   ```

## 7. Production

### Configuration Changes

1. Disable `dev` profiles in application.yml
2. Set strong passwords and secrets
3. Enable SSL/TLS for database
4. Configure CORS for frontend domain
5. Enable WARN/ERROR level logging

### Docker Compose (production)

```yaml
# docker-compose.prod.yml
version: '3.8'
services:
  postgres:
    environment:
      POSTGRES_PASSWORD: ${PROD_DB_PASSWORD}
    volumes:
      - pgdata:/var/lib/postgresql/data
    
  identity-service:
    environment:
      JWT_SECRET: ${PROD_JWT_SECRET}
      SPRING_PROFILES_ACTIVE: production
    
  core-service:
    environment:
      SPRING_PROFILES_ACTIVE: production
```

### Running

```bash
docker-compose -f docker-compose.prod.yml up -d
```

## 8. Monitoring

### Health Endpoints

```bash
# Identity Service
curl http://localhost:8081/actuator/health

# Core Service
curl http://localhost:8080/actuator/health
```

### Logs

```bash
# Docker logs
docker-compose logs -f identity-service
docker-compose logs -f core-service

# File logs (production)
tail -f /var/log/dentflow/identity-service.log
tail -f /var/log/dentflow/core-service.log
```

## 9. Troubleshooting

### Problem: Database not starting
```bash
# Check logs
docker-compose logs postgres

# Check port
docker-compose ps

# Restart
docker-compose restart postgres
```

### Problem: Authorization not working
```bash
# Check JWT_SECRET
echo $JWT_SECRET

# Check token expiration
# Token expires after 1 hour (default)
```

### Problem: Files not uploading
```bash
# Check S3 configuration
echo $AWS_ACCESS_KEY_ID
echo $AWS_ENDPOINT_URL

# Check S3 logs
docker-compose logs core-service | grep -i s3
```

## 10. Backup and Restore

### Database Backup

```bash
# Backup
docker-compose exec -T postgres pg_dump -U dentflow dentflow > backup_$(date +%Y%m%d_%H%M%S).sql

# Backup with compression
docker-compose exec -T postgres pg_dump -U dentflow dentflow | gzip > backup_$(date +%Y%m%d_%H%M%S).sql.gz
```

### Database Restore

```bash
# Restore
docker-compose exec -T postgres psql -U dentflow -d dentflow < backup_20260607_120000.sql

# Restore from compressed backup
gunzip < backup_20260607_120000.sql.gz | docker-compose exec -T postgres psql -U dentflow -d dentflow
```
