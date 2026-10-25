# Créditos e materiais de terceiros

Este aplicativo Android contém adaptações e recursos de projetos anteriores. A implementação Android, as adaptações para Kotlin e as alterações posteriores não devem ser confundidas com autoria exclusiva de todo o material incluído.

O código próprio deste repositório é disponibilizado sob **GNU GPL versão 3**, conforme [LICENSE](LICENSE). Os materiais de terceiros mantêm seus avisos e condições originais. Não há garantia de funcionamento ou adequação, nos termos das licenças aplicáveis.

Copyright (C) 2026 João Marcos Epifanio, para as contribuições próprias. Esse aviso não substitui os direitos e avisos dos autores relacionados abaixo.

## GP200 Studio

- Projeto: [kabir0st/gp200-studio](https://github.com/kabir0st/gp200-studio).
- Autor creditado pelo projeto: **Kabir Tamari**.
- Licença declarada: **GPL-3.0**; [cópia do arquivo original](docs/licenses/kabir0st-gp200-studio-LICENSE.txt).
- Revisão examinada: `7d534c290aed969a32ab5a7857dc1a5f6d817be3`.
- Recursos reutilizados: os 278 SVGs em `app/src/main/assets/pedals/`, idênticos aos arquivos de `public/pedals/` nessa revisão.
- Referências/adaptações identificadas: núcleo de presets/protocolo compartilhado com gp200editor, leitura de nomes de presets e referências de interface e catálogo. A revisão não permite atribuir cada trecho comum exclusivamente a um dos dois projetos.

## gp200editor / Preset Forge

- Projeto: [phash/gp200editor](https://github.com/phash/gp200editor).
- Aviso específico encontrado: **Copyright (C) 2026 phash**.
- Licença declarada no aviso: **GNU GPL versão 3 ou posterior**; [cópia preservada do arquivo original](docs/licenses/phash-gp200editor-LICENSE.txt).
- Revisão examinada: `80f7046e80ee9b2c38b6ad588d5f9c42eedf34af`.
- Correspondências que indicam adaptação: utilitários `BinaryParser` e `BufferGenerator`, codecs `PRSTDecoder`, `PRSTEncoder` e partes de `SysExCodec`, além das tabelas de efeitos e parâmetros utilizadas em `EffectCatalog`.
- Os equivalentes TypeScript estão em `src/core/` do projeto original; os arquivos Kotlin ficam em `core-protocol/src/main/kotlin/com/valeton/editor/core/` neste repositório.

**Ressalva:** o arquivo de licença original declara GPLv3 ou posterior, mas apresenta divergências em relação ao texto padrão da GPLv3. A cópia acima foi preservada sem correções editoriais. A licença padrão em `LICENSE` rege o código próprio e não substitui nem esclarece unilateralmente a autorização de terceiros. Essa divergência deve ser esclarecida com o titular antes de distribuir os trechos derivados como se estivessem sob uma GPLv3 padrão sem ressalvas.

## Adaptações neste aplicativo

Partes do núcleo TypeScript foram adaptadas para Kotlin. As alterações neste aplicativo incluem integração nativa Android MIDI, modelos Kotlin, validação de parâmetros, preservação de dados opacos, edição de controles e verificação de leitura após o salvamento.

O ícone do aplicativo foi gerado separadamente com IA, sem imagens de terceiros como referência.

As revisões acima são as usadas na comparação, não necessariamente as revisões exatas copiadas durante o desenvolvimento.

## Presets, dependências e marcas

O preset atual `core-protocol/src/test/resources/fixtures/clean.prst` foi criado e fornecido pelo mantenedor para os testes e incluído sob a licença do repositório. A origem e o hash estão no README da pasta de fixtures.

Este documento registra os projetos diretamente reaproveitados identificados na revisão; não é um inventário completo das dependências transitivas. Bibliotecas externas mantêm suas próprias licenças.

Valeton, GP-200 e os nomes de equipamentos citados são referências de identificação de seus respectivos titulares. Os créditos não implicam afiliação, autorização de uso de marcas ou endosso por esses titulares ou pelos autores dos projetos originais.
