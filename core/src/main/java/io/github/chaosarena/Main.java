package io.github.chaosarena;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad.TouchpadStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Jugador jugador1;
    private Jugador jugador2;
    private Texture fondo;
    private Texture barraTextura;
    private BitmapFont fuente;

    private Stage stage;
    private Touchpad joystick;
    private Texture texturaFondoJoystick;
    private Texture texturaBotonJoystick;

    @Override
    public void create() {
        batch = new SpriteBatch();
        fuente = new BitmapFont(); // Fuente básica para los nombres
        fuente.getData().setScale(2.5f);

        // Creamos una textura blanca de 1x1 para dibujar las barras de vida
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        barraTextura = new Texture(pixmap);
        pixmap.dispose();

        fondo = new Texture("backgrounds/zigala.png");

        // Inicializar jugadores (con la nueva escala interna de Jugador.java de 8.0f)
        jugador1 = new Jugador("Abel", "sprites/player/player_atlas/game_atlas.atlas", 100, 100, true);
        jugador2 = new Jugador("Enemigo", "sprites/player/player_atlas/game_atlas.atlas", Gdx.graphics.getWidth() - 400, 100, false);

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        // Área de golpe (Tap)
        Actor areaGolpe = new Actor();
        areaGolpe.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        areaGolpe.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                jugador1.golpear();
                // Simulación: al golpear el jugador 1, el 2 pierde un poco de vida
                jugador2.vidaActual -= 5;
                if (jugador2.vidaActual < 0) jugador2.vidaActual = 0;
            }
        });
        stage.addActor(areaGolpe);

        // CONFIGURACIÓN DEL JOYSTICK (Más grande y desplazado)
        texturaFondoJoystick = new Texture("joystick/AIR_joystick_bg600.png");
        texturaBotonJoystick = new Texture("joystick/AIR_joystick_stick600.png");

        Image fondoJoystickImagen = new Image(new TextureRegionDrawable(texturaFondoJoystick));
        fondoJoystickImagen.setTouchable(Touchable.disabled);

        TouchpadStyle estilo = new TouchpadStyle();
        estilo.knob = new TextureRegionDrawable(texturaBotonJoystick);
        estilo.knob.setMinHeight(150);
        estilo.knob.setMinWidth(150);

        joystick = new Touchpad(15, estilo);

        Stack stackJoystick = new Stack();
        stackJoystick.add(fondoJoystickImagen);
        stackJoystick.add(joystick);

        // Joystick más grande (350) y movido a la derecha (150) y arriba (150)
        float tamañoJoystick = 350;
        stackJoystick.setBounds(150, 150, tamañoJoystick, tamañoJoystick);

        stage.addActor(stackJoystick);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);
        float delta = Gdx.graphics.getDeltaTime();

        boolean irDerecha = false;
        boolean irIzquierda = false;
        float velocidad = 400;

        // Movimiento Jugador 1
        float porcentajeX = joystick.getKnobPercentX();
        if (!jugador1.estaGolpeando()) {
            if (porcentajeX > 0.3f) {
                jugador1.x += velocidad * delta;
                irDerecha = true;
            } else if (porcentajeX < -0.3f) {
                jugador1.x -= velocidad * delta;
                irIzquierda = true;
            }
        }

        // Límites de pantalla para ambos
        limitarPosicion(jugador1);
        limitarPosicion(jugador2);

        batch.begin();
        if (fondo != null) {
            batch.draw(fondo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        // Dibujar personajes
        jugador1.dibujar(batch, irDerecha || irIzquierda, irDerecha, irIzquierda);
        jugador2.dibujar(batch, false, false, false); // El segundo está quieto por ahora

        // DIBUJAR INTERFAZ (Barras de vida)
        dibujarUI();

        batch.end();

        stage.act(delta);
        stage.draw();
    }

    private void limitarPosicion(Jugador j) {
        if (j.x < 0) j.x = 0;
        float anchoReal = 100 * j.escala; // Estimación del ancho del sprite escalado
        if (j.x > Gdx.graphics.getWidth() - anchoReal) {
            j.x = Gdx.graphics.getWidth() - anchoReal;
        }
    }

    private void dibujarUI() {
        float margen = 50;
        float anchoBarra = 400;
        float altoBarra = 30;
        float yBarras = Gdx.graphics.getHeight() - 80;

        // --- Jugador 1 (Izquierda) ---
        fuente.draw(batch, jugador1.nombre, margen, yBarras + 60);
        // Fondo Rojo (Vida perdida)
        batch.setColor(Color.RED);
        batch.draw(barraTextura, margen, yBarras, anchoBarra, altoBarra);
        // Frente Verde (Vida actual)
        batch.setColor(Color.GREEN);
        float anchoVida1 = (jugador1.vidaActual / jugador1.vidaMax) * anchoBarra;
        batch.draw(barraTextura, margen, yBarras, anchoVida1, altoBarra);

        // --- Jugador 2 (Derecha) ---
        float xBarra2 = Gdx.graphics.getWidth() - margen - anchoBarra;
        fuente.draw(batch, jugador2.nombre, xBarra2, yBarras + 60);
        // Fondo Rojo
        batch.setColor(Color.RED);
        batch.draw(barraTextura, xBarra2, yBarras, anchoBarra, altoBarra);
        // Frente Verde
        batch.setColor(Color.GREEN);
        float anchoVida2 = (jugador2.vidaActual / jugador2.vidaMax) * anchoBarra;
        batch.draw(barraTextura, xBarra2, yBarras, anchoVida2, altoBarra);

        batch.setColor(Color.WHITE); // Resetear color del batch
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (fondo != null) fondo.dispose();
        jugador1.dispose();
        jugador2.dispose();
        stage.dispose();
        texturaFondoJoystick.dispose();
        texturaBotonJoystick.dispose();
        barraTextura.dispose();
        fuente.dispose();
    }
}
