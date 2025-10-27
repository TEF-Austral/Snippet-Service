#!/bin/bash

# Verificar que existan las credenciales
if [ -z "$GITHUB_ACTOR" ] || [ -z "$REDIS_TOKEN" ]; then
    echo "❌ Error: GITHUB_ACTOR y REDIS_TOKEN deben estar configurados"
    echo "Opciones:"
    echo "1. Exporta las variables: export GITHUB_ACTOR=tu-usuario REDIS_TOKEN=tu-token"
    echo "2. O crea un archivo .env con estas variables"
    exit 1
fi

echo "✅ Construyendo imagen Docker..."
docker build \
  --build-arg GITHUB_ACTOR="$GITHUB_ACTOR" \
  --build-arg REDIS_TOKEN="$REDIS_TOKEN" \
  -t snippet-api .

echo "✅ Build completado!"