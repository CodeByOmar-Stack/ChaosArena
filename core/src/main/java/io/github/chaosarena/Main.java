package io.github.chaosarena;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    private BitmapFont font;

    private Texture background;
    private Texture whiteTexture;

    private Player player1;
    private Player player2;

    private Stage stage;
    private Touchpad joystick;

    private Texture joystickBg;
    private Texture joystickKnob;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // Fuente simple para UI
        font = new BitmapFont();
        font.getData().setScale(2.2f);

        // Textura base (1x1) para dibujar UI (barras)
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        whiteTexture = new Texture(pixmap);
        pixmap.dispose();

        background = new Texture("backgrounds/zigala.png");

        // Crear jugadores
        player1 = new Player("Abel", "sprites/player/player_atlas/game_atlas.atlas", 100, 100, true);
        player2 = new Player("Enemy", "sprites/player/player_atlas/game_atlas.atlas",
            Gdx.graphics.getWidth() - 400, 100, false);

        setupInputUI();
    }

    private void setupInputUI() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Zona de ataque (tap en pantalla)
        Actor attackArea = new Actor();
        attackArea.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        attackArea.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                player1.attack();

                // Daño básico de prueba
                player2.takeDamage(5);
            }
        });

        stage.addActor(attackArea);

        // Crear joystick
        joystickBg = new Texture("joystick/AIR_joystick_bg600.png");
        joystickKnob = new Texture("joystick/AIR_joystick_stick600.png");

        Image bgImage = new Image(new TextureRegionDrawable(joystickBg));
        bgImage.setTouchable(Touchable.disabled);

        Touchpad.TouchpadStyle style = new Touchpad.TouchpadStyle();
        style.knob = new TextureRegionDrawable(joystickKnob);
        style.knob.setMinWidth(150);
        style.knob.setMinHeight(150);

        joystick = new Touchpad(15, style);

        Stack joystickStack = new Stack(bgImage, joystick);

        float size = 350;
        joystickStack.setBounds(150, 150, size, size);

        stage.addActor(joystickStack);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        float delta = Gdx.graphics.getDeltaTime();

        handlePlayerMovement(delta);

        // Dibujado
        batch.begin();

        batch.draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        player1.draw(batch);
        player2.draw(batch);

        drawUI();

        batch.end();

        stage.act(delta);
        stage.draw();
    }

    private void handlePlayerMovement(float delta) {
        float speed = 400;

        float inputX = joystick.getKnobPercentX();

        boolean moveRight = inputX > 0.3f;
        boolean moveLeft = inputX < -0.3f;

        if (!player1.isAttacking()) {
            if (moveRight) player1.move(speed * delta);
            if (moveLeft) player1.move(-speed * delta);
        }

        player1.updateDirection(moveRight, moveLeft);

        limitToScreen(player1);
        limitToScreen(player2);
    }

    private void limitToScreen(Player p) {
        if (p.x < 0) p.x = 0;

        float width = 100 * p.scale;

        if (p.x > Gdx.graphics.getWidth() - width) {
            p.x = Gdx.graphics.getWidth() - width;
        }
    }

    private void drawUI() {
        float margin = 50;
        float width = 400;
        float height = 30;
        float y = Gdx.graphics.getHeight() - 80;

        // Player 1
        font.draw(batch, player1.name, margin, y + 60);
        drawHealthBar(margin, y, width, height, player1);

        // Player 2
        float x2 = Gdx.graphics.getWidth() - margin - width;
        font.draw(batch, player2.name, x2, y + 60);
        drawHealthBar(x2, y, width, height, player2);
    }

    private void drawHealthBar(float x, float y, float w, float h, Player p) {
        // Fondo rojo (vida perdida)
        batch.setColor(Color.RED);
        batch.draw(whiteTexture, x, y, w, h);

        // Vida actual
        batch.setColor(Color.GREEN);
        float currentWidth = (p.currentHealth / p.maxHealth) * w;
        batch.draw(whiteTexture, x, y, currentWidth, h);

        batch.setColor(Color.WHITE);
    }

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        background.dispose();
        whiteTexture.dispose();

        player1.dispose();
        player2.dispose();

        stage.dispose();
        joystickBg.dispose();
        joystickKnob.dispose();
    }
}
