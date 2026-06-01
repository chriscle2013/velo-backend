#!/bin/bash
set -e

# Transforma DATABASE_URL para que tenga el formato jdbc:postgresql://...?sslmode=require
export DATABASE_URL="jdbc:${DATABASE_URL}?sslmode=require"

# Ejecuta el comando principal
exec java -jar app.jar
