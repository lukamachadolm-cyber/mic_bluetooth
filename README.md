# Mic Bluetooth 🎤

Transforme seu dispositivo Android em um microfone profissional de baixa latência, otimizado para uso com caixas de som e fones Bluetooth. Ideal para Karaokê, palestras ou apresentações improvisadas.

## 🚀 Funcionalidades Principal

- **Filtro Inteligente de Voz (Noise Gate):** Algoritmo avançado que identifica a voz humana e silencia automaticamente ruídos de fundo ou o vazamento da música de fundo.
- **Efeito de Karaokê (Eco):** Adicione profundidade à sua voz com um efeito de eco (delay) ajustável em tempo real.
- **Ajustes de Tom Simplificados:**
  - **Grave:** Mais corpo e peso para a voz.
  - **Médio:** Foco na clareza das palavras.
  - **Agudo:** Brilho e definição cristalina.
- **Potência da Voz (Amplificação):** Ganho digital de até 5x com limitador integrado para evitar distorções agressivas.
- **Baixa Latência:** Pipeline de áudio otimizado para o menor atraso possível entre a fala e a saída.
- **Persistência de Dados:** O aplicativo lembra de todos os seus ajustes automaticamente.
- **Modo Standby Inteligente:** Pausa o processamento de áudio ao minimizar o app, economizando bateria.
- **Tela Sempre Ativa:** Impede o bloqueio automático enquanto você estiver usando o microfone.

---

## 📱 Rascunho da Interface (UI Sketch)

```text
+---------------------------------------+
| [ ] Mic Bluetooth                 (X) |  <-- Cabeçalho Moderno
+---------------------------------------+
|                                       |
|  Configurações de Transmissão         |
|  +---------------------------------+  |
|  | Volume do Microfone (Força)     |  |
|  | [===========O-----------------] |  |
|  |                                 |  |
|  | Bloqueio de Som Externo         |  |
|  | [=====O-----------------------] |  |
|  | Modo: Equilibrado               |  |
|  +---------------------------------+  |
|                                       |
|  Ajustes de Tom e Efeitos             |
|  +---------------------------------+  |
|  | Voz de Karaokê (Eco)            |  |
|  | Intensidade: 25%                |  |
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

- **Linguagem:** Java
- **SDK Alvo:** Android 31 (Android 12)
- **Tecnologias de Áudio:**
  - `AudioRecord` & `AudioTrack` com modo `PERFORMANCE_MODE_LOW_LATENCY`.
  - Processamento PCM 16-bit em tempo real.
  - Integração com efeitos de hardware: `AcousticEchoCanceler`, `NoiseSuppressor` e `AutomaticGainControl`.
- **Armazenamento:** `SharedPreferences` para persistência de configurações.

---

## 📦 Como Instalar

1. Clone o repositório.
2. Abra no **Android Studio**.
3. Compile e instale no seu dispositivo.
4. Certifique-se de conceder a permissão de **Gravação de Áudio**.
5. Conecte sua caixa de som Bluetooth e divirta-se!

---

*Desenvolvido com foco em usabilidade e performance sonora.*
