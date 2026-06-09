#!/bin/bash
# ============================================================
# DentFlow - Database Restore Script
# Usage: ./restore.sh <backup_file>
# Example: ./restore.sh ../backups/backup_20260607_120000.sql.gz
# ============================================================

set -e

# Configuration
CONTAINER_NAME="postgres"
DB_NAME="${POSTGRES_DB:-dentflow}"
DB_USER="${POSTGRES_USER:-dentflow}"

# Check arguments
if [ -z "$1" ]; then
    echo "Usage: $0 <backup_file>"
    echo ""
    echo "Available backups:"
    ls -lh ../backups/*.sql.gz 2>/dev/null || echo "No backups found"
    exit 1
fi

BACKUP_FILE="$1"

# Check if file exists
if [ ! -f "${BACKUP_FILE}" ]; then
    echo "ERROR: File ${BACKUP_FILE} does not exist!"
    exit 1
fi

echo "=========================================="
echo "DentFlow - Database Restore"
echo "=========================================="
echo "Database: ${DB_NAME}"
echo "Container: ${CONTAINER_NAME}"
echo "Backup file: ${BACKUP_FILE}"
echo ""

# Check if container is running
if ! docker ps | grep -q "${CONTAINER_NAME}"; then
    echo "ERROR: Container ${CONTAINER_NAME} is not running!"
    echo "Start with: docker-compose up -d postgres"
    exit 1
fi

# Confirmation
echo "WARNING: This will replace the current database!"
read -p "Continue? (y/N): " CONFIRM
if [ "${CONFIRM}" != "y" ] && [ "${CONFIRM}" != "Y" ]; then
    echo "Canceled."
    exit 0
fi

# Decompress if compressed
if [[ "${BACKUP_FILE}" == *.gz ]]; then
    echo "Decompressing backup..."
    TEMP_FILE=$(mktemp)
    gunzip -c "${BACKUP_FILE}" > "${TEMP_FILE}"
    RESTORE_FILE="${TEMP_FILE}"
else
    RESTORE_FILE="${BACKUP_FILE}"
fi

# Restore
echo "Restoring database..."
docker exec -i "${CONTAINER_NAME}" psql -U "${DB_USER}" -d "${DB_NAME}" < "${RESTORE_FILE}"

# Cleanup
if [ -n "${TEMP_FILE}" ]; then
    rm -f "${TEMP_FILE}"
fi

echo ""
echo "=========================================="
echo "Restore completed successfully!"
echo "=========================================="
echo "Database ${DB_NAME} has been restored."
echo ""
