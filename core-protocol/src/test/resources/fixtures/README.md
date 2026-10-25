# Fixtures de referência

## clean.prst

- Preset criado e fornecido pelo mantenedor para uso nos testes deste repositório em 25/10/2026.
- Arquivo original: `01-A Clean.prst`; nome interno: `Clean`; campo de autor vazio.
- Incluído como contribuição do mantenedor sob a GPLv3 adotada pelo repositório.
- Cópia integral do original: **1224 bytes**, sem regenerar ou editar os bytes.
- SHA-256: `047a6c2966397779a554dfe7c55d7ccd7eba1d72b4a25686419544f044bac0a6`.
- Volume do patch: 50; Pan: 0; tempo: 160 BPM; checksum BE16: `0x5362`.
- Cadeia na ordem 0–10; AMP, CAB e VOL ativos. Os demais blocos estão desativados.
- Modelo da pedaleira e firmware usados na criação não foram informados junto ao arquivo. Sua inclusão não comprova compatibilidade com todas as variantes.

Usado nos testes de decodificação, round-trip byte a byte, mapeamentos de controles, planos de escrita, transporte e persistência. Casos que precisam de valores diferentes de autor/Pan ou campos opacos são construídos explicitamente pelos testes, sem alterar esta referência.

O preset de teste é separado do preset offline criado pelo aplicativo. Não é necessário que tenha os mesmos efeitos, parâmetros ou blocos ativos.

## effect-parameter-schema.tsv

Fixture textual do esquema de parâmetros derivada da análise documentada do catálogo do editor Windows. Sua fonte e limitações constam do cabeçalho do arquivo e de `AGENTS.md`; não representa uma captura física da pedaleira nem faz parte da substituição dos presets binários.
