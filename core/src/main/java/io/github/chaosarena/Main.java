package io.github.chaosarena;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
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
    private String winMessage = "";
    private final GlyphLayout layout = new GlyphLayout();

    private Table attackButtonTable, endGameTable;
    private Container<Touchpad> joystickContainer;
    private TextButton endGameBtn; // Declarado correctamente

    // Dimensiones de diseño
    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    @Override
    public void create() {
        batch = new SpriteBatch();
        // ExtendViewport: Elimina barras negras permitiendo que el fondo crezca a los lados
        viewport = new ExtendViewport(WORLD_WIDTH, WORLD_HEIGHT);

        setupFonts();
        setupTextures();

        // --- AUDIO COMPATIBLE ---
        try {
            music = Gdx.audio.newMusic(Gdx.files.internal("sounds/Techno_Syndrome.mp3"));
            music.setLooping(true);
            music.setVolume(0.8f); // Sube el volumen para probar
            music.play();
        } catch (Exception e) {
            Gdx.app.error("AUDIO", "Error: " + e.getMessage());
        }

        player1 = new Player("Abel", "sprites/player/player_atlas/game_atlas.atlas", 300, 150, true, WORLD_HEIGHT);
        player2 = new Player("Kano", "sprites/player/player_atlas/game_atlas.atlas", WORLD_WIDTH - 900, 150, false, WORLD_HEIGHT);

        enemyAI = new EnemyAI(player2, player1);

        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        setupGameUI();
        resetGame();
    }

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

        background = new Texture("backgrounds/zigala.png");
        joystickBg = new Texture("joystick/AIR_joystick_bg600.png");
        joystickKnob = new Texture("joystick/AIR_joystick_stick600.png");
    }

    private void setupGameUI() {
        Touchpad.TouchpadStyle jsStyle = new Touchpad.TouchpadStyle();
        jsStyle.background = new TextureRegionDrawable(joystickBg);
        jsStyle.knob = new TextureRegionDrawable(joystickKnob);
        jsStyle.knob.setMinWidth(180);
        jsStyle.knob.setMinHeight(180);

        joystick = new Touchpad(20, jsStyle);

        joystickContainer = new Container<Touchpad>(joystick);
        joystickContainer.size(350);
        joystickContainer.setFillParent(true);
        joystickContainer.bottom().left().padLeft(100).padBottom(100);

        attackButtonTable = new Table();
        attackButtonTable.setFillParent(true);
        attackButtonTable.bottom().right().padRight(100).padBottom(100);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.up = new TextureRegionDrawable(whiteTexture).tint(new Color(0.1f, 0.1f, 0.1f, 0.7f));
        btnStyle.down = new TextureRegionDrawable(whiteTexture).tint(Color.FIREBRICK);

        TextButton punch = new TextButton("PUNCH", btnStyle);
        TextButton kick = new TextButton("KICK", btnStyle);
        TextButton special = new TextButton("SPECIAL", btnStyle);

        punch.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isGameOver) {
                    player1.attack(Player.AttackType.PUNCH);
                    if (player1.canHit(player2)) { player2.takeDamage(4); player1.addCharge(10); }
                }
            }
        });
        kick.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isGameOver) {
                    player1.attack(Player.AttackType.KICK);
                    if (player1.canHit(player2)) { player2.takeDamage(8); player1.addCharge(15); }
                }
            }
        });
        special.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isGameOver && player1.comboCharge >= player1.MAX_COMBO_CHARGE) {
                    player1.attack(Player.AttackType.SPECIAL);
                    if (player1.canHit(player2)) player2.takeDamage(35);
                }
            }
        });

        attackButtonTable.add(special).size(350, 130).padBottom(25).row();
        attackButtonTable.add(kick).size(350, 130).padBottom(25).row();
        attackButtonTable.add(punch).size(350, 130);

        // Tabla de fin de juego
        endGameTable = new Table();
        endGameTable.setFillParent(true);
        endGameBtn = new TextButton("REINTENTAR", btnStyle);
        endGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { resetGame(); }
        });
        endGameTable.add(endGameBtn).size(600, 180).center();
        endGameTable.setVisible(false);

        stage.addActor(joystickContainer);
        stage.addActor(attackButtonTable);
        stage.addActor(endGameTable);
    }

    private void resetGame() {
        player1.currentHealth = player1.maxHealth;
        player1.comboCharge = 0;
        player1.x = 200;
        player2.currentHealth = player2.maxHealth;
        player2.comboCharge = 0;
        player2.x = WORLD_WIDTH - 1000;
        isGameOver = false;
        winMessage = "";
        endGameTable.setVisible(false);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        float delta = Gdx.graphics.getDeltaTime();

        if (!isGameOver) {
            handlePlayerMovement(delta);
            enemyAI.update(delta);
            checkGameOver();
        }

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        // EL TRUCO PARA LOS BORDES: Dibujar el fondo usando el ancho real del Viewport
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        player1.draw(batch);
        player2.draw(batch);
        drawUI();

        if (isGameOver) {
            layout.setText(bigFont, winMessage);
            bigFont.setColor(winMessage.equals("Game Over") ? Color.RED : Color.YELLOW);
            bigFont.draw(batch, winMessage, (viewport.getWorldWidth() - layout.width) / 2, (viewport.getWorldHeight() + layout.height) / 2);
        }
        batch.end();

        stage.act(delta);
        stage.draw();
    }

    private void handlePlayerMovement(float delta) {
        float inputX = joystick.getKnobPercentX();
        if (!player1.isAttacking()) {
            float speed = 850 * delta;
            if (inputX > 0.25f) player1.move(speed);
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
        float margin = 80;
        float barW = 750;
        float barH = 55;
        // Usamos el alto real del viewport para que siempre esté arriba
        float yPos = viewport.getWorldHeight() - 130;

        // --- JUGADOR 1 (Abel) ---
        nameFont.draw(batch, player1.name, margin, yPos + 75);
        // Barra de Vida
        drawBar(margin, yPos, barW, barH, player1.currentHealth / player1.maxHealth, Color.GREEN);
        // Barra de Especial (DORADA) - REAÑADIDA
        drawBar(margin, yPos - 35, barW, 20, player1.comboCharge / 100f, Color.GOLD);

        // --- JUGADOR 2 (Kano) ---
        float x2 = viewport.getWorldWidth() - margin - barW;
        layout.setText(nameFont, player2.name);
        nameFont.draw(batch, player2.name, viewport.getWorldWidth() - margin - layout.width, yPos + 75);
        // Barra de Vida
        drawBar(x2, yPos, barW, barH, player2.currentHealth / player2.maxHealth, Color.GREEN);
        // Barra de Especial (DORADA) - REAÑADIDA
        drawBar(x2, yPos - 35, barW, 20, player2.comboCharge / 100f, Color.GOLD);
    }

    private void drawBar(float x, float y, float w, float h, float pct, Color c) {
        batch.setColor(Color.RED);
        batch.draw(whiteTexture, x, y, w, h);
        batch.setColor(c);
        batch.draw(whiteTexture, x, y, w * Math.max(0, pct), h);
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
        // 'false' para que no centre la cámara y usemos la esquina 0,0
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        bigFont.dispose();
        nameFont.dispose();
        background.dispose();
        whiteTexture.dispose();
        player1.dispose();
        player2.dispose();
        stage.dispose();
        if (music != null) music.dispose();
        joystickBg.dispose();
        joystickKnob.dispose();
    }
}
