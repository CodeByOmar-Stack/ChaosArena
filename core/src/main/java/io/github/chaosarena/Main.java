package io.github.chaosarena;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
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
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Main extends ApplicationAdapter {

    private enum GameState { MENU, PLAYING }
    // Cambiado a PLAYING por defecto para saltar el menú temporalmente
    private GameState currentState = GameState.PLAYING;

    private SpriteBatch batch;
    private BitmapFont font, bigFont;
    private Texture background, whiteTexture;
    private Player player1, player2;
    private EnemyAI enemyAI;
    private Stage stage;
    private Touchpad joystick;
    private Texture joystickBg, joystickKnob;

    private boolean isGameOver = false;
    private String winMessage = "";
    private final GlyphLayout layout = new GlyphLayout();

    // Comentado para uso futuro
    // private Table menuTable;
    private Table attackButtonTable, endGameTable;
    private Container<Stack> joystickContainer;
    private TextButton endGameBtn;
    private Preferences prefs;

    @Override
    public void create() {
        batch = new SpriteBatch();
        prefs = Gdx.app.getPreferences("ChaosArenaPrefs");

        setupFonts();
        setupTextures();

        player1 = new Player("Abel", "sprites/player/player_atlas/game_atlas.atlas", 150, 100, true);
        player2 = new Player("CPU", "sprites/player/player_atlas/game_atlas.atlas", Gdx.graphics.getWidth() - 400, 100, false);

        // Configuramos al enemigo con vida alta por defecto
        player2.maxHealth = 150;
        player2.currentHealth = 150;

        enemyAI = new EnemyAI(player2, player1);

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // setupMenuUI(); // Comentado de momento
        setupGameUI();

        // Iniciamos el juego directamente
        startNewGame();
    }

    private void setupFonts() {
        font = new BitmapFont();
        font.getData().setScale(2.2f);
        bigFont = new BitmapFont();
        bigFont.getData().setScale(5f);
    }

    private void setupTextures() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE); pixmap.fill();
        whiteTexture = new Texture(pixmap); pixmap.dispose();
        background = new Texture("backgrounds/zigala.png");
        joystickBg = new Texture("joystick/AIR_joystick_bg600.png");
        joystickKnob = new Texture("joystick/AIR_joystick_stick600.png");
    }

    /*
    // Comentado para no usar de momento
    private void setupMenuUI() {
        menuTable = new Table();
        menuTable.setFillParent(true);
        // ... (resto del código del menú)
    }
    */

    private void setupGameUI() {
        // Joystick Layer
        Touchpad.TouchpadStyle jsStyle = new Touchpad.TouchpadStyle();
        jsStyle.knob = new TextureRegionDrawable(joystickKnob);
        jsStyle.knob.setMinWidth(150); jsStyle.knob.setMinHeight(150);
        joystick = new Touchpad(15, jsStyle);
        Stack jsStack = new Stack(new Image(new TextureRegionDrawable(joystickBg)), joystick);
        joystickContainer = new Container<>(jsStack).size(350).bottom().left().pad(50);
        joystickContainer.setFillParent(true);

        // Buttons Layer
        attackButtonTable = new Table();
        attackButtonTable.setFillParent(true);
        attackButtonTable.bottom().right().pad(50);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.up = new TextureRegionDrawable(whiteTexture).tint(new Color(0.2f, 0.2f, 0.2f, 0.8f));
        btnStyle.down = new TextureRegionDrawable(whiteTexture).tint(Color.GRAY);

        TextButton punch = new TextButton("PUNCH", btnStyle);
        TextButton kick = new TextButton("KICK", btnStyle);
        TextButton special = new TextButton("SPECIAL", btnStyle);

        punch.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { if (!isGameOver) { player1.attack(Player.AttackType.PUNCH); if (player1.canHit(player2)) { player2.takeDamage(4); player1.addCharge(10); } } }
        });
        kick.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { if (!isGameOver) { player1.attack(Player.AttackType.KICK); if (player1.canHit(player2)) { player2.takeDamage(7); player1.addCharge(15); } } }
        });
        special.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { if (!isGameOver && player1.comboCharge >= player1.MAX_COMBO_CHARGE) { player1.attack(Player.AttackType.SPECIAL); if (player1.canHit(player2)) player2.takeDamage(25); } }
        });

        attackButtonTable.add(special).size(220, 100).padBottom(15).row();
        attackButtonTable.add(kick).size(220, 100).padBottom(15).row();
        attackButtonTable.add(punch).size(220, 100);

        // End Game Layer
        endGameTable = new Table();
        endGameTable.setFillParent(true);
        endGameBtn = new TextButton("", btnStyle);
        endGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { resetGame(); }
        });
        endGameTable.add(endGameBtn).size(400, 120).padTop(200);

        stage.addActor(joystickContainer);
        stage.addActor(attackButtonTable);
        stage.addActor(endGameTable);
    }

    /*
    private void showMenu() {
        currentState = GameState.MENU;
        // menuTable.setVisible(true);
        joystickContainer.setVisible(false);
        attackButtonTable.setVisible(false);
        endGameTable.setVisible(false);
    }
    */

    private void startNewGame() {
        currentState = GameState.PLAYING;
        // menuTable.setVisible(false);
        joystickContainer.setVisible(true);
        attackButtonTable.setVisible(true);
        resetGame();
    }

    private void resetGame() {
        player1.currentHealth = player1.maxHealth; player1.comboCharge = 0; player1.x = 150;
        player2.currentHealth = player2.maxHealth; player2.comboCharge = 0; player2.x = Gdx.graphics.getWidth() - 400;
        isGameOver = false; winMessage = ""; endGameTable.setVisible(false);
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

        batch.begin();
        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        if (currentState == GameState.PLAYING) {
            player1.draw(batch); player2.draw(batch);
            drawUI();
            if (isGameOver) {
                layout.setText(bigFont, winMessage);
                bigFont.setColor(winMessage.equals("Game Over") ? Color.RED : Color.YELLOW);
                bigFont.draw(batch, winMessage, (Gdx.graphics.getWidth()-layout.width)/2, (Gdx.graphics.getHeight()+layout.height)/2);
            }
        }
        batch.end();

        stage.act(delta); stage.draw();
    }

    private void checkGameOver() {
        if (player1.currentHealth <= 0) { isGameOver = true; winMessage = "Game Over"; endGameBtn.setText("Revancha"); endGameTable.setVisible(true); }
        else if (player2.currentHealth <= 0) { isGameOver = true; winMessage = "Has Ganado!!"; endGameBtn.setText("Siguiente"); endGameTable.setVisible(true); }
    }

    private void handlePlayerMovement(float delta) {
        float inputX = joystick.getKnobPercentX();
        if (!player1.isAttacking()) {
            if (inputX > 0.3f) player1.move(400 * delta, player2);
            if (inputX < -0.3f) player1.move(-400 * delta, player2);
        }
        player1.updateDirection(inputX > 0.3f, inputX < -0.3f);
        limit(player1); limit(player2);
    }

    private void limit(Player p) {
        float w = 60 * p.scale;
        if (p.x < 0) p.x = 0;
        if (p.x > Gdx.graphics.getWidth() - w) p.x = Gdx.graphics.getWidth() - w;
    }

    private void drawUI() {
        float m = 50, w = 400, h = 30, y = Gdx.graphics.getHeight() - 80;
        drawBar(m, y, w, h, player1.currentHealth/player1.maxHealth, Color.GREEN);
        drawBar(m, y-40, w, h/2, player1.comboCharge/100f, Color.CYAN);
        float x2 = Gdx.graphics.getWidth() - m - w;
        drawBar(x2, y, w, h, player2.currentHealth/player2.maxHealth, Color.GREEN);
        drawBar(x2, y-40, w, h/2, player2.comboCharge/100f, Color.CYAN);
    }

    private void drawBar(float x, float y, float w, float h, float pct, Color c) {
        batch.setColor(Color.DARK_GRAY); batch.draw(whiteTexture, x, y, w, h);
        batch.setColor(c); batch.draw(whiteTexture, x, y, w * pct, h);
        batch.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        batch.dispose(); font.dispose(); bigFont.dispose(); background.dispose();
        whiteTexture.dispose(); player1.dispose(); player2.dispose(); stage.dispose();
        if (joystickBg != null) joystickBg.dispose();
        if (joystickKnob != null) joystickKnob.dispose();
    }
}
