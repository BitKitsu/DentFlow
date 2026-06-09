#!/bin/bash
# ============================================================
# DentFlow - Database Backup Script
# Usage: ./backup.sh [backup_name]
# Example: ./backup.sh before_migration
# ============================================================

set -e

# Configuration
CONTAINER_NAME="postgres"
DB_NAME="${POSTGRES_DB:-dentflow}"
DB_USER="${POSTGRES_USER:-dentflow}"

# Backup name
BACKUP_NAME="${1:-backup_$(date +%Y%m%d_%H%M%S)}"
BACKUP_DIR="../backups"
BACKUP_FILE="${BACKUP_DIR}/${BACKUP_NAME}.sql"
BACKUP_GZ="${BACKUP_DIR}/${BACKUP_NAME}.sql.gz"

# Create backup directory
mkdir -p "${BACKUP_DIR}"

echo "=========================================="
echo "DentFlow - Database Backup"
echo "=========================================="
echo "Database: ${DB_NAME}"
echo "Container: ${CONTAINER_NAME}"
echo "File: ${BACKUP_FILE}"
echo ""

# Check if container is running
if ! docker ps | grep -q "${CONTAINER_NAME}"; then
    echo "ERROR: Container ${CONTAINER_NAME} is not running!"
    echo "Start with: docker-compose up -d postgres"
    exit 1
fi

# SQL backup
echo "Creating SQL backup..."
docker exec -t "${CONTAINER_NAME}" pg_dump -U "${DB_USER}" -d "${DB_NAME}" --no-owner --no-acl > "${BACKUP_FILE}"

# Compression
echo "Compressing backup..."
gzip "${BACKUP_FILE}"

# Backup information
FILESIZE=$(du -h "${BACKUP_GZ}" | cut -f1)
echo ""
echo "=========================================="
echo "Backup completed successfully!"
echo "=========================================="
echo "File: ${BACKUP_GZ}"
echo "Size: ${FILESIZE}"
echo "Date: $(date)"
echo ""

# List recent backups
echo "Recent backups:"
ls -lh "${BACKUP_DIR}"/*.sql.gz 2>/dev/null | tail -5
