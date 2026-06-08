#!/bin/bash
# ============================================================
# DentFlow - Database Test Data Initialization Script
# Usage: ./init-test-data.sh
# ============================================================

set -e

# Configuration
CONTAINER_NAME="postgres"
DB_NAME="${POSTGRES_DB:-dentflow}"
DB_USER="${POSTGRES_USER:-dentflow}"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SCHEMA_FILE="${SCRIPT_DIR}/../init_schema_with_data.sql"

echo "=========================================="
echo "DentFlow - Database Initialization"
echo "=========================================="
echo "Database: ${DB_NAME}"
echo "Container: ${CONTAINER_NAME}"
echo ""

# Check if container is running
if ! docker ps | grep -q "${CONTAINER_NAME}"; then
    echo "ERROR: Container ${CONTAINER_NAME} is not running!"
    echo "Start with: docker-compose up -d postgres"
    exit 1
fi

# Check if schema file exists
if [ ! -f "${SCHEMA_FILE}" ]; then
    echo "ERROR: File ${SCHEMA_FILE} does not exist!"
    exit 1
fi

# Confirmation
echo "WARNING: This will replace the current database!"
read -p "Continue? (y/N): " CONFIRM
if [ "${CONFIRM}" != "y" ] && [ "${CONFIRM}" != "Y" ]; then
    echo "Canceled."
    exit 0
fi

# Initialize
echo "Initializing database..."
docker exec -i "${CONTAINER_NAME}" psql -U "${DB_USER}" -d "${DB_NAME}" < "${SCHEMA_FILE}"

echo ""
echo "=========================================="
echo "Initialization completed successfully!"
echo "=========================================="
echo ""
echo "Test data:"
echo "- 2 clinics (DentCare Krakow, SmileClinic Warszawa)"
echo "- 10 users"
echo "- 5 staff members"
echo "- 10 patients"
echo "- 20 services"
echo "- 24 appointments"
echo "- 8 notifications"
echo ""
