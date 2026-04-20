package io.github.chaosarena;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {

    // ══════════════════════════════════════════════════════════════════════════
    //  ESTADOS
    // ══════════════════════════════════════════════════════════════════════════

    private enum GameState { MAIN_MENU, NEW_GAME, CONTINUE, CHAR_SELECT, PLAYING }
    private GameState currentState = GameState.MAIN_MENU;

    // ══════════════════════════════════════════════════════════════════════════
    //  NIVELES
    //  Toda la dificultad de cada nivel está aquí concentrada.
    //  Cuando cambies sprites, hazlo también aquí añadiendo un campo atlasPath.
    // ══════════════════════════════════════════════════════════════════════════

    private static final int MAX_LEVELS = 3;
    private int currentLevel = 1;   // 1, 2 o 3
    private int activeSlot   = -1;  // ranura de guardado activa (-1 = duelo/sin guardado)

    // Configuración por nivel: { vidaEnemigo, velocidadIA, dañoGolpeIA, frecuenciaAtaqueIA }
    // velocidadIA    → píxeles/seg que se mueve la IA (se pasa a EnemyAI)
    // dañoGolpeIA    → daño base que hace la IA al jugador en cada golpe
    // frecAtaqueIA   → cada cuántos segundos ataca la IA (menor = más agresiva)
    private static final float[] LEVEL_ENEMY_HEALTH = { 300f,  600f,  1000f };
    private static final float[] LEVEL_ENEMY_SPEED  = { 350f,  520f,   750f };
    private static final float[] LEVEL_ENEMY_DAMAGE = {   5f,   10f,    18f };
    private static final float[] LEVEL_ATTACK_RATE  = {   1.8f,  1.1f,   0.6f };

    // Nombres del enemigo por nivel
    // TODO: cuando tengas los sprites, cambia también el atlas en applyLevelConfig()
    private static final String[] LEVEL_ENEMY_NAMES = { "Kano", "Shang Tsung", "Shao Kahn" };

    // ══════════════════════════════════════════════════════════════════════════
    //  CORE
    // ══════════════════════════════════════════════════════════════════════════

    private SpriteBatch batch;
    private BitmapFont font, bigFont, nameFont;
    private Texture background, whiteTexture;
    private Player player1, player2;
    private EnemyAI enemyAI;
    private Stage stage;
    private Viewport viewport;

    private Touchpad joystick;
    private Texture joystickBg, joystickKnob;
    private Music music;

    private boolean isGameOver = false;
    private String winMessage  = "";
    private final GlyphLayout layout = new GlyphLayout();

    // ── UI combate ────────────────────────────────────────────────────────────
    private Table attackButtonTable;
    private Container<Touchpad> joystickContainer;

    // ── Pantalla de resultado (victoria / derrota) ────────────────────────────
    private Table resultTable;          // tabla central con el mensaje
    private Label  lblResultTitle;      // "NIVEL 2" / "GAME OVER" / "¡JUEGO COMPLETADO!"
    private Label  lblResultSub;        // subtítulo descriptivo
    private TextButton btnResultAction; // "SIGUIENTE NIVEL" / "MENU PRINCIPAL"

    // ── UI menús ──────────────────────────────────────────────────────────────
    private Table mainMenuTable;
    private Table newGameTable;
    private Table continueTable;
    private Table charSelectTable;
    private Preferences prefs;

    // Selección de personajes para duelo
    private int duelP1 = -1;
    private int duelP2 = -1;
    private Label lblP1sel, lblP2sel;

    private static final String[] CHAR_NAMES = { "Abel", "Kano", "Sonya", "Johnny", "Raiden", "Scorpion" };

    // ── Dimensiones ───────────────────────────────────────────────────────────
    private static final float WORLD_WIDTH  = 1920;
    private static final float WORLD_HEIGHT = 1080;
    private static final int   MAX_SLOTS    = 3;

    // ══════════════════════════════════════════════════════════════════════════
    //  CREATE
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public void create() {
        batch    = new SpriteBatch();
        viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);
        prefs    = Gdx.app.getPreferences("ChaosArenaPrefs");

        setupFonts();
        setupTextures();

        try {
            music = Gdx.audio.newMusic(Gdx.files.internal("sounds/Techno_Syndrome.mp3"));
            music.setLooping(true);
            music.setVolume(0.8f);
            music.play();
        } catch (Exception e) {
            Gdx.app.error("AUDIO", "Error: " + e.getMessage());
        }

        player1 = new Player("Abel", "sprites/player/player_atlas/game_atlas.atlas",
            300, 150, true, WORLD_HEIGHT);
        player2 = new Player("Kano", "sprites/player/player_atlas/game_atlas.atlas",
            WORLD_WIDTH - 900, 150, false, WORLD_HEIGHT);

        enemyAI = new EnemyAI(player2, player1);

        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        setupGameUI();
        setupMenuUI();

        hideGameUI();
        showMainMenu();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FUENTES Y TEXTURAS
    // ══════════════════════════════════════════════════════════════════════════

    private void setupFonts() {
        font = new BitmapFont();     font.getData().setScale(3.5f);
        bigFont = new BitmapFont();  bigFont.getData().setScale(8f);
        nameFont = new BitmapFont(); nameFont.getData().setScale(2.5f);
    }

    private void setupTextures() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE); pixmap.fill();
        whiteTexture = new Texture(pixmap);
        pixmap.dispose();

        background   = new Texture("backgrounds/zigala.png");
        joystickBg   = new Texture("joystick/AIR_joystick_bg600.png");
        joystickKnob = new Texture("joystick/AIR_joystick_stick600.png");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ESTILOS
    // ══════════════════════════════════════════════════════════════════════════

    private TextButton.TextButtonStyle makeStyle(Color bg) {
        TextButton.TextButtonStyle s = new TextButton.TextButtonStyle();
        s.font = font;
        s.up   = new TextureRegionDrawable(whiteTexture).tint(bg);
        s.down = new TextureRegionDrawable(whiteTexture).tint(Color.FIREBRICK);
        return s;
    }

    private TextButton.TextButtonStyle fightStyle()  { return makeStyle(new Color(0.1f,  0.1f,  0.1f, 0.7f)); }
    private TextButton.TextButtonStyle redStyle()    { return makeStyle(new Color(0.55f, 0.05f, 0.05f, 0.9f)); }
    private TextButton.TextButtonStyle goldStyle()   { return makeStyle(new Color(0.45f, 0.35f, 0.0f,  0.9f)); }
    private TextButton.TextButtonStyle dangerStyle() { return makeStyle(new Color(0.25f, 0.0f,  0.0f,  0.85f)); }
    private TextButton.TextButtonStyle greenStyle()  { return makeStyle(new Color(0.05f, 0.4f,  0.05f, 0.9f)); }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI DE COMBATE
    // ══════════════════════════════════════════════════════════════════════════

    private void setupGameUI() {
        // Joystick
        Touchpad.TouchpadStyle jsStyle = new Touchpad.TouchpadStyle();
        jsStyle.background = new TextureRegionDrawable(joystickBg);
        jsStyle.knob       = new TextureRegionDrawable(joystickKnob);
        jsStyle.knob.setMinWidth(180);
        jsStyle.knob.setMinHeight(180);

        joystick          = new Touchpad(20, jsStyle);
        joystickContainer = new Container<Touchpad>(joystick);
        joystickContainer.size(350);
        joystickContainer.setFillParent(true);
        joystickContainer.bottom().left().padLeft(100).padBottom(100);

        // Botones de ataque
        attackButtonTable = new Table();
        attackButtonTable.setFillParent(true);
        attackButtonTable.bottom().right().padRight(100).padBottom(100);

        TextButton punch   = new TextButton("PUNCH",   fightStyle());
        TextButton kick    = new TextButton("KICK",    fightStyle());
        TextButton special = new TextButton("SPECIAL", fightStyle());

        punch.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (!isGameOver) {
                    player1.attack(Player.AttackType.PUNCH);
                    if (player1.canHit(player2)) { player2.takeDamage(4); player1.addCharge(10); }
                }
            }
        });
        kick.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (!isGameOver) {
                    player1.attack(Player.AttackType.KICK);
                    if (player1.canHit(player2)) { player2.takeDamage(8); player1.addCharge(15); }
                }
            }
        });
        special.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (!isGameOver && player1.comboCharge >= player1.MAX_COMBO_CHARGE) {
                    player1.attack(Player.AttackType.SPECIAL);
                    if (player1.canHit(player2)) player2.takeDamage(35);
                }
            }
        });

        attackButtonTable.add(special).size(350, 130).padBottom(25).row();
        attackButtonTable.add(kick)   .size(350, 130).padBottom(25).row();
        attackButtonTable.add(punch)  .size(350, 130);

        // ── Pantalla de resultado ─────────────────────────────────────────────
        // Se reutiliza para victoria, derrota y juego completado.
        // Se configura dinámicamente en showResult().
        resultTable = new Table();
        resultTable.setFillParent(true);
        resultTable.center();

        Label.LabelStyle titleStyle = new Label.LabelStyle(bigFont, Color.YELLOW);
        Label.LabelStyle subStyle   = new Label.LabelStyle(font,    Color.WHITE);

        lblResultTitle  = new Label("", titleStyle);
        lblResultSub    = new Label("", subStyle);
        btnResultAction = new TextButton("", fightStyle());

        // El listener del botón decide qué hacer según el estado actual
        btnResultAction.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                onResultButtonPressed();
            }
        });

        TextButton btnBackToMenu = new TextButton("MENU PRINCIPAL", dangerStyle());
        btnBackToMenu.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                hideGameUI();
                showMainMenu();
            }
        });

        resultTable.add(lblResultTitle) .padBottom(20).row();
        resultTable.add(lblResultSub)   .padBottom(50).row();
        resultTable.add(btnResultAction).size(650, 160).padBottom(20).row();
        resultTable.add(btnBackToMenu)  .size(500, 130);
        resultTable.setVisible(false);

        stage.addActor(joystickContainer);
        stage.addActor(attackButtonTable);
        stage.addActor(resultTable);
    }

    private void hideGameUI() {
        joystickContainer.setVisible(false);
        attackButtonTable.setVisible(false);
        resultTable.setVisible(false);
    }

    private void showGameUI() {
        joystickContainer.setVisible(true);
        attackButtonTable.setVisible(true);
        resultTable.setVisible(false);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  PANTALLA DE RESULTADO  (victoria / derrota / juego completado)
    // ══════════════════════════════════════════════════════════════════════════

    /** Configura y muestra la pantalla de resultado según si ganó o perdió. */
    private void showResult(boolean playerWon) {
        isGameOver = true;
        joystickContainer.setVisible(false);
        attackButtonTable.setVisible(false);

        if (!playerWon) {
            // ── DERROTA ───────────────────────────────────────────────────────
            lblResultTitle.setText("GAME OVER");
            lblResultTitle.setStyle(new Label.LabelStyle(bigFont, Color.RED));
            lblResultSub.setText("Has sido derrotado en el Nivel " + currentLevel);
            btnResultAction.setText("REINTENTAR");
            // No guardamos progreso al perder, pero sí mantenemos el nivel alcanzado
            // La ranura ya tiene guardado el nivel antes de empezar el combate

        } else if (currentLevel >= MAX_LEVELS) {
            // ── JUEGO COMPLETADO ──────────────────────────────────────────────
            lblResultTitle.setText("¡JUEGO COMPLETADO!");
            lblResultTitle.setStyle(new Label.LabelStyle(bigFont, Color.GOLD));
            lblResultSub.setText("¡Has dominado los 3 niveles de Chaos Arena!");
            btnResultAction.setText("MENU PRINCIPAL");
            // Marcamos la ranura como completada
            if (activeSlot >= 0) {
                int wins = prefs.getInteger("slot_" + activeSlot + "_wins", 0) + 1;
                prefs.putInteger("slot_" + activeSlot + "_wins", wins);
                prefs.putInteger("slot_" + activeSlot + "_level", 1); // reinicia al nivel 1
                prefs.flush();
            }

        } else {
            // ── NIVEL SUPERADO ────────────────────────────────────────────────
            int nextLevel = currentLevel + 1;
            lblResultTitle.setText("NIVEL " + currentLevel + " SUPERADO");
            lblResultTitle.setStyle(new Label.LabelStyle(bigFont, Color.YELLOW));
            lblResultSub.setText("Prepárate para el Nivel " + nextLevel + "...");
            btnResultAction.setText("NIVEL " + nextLevel + "  >");
            // Guardamos que el jugador ha alcanzado el siguiente nivel
            if (activeSlot >= 0) {
                prefs.putInteger("slot_" + activeSlot + "_level", nextLevel);
                prefs.flush();
            }
        }

        resultTable.setVisible(true);
        winMessage = ""; // ya no necesitamos el texto superpuesto del render
    }

    /** Lo que hace el botón principal de resultado según el contexto. */
    private void onResultButtonPressed() {
        if (!isGameOver) return;

        boolean playerWon = player2.currentHealth <= 0;

        if (!playerWon) {
            // Derrota → reintentar el mismo nivel
            applyLevelConfig();
            startCombat();
        } else if (currentLevel >= MAX_LEVELS) {
            // Juego completado → menú principal
            hideGameUI();
            showMainMenu();
        } else {
            // Victoria en nivel 1 o 2 → avanza al siguiente
            currentLevel++;
            applyLevelConfig();
            startCombat();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CONFIGURACIÓN DE NIVEL
    //  Aquí se ajustan vida, velocidad y agresividad según el nivel actual.
    //  TODO: cuando tengas sprites nuevos para cada nivel, cámbialo aquí.
    // ══════════════════════════════════════════════════════════════════════════

    private void applyLevelConfig() {
        int idx = currentLevel - 1; // índice 0-based

        // Nombre del enemigo
        player2.name = LEVEL_ENEMY_NAMES[idx];

        // Vida del enemigo
        player2.maxHealth     = LEVEL_ENEMY_HEALTH[idx];
        player2.currentHealth = LEVEL_ENEMY_HEALTH[idx];

        // Velocidad y agresividad de la IA
        enemyAI.setSpeed(LEVEL_ENEMY_SPEED[idx]);
        enemyAI.setDamage(LEVEL_ENEMY_DAMAGE[idx]);
        enemyAI.setAttackRate(LEVEL_ATTACK_RATE[idx]);

        // TODO Nivel 1 → enemyAI.setAtlasPath("sprites/enemy/kano_atlas.atlas");
        // TODO Nivel 2 → enemyAI.setAtlasPath("sprites/enemy/shang_atlas.atlas");
        // TODO Nivel 3 → enemyAI.setAtlasPath("sprites/enemy/shaokahn_atlas.atlas");

        Gdx.app.log("NIVEL", "Nivel " + currentLevel
            + " | HP=" + player2.maxHealth
            + " | Speed=" + enemyAI.getSpeed()
            + " | Damage=" + enemyAI.getDamage()
            + " | AttackRate=" + enemyAI.getAttackRate());
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ARRANCAR / REINICIAR COMBATE
    // ══════════════════════════════════════════════════════════════════════════

    /** Inicia el combate del nivel actual (ya configurado con applyLevelConfig). */
    private void startCombat() {
        currentState = GameState.PLAYING;
        hideAllMenus();
        showGameUI();

        // Resetea posiciones y estado, respetando la vida/config ya aplicada
        player1.currentHealth = player1.maxHealth;
        player1.comboCharge   = 0;
        player1.x             = 200;
        player2.comboCharge   = 0;
        player2.x             = WORLD_WIDTH - 1000;
        isGameOver            = false;
        winMessage            = "";
        resultTable.setVisible(false);
    }

    /** Arranca desde el nivel indicado (usado por Nueva partida y Continuar). */
    private void startFromLevel(int level, int slot) {
        activeSlot   = slot;
        currentLevel = level;
        applyLevelConfig();
        startCombat();
    }

    private void startDuel() {
        activeSlot   = -1; // duelo no guarda progreso
        currentLevel =  1;
        player1.name = CHAR_NAMES[duelP1];
        player2.name = CHAR_NAMES[duelP2];
        // En duelo usamos configuración de nivel 1 para la IA
        applyLevelConfig();
        startCombat();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI DE MENÚS
    // ══════════════════════════════════════════════════════════════════════════

    private void setupMenuUI() {
        mainMenuTable   = new Table(); mainMenuTable.setFillParent(true);
        newGameTable    = new Table(); newGameTable.setFillParent(true);
        continueTable   = new Table(); continueTable.setFillParent(true);
        charSelectTable = new Table(); charSelectTable.setFillParent(true);

        stage.addActor(mainMenuTable);
        stage.addActor(newGameTable);
        stage.addActor(continueTable);
        stage.addActor(charSelectTable);

        hideAllMenus();
        buildMainMenu();
    }

    private void hideAllMenus() {
        mainMenuTable.setVisible(false);
        newGameTable.setVisible(false);
        continueTable.setVisible(false);
        charSelectTable.setVisible(false);
    }

    // ─── MENÚ PRINCIPAL ───────────────────────────────────────────────────────

    private void buildMainMenu() {
        mainMenuTable.center().padTop(220);

        TextButton btnNew      = new TextButton("NUEVA PARTIDA", redStyle());
        TextButton btnContinue = new TextButton("CONTINUAR",     fightStyle());
        TextButton btnDuel     = new TextButton("DUELO",         goldStyle());

        btnNew.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { showNewGameMenu(); }
        });
        btnContinue.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { showContinueMenu(); }
        });
        btnDuel.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { showCharSelect(); }
        });

        mainMenuTable.add(btnNew)     .size(700, 150).padBottom(30).row();
        mainMenuTable.add(btnContinue).size(700, 150).padBottom(30).row();
        mainMenuTable.add(btnDuel)    .size(700, 150);
    }

    private void showMainMenu() {
        currentState = GameState.MAIN_MENU;
        hideAllMenus();
        hideGameUI();
        mainMenuTable.setVisible(true);
    }

    // ─── NUEVA PARTIDA ────────────────────────────────────────────────────────

    private void showNewGameMenu() {
        currentState = GameState.NEW_GAME;
        hideAllMenus();
        newGameTable.clear();
        newGameTable.center();

        for (int i = 0; i < MAX_SLOTS; i++) {
            final int slot = i;
            String label = slotExists(i)
                ? "Ranura " + (i+1) + "  -  OCUPADA [SOBREESCRIBIR]"
                : "Ranura " + (i+1) + "  -  VACIA";
            TextButton btn = new TextButton(label, slotExists(i) ? redStyle() : fightStyle());
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    // Nueva partida siempre empieza en nivel 1
                    saveSlotFull(slot, "Guerrero", 0, 1);
                    startFromLevel(1, slot);
                }
            });
            newGameTable.add(btn).size(900, 130).padBottom(20).row();
        }

        TextButton btnBack = new TextButton("VOLVER", dangerStyle());
        btnBack.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { showMainMenu(); }
        });
        newGameTable.add(btnBack).size(400, 110).padTop(20);
        newGameTable.setVisible(true);
    }

    // ─── CONTINUAR ────────────────────────────────────────────────────────────

    private void showContinueMenu() {
        currentState = GameState.CONTINUE;
        hideAllMenus();
        continueTable.clear();
        continueTable.center();

        boolean any = false;
        for (int i = 0; i < MAX_SLOTS; i++) {
            if (!slotExists(i)) continue;
            any = true;
            final int slot = i;

            TextButton btnLoad = new TextButton(slotLabel(i) + "  > JUGAR", fightStyle());
            btnLoad.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    int level = prefs.getInteger("slot_" + slot + "_level", 1);
                    startFromLevel(level, slot);
                }
            });

            TextButton btnDel = new TextButton("X", dangerStyle());
            btnDel.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    deleteSlot(slot);
                    showContinueMenu();
                }
            });

            Table row = new Table();
            row.add(btnLoad).size(750, 130).padRight(20);
            row.add(btnDel) .size(130, 130);
            continueTable.add(row).padBottom(20).row();
        }

        if (!any) {
            Label noSaves = new Label("No hay partidas guardadas", new Label.LabelStyle(font, Color.GRAY));
            continueTable.add(noSaves).padBottom(30).row();
        }

        TextButton btnBack = new TextButton("VOLVER", dangerStyle());
        btnBack.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { showMainMenu(); }
        });
        continueTable.add(btnBack).size(400, 110).padTop(10);
        continueTable.setVisible(true);
    }

    // ─── SELECCIÓN DE PERSONAJE (Duelo) ───────────────────────────────────────

    private void showCharSelect() {
        currentState = GameState.CHAR_SELECT;
        duelP1 = -1; duelP2 = -1;
        hideAllMenus();
        charSelectTable.clear();
        charSelectTable.center();

        lblP1sel = new Label("J1: ---", new Label.LabelStyle(font, new Color(1f, 0.5f, 0f, 1f)));
        lblP2sel = new Label("J2: ---", new Label.LabelStyle(font, new Color(0.3f, 0.7f, 1f, 1f)));

        Table vsRow = new Table();
        vsRow.add(lblP1sel).padRight(80);
        vsRow.add(new Label("VS", new Label.LabelStyle(bigFont, Color.RED))).padRight(80);
        vsRow.add(lblP2sel);
        charSelectTable.add(vsRow).padBottom(40).row();

        Table grid = new Table();
        for (int i = 0; i < CHAR_NAMES.length; i++) {
            final int idx = i;
            TextButton btn = new TextButton(CHAR_NAMES[i], fightStyle());
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) { onCharSelected(idx); }
            });
            grid.add(btn).size(380, 120).pad(12);
            if ((i + 1) % 3 == 0) grid.row();
        }
        charSelectTable.add(grid).padBottom(30).row();

        TextButton btnFight = new TextButton("FIGHT!", redStyle());
        btnFight.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (duelP1 >= 0 && duelP2 >= 0) startDuel();
            }
        });
        TextButton btnBack = new TextButton("VOLVER", dangerStyle());
        btnBack.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { showMainMenu(); }
        });

        Table btnRow = new Table();
        btnRow.add(btnFight).size(500, 150).padRight(30);
        btnRow.add(btnBack) .size(380, 150);
        charSelectTable.add(btnRow);
        charSelectTable.setVisible(true);
    }

    private void onCharSelected(int idx) {
        if (duelP1 == -1) {
            duelP1 = idx;
            lblP1sel.setText("J1: " + CHAR_NAMES[idx]);
        } else if (duelP2 == -1 && idx != duelP1) {
            duelP2 = idx;
            lblP2sel.setText("J2: " + CHAR_NAMES[idx]);
        } else {
            duelP1 = -1; duelP2 = -1;
            lblP1sel.setText("J1: ---");
            lblP2sel.setText("J2: ---");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GUARDADO
    // ══════════════════════════════════════════════════════════════════════════

    private boolean slotExists(int slot) {
        return prefs.getBoolean("slot_" + slot + "_exists", false);
    }

    private void saveSlotFull(int slot, String name, int wins, int level) {
        prefs.putBoolean("slot_" + slot + "_exists", true);
        prefs.putString( "slot_" + slot + "_name",   name);
        prefs.putInteger("slot_" + slot + "_wins",   wins);
        prefs.putInteger("slot_" + slot + "_level",  level);
        prefs.flush();
    }

    private void deleteSlot(int slot) {
        prefs.remove("slot_" + slot + "_exists");
        prefs.remove("slot_" + slot + "_name");
        prefs.remove("slot_" + slot + "_wins");
        prefs.remove("slot_" + slot + "_level");
        prefs.flush();
    }

    private String slotLabel(int slot) {
        if (!slotExists(slot)) return "Ranura " + (slot + 1) + "  -  VACIA";
        String name  = prefs.getString( "slot_" + slot + "_name",  "?");
        int    wins  = prefs.getInteger("slot_" + slot + "_wins",   0);
        int    level = prefs.getInteger("slot_" + slot + "_level",  1);
        return "Ranura " + (slot+1) + "  |  " + name + "  |  Nivel " + level + "  |  Victorias: " + wins;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  RENDER
    // ══════════════════════════════════════════════════════════════════════════

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        float delta = Gdx.graphics.getDeltaTime();

        if (currentState == GameState.PLAYING && !isGameOver) {
            handlePlayerMovement(delta);
            enemyAI.update(delta);
            checkGameOver();
        }

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        if (currentState == GameState.PLAYING) {
            player1.draw(batch);
            player2.draw(batch);
            drawUI();
        } else {
            drawMenuTitle();
        }

        batch.end();
        stage.act(delta);
        stage.draw();
    }

    private void drawMenuTitle() {
        String title = "CHAOS ARENA";
        layout.setText(bigFont, title);
        bigFont.setColor(Color.RED);
        bigFont.draw(batch, title,
            (viewport.getWorldWidth() - layout.width) / 2f,
            viewport.getWorldHeight() - 80);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LÓGICA DE COMBATE
    // ══════════════════════════════════════════════════════════════════════════

    private void checkGameOver() {
        if (player1.currentHealth <= 0) {
            showResult(false); // derrota
        } else if (player2.currentHealth <= 0) {
            showResult(true);  // victoria
        }
    }

    private void handlePlayerMovement(float delta) {
        float inputX = joystick.getKnobPercentX();
        if (!player1.isAttacking()) {
            float speed = 850 * delta;
            if (inputX >  0.25f) player1.move( speed);
            if (inputX < -0.25f) player1.move(-speed);
        }
        player1.updateDirection(inputX > 0.25f, inputX < -0.25f);
        limit(player1);
        limit(player2);
    }

    private void limit(Player p) {
        float w = 120 * p.scale;
        if (p.x < 0) p.x = 0;
        if (p.x > viewport.getWorldWidth() - w) p.x = viewport.getWorldWidth() - w;
    }

    private void drawUI() {
        float margin = 80, barW = 750, barH = 55;
        float yPos   = viewport.getWorldHeight() - 130;

        // Indicador de nivel (centro arriba)
        String lvlText = "NIVEL  " + currentLevel;
        layout.setText(nameFont, lvlText);
        nameFont.setColor(Color.YELLOW);
        nameFont.draw(batch, lvlText,
            (viewport.getWorldWidth() - layout.width) / 2f,
            viewport.getWorldHeight() - 30);
        nameFont.setColor(Color.WHITE);

        // Barras jugador 1
        nameFont.draw(batch, player1.name, margin, yPos + 75);
        drawBar(margin, yPos,      barW, barH, player1.currentHealth / player1.maxHealth, Color.GREEN);
        drawBar(margin, yPos - 35, barW, 20,   player1.comboCharge   / 100f,              Color.GOLD);

        // Barras jugador 2
        float x2 = viewport.getWorldWidth() - margin - barW;
        layout.setText(nameFont, player2.name);
        nameFont.draw(batch, player2.name, viewport.getWorldWidth() - margin - layout.width, yPos + 75);
        drawBar(x2, yPos,      barW, barH, player2.currentHealth / player2.maxHealth, Color.GREEN);
        drawBar(x2, yPos - 35, barW, 20,   player2.comboCharge   / 100f,              Color.GOLD);
    }

    private void drawBar(float x, float y, float w, float h, float pct, Color c) {
        batch.setColor(Color.RED);   batch.draw(whiteTexture, x, y, w, h);
        batch.setColor(c);           batch.draw(whiteTexture, x, y, w * Math.max(0, pct), h);
        batch.setColor(Color.WHITE);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose(); bigFont.dispose(); nameFont.dispose();
        background.dispose(); whiteTexture.dispose();
        player1.dispose(); player2.dispose();
        stage.dispose();
        if (music        != null) music.dispose();
        if (joystickBg   != null) joystickBg.dispose();
        if (joystickKnob != null) joystickKnob.dispose();
    }
}
