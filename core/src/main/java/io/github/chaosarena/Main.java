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

    // ── Estados ────────────────────────────────────────────────────────────────
    private enum GameState { MAIN_MENU, NEW_GAME, CONTINUE, CHAR_SELECT, PLAYING }
    private GameState currentState = GameState.MAIN_MENU;

    // ── Core (igual que tenías) ────────────────────────────────────────────────
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

    // ── UI combate (igual que tenías) ──────────────────────────────────────────
    private Table attackButtonTable, endGameTable;
    private Container<Touchpad> joystickContainer;
    private TextButton endGameBtn;

    // ── UI menús (nuevo) ───────────────────────────────────────────────────────
    private Table mainMenuTable;
    private Table newGameTable;
    private Table continueTable;
    private Table charSelectTable;
    private Preferences prefs;

    // Selección de personajes para duelo
    private int duelP1 = -1;
    private int duelP2 = -1;
    private Label lblP1sel, lblP2sel;

    // Personajes disponibles (añade los tuyos aquí)
    private static final String[] CHAR_NAMES = { "Abel", "Kano", "Sonya", "Johnny", "Raiden", "Scorpion" };

    // ── Dimensiones (igual que tenías) ────────────────────────────────────────
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

        player1 = new Player("Abel", "sprites/player/player_atlas/game_atlas.atlas", 300,              150, true,  WORLD_HEIGHT);
        player2 = new Player("Kano", "sprites/player/player_atlas/game_atlas.atlas", WORLD_WIDTH - 900, 150, false, WORLD_HEIGHT);

        enemyAI = new EnemyAI(player2, player1);

        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        setupGameUI();   // UI de combate (igual que tenías)
        setupMenuUI();   // Menús nuevos

        hideGameUI();
        showMainMenu();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  FUENTES Y TEXTURAS  (sin cambios)
    // ══════════════════════════════════════════════════════════════════════════

    private void setupFonts() {
        font = new BitmapFont();
        font.getData().setScale(3.5f);
        bigFont = new BitmapFont();
        bigFont.getData().setScale(8f);
        nameFont = new BitmapFont();
        nameFont.getData().setScale(2.5f);
    }

    private void setupTextures() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whiteTexture = new Texture(pixmap);
        pixmap.dispose();

        background   = new Texture("backgrounds/zigala.png");
        joystickBg   = new Texture("joystick/AIR_joystick_bg600.png");
        joystickKnob = new Texture("joystick/AIR_joystick_stick600.png");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ESTILOS DE BOTONES
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

    // ══════════════════════════════════════════════════════════════════════════
    //  UI DE COMBATE  (sin cambios respecto a tu código)
    // ══════════════════════════════════════════════════════════════════════════

    private void setupGameUI() {
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

        endGameTable = new Table();
        endGameTable.setFillParent(true);
        endGameBtn = new TextButton("REINTENTAR", fightStyle());
        endGameBtn.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { resetGame(); }
        });

        TextButton btnMenu = new TextButton("MENU PRINCIPAL", dangerStyle());
        btnMenu.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                hideGameUI();
                showMainMenu();
            }
        });

        endGameTable.center();
        endGameTable.add(endGameBtn).size(600, 180).padBottom(20).row();
        endGameTable.add(btnMenu)   .size(500, 140);
        endGameTable.setVisible(false);

        stage.addActor(joystickContainer);
        stage.addActor(attackButtonTable);
        stage.addActor(endGameTable);
    }

    private void hideGameUI() {
        joystickContainer.setVisible(false);
        attackButtonTable.setVisible(false);
        endGameTable.setVisible(false);
    }

    private void showGameUI() {
        joystickContainer.setVisible(true);
        attackButtonTable.setVisible(true);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  UI DE MENÚS
    // ══════════════════════════════════════════════════════════════════════════

    private void setupMenuUI() {
        mainMenuTable  = new Table(); mainMenuTable.setFillParent(true);
        newGameTable   = new Table(); newGameTable.setFillParent(true);
        continueTable  = new Table(); continueTable.setFillParent(true);
        charSelectTable = new Table(); charSelectTable.setFillParent(true);

        stage.addActor(mainMenuTable);
        stage.addActor(newGameTable);
        stage.addActor(continueTable);
        stage.addActor(charSelectTable);

        hideAllMenus();
        buildMainMenu(); // El menú principal es estático, se construye una vez
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
            TextButton btn = new TextButton(slotLabel(i), slotExists(i) ? redStyle() : fightStyle());
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    saveSlot(slot, "Guerrero", 0);
                    startGame();
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
                    loadSlot(slot);
                    startGame();
                }
            });

            TextButton btnDel = new TextButton("X", dangerStyle());
            btnDel.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    deleteSlot(slot);
                    showContinueMenu(); // refresca la pantalla
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
        duelP1 = -1;
        duelP2 = -1;
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

        // Grid de personajes — 3 por fila
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
            // Toca de nuevo para resetear
            duelP1 = -1; duelP2 = -1;
            lblP1sel.setText("J1: ---");
            lblP2sel.setText("J2: ---");
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  ARRANCAR PARTIDA
    // ══════════════════════════════════════════════════════════════════════════

    private void startGame() {
        currentState = GameState.PLAYING;
        hideAllMenus();
        showGameUI();
        resetGame();
    }

    private void startDuel() {
        player1.name = CHAR_NAMES[duelP1];
        player2.name = CHAR_NAMES[duelP2];
        startGame();
    }

    private void loadSlot(int slot) {
        player1.name = prefs.getString("slot_" + slot + "_name", "Abel");
    }

    private void resetGame() {
        player1.currentHealth = player1.maxHealth;
        player1.comboCharge   = 0;
        player1.x             = 200;
        player2.currentHealth = player2.maxHealth;
        player2.comboCharge   = 0;
        player2.x             = WORLD_WIDTH - 1000;
        isGameOver            = false;
        winMessage            = "";
        endGameTable.setVisible(false);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GUARDADO
    // ══════════════════════════════════════════════════════════════════════════

    private boolean slotExists(int slot) {
        return prefs.getBoolean("slot_" + slot + "_exists", false);
    }

    private void saveSlot(int slot, String name, int wins) {
        prefs.putBoolean("slot_" + slot + "_exists", true);
        prefs.putString( "slot_" + slot + "_name",   name);
        prefs.putInteger("slot_" + slot + "_wins",   wins);
        prefs.flush();
    }

    private void deleteSlot(int slot) {
        prefs.remove("slot_" + slot + "_exists");
        prefs.remove("slot_" + slot + "_name");
        prefs.remove("slot_" + slot + "_wins");
        prefs.flush();
    }

    private String slotLabel(int slot) {
        if (!slotExists(slot)) return "Ranura " + (slot + 1) + "  -  VACIA";
        String name = prefs.getString( "slot_" + slot + "_name", "?");
        int    wins = prefs.getInteger("slot_" + slot + "_wins",  0);
        return "Ranura " + (slot + 1) + "  -  " + name + "   Victorias: " + wins;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  RENDER  (sin cambios salvo el título en menús)
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
            if (isGameOver) {
                layout.setText(bigFont, winMessage);
                bigFont.setColor(winMessage.equals("Game Over") ? Color.RED : Color.YELLOW);
                bigFont.draw(batch, winMessage,
                    (viewport.getWorldWidth()  - layout.width)  / 2f,
                    (viewport.getWorldHeight() + layout.height) / 2f);
            }
        } else {
            // Título visible en todos los menús
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

        nameFont.draw(batch, player1.name, margin, yPos + 75);
        drawBar(margin, yPos,      barW, barH, player1.currentHealth / player1.maxHealth, Color.GREEN);
        drawBar(margin, yPos - 35, barW, 20,   player1.comboCharge   / 100f,              Color.GOLD);

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

    private void checkGameOver() {
        if (player1.currentHealth <= 0) {
            isGameOver = true; winMessage = "Game Over";
            endGameBtn.setText("REVANCHA");
            endGameTable.setVisible(true);
        } else if (player2.currentHealth <= 0) {
            isGameOver = true; winMessage = "VICTORIA!";
            endGameBtn.setText("SIGUIENTE NIVEL");
            endGameTable.setVisible(true);
        }
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
