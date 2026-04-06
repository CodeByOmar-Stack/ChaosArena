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
    private Animation<TextureRegion> animGolpe;

    private float tiempo;
    private float tiempoGolpe = 0;
    public float x, y;
    private boolean mirandoDerecha = true;
    private boolean estaGolpeando = false;

    // Campos de estado
    public String nombre;
    public float vidaMax = 100;
    public float vidaActual = 100;
    public float escala = 8.0f; // Aumentado considerablemente para que se vean bien

    public Jugador(String nombre, String rutaAtlas, float x, float y, boolean mirandoDerecha) {
        this.nombre = nombre;
        this.x = x;
        this.y = y;
        this.mirandoDerecha = mirandoDerecha;
        this.atlas = new TextureAtlas(Gdx.files.internal(rutaAtlas));

        // Caminar
        Array<TextureRegion> walkFrames = new Array<>();
        String[] walkNames = {"abel_walk1", "abel_walk3", "abel_walk4"};
        for (String name : walkNames) {
            TextureRegion reg = atlas.findRegion(name);
            if (reg != null) walkFrames.add(reg);
        }
        animCaminar = new Animation<>(0.1f, walkFrames, Animation.PlayMode.LOOP);

        // Quieto
        Array<TextureRegion> stopFrames = new Array<>();
        for (int i = 1; i <= 4; i++) {
            TextureRegion region = atlas.findRegion("abel_stop" + i);
            if (region != null) stopFrames.add(region);
        }
        animEstarQuieto = new Animation<>(0.15f, stopFrames, Animation.PlayMode.LOOP);

        // Golpe
        Array<TextureRegion> punchFrames = new Array<>();
        TextureRegion punch = atlas.findRegion("abel_punch1");
        if (punch != null) punchFrames.add(punch);
        animGolpe = new Animation<>(0.2f, punchFrames, Animation.PlayMode.NORMAL);

        tiempo = 0;
    }

    public void golpear() {
        if (!estaGolpeando) {
            estaGolpeando = true;
            tiempoGolpe = 0;
        }
    }

    public void dibujar(SpriteBatch batch, boolean estaMoviendose, boolean irDerecha, boolean irIzquierda) {
        float delta = Gdx.graphics.getDeltaTime();
        tiempo += delta;

        if (estaGolpeando) {
            tiempoGolpe += delta;
            if (animGolpe.isAnimationFinished(tiempoGolpe)) {
                estaGolpeando = false;
            }
        }

        if (!estaGolpeando) {
            if (irDerecha) mirandoDerecha = true;
            if (irIzquierda) mirandoDerecha = false;
        }

        TextureRegion frame;
        if (estaGolpeando) {
            frame = animGolpe.getKeyFrame(tiempoGolpe);
        } else if (estaMoviendose) {
            frame = animCaminar.getKeyFrame(tiempo);
        } else {
            frame = animEstarQuieto.getKeyFrame(tiempo);
        }

        if (frame != null) {
            float ancho = frame.getRegionWidth() * escala;
            float alto = frame.getRegionHeight() * escala;

            if (mirandoDerecha) {
                batch.draw(frame, x, y, ancho, alto);
            } else {
                batch.draw(frame, x + ancho, y, -ancho, alto);
            }
        }
    }

    public boolean estaGolpeando() {
        return estaGolpeando;
    }

    public void dispose() {
        atlas.dispose();
    }
}
