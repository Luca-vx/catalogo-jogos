#!/usr/bin/env bash

# Script para iniciar todos os serviços do ecossistema catalogo-jogos

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
LOG_DIR="${ROOT_DIR}/logs"
mkdir -p "${LOG_DIR}"

echo "=================================================="
echo "Iniciando Serviços do Catálogo de Jogos..."
echo "=================================================="

# 1. Serviço C - Loja Externa (Porta 8083)
echo "Iniciando Serviço C (Loja Externa) na porta 8083..."
nohup java -jar "${ROOT_DIR}/servico-c-loja/target/servico-c-loja-0.0.1-SNAPSHOT.jar" > "${LOG_DIR}/servico-c.log" 2>&1 &
PID_C=$!
echo ${PID_C} > "${LOG_DIR}/servico-c.pid"

# 2. Serviço B - Avaliações (Porta 8082)
echo "Iniciando Serviço B (Avaliações) na porta 8082..."
nohup java -jar "${ROOT_DIR}/servico-b-avaliacoes/target/servico-b-avaliacoes-0.0.1-SNAPSHOT.jar" > "${LOG_DIR}/servico-b.log" 2>&1 &
PID_B=$!
echo ${PID_B} > "${LOG_DIR}/servico-b.pid"

# 3. Serviço A - Catálogo (Porta 8081)
echo "Iniciando Serviço A (Catálogo) na porta 8081..."
nohup java -jar "${ROOT_DIR}/servico-a-catalogo/target/servico-a-catalogo-0.0.1-SNAPSHOT.jar" > "${LOG_DIR}/servico-a.log" 2>&1 &
PID_A=$!
echo ${PID_A} > "${LOG_DIR}/servico-a.pid"

# 4. Gateway / Entrada Principal (Porta 8080)
echo "Iniciando Gateway (Entrada Principal / Proxy / Aggregator / Adapter) na porta 8080..."
nohup java -jar "${ROOT_DIR}/gateway-service/target/gateway-service-0.0.1-SNAPSHOT.jar" > "${LOG_DIR}/gateway.log" 2>&1 &
PID_GW=$!
echo ${PID_GW} > "${LOG_DIR}/gateway.pid"

echo ""
echo "Todos os serviços foram disparados em background!"
echo "PIDs: Gateway=${PID_GW}, ServicoA=${PID_A}, ServicoB=${PID_B}, ServicoC=${PID_C}"
echo "Aguarde cerca de 5 a 10 segundos para a inicialização completa das aplicações."
echo "Logs disponíveis na pasta: ${LOG_DIR}"

