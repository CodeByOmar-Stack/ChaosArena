# ⚔️ Chaos Arena

> Videojuego de acción y combate para Android desarrollado con LibGDX.

---

## 👥 Desarrolladores

| Nombre | Rol |
|---|---|
| Álvaro Rodrigo | Desarrollador |
| Omar Barrero | Desarrollador |

---

## 📖 Descripción

**Chaos Arena** es un videojuego de acción en el que el jugador controla a un guerrero que debe sobrevivir a oleadas de enemigos en distintos escenarios. Con controles táctiles intuitivos, múltiples personajes jugables y una banda sonora épica, el objetivo es claro: sobrevivir al caos.

---

## 🎮 Características

- Combate en tiempo real con **joystick virtual** y botones de acción en pantalla
- **3 personajes** jugables: Shadow Fist, Iron Claw y Ronin, cada uno con sus propias animaciones
- **3 escenarios** de combate: Bosque, Desierto y Ciudad
- **IA del enemigo** con comportamiento por estados: persecución, ataque y retirada
- **Sistema de buffs** roguelike entre combates (daño, velocidad, vida, crítico…)
- **Modos de juego**: Historia, Duelo y Arcade
- Música de fondo con control de sonido desde la UI
- Modo inmersivo en Android (pantalla completa)

---

## 🛠️ Tecnologías utilizadas

| Tecnología | Versión | Uso |
|---|---|---|
| [LibGDX](https://libgdx.com/) | 1.14.0 | Framework principal del juego |
| Java | 21 | Lenguaje de programación |
| Android SDK | API 21 – 35 | Plataforma objetivo |
| Box2D | incluido en LibGDX | Físicas y colisiones |
| FreeType | incluido en LibGDX | Renderizado de fuentes |
| Gradle | 8.x | Build y gestión de dependencias |
| Android Studio | — | IDE de desarrollo |

---

## 📁 Estructura del proyecto

```
ChaosArena/
├── core/                        # Lógica del juego (compartida entre plataformas)
│   └── src/main/java/
│       ├── Main.java            # Punto de entrada y constantes globales
│       ├── MainMenuScreen.java  # Menú principal
│       ├── CharacterSelectScreen.java  # Selección de personaje y escenario
│       ├── GameScreen.java      # Bucle principal del combate
│       ├── Player.java          # Entidad jugador/enemigo con animaciones
│       ├── EnemyAI.java         # Inteligencia artificial del enemigo (FSM)
│       ├── HUD.java             # Joystick, botones y elementos en pantalla
│       ├── RunManager.java      # Gestión de buffs y progreso entre combates
│       ├── Buff.java / BuffType.java / BuffSelectionUI.java  # Sistema de mejoras
│       ├── Event.java           # Eventos aleatorios entre combates
│       ├── StageDef.java        # Definición de escenarios
│       ├── PauseMenu.java       # Menú de pausa
│       └── ResourceManager.java # Carga y gestión centralizada de assets
│
├── android/                     # Módulo Android
│   ├── AndroidManifest.xml      # Configuración de la app
│   ├── src/                     # AndroidLauncher (arranca el juego)
│   └── res/                     # Iconos en distintas resoluciones (mipmap)
│
├── lwjgl3/                      # Módulo de escritorio (pruebas en PC)
│
└── assets/                      # Recursos compartidos
    ├── backgrounds/             # Fondos de cada escenario
    ├── sprites/                 # Spritesheets y atlas de personajes y enemigos
    ├── ui/                      # Botones, joystick e iconos de interfaz
    └── sounds/                  # Música principal del juego
```

---

## 🚀 Cómo ejecutar el proyecto

### Requisitos

- [Android Studio](https://developer.android.com/studio)
- Android SDK (API 21 o superior)
- JDK 21
- Dispositivo Android o emulador

### Pasos

1. Abre Android Studio → **Open** → selecciona la carpeta `ChaosArena/`
2. Espera a que Gradle sincronice
3. Conecta tu móvil con **depuración USB** activada
4. Pulsa ▶️ **Run**

También puedes probarlo en PC sin móvil:

```bash
./gradlew lwjgl3:run
```

---

## 📱 Configuración Android

| Parámetro | Valor |
|---|---|
| `applicationId` | `io.github.chaosarena` |
| `minSdkVersion` | 21 (Android 5.0) |
| `targetSdkVersion` | 35 |
| `versionName` | 1.0 |

---

## 📄 Licencia

Proyecto académico desarrollado por **Álvaro Rodrigo** y **Omar Barrero** — 2025/2026.
