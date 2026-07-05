# Mic Bluetooth 🎤

Transforme seu dispositivo Android em um microfone profissional de baixa latência, otimizado para uso com caixas de som e fones Bluetooth. Ideal para Karaokê, palestras ou apresentações improvisadas.

## 🚀 Funcionalidades Principais

- **VU Meter em Tempo Real:** Indicador visual de volume que ajuda na calibração precisa da sensibilidade do microfone.
- **Status do Dispositivo:** Identificação instantânea do aparelho conectado (ex: JBL, Sony, Fones) diretamente na interface.
- **Filtro Inteligente de Voz (Noise Gate):** Algoritmo avançado com **Histerese** que identifica a voz humana e silencia ruídos de fundo ou vazamento de música.
- **Segurança Bluetooth:** Detecção automática de desconexão de fones/caixas para interromper o áudio e evitar ruídos indesejados.
- **Efeito de Karaokê (Eco):** Controle deslizante para adicionar profundidade e preenchimento à voz.
- **Ajustes de Tom Simplificados:** Controles intuitivos de **Grave**, **Médio** e **Agudo**.
- **Potência da Voz (Amplificação):** Ganho digital de até 5x com limitador para evitar distorções agressivas.
- **Baixa Latência:** Pipeline otimizado (`PERFORMANCE_MODE_LOW_LATENCY`) para processamento instantâneo.
- **Áudio Suave (Modo Estável):** Opção de buffer otimizado para garantir transmissões contínuas mesmo em hardware antigo ou conexões instáveis.
- **Pronto para Globalização:** Totalmente internacionalizado via `strings.xml`.
- **Persistência de Dados:** O app lembra de todas as suas preferências automaticamente.

---

## 📱 Interface do Usuário (UI Sketch)

```text
+---------------------------------------+
| Mic Bluetooth                     (X) |  <-- Cabeçalho Toolbar
+---------------------------------------+
| Transmitindo para: JBL Flip 5         |  <-- Status em Tempo Real
+---------------------------------------+
|  💡 Dica: Use fones para evitar eco.  |  <-- Card Educativo
+---------------------------------------+
|                                       |
|  Configurações de Transmissão         |
|  +---------------------------------+  |
|  | Nível de Captura (Voz)          |  |
|  | [########----------] (VU Meter) |  |
|  |                                 |  |
|  | Volume do Microfone (Força)     |  |
|  | [===========O-----------------] |  |
|  |                                 |  |
|  | Bloqueio de Som Externo         |  |
|  | [=====O-----------------------] |  |
|  +---------------------------------+  |
|                                       |
|  Ajustes de Tom e Efeitos             |
|  +---------------------------------+  |
|  | Voz de Karaokê (Eco)            |  |
|  | [===O-------------------------] |  |
|  | ------------------------------- |  |
|  | Voz Grossa (Grave)              |  |
|  | [-----------O-----------------] |  |
|  | Clareza das Palavras (Médio)    |  |
|  | [-----------O-----------------] |  |
|  | Brilho da Voz (Agudo)           |  |
|  | [-----------O-----------------] |  |
|  +---------------------------------+  |
|                                       |
+---------------------------------------+
```

---

## 🛠️ Detalhes Técnicos

- **Package:** `com.lukamachado.micbluetooth`
- **Linguagem:** Java (Android Nativo)
- **SDK Alvo:** Android 34 (Android 14)
- **Motor de Áudio:** `AudioRecord` (VOICE_COMMUNICATION) & `AudioTrack` (Builder API).
- **Detecção de Hardware:** `AudioDeviceCallback` para monitoramento de periféricos em tempo real.
- **Arquitetura:** Veja o arquivo [PROJECT_HISTORY.md](./PROJECT_HISTORY.md) para detalhes completos da evolução técnica.

---

## ⚖️ Legal e Privacidade

Este aplicativo respeita a sua privacidade. O áudio captado é processado exclusivamente em tempo real no seu dispositivo.
- **NÃO** gravamos áudio.
- **NÃO** enviamos dados para nuvem.
Veja a [Política de Privacidade completa aqui](./PRIVACY_POLICY.md).

---

## 📦 Como Instalar

1. Clone o repositório.
2. Abra no **Android Studio**.
3. Execute um **Build > Clean Project**.
4. Compile e instale no seu dispositivo Android.

---

*Desenvolvido com foco em usabilidade, clareza vocal e performance sonora.*
