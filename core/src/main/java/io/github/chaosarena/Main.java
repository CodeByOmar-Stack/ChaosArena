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
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class Main extends ApplicationAdapter {

    private enum GameState { MENU, PLAYING }
    private GameState currentState = GameState.PLAYING;

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
    private TextButton endGameBtn;

    private static final float WORLD_WIDTH = 1920;
    private static final float WORLD_HEIGHT = 1080;

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT);

        setupFonts();
        setupTextures();
        setupAudio();

        player1 = new Player("Abel", "sprites/player/player_atlas/game_atlas.atlas", 300, 150, true, WORLD_HEIGHT);
        player2 = new Player("Kano", "sprites/player/player_atlas/game_atlas.atlas", WORLD_WIDTH - 900, 150, false, WORLD_HEIGHT);

        player2.maxHealth = 200;
        player2.currentHealth = 200;

        enemyAI = new EnemyAI(player2, player1);

        stage = new Stage(viewport, batch);
        Gdx.input.setInputProcessor(stage);

        setupGameUI();
        startNewGame();
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

        // bg = fondo de flechas, knob = círculo gris
        joystickBg = new Texture("joystick/AIR_joystick_bg600.png");
        joystickKnob = new Texture("joystick/AIR_joystick_stick600.png");
    }

    private void setupAudio() {
        music = Gdx.audio.newMusic(Gdx.files.internal("sounds/Techno_Syndrome.mp3"));
        music.setLooping(true);
        music.setVolume(0.4f);
        music.play();
    }

    private void setupGameUI() {
        // 1. ESTILO DEL JOYSTICK
        Touchpad.TouchpadStyle jsStyle = new Touchpad.TouchpadStyle();
        jsStyle.background = new TextureRegionDrawable(joystickBg);
        jsStyle.knob = new TextureRegionDrawable(joystickKnob);

        // --- TAMAÑO DE LA "BOLITA" INTERNA (STICK) ---
        // Lo hemos subido a 180 para que se vea más grande y manejable
        jsStyle.knob.setMinWidth(180);
        jsStyle.knob.setMinHeight(180);

        // 2. CREACIÓN DEL JOYSTICK
        joystick = new Touchpad(20, jsStyle);

        // 3. CONTENEDOR PARA EL JOYSTICK
        joystickContainer = new Container<Touchpad>(joystick);
        joystickContainer.size(300); // El tamaño total del mando
        joystickContainer.setFillParent(true);
        joystickContainer.bottom().left().padLeft(100).padBottom(100);

        // --- BOTONES DE ATAQUE ---
        attackButtonTable = new Table();
        attackButtonTable.setFillParent(true);
        attackButtonTable.bottom().right().pad(100);

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

        // --- PANTALLA FIN DE JUEGO ---
        endGameTable = new Table();
        endGameTable.setFillParent(true);
        endGameBtn = new TextButton("", btnStyle);
        endGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { resetGame(); }
        });
        endGameTable.add(endGameBtn).size(600, 180).padTop(300);

        stage.addActor(joystickContainer);
        stage.addActor(attackButtonTable);
        stage.addActor(endGameTable);
    }

    private void startNewGame() {
        currentState = GameState.PLAYING;
        joystickContainer.setVisible(true);
        attackButtonTable.setVisible(true);
        resetGame();
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

        if (currentState == GameState.PLAYING && !isGameOver) {
            handlePlayerMovement(delta);
            enemyAI.update(delta);
            checkGameOver();
        }

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        batch.draw(background, 0, 0, WORLD_WIDTH, WORLD_HEIGHT);

        if (currentState == GameState.PLAYING) {
            player1.draw(batch);
            player2.draw(batch);
            drawUI();

            if (isGameOver) {
                layout.setText(bigFont, winMessage);
                bigFont.setColor(winMessage.equals("Game Over") ? Color.RED : Color.YELLOW);
                bigFont.draw(batch, winMessage, (WORLD_WIDTH - layout.width) / 2, (WORLD_HEIGHT + layout.height) / 2);
            }
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
        if (p.x > WORLD_WIDTH - w) p.x = WORLD_WIDTH - w;
    }

    private void drawUI() {
        float margin = 80;
        float barW = 750;
        float barH = 55;
        float yPos = WORLD_HEIGHT - 130;

        nameFont.draw(batch, player1.name, margin, yPos + 75);
        drawProgressiveBar(margin, yPos, barW, barH, player1.currentHealth / player1.maxHealth);
        drawBar(margin, yPos - 35, barW, 20, player1.comboCharge / 100f, Color.GOLD);

        float x2 = WORLD_WIDTH - margin - barW;
        layout.setText(nameFont, player2.name);
        nameFont.draw(batch, player2.name, WORLD_WIDTH - margin - layout.width, yPos + 75);
        drawProgressiveBar(x2, yPos, barW, barH, player2.currentHealth / player2.maxHealth);
        drawBar(x2, yPos - 35, barW, 20, player2.comboCharge / 100f, Color.GOLD);
    }

    private void drawProgressiveBar(float x, float y, float w, float h, float pct) {
        batch.setColor(Color.RED);
        batch.draw(whiteTexture, x, y, w, h);
        batch.setColor(Color.GREEN);
        batch.draw(whiteTexture, x, y, w * pct, h);
        batch.setColor(Color.WHITE);
    }

    private void drawBar(float x, float y, float w, float h, float pct, Color c) {
        batch.setColor(Color.DARK_GRAY);
        batch.draw(whiteTexture, x, y, w, h);
        batch.setColor(c);
        batch.draw(whiteTexture, x, y, w * pct, h);
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
        font.dispose();
        bigFont.dispose();
        nameFont.dispose();
        background.dispose();
        whiteTexture.dispose();
        player1.dispose();
        player2.dispose();
        stage.dispose();
        music.dispose();
        if (joystickBg != null) joystickBg.dispose();
        if (joystickKnob != null) joystickKnob.dispose();
    }
}
