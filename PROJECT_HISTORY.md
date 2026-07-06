# Histórico de Desenvolvimento - Mic Bluetooth 🎤

Este documento registra toda a evolução técnica, funcional e estética do projeto para fins de auditoria, histórico e referência para futuros modelos de IA (agentes).

---

## 1. Visão Geral do Projeto
- **Nome:** Mic Bluetooth
- **Identificador:** `com.lukamachado.micbluetooth`
- **Propósito:** Aplicativo de microfone em tempo real com baixa latência, otimizado para dispositivos Bluetooth e focado em clareza de voz para usuários leigos.
- **Tecnologia:** Java (Android Nativo), API 35 (Android 15), Gradle 8.5.

---

## 2. Linha do Tempo e Evolução

### Fase 1: Recuperação de Estrutura
- **Sincronização Gradle:** O projeto apresentava falhas críticas de sincronização devido a arquivos corrompidos (encoding UTF-16). Todos os arquivos de configuração (`build.gradle`, `settings.gradle`) foram reescritos em UTF-8.
- **Otimização de Namespace:** Adição do `namespace` e ativação do AndroidX via `gradle.properties`.

### Fase 2: Motor de Áudio & Latência
- **Processamento Real-time:** Migração de `byte[]` para `short[]` para manipulação precisa de amostras de 16-bit.
- **Baixa Latência:** Implementação do `PERFORMANCE_MODE_LOW_LATENCY` e uso de `USAGE_VOICE_COMMUNICATION` para minimizar o atraso digital.
- **Ducking Automático:** Integração com o sistema de Foco de Áudio do Android para abaixar o volume de músicas de fundo enquanto o app está ativo.

### Fase 3: Inteligência e Efeitos
- **Voz de Karaokê (Eco):** Buffer circular de 150ms para efeito de delay ajustável.
- **Filtro Inteligente (Noise Gate):** 
    - Algoritmo de detecção de energia (RMS).
    - Lógica de **Histerese (Hold Time 0.4s)** para evitar cortes bruscos.
- **Amplificação:** Ganho digital de entrada com limitador contra distorção.

### Fase 4: UX e Design Profissional
- **Simplificação para Leigos:** Substituição do equalizador por controles de **Grave**, **Médio** e **Agudo**.
- **Design Moderno:** Interface baseada em Cards, fundo `#F5F5F5` e cabeçalho unificado com Toolbar roxa.
- **VU Meter:** Indicador visual de volume para calibração de sensibilidade.

### Fase 5: Robustez e Release (Final)
- **Persistência:** Uso de `SharedPreferences` para salvar todos os ajustes automaticamente.
- **Segurança Bluetooth:** Interrupção automática do áudio em caso de desconexão de fones/caixas via `BroadcastReceiver`.
- **Status em Tempo Real:** Implementação de `AudioDeviceCallback` para detectar e exibir o nome do dispositivo conectado instantaneamente.
- **Áudio Suave (Modo Estável):** Opção de buffer triplicado para hardware antigo ou conexões instáveis, garantindo transmissão contínua.
- **Identidade Visual Premium:** Criação de um ícone adaptativo com gradiente e símbolo oficial do Bluetooth em alta definição.
- **Educação do Usuário:** Card de dicas para evitar microfonia ao usar alto-falantes internos.

---

## 3. Arquitetura Técnica e Guia de Portabilidade (ex: para Flutter)

Se desejar portar este projeto para frameworks como Flutter, os seguintes pontos técnicos são cruciais:

### A. Fluxo de Dados (Pipeline)
1.  **Captura:** `AudioRecord` usando `VOICE_COMMUNICATION`.
2.  **Processamento:**
    - RMS: `sqrt(sum(sample^2) / n)`.
    - Ganho Suave: Transição linear (`lerp`) entre ganhos para evitar estalos.
    - Eco: Buffer circular externo.
3.  **Saída:** `AudioTrack` em modo `PERFORMANCE_MODE_LOW_LATENCY`.

### B. Desafios de Performance
- **Garbage Collection:** Evitar alocação de objetos no loop de áudio (crucial em Dart/Flutter).
- **Threading:** Áudio e UI devem rodar em threads separadas para evitar latência induzida.

---

## 4. Instruções para Futuros Agentes
1.  **Priorize Latência:** Sempre use o menor buffer possível, a menos que o "Modo Estabilidade" esteja ativo.
2.  **Mantenha Simplicidade:** Use Labels focados no resultado (ex: "Voz mais clara") e não em termos técnicos.
3.  **Segurança de Thread:** Use variáveis `volatile` para estados compartilhados entre a Thread de Áudio e a UI.

---
*Documento gerado em 04/07/2026 para registro histórico do projeto.*
