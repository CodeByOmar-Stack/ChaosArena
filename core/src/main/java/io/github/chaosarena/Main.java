package io.github.chaosarena;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;

    // 1. DECLARAR: Aquí le decimos al programa que existirá un "Jugador" llamado "jugador"
    private Jugador jugador;

    @Override
    public void create() {
        batch = new SpriteBatch();

        // 2. INSTANCIAR: Creamos el objeto real.
        // Asegúrate de que el archivo "personajes/subzero.png" exista en assets/
        jugador = new Jugador("personajes/subzero.png", 100, 100);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 1);

        float delta = Gdx.graphics.getDeltaTime();

        // Ahora "jugador" ya no da error porque fue creado en create()
        if (Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT) ||
            (Gdx.input.isTouched() && Gdx.input.getX() > Gdx.graphics.getWidth() / 2)) {
            jugador.x += 200 * delta; // Mover a la derecha
        }

        if (Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT) ||
            (Gdx.input.isTouched() && Gdx.input.getX() < Gdx.graphics.getWidth() / 2)) {
            jugador.x -= 200 * delta; // Mover a la izquierda
        }

        batch.begin();
        jugador.dibujar(batch); // Dibujamos al personaje
        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        // 3. LIMPIAR: Liberamos la memoria de la imagen del jugador
        if (jugador != null) jugador.dispose();
    }
}
