package io.github.chaosarena;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class Jugador {
    private TextureAtlas atlas;

    private Animation<TextureRegion> animCaminar;
    private Animation<TextureRegion> animEstarQuieto;

    private float tiempo;
    public float x, y;
    private boolean mirandoDerecha = true;

    public Jugador(String rutaAtlas, float x, float y) {
        this.x = x;
        this.y = y;
        this.atlas = new TextureAtlas(rutaAtlas);

        // Carga manual de frames de caminata (saltando el 2 que no está en el atlas)
        Array<TextureRegion> walkFrames = new Array<>();
        String[] walkNames = {"abel_walk1", "abel_walk3", "abel_walk4"};
        for (String name : walkNames) {
            TextureRegion region = atlas.findRegion(name);
            if (region != null) walkFrames.add(region);
        }
        animCaminar = new Animation<>(0.1f, walkFrames, Animation.PlayMode.LOOP);

        // Carga de frames de estar quieto
        Array<TextureRegion> stopFrames = new Array<>();
        for (int i = 1; i <= 4; i++) {
            TextureRegion region = atlas.findRegion("abel_stop" + i);
            if (region != null) stopFrames.add(region);
        }
        animEstarQuieto = new Animation<>(0.15f, stopFrames, Animation.PlayMode.LOOP);

        tiempo = 0;
    }

    public void dibujar(SpriteBatch batch, boolean estaMoviendose, boolean irDerecha, boolean irIzquierda) {
        tiempo += Gdx.graphics.getDeltaTime();

        // Actualizar dirección
        if (irDerecha) mirandoDerecha = true;
        if (irIzquierda) mirandoDerecha = false;

        TextureRegion frame;
        if (estaMoviendose) {
            frame = animCaminar.getKeyFrame(tiempo);
        } else {
            frame = animEstarQuieto.getKeyFrame(tiempo);
        }

        if (frame != null) {
            float ancho = frame.getRegionWidth();
            float alto = frame.getRegionHeight();

            // Dibujar con flip si mira a la izquierda
            if (!mirandoDerecha && !frame.isFlipX()) {
                frame.flip(true, false);
            } else if (mirandoDerecha && frame.isFlipX()) {
                frame.flip(true, false);
            }

            batch.draw(frame, x, y, ancho, alto);
        }
    }

    public void dispose() {
        atlas.dispose();
    }
}
