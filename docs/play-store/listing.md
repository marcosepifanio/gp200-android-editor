# Ficha da Play Store

Textos e respostas para preencher a Play Console. Revise os valores com o estado atual do app antes de cada envio.

## Detalhes do app

- **Nome do app** (até 30 caracteres): `Editor para GP-200`
- **Idioma padrão:** Português (Brasil) — pt-BR
- **Tipo:** App
- **Categoria:** Música e áudio
- **Gratuito ou pago:** Gratuito
- **Política de privacidade:** https://github.com/marcosepifanio/gp200-android-editor/blob/main/docs/privacy-policy.md

### Descrição curta (até 80 caracteres)

```
Edite presets da pedaleira GP-200 por USB. App independente e não oficial.
```

### Descrição completa (até 4000 caracteres)

```
Editor para GP-200 é um aplicativo independente para editar e organizar presets da pedaleira Valeton GP-200 pelo celular, conectado por cabo USB.

O que você pode fazer:
• Ver a cadeia de sinal do preset, ligar e desligar blocos e mudar a ordem dos efeitos.
• Trocar modelos de efeitos e ajustar seus parâmetros, com busca por categoria e efeitos favoritos.
• Navegar pelos slots de presets da pedaleira, renomear e salvar alterações, com conferência da gravação.
• Importar arquivos .prst e exportar ou compartilhar presets com outros apps.
• Configurar os três knobs físicos e os controles CTRL 1 a CTRL 4.
• Controlar os ritmos: padrão, volume, andamento e reprodução.
• Explorar o app sem a pedaleira, no modo offline.

Requisitos:
• Aparelho Android com suporte a USB host (OTG) e MIDI.
• Cabo USB-C para USB-C ou adaptador OTG.

Compatibilidade: os testes físicos foram feitos com a GP-200 LT. Outras variantes e versões de firmware podem se comportar de forma diferente.

Privacidade: o app não acessa a internet e não coleta dados pessoais. Os presets e preferências ficam somente no seu aparelho.

Código aberto sob a licença GPL-3.0: https://github.com/marcosepifanio/gp200-android-editor

Aviso: este é um aplicativo não oficial, sem vínculo, afiliação ou endosso da Valeton. Valeton e GP-200 são marcas de seus respectivos titulares e são citadas apenas para identificar o equipamento compatível.
```

## Segurança dos dados (Data Safety)

- **O app coleta ou compartilha algum dos tipos de dados de usuário obrigatórios?** Não.
- Com "Não", a Play Console dispensa as perguntas sobre criptografia em trânsito e exclusão de dados.

Base da resposta, conferida no código e no manifesto mesclado de release:

- O manifesto não declara `INTERNET` nem permissões perigosas; não há SDK de análise, anúncios ou relatório de falhas.
- Biblioteca de presets, nomes e favoritos ficam no armazenamento privado do app (`filesDir` e `SharedPreferences`); exportações temporárias ficam em `cacheDir`.
- O tráfego MIDI é local, pelo cabo USB.
- O backup automático do Android (`allowBackup="true"`) é feito pelo sistema na conta Google do usuário, sem acesso do desenvolvedor.

Se alguma dessas condições mudar (por exemplo, inclusão de Crashlytics ou de acesso à rede), revise esta seção e a política de privacidade.

## Conteúdo do app

- **Acesso ao app:** todas as funções estão disponíveis sem login. Informe que as funções de edição ao vivo exigem a pedaleira conectada por USB e que o modo offline permite explorar o app sem ela.
- **Anúncios:** o app não contém anúncios.
- **Classificação de conteúdo:** categoria "Utilitário, produtividade, comunicação ou outro"; responda "Não" a violência, sexo, linguagem, drogas, jogos de azar, interação entre usuários, compartilhamento de localização e compras digitais.
- **Público-alvo:** 18 anos ou mais (ou 13+); o app não é direcionado a crianças.
- **App de notícias:** Não.
- **Apps governamentais / financeiros / de saúde:** Não.

## Teste interno

- **Notas da versão (pt-BR)** para 1.0.3 (12):

```
Primeira versão de testes na Play Store.
```

- Peça aos testadores para informar o modelo da pedaleira (GP-200, LT, JR…) e a versão do firmware ao relatar problemas.

## Pendências antes da produção

Decisões tomadas para o teste interno e que devem ser revistas antes de abrir o app ao público:

- **Marcas no `basedOn`:** 197 dos 305 efeitos citam equipamentos de terceiros (por exemplo, "Ibanez® TS-808") no seletor e no editor. Para o teste interno, o texto foi mantido e o seletor ganhou um aviso de que os nomes são só referência de timbre, sem vínculo ou endosso dos titulares.
- **Rótulos dos SVGs:** os desenhos são genéricos e não têm logotipos, mas alguns exibem números de modelo de terceiros, como "SD·1", "BD·2", "808" e "OD·9". Mantidos por ora; avalie trocar pelos nomes dos modelos da Valeton.
- **Licença do gp200editor:** o `LICENSE` de [phash/gp200editor](https://github.com/phash/gp200editor) (commit `fed73a6`, "chore: add GPL-3.0 license") não é o texto oficial da GPL-3.0: há trechos reescritos e outros ausentes, como a definição de "Corresponding Source", e o GitHub classifica a licença como "Other". A intenção declarada no commit é GPL-3.0. Peça ao autor a confirmação e a troca pelo texto oficial antes da distribuição pública.
