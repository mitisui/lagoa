# Lagoa - Mod de Servidor Minecraft

**Versão:** 0.3.3  
**Tipo:** Server-side  
**Autor:** mitisui

## 📥 Downloads

- [Mod JAR](https://github.com/mitisui/lagoa/raw/refs/heads/master/lagoa-0.3.3.jar)
- [Pacote de Recursos](https://github.com/mitisui/lagoa/raw/refs/heads/master/lagoa.zip)

---

## 📋 Índice

1. [Sistema de Algemas](#-sistema-de-algemas)
2. [Sistema de Investigação](#-sistema-de-investigação)
3. [Armas](#-armas)
4. [Espadas Especiais](#%EF%B8%8F-espadas-especiais)
5. [Sistema de Teleporte](#-sistema-de-teleporte)
6. [Bloqueio de Magia](#-bloqueio-de-magia)
7. [Itens Customizados](#-itens-customizados)
8. [Web Logger](#-web-logger)
9. [Configuração](#%EF%B8%8F-configuração)
10. [Notas Importantes](#-notas-importantes)

---

## 🔗 Sistema de Algemas

### Obter Algemas
```
/lagoa get algemas
```

### Comandos de Prisão
```
/lagoa algema <jogador> prender
/lagoa algema <jogador> soltar
/lagoa algema <jogador> status
```

### Como Usar
- **Prender:** Clique direito em um jogador com as algemas
- **Soltar:** Clique direito novamente no jogador preso
- **Teleportar preso:** Clique direito em um bloco com as algemas (quando há um prisioneiro vinculado)

### Efeitos no Jogador Preso
- Lentidão configurável
- Brilho opcional
- Azar permanente

### Bloqueios Configuráveis
- ❌ Quebrar blocos
- ❌ Colocar blocos
- ❌ Usar itens
- ❌ Dropar itens
- ❌ Pegar itens
- ❌ Atacar
- ❌ Interagir com blocos/entidades

---

## 🔍 Sistema de Investigação

### Obter Lupa
```
/lagoa get lupa
```

### Comandos
```
/lagoa inspecionar <alvo>
```

### Como Usar
- Mire em um jogador e clique direito com a lupa
- Ou use o comando `/lagoa inspecionar <alvo>`

### Funcionalidades
- ✅ Visualiza inventário completo do alvo (somente leitura)
- ✅ Mostra armadura e offhand
- ✅ Interface atualizada em tempo real
- ⚙️ Distância máxima configurável

---

## 🔫 Armas

### Pistola

#### Obter
```
/lagoa get pistola
```

#### Características
- 💥 Dano configurável
- 📏 Alcance de 50 blocos
- 🧪 Efeito de poção configurável nos acertos
- ⏱️ Cooldown de 1 segundo (20 ticks)
- ✨ Trajetória visual com partículas

#### Como Usar
Clique direito para disparar na direção em que você está olhando

---

## ⚔️ Espadas Especiais

### Divisor de Almas

#### Obter
```
/lagoa get espadas divisorDeAlmas
```

#### Habilidades

**1️⃣ Soul Division (com alvo)**
- 🎯 Clique direito mirando em uma entidade
- 🌀 Puxa o alvo em direção ao atacante
- ⛓️ Aprisiona o alvo em uma esfera de almas
- 💔 Causa dano contínuo durante a prisão
- 🕐 Cooldown configurável

**2️⃣ Massive Attack (sem alvo)**
- 💥 Clique direito sem mirar em ninguém
- 🌊 Cria área de dano massivo
- 😵 Aplica Escuridão e Azar aos afetados
- 🌟 Pilar de partículas até o céu
- 🖤 Transforma bloco abaixo em concreto preto
- 🕐 Cooldown configurável

**🛡️ Passivo**
- ⚡ Bloqueia projéteis automaticamente
- 🔷 Cria escudo circular de concreto preto
- ⏳ Escudo dura 60 ticks (3 segundos)

### Katana

#### Obter
```
/lagoa get espadas katana
```

#### Características
- 💪 19 de dano base
- 🔧 Indestrutível
- 🎨 Custom Model Data: 102

---

## 🌀 Sistema de Teleporte

### Comando Trazer
```
/lagoa trazer <jogador> [cor]
```

**Cores disponíveis:** branco (padrão), vermelho, azul, verde, amarelo, roxo

### Características
- ✨ Animação de teleporte com partículas coloridas
- ⏱️ Sistema de cooldown por jogador
- 🚫 Prevenção de múltiplos teleportes simultâneos

---

## 🚫 Bloqueio de Magia

Sistema para criar zonas onde magias e habilidades especiais são bloqueadas.

### Comandos

#### Criar zona simples (10x10x10 ao redor do jogador)
```
/lagoa magicblock add <id> <nome>
```

#### Criar zona customizada
```
/lagoa magicblock addcustom <id> <pos1> <pos2> <nome>
```

#### Remover zona
```
/lagoa magicblock remove <id>
```

#### Listar todas as zonas
```
/lagoa magicblock list
```

#### Ver informações de uma zona
```
/lagoa magicblock info <id>
```

#### Limpar todas as zonas
```
/lagoa magicblock clear
```

### Exemplos de Uso

```
/lagoa magicblock add prisao Prisão Central
/lagoa magicblock addcustom spawn 100 60 100 200 120 200 Spawn Protegido
/lagoa magicblock remove prisao
/lagoa magicblock list
/lagoa magicblock info spawn
```

---

## 🎨 Itens Customizados

### 📱 Telefone

#### Como Criar
1. Coloque uma **Carrot on a Stick** na bigorna
2. Renomeie para algo contendo "**telefone**"
3. Custo: **5 níveis**

#### Como Usar
- 📞 Clique direito para tocar sons de telefone
- 🔢 4 estágios de toque diferentes
- ⏱️ Cooldown de 3 segundos (1.5 segundos no último estágio)

### 📯 Cornetas (Goat Horn)

#### Como Criar
1. Coloque um **Goat Horn** na bigorna
2. Renomeie para: **policia**, **flamengo**, **remo**, **paysandu** ou **corinthians**
3. Custo configurável (padrão: **5 níveis**)

#### Sons Disponíveis
- 🚨 **policia** → sirene
- ⚫🔴 **flamengo** → hino do Flamengo
- 🔵⚪ **remo** → hino do Remo
- 🔵⚪ **paysandu** → hino do Paysandu
- ⚫⚪ **corinthians** → hino do Corinthians

---

## 📊 Web Logger

Sistema de logs em tempo real via interface web.

### Comandos

#### Iniciar servidor web
```
/lagoa logger on
```

#### Parar servidor web
```
/lagoa logger off
```

### Informações
- 🌐 **Porta padrão:** 8080 (configurável)
- 🔗 **Acesso:** `http://localhost:8080`

---

## ⚙️ Configuração

O mod possui arquivo de configuração extenso com opções para:

### Algemas
- `SLOWNESS_LEVEL` - Nível de lentidão (padrão: 3)
- `ENABLE_GLOWING` - Ativar/desativar brilho (padrão: true)
- `PREVENT_BLOCK_BREAK` - Bloquear quebrar blocos (padrão: true)
- `PREVENT_BLOCK_PLACE` - Bloquear colocar blocos (padrão: true)
- `PREVENT_ITEM_USE` - Bloquear uso de itens (padrão: true)
- `PREVENT_ITEM_DROP` - Bloquear dropar itens (padrão: true)
- `PREVENT_ITEM_PICKUP` - Bloquear pegar itens (padrão: true)
- `PREVENT_ATTACK` - Bloquear ataques (padrão: true)
- `PREVENT_INTERACTIONS` - Bloquear interações (padrão: true)

### Pistola
- `ENABLE_PISTOLA` - Ativar/desativar pistola (padrão: true)
- `SHOT_DAMAGE` - Dano do tiro (padrão: 8.0)
- `BULLET_EFFECT` - Efeito aplicado no alvo (padrão: "minecraft:poison")

### Divisor de Almas
- `E1_DANO_BASE` - Dano base (padrão: 25.0)
- `E1_RAYCAST_RANGE` - Alcance do raycast (padrão: 30.0)
- `E1_HOMING_COOLDOWN` - Cooldown ataque teleguiado (padrão: 200 ticks)
- `E1_AOE_COOLDOWN` - Cooldown ataque AOE (padrão: 600 ticks)
- `E1_ATAQUE_AOE_DANO` - Dano do AOE (padrão: 15.0)
- `E1_ATAQUE_AOE_AREA` - Área do AOE (padrão: 10.0)

### Investigação
- `LUPA_RAIO_DE_USO` - Raio de uso da lupa (padrão: 100.0)

### Cornetas e Telefone
- `ENABLE_CORNETAS` - Ativar/desativar cornetas (padrão: true)
- `CORNETA_CUSTO` - Custo em níveis (padrão: 5)
- `ENABLE_TELEFONE` - Ativar/desativar telefone (padrão: true)

### Web Logger
- `ALLOW_WEB_LOGGER` - Ativar/desativar web logger (padrão: false)
- `WEB_LOGGER_PORT` - Porta do servidor (padrão: 8080)

---

## 📝 Notas Importantes

- ✅ Mod **server-side only** - não precisa ser instalado no cliente
- 🎨 Pacote de recursos recomendado para texturas customizadas
- 🔑 Requer permissão de operador (nível 2) para a maioria dos comandos
- ⚙️ Compatível com Minecraft Forge
- 💾 Configurações salvas em `config/lagoa-common.toml`

---

## 🔗 Links Úteis

- 📦 [Download Mod](https://github.com/mitisui/lagoa/raw/refs/heads/master/lagoa-0.3.3.jar)
- 🎨 [Download Resource Pack](https://github.com/mitisui/lagoa/raw/refs/heads/master/lagoa.zip)
- 💻 [GitHub Repository](https://github.com/mitisui/lagoa)

---

## 👤 Autor

**mitisui**

---

## 📜 Changelog

### Versão 0.3.3
- Sistema de Bloqueio de Magia
- Sistema de Algemas completo
- Sistema de Investigação
- Pistola com efeitos
- Espadas especiais (Divisor de Almas e Katana)
- Sistema de Teleporte
- Itens customizados (Telefone e Cornetas)
- Web Logger

---

**Última atualização:** 23 de Janeiro 2025