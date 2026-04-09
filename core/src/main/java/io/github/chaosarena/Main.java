package io.github.chaosarena;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Jugador jugador;
    private Texture fondo;

    @Override
    public void create() {
        batch = new SpriteBatch();
        fondo = new Texture("backgrounds/zigala.png");
        // Ruta corregida según la estructura del proyecto
        jugador = new Jugador("sprites/player/player_atlas/game_atlas.atlas", 100, 100);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        float delta = Gdx.graphics.getDeltaTime();
        boolean irDerecha = false;
        boolean irIzquierda = false;

        // Lógica de movimiento
        if (Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            jugador.x += 250 * delta;
            irDerecha = true;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            jugador.x -= 250 * delta;
            irIzquierda = true;
        }

        // Soporte para touch/ratón
        if (Gdx.input.isTouched()) {
            if (Gdx.input.getX() > Gdx.graphics.getWidth() / 2) {
                jugador.x += 250 * delta;
                irDerecha = true;
            } else {
                jugador.x -= 250 * delta;
                irIzquierda = true;
            }
        }

        boolean estaMoviendose = irDerecha || irIzquierda;

        batch.begin();
        if (fondo != null) {
            batch.draw(fondo, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }

        // Llamamos al método actualizado de Jugador
        jugador.dibujar(batch, estaMoviendose, irDerecha, irIzquierda);
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (fondo != null) fondo.dispose();
        if (jugador != null) jugador.dispose();
    }
}
