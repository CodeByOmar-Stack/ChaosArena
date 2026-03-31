package io.github.chaosarena;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
    private SpriteBatch batch;

    // Estados del juego
    enum Estado { MENU, COMBATE, VICTORIA, GAMEOVER }
    Estado estadoActual = Estado.MENU;

    int nivelActual = 1;
    Texture fondo;
    // Aquí irían vuestras clases de Personaje (que debéis crear)
    // Personaje jugador;
    // Personaje enemigo;

    @Override
    public void create() {
        batch = new SpriteBatch();
        cargarNivel(nivelActual);
    }

    public void cargarNivel(int numeroNivel) {
        // Lógica para cambiar de nivel estilo "Nobody's Beat Hammer"
        // Fondo = new Texture("escenario" + numeroNivel + ".png");
        // enemigo = new Personaje("EnemigoNivel" + numeroNivel);
    }

    @Override
    public void render() {
        // 1. Actualizar lógica según el estado
        actualizar();

        // 2. Dibujar
        ScreenUtils.clear(0, 0, 0, 1); // Fondo negro estilo MK
        batch.begin();

        if (estadoActual == Estado.COMBATE) {
            // batch.draw(fondo, 0, 0);
            // jugador.dibujar(batch);
            // enemigo.dibujar(batch);
        }

        batch.end();
    }

    private void actualizar() {
        if (estadoActual == Estado.COMBATE) {
            // Lógica de hostias aquí
            // Si vidaEnemigo <= 0 -> estadoActual = Estado.VICTORIA
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (fondo != null) fondo.dispose();
    }
}
