# ⚔️ ChaosArena

**ChaosArena** es un juego de lucha 2D para Android y escritorio desarrollado con [libGDX](https://libgdx.com/). Elige tu personaje, combate contra enemigos con IA progresiva y desafía a un amigo en duelos locales.

---

## 🎮 Modos de juego

| Modo | Descripción |
|------|-------------|
| **Historia** | Progresa por 3 niveles con narrativa propia. Cada victoria desbloquea el siguiente combate. Hasta 3 partidas guardadas. |
| **Arcade (Roguelike)** | Combates infinitos con dificultad creciente. Tras cada victoria elige buffs permanentes y enfréntate a eventos aleatorios. |
| **Duelo local** | Dos jugadores en el mismo dispositivo eligen personaje y escenario libremente. |

---

## 🥊 Personajes

- **Shadow Fist** — Luchador de artes marciales ágil y versátil.
- **Iron Claw** — Guerrero de combate cuerpo a cuerpo con gran potencia.
- **Ronin** — Maestro de la espada con ataques de largo alcance.

Cada personaje tiene animaciones únicas: idle, correr, puño, patada, ataque especial, recibir daño y salto.

---

## 🗺️ Escenarios

- **Bosque** — Entorno natural, primer nivel de la historia.
- **Desierto** — Arena bajo el sol abrasador, segundo nivel.
- **Ciudad cyberpunk** — Escenario final, combate bajo las luces de neón.

---

## ⚡ Sistema de combate

- **Puño** — Ataque rápido, daño bajo.
- **Patada** — Mayor alcance y daño que el puño.
- **Especial** — Ataque devastador que consume la barra de combo al 100%. Doble animación encadenada y knockback elevado.
- **Salto** — Permite esquivar y reposicionarse.
- **Barra de combo** — Se carga con cada golpe conectado. Al llenarse habilita el ataque especial.
- **Crítico** — Probabilidad de doblar el daño de un golpe (mejorable con buffs).

Los combates tienen un tiempo límite de **90 segundos**. Si el tiempo se agota, gana quien tenga más vida.

---

## 🎲 Sistema roguelike (modo Arcade)

Cada 3 victorias consecutivas ocurre un **evento aleatorio**:

- 💚 Curación del 30% de vida máxima.
- ☠️ Veneno: pierdes el 20% de tu vida actual.
- 💪 Combate más difícil: el enemigo siguiente es un 50% más fuerte.
- ✨ Modificador global: daño x1.5 en el siguiente combate.
- ❤️ El próximo enemigo llega con vida extra.

Tras cada victoria se ofrece una **selección de 3 buffs** de rareza aleatoria (COMMON / RARE / EPIC):

| Buff | Efecto |
|------|--------|
| `+Daño` | Aumenta el multiplicador de daño. |
| `+Velocidad` | Aumenta la velocidad de movimiento. |
| `+Vida Máx` | Incrementa la salud máxima. |
| `+Robo Vida` | Recupera vida proporcional al daño infligido. |
| `+Defensa` | Reduce el daño recibido (armor %). |
| `+Crítico` | Aumenta la probabilidad de golpe crítico. |
| `+Regen. Vida` | Regeneración pasiva de vida por segundo. |

---

## 🤖 IA enemiga

La inteligencia artificial adapta su comportamiento según el nivel de dificultad:

| Nivel | Vida | Velocidad | Agresividad |
|-------|------|-----------|-------------|
| 1 — Dark Fighter | 250 | 300 | Baja |
| 2 — Ronin Boss | 250 | 400 | Media |
| 3 — Shadow Master | 250 | 500 | Alta |

---

## 🛠️ Tecnologías

- **Java** con framework [libGDX](https://libgdx.com/)
- **Gradle** como sistema de build
- **Box2D** (incluido vía libGDX)
- **Scene2D** para la interfaz de usuario
- **TextureAtlas** para la gestión de sprites animados
- Plataformas: **Android** y **Escritorio (LWJGL3)**

---

## 🚀 Compilar y ejecutar

### Requisitos

- JDK 11 o superior
- Android SDK (solo para build Android)

### Escritorio

```bash
./gradlew lwjgl3:run
```

### Generar JAR ejecutable

```bash
./gradlew lwjgl3:jar
# El JAR se genera en: lwjgl3/build/libs/
```

### Android

```bash
./gradlew android:installDebug
```

### Otros comandos útiles

```bash
./gradlew build          # Compila todos los módulos
./gradlew clean          # Limpia los directorios de build
./gradlew android:lint   # Valida el proyecto Android
```

---

## 📁 Estructura del proyecto

```
ChaosArena/
├── core/               # Lógica principal del juego (compartida)
│   └── src/main/java/io/github/chaosarena/
│       ├── Main.java               # Punto de entrada, recursos globales
│       ├── GameScreen.java         # Pantalla principal de combate
│       ├── Player.java             # Lógica del jugador y animaciones
│       ├── EnemyAI.java            # Inteligencia artificial del enemigo
│       ├── RunManager.java         # Gestión del modo roguelike
│       ├── CharacterSelectScreen.java
│       ├── MainMenuScreen.java
│       ├── Buff.java / BuffType.java
│       ├── Event.java
│       └── StageDef.java
├── android/            # Módulo Android
├── lwjgl3/             # Módulo escritorio
└── assets/             # Recursos del juego
    ├── sprites/        # Atlases de personajes y enemigos
    ├── backgrounds/    # Fondos de escenarios
    ├── sounds/         # Música
    ├── joystick/       # Gráficos del joystick táctil
    └── ui/             # Botones e iconos de interfaz
```

---

## 🎵 Audio

El juego incluye música de fondo en bucle con control de volumen persistente (activar/desactivar) guardado en las preferencias del dispositivo.

---

## 📄 Licencia

Este proyecto fue generado con [gdx-liftoff](https://github.com/libgdx/gdx-liftoff). Consulta los términos de licencia de libGDX para el uso del framework.
