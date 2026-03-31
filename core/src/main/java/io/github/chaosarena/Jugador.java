package io.github.chaosarena;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Jugador {
    public float x, y;
    public int vida = 100;
    private Texture imagen;
    private Rectangle hitbox; // El cuadro invisible para detectar golpes

    public Jugador(String rutaImagen, float xInicial, float yInicial) {
        this.imagen = new Texture(rutaImagen);
        this.x = xInicial;
        this.y = yInicial;
        // Creamos un rectángulo del tamaño de la imagen para las colisiones
        this.hitbox = new Rectangle(x, y, imagen.getWidth(), imagen.getHeight());
    }

    public void dibujar(SpriteBatch batch) {
        batch.draw(imagen, x, y);
    }

    public void actualizarHitbox() {
        hitbox.setPosition(x, y);
    }

    public Rectangle getHitbox() {
        return hitbox;
    }

    public void dispose() {
        imagen.dispose();
    }
}
