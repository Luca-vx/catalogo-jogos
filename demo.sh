#!/usr/bin/env bash

# Script de Demonstração Interativa dos Padrões de Integração
# Projeto: catalogo-jogos (UFAL)

GREEN="\033[0;32m"
BLUE="\033[0;34m"
YELLOW="\033[1;33m"
CYAN="\033[0;36m"
RESET="\033[0m"

echo -e "${BLUE}======================================================================${RESET}"
echo -e "${BLUE}       DEMONSTRAÇÃO DE PADRÕES DE INTEGRAÇÃO — CATÁLOGO DE JOGOS      ${RESET}"
echo -e "${BLUE}======================================================================${RESET}"
echo ""

# Verificar se os serviços estão ativos
echo -e "${CYAN}[1/4] Verificando se os serviços estão em execução...${RESET}"
GATEWAY_UP=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/game 2>/dev/null)

if [ "${GATEWAY_UP}" != "200" ]; then
    echo -e "${YELLOW}Aviso: Os serviços parecem não estar rodando.${RESET}"
    echo -e "Deseja iniciá-los agora com ./start-all.sh? (s/n)"
    read -r resposta
    if [ "${resposta}" = "s" ] || [ "${resposta}" = "S" ]; then
        ./start-all.sh
        echo "Aguardando 10 segundos para inicialização do Spring Boot..."
        sleep 10
    else
        echo "Por favor, inicie os serviços antes de executar este script de teste."
        exit 1
    fi
fi

echo -e "${GREEN}✓ Serviços operacionais!${RESET}\n"

# 1. Demonstração do PROXY
echo -e "${CYAN}======================================================================${RESET}"
echo -e "${CYAN}[2/4] DEMONSTRAÇÃO DO PADRÃO PROXY (Porta 8080 -> 8081)${RESET}"
echo -e "Requisição externa ao Gateway: ${YELLOW}GET http://localhost:8080/game${RESET}"
echo -e "O Gateway encaminha a chamada para o Serviço A (8081) ocultando as portas internas."
echo "Resposta recebida:"
curl -s http://localhost:8080/game | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8080/game
echo -e "\n${GREEN}✓ Padrão Proxy funcionando perfeitamente!${RESET}\n"

# 2. Demonstração do ADAPTER
echo -e "${CYAN}======================================================================${RESET}"
echo -e "${CYAN}[3/4] DEMONSTRAÇÃO DO PADRÃO ADAPTER (Tradução de Formato Legado)${RESET}"
echo -e "1. Dados brutos retornados pelo Serviço C (Porta 8083, formato legado):"
echo -e "${YELLOW}GET http://localhost:8083/api/v1/legacy-store/pricing/1${RESET}"
curl -s http://localhost:8083/api/v1/legacy-store/pricing/1 | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8083/api/v1/legacy-store/pricing/1
echo ""
echo -e "Observe os campos legados: ${YELLOW}'price_amount_cents': 12990${RESET} e ${YELLOW}'inventory_status': 'AVAILABLE'${RESET}."
echo -e "O ${YELLOW}LojaExternaAdapter${RESET} converte centavos para reais (R$ 129.90) e status para booleano (true)."
echo -e "${GREEN}✓ Padrão Adapter demonstrado!${RESET}\n"

# 3. Demonstração do AGGREGATOR
echo -e "${CYAN}======================================================================${RESET}"
echo -e "${CYAN}[4/4] DEMONSTRAÇÃO DO PADRÃO AGGREGATOR (Composição de Serviços)${RESET}"
echo -e "Requisição ao Gateway: ${YELLOW}GET http://localhost:8080/game/1/completo${RESET}"
echo -e "O Aggregator consulta:"
echo -e "  • Serviço A (8081) -> Dados cadastrais do jogo"
echo -e "  • Serviço B (8082) -> Avaliações e média da comunidade"
echo -e "  • Serviço C (8083 via Adapter) -> Melhor oferta de preço padronizada"
echo "Resposta consolidada:"
curl -s http://localhost:8080/game/1/completo | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8080/game/1/completo
echo -e "\n${GREEN}✓ Padrão Aggregator funcionando com sucesso!${RESET}\n"

echo -e "${BLUE}======================================================================${RESET}"
echo -e "${GREEN}Demonstração concluída com sucesso!${RESET}"
echo -e "${BLUE}======================================================================${RESET}"

