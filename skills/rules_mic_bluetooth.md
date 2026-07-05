markdown_content = """# Skill de Desenvolvimento: App Microfone Bluetooth com Equalizador

Esta especificação técnica (Skill) serve como diretriz detalhada para o desenvolvimento de um aplicativo Android nativo focado em capturar áudio do microfone em tempo real, aplicar equalização e transmiti-lo para um dispositivo de saída Bluetooth (caixa de som/fone).

---

## 1. Visão Geral do Sistema
O objetivo principal é criar um pipeline de áudio de baixa latência. O fluxo de dados segue a seguinte arquitetura linear:
1. **Entrada:** Microfone do dispositivo capturado via `AudioRecord` (PCM bruto).
2. **Processamento:** O fluxo é direcionado a uma sessão de áudio que possui um efeito de `Equalizer` acoplado.
3. **Saída:** Transmissão imediata via `AudioTrack` configurado para streaming, canalizado automaticamente pelo Android para o dispositivo Bluetooth conectado.

---

## 2. Permissões e Configurações (AndroidManifest.xml)

Para o correto funcionamento do hardware de áudio e bluetooth, o manifesto deve conter as seguintes declarações. A partir do Android 12 (API 31), as permissões de Bluetooth mudaram e devem ser solicitadas explicitamente em tempo de execução.