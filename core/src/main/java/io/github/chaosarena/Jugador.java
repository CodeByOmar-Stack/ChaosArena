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

        Array<TextureRegion> walkFrames = new Array<>();
        // Cargamos solo los frames que existen
        String[] names = {"abel_walk1", "abel_walk3", "abel_walk4"};
        for (String name : names) {
            TextureRegion reg = atlas.findRegion(name);
            if (reg != null) walkFrames.add(reg);
        }
        // Animación normal (sin experimentos de flip aquí)
        animCaminar = new Animation<>(0.1f, walkFrames, Animation.PlayMode.LOOP);

        // Lo mismo para el stop...
        tiempo = 0;

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

        // Guardamos la dirección última
        if (irDerecha) mirandoDerecha = true;
        if (irIzquierda) mirandoDerecha = false;

        TextureRegion frame = estaMoviendose ? animCaminar.getKeyFrame(tiempo) : animEstarQuieto.getKeyFrame(tiempo);

        if (frame != null) {
            float ancho = frame.getRegionWidth();
            float alto = frame.getRegionHeight();

            if (mirandoDerecha) {
                // Dibujo normal
                batch.draw(frame, x, y, ancho, alto);
            } else {
                // Dibujo invertido:
                // Sumamos 'ancho' a la X porque al poner ancho negativo,
                // el dibujo se proyecta hacia la izquierda desde el punto de origen.
                batch.draw(frame, x + ancho, y, -ancho, alto);
            }
        }
    }
    public void dispose() {
        atlas.dispose();
    }
}
