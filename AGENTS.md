# Contexto do projeto para agentes de IA

## O que é este projeto

Aplicativo Android nativo para editar e gerenciar presets de pedaleiras Valeton GP-200. A interface usa Kotlin, Jetpack Compose e Material 3. A comunicação com a pedaleira é MIDI por USB usando as APIs Android; não é um editor web nem um aplicativo baseado em navegador.

O preset representa 11 blocos da cadeia: PRE, WAH, DST, AMP, NR, CAB, EQ, MOD, DLY, RVB e VOL. O catálogo de efeitos e parâmetros fica no módulo `core-protocol`.

## Estrutura atual

- `app/` — interface, navegação, estado da tela, repositórios locais e integração da aplicação.
  - `app/src/main/kotlin/com/valeton/editor/MainActivity.kt` conecta navegação, view models e fluxos de abrir/compartilhar `.prst`.
  - `app/src/main/kotlin/com/valeton/editor/viewmodel/PresetViewModel.kt` coordena edição do preset, cache, sincronização, importação e salvamento.
  - `app/src/main/kotlin/com/valeton/editor/viewmodel/PresetUiState.kt` define o estado observado pela UI.
  - `app/src/main/kotlin/com/valeton/editor/ui/` contém telas Compose, componentes e tema. As telas principais estão em `chain/`, `manage/`, `drums/` e `controls/`.
  - `app/src/main/kotlin/com/valeton/editor/repository/PresetLibraryRepository.kt` persiste a biblioteca local em `library_cache.json`.
- `core-protocol/` — Kotlin/JVM sem dependência de Android para modelos, catálogo e formatos de preset/protocolo.
  - `core-protocol/src/main/kotlin/com/valeton/editor/core/model/` contém `GP200Preset`, efeitos, tipos de bloco e mapeamentos.
  - `core-protocol/src/main/kotlin/com/valeton/editor/core/catalog/EffectCatalog.kt` define os modelos de efeitos e seus parâmetros.
  - `core-protocol/src/main/kotlin/com/valeton/editor/core/codec/SysExCodec.kt` constrói e interpreta mensagens SysEx/MIDI ao vivo.
  - `core-protocol/src/main/kotlin/com/valeton/editor/core/codec/PRSTDecoder.kt` e `PRSTEncoder.kt` leem e escrevem arquivos `.prst`.
  - `core-protocol/src/main/kotlin/com/valeton/editor/core/codec/PresetWritePlan.kt` seleciona o estado pendente a reenviar; `core-protocol/src/main/kotlin/com/valeton/editor/core/codec/PresetSaveVerifier.kt` compara o preset esperado com o que foi lido de volta.
- `device-usb/` — integração MIDI Android e transporte.
  - `device-usb/src/main/kotlin/com/valeton/editor/usb/AndroidMidiDriver.kt` encontra dispositivos MIDI compatíveis, abre as portas, conduz sincronização/leitura/salvamento e emite eventos.
  - `device-usb/src/main/kotlin/com/valeton/editor/usb/MidiConnectionManager.kt` define a interface de conexão usada pelo app e pelos doubles de teste.
  - `device-usb/src/main/kotlin/com/valeton/editor/usb/MidiPacketFramer.kt` ordena/envia mensagens e agrupa atualizações rápidas de parâmetros; o intervalo padrão atual é 40 ms.
  - `device-usb/src/main/kotlin/com/valeton/editor/usb/MidiConnectionState.kt` modela os estados da conexão.

Os módulos e plugins Gradle estão declarados em `settings.gradle.kts` e nos `build.gradle.kts` de cada módulo. Confirme as configurações atuais nesses arquivos em vez de confiar em versões copiadas para documentos.

## Áreas funcionais da aplicação

- **Cadeia:** `app/src/main/kotlin/com/valeton/editor/ui/chain/` exibe os blocos, a ordem e controles globais do patch; a edição detalhada de efeitos fica em `app/src/main/kotlin/com/valeton/editor/ui/editor/`.
- **Presets:** `app/src/main/kotlin/com/valeton/editor/ui/manage/` lista e gerencia slots, importa `.prst`, salva na pedaleira, exporta para armazenamento local e compartilha por outros apps.
- **Ritmos:** `app/src/main/kotlin/com/valeton/editor/ui/drums/` controla reprodução, padrão, gênero, volume e andamento; padrões ficam no catálogo `core-protocol/src/main/kotlin/com/valeton/editor/core/catalog/DrumCatalog.kt`.
- **Controles:** `app/src/main/kotlin/com/valeton/editor/ui/controls/` configura os três knobs físicos e CTRL 1 do patch; codec e modelos correspondentes ficam em `core-protocol/src/main/kotlin/com/valeton/editor/core/codec/ControlMappingCodec.kt` e `core-protocol/src/main/kotlin/com/valeton/editor/core/model/ControlMappings.kt`.
- **Sobre:** `app/src/main/kotlin/com/valeton/editor/ui/about/StudioAboutScreen.kt` informa a versão via `BuildConfig`, o caráter independente e não oficial da Valeton e os créditos dos projetos utilizados.

