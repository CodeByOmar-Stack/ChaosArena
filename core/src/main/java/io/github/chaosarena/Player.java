package io.github.chaosarena;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.Array;

public class Player {

    // Estados del jugador (mucho más limpio que booleans sueltos)
    private enum State {
        IDLE, WALK, ATTACK
    }

    private TextureAtlas atlas;

    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> attackAnim;

    private State currentState = State.IDLE;

    private float stateTime;
    private float attackTime;

    public float x, y;
    public float scale = 8f;

    public String name;
    public float maxHealth = 100;
    public float currentHealth = 100;

    private boolean facingRight;

    public Player(String name, String atlasPath, float x, float y, boolean facingRight) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.facingRight = facingRight;

        atlas = new TextureAtlas(Gdx.files.internal(atlasPath));

        loadAnimations();
    }

    private void loadAnimations() {
        // WALK
        Array<TextureRegion> walk = new Array<>();
        String[] names = {"abel_walk1", "abel_walk3", "abel_walk4"};
        for (String n : names) {
            TextureRegion r = atlas.findRegion(n);
            if (r != null) walk.add(r);
        }
        walkAnim = new Animation<>(0.1f, walk, Animation.PlayMode.LOOP);

        // IDLE
        Array<TextureRegion> idle = new Array<>();
        for (int i = 1; i <= 4; i++) {
            TextureRegion r = atlas.findRegion("abel_stop" + i);
            if (r != null) idle.add(r);
        }
        idleAnim = new Animation<>(0.15f, idle, Animation.PlayMode.LOOP);

        // ATTACK
        Array<TextureRegion> attack = new Array<>();
        TextureRegion punch = atlas.findRegion("abel_punch1");
        if (punch != null) attack.add(punch);

        attackAnim = new Animation<>(0.2f, attack, Animation.PlayMode.NORMAL);
    }

    public void move(float amount) {
        // Movimiento horizontal
        x += amount;

        if (currentState != State.ATTACK) {
            currentState = State.WALK;
        }
    }

    public void updateDirection(boolean right, boolean left) {
        // Solo cambia dirección si no está atacando
        if (currentState != State.ATTACK) {
            if (right) facingRight = true;
            if (left) facingRight = false;

            if (!right && !left) {
                currentState = State.IDLE;
            }
        }
    }

    public void attack() {
        // Evitar cancelar ataques en curso
        if (currentState != State.ATTACK) {
            currentState = State.ATTACK;
            attackTime = 0;
        }
    }

    public void takeDamage(float amount) {
        currentHealth -= amount;
        if (currentHealth < 0) currentHealth = 0;
    }

    public boolean isAttacking() {
        return currentState == State.ATTACK;
    }

    public void draw(SpriteBatch batch) {
        float delta = Gdx.graphics.getDeltaTime();
        stateTime += delta;

        TextureRegion frame;

        switch (currentState) {
            case ATTACK:
                attackTime += delta;
                frame = attackAnim.getKeyFrame(attackTime);

                if (attackAnim.isAnimationFinished(attackTime)) {
                    currentState = State.IDLE;
                }
                break;

            case WALK:
                frame = walkAnim.getKeyFrame(stateTime);
                break;

            default:
                frame = idleAnim.getKeyFrame(stateTime);
                break;
        }

        drawFrame(batch, frame);
    }

    private void drawFrame(SpriteBatch batch, TextureRegion frame) {
        if (frame == null) return;

        float width = frame.getRegionWidth() * scale;
        float height = frame.getRegionHeight() * scale;

        if (facingRight) {
            batch.draw(frame, x, y, width, height);
        } else {
            // Flip horizontal sin modificar textura
            batch.draw(frame, x + width, y, -width, height);
        }
    }

    public void dispose() {
        atlas.dispose();
    }
}
