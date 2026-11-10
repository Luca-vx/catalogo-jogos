#!/usr/bin/env bash

# Script para finalizar todos os serviços do ecossistema catalogo-jogos

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="${ROOT_DIR}/logs"

echo "Parando todos os serviços do catálogo de jogos..."

for service in gateway servico-a servico-b servico-c; do
    PID_FILE="${LOG_DIR}/${service}.pid"
    if [ -f "${PID_FILE}" ]; then
        PID=$(cat "${PID_FILE}")
        if kill -0 "${PID}" 2>/dev/null; then
            echo "Finalizando ${service} (PID: ${PID})..."
            kill "${PID}" 2>/dev/null || true
        fi
        rm -f "${PID_FILE}"
    fi
done

# Encerramento garantido por portas
for PORT in 8080 8081 8082 8083; do
    PID_PORT=$(lsof -t -i :${PORT} 2>/dev/null)
    if [ -n "${PID_PORT}" ]; then
        echo "Liberando porta ${PORT} (PID: ${PID_PORT})..."
        kill -9 "${PID_PORT}" 2>/dev/null || true
    fi
done

echo "Todos os serviços foram finalizados com sucesso."