O fluxo atual em `MainActivity` usa `StudioChainScreen`, `StudioPatchManagerScreen`, `StudioDrumScreen`, `StudioControlsScreen`, `StudioPedalEditorScreen` e `StudioAboutScreen`. Há outros componentes com nomes semelhantes no repositório; confira os usos e a navegação antes de escolher qual tela alterar.

## Fluxos que importam

### Conexão e sincronização

`AndroidMidiDriver` usa `android.media.midi`, escaneia dispositivos e identifica nomes/produtos que contenham GP-200, GP200 ou Valeton. O view model inicia a sincronização da biblioteca quando não encontra um cache completo. A biblioteca da pedaleira contém 256 slots. Com um cache completo, os dados locais podem ser exibidos antes de qualquer nova leitura do hardware; não trate o cache como estado atual da pedaleira quando houver alterações locais ou uma sincronização pendente.

O código de detecção não garante compatibilidade física com todas as variantes da família. O mantenedor relatou testes em hardware com a GP-200 LT; valide fisicamente cada variante antes de afirmar suporte equivalente.

### Edição e salvamento

As telas atualizam o estado local por meio do `PresetViewModel`, que envia alterações ao driver MIDI. Ao salvar, o app pode reenviar mudanças pendentes, solicita uma leitura do slot e compara a resposta com o esperado. A confirmação da pedaleira, e não apenas o envio do comando, determina se a alteração foi salva.

Ao mexer neste fluxo:

- preserve dados que o app não entende, especialmente campos opacos e registros de mapeamento dos controles;
- não elimine regras de normalização do dispositivo sem evidência de hardware;
- mantenha o estado visível e o resultado da leitura de confirmação coerentes;
- trate eventos de pedal como eventos de hardware, não automaticamente como alterações que devam substituir o estado editado no app.

Preserve os bloqueios de edição durante salvamento, importação e sincronização completa, tanto na UI quanto no view model. Os mapeamentos de controles também ficam bloqueados durante a sincronização do preset atual. Ao adicionar ações, consulte `editingBusy()`, `controlsBusy()` e as condições usadas em `MainActivity`.

### Catálogo e parâmetros

O `index` de um parâmetro identifica sua posição no protocolo; não o substitua pela posição visual na lista de controles. Preserve também os vínculos de switches (`boundParamIndex`). O teste `EffectParameterSchemaTest` confronta o catálogo com `core-protocol/src/test/resources/fixtures/effect-parameter-schema.tsv`; a fixture documenta a referência analisada, não uma captura física da pedaleira.

Na edição de knobs, respeite `min`, `max` e `step` do catálogo. O valor no estado local e o enviado por MIDI devem ser o mesmo valor ajustado; arredondar apenas o texto exibido pode esconder divergências. Consulte `PresetViewModel.updateParam` e seus testes de parâmetros inteiros e decimais antes de alterar essa regra.

### Persistência local

Ao adicionar ou alterar campos de `GP200Preset`, revise também a leitura e a escrita em `PresetLibraryRepository`, além dos codecs de arquivo e MIDI. Preserve `rawSource` e os mapeamentos de controles no cache e mantenha a leitura de caches antigos que não contenham campos novos. Valide a persistência com `PresetLibraryRepositoryTest`.

### `.prst` e SysEx ao vivo

São representações distintas do mesmo preset. Arquivos passam por `PRSTDecoder`/`PRSTEncoder`; mensagens da pedaleira passam por `SysExCodec`. O decoder `.prst` aceita arquivos de 1224 ou 1176 bytes; o encoder gera arquivo de 1224 bytes. Não transfira offsets, ordem de bytes ou regras de um formato para o outro sem verificar a fonte correspondente. Exemplo: o Pan no arquivo `.prst` é lido/escrito pelo codec de arquivo em little-endian; o dump SysEx ao vivo tem uma representação diferente.

A importação recebe um `Uri` e decodifica o arquivo antes de apresentar a confirmação. A exportação serializa o preset para `.prst` e oferece salvamento por `ActivityResultContracts.CreateDocument` ou compartilhamento por `FileProvider`/`Intent.ACTION_SEND`. Verifique ambos os sentidos ao alterar campos do modelo; preserve `rawSource` quando os bytes originais forem necessários para manter dados desconhecidos.

### MIDI e protocolo

As mensagens SysEx usam o envelope GP-200 implementado em `SysExCodec`, com payload codificado em nibbles. Deixe a construção e o parsing de comandos nesse codec; não monte mensagens por índices copiados de um log ou de uma spec histórica. `PresetWritePlan` e `PresetSaveVerifier` são partes do contrato de confiabilidade de escrita.

Para o protocolo de knobs físicos e CTRL 1, consulte [`docs/protocol/control-mapping.md`](docs/protocol/control-mapping.md). Esse documento descreve evidências obtidas por análise do software desktop e fixtures e declara explicitamente que não é uma captura física. Não apresente uma inferência desse documento como comportamento universal confirmado pela pedaleira.

## Fontes de verdade e evidência

Escolha a fonte conforme a pergunta:

- Para saber como a aplicação está implementada, consulte o código atual e os testes correspondentes.
- Para afirmar como a pedaleira realmente responde, prefira logs/capturas reproduzíveis que identifiquem o modelo e, se disponível, o firmware.
- Para afirmar detalhes do formato de arquivo, confronte `PRSTDecoder`/`PRSTEncoder` com fixtures conhecidas.
- Use documentos técnicos somente considerando a origem e as limitações descritas neles. Specs e planos históricos ajudam a entender decisões, mas não substituem a implementação atual nem uma observação física.

Documentos de planejamento locais podem não estar presentes no clone. Compare afirmações operacionais com o código e os testes atuais. Em especial, verifique se um driver, fluxo ou funcionalidade mencionados em qualquer documento realmente existem antes de citá-los.

O mantenedor relatou validações na GP-200 LT para importação/exportação e correções de salvamento, incluindo Pan. Isso não comprova cada função em todas as versões de firmware ou variantes da família. Ao corrigir comportamento dependente de MIDI, identifique o modelo usado e peça/consulte evidência física quando necessário.

## Build, testes e versão

- JDK/toolchain atual: Java 21. Confira os Gradle files e o SDK Android local para os valores atuais de compile/target SDK.
- Gerar APK de depuração: `./gradlew :app:assembleDebug`.
- Testes unitários do protocolo: `./gradlew :core-protocol:test`.
- Testes unitários do transporte: `./gradlew :device-usb:test`.
- Testes unitários do app: `./gradlew :app:testDebugUnitTest`.
- Após um build debug bem-sucedido, o APK normalmente fica em `app/build/outputs/apk/debug/app-debug.apk`.

Antes da primeira publicação oficial, use `versionName` no formato `0.x.y-beta.N` e incremente `N` a cada versão beta distribuída. A primeira publicação oficial deve usar `1.0.0`; depois, continue a sequência semântica a partir dela. Incremente `versionCode` para cada APK distribuído, incluindo versões beta e oficiais. Compilações locais feitas apenas para verificar uma mudança não exigem incremento de versão. Consulte os valores atuais em `app/build.gradle.kts` antes de alterá-los; não reutilize um número citado neste guia ou em log antigo.

### Testes de referência

Os testes ficam em `src/test/kotlin` de cada módulo e usam JUnit 5. Escolha as verificações conforme a área alterada:

- **Formato e protocolo:** `PRSTCodecTest`, `SysExCodecTest`, `ControlMappingCodecTest` e `EffectParameterSchemaTest`. Fixtures de referência ficam em `core-protocol/src/test/resources/fixtures/`.
- **Confiabilidade de escrita:** `PresetWritePlanTest` e `PresetSaveVerifierTest` no `core-protocol`; `AndroidMidiDriverTest` e `MidiPacketFramerTest` no `device-usb`.
- **Estado e persistência:** `PresetViewModelTest`, `PresetLibraryRepositoryTest` e `PresetNamesRepositoryTest` no `app`.
- **UI e recursos:** testes sob `app/src/test/kotlin/com/valeton/editor/ui/`. Ao alterar referências às artes dos efeitos, confira também `PedalArtworkAssetTest` e os SVGs em `app/src/main/assets/pedals/`.

Os testes unitários de lógica de UI não comprovam aparência ou interação em um dispositivo Android. Por exemplo, `StudioFullSyncScreenTest` verifica formatação de texto. Para mudanças visuais, valide também em emulador ou aparelho e informe quando essa verificação não tiver sido realizada. Doubles de MIDI também não substituem o teste físico na pedaleira.

## Orientações para mudanças

- Siga a separação atual: UI em Compose no `app`, regras/formato independente de Android em `core-protocol`, transporte Android MIDI em `device-usb`.
- Evite colocar protocolo MIDI dentro de composables ou acoplar codecs a APIs Android.
- Faça mudanças em codecs com cuidado para round-trip, campos desconhecidos, endianess e compatibilidade com presets antigos.
- Em escrita no dispositivo, preserve o fluxo de leitura de volta e não declare o salvamento concluído apenas porque os bytes foram enviados.
- Mantenha a interface em português e siga o tema/componentes existentes antes de criar novos padrões visuais.
- Ao relatar validação, separe claramente: compilação, testes automatizados e teste físico na pedaleira.
