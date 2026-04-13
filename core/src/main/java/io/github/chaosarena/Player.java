package io.github.chaosarena;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.Array;

public class Player {

    public enum AttackType {
        PUNCH, KICK, SPECIAL
    }

    public enum State {
        IDLE, WALK, ATTACKING
    }

    private TextureAtlas atlas;

    private Animation<TextureRegion> walkAnim;
    private Animation<TextureRegion> idleAnim;
    private Animation<TextureRegion> punchAnim;
    private Animation<TextureRegion> kickAnim;
    private Animation<TextureRegion> specialAnim;

    private State currentState = State.IDLE;
    private AttackType currentAttackType = AttackType.PUNCH;

    private float stateTime;
    private float attackTime;

    public float x, y;
<<<<<<< HEAD
    public float scale = 4f;
=======
    public float scale = 4f; // Tamaño reducido como pediste
>>>>>>> 5d27d9d8d06851bbc3c32233a94225195e326e56

    public String name;
    public float maxHealth = 100;
    public float currentHealth = 100;

    public float comboCharge = 0;
    public final float MAX_COMBO_CHARGE = 100;

    public boolean facingRight;

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
        String[] walkNames = {"abel_walk1", "abel_walk3", "abel_walk4"};
        for (String n : walkNames) {
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

        // PUNCH
        Array<TextureRegion> punch = new Array<>();
        TextureRegion punchFrame = atlas.findRegion("abel_punch1");
        if (punchFrame != null) punch.add(punchFrame);
        punchAnim = new Animation<>(0.2f, punch, Animation.PlayMode.NORMAL);

        // KICK
        Array<TextureRegion> kick = new Array<>();
        TextureRegion kickFrame = atlas.findRegion("abel_kick1");
        if (kickFrame != null) kick.add(kickFrame);
        kickAnim = new Animation<>(0.25f, kick, Animation.PlayMode.NORMAL);

        // SPECIAL
        Array<TextureRegion> special = new Array<>();
        TextureRegion specialFrame = atlas.findRegion("abel_specialPunch");
        if (specialFrame != null) special.add(specialFrame);
        specialAnim = new Animation<>(0.4f, special, Animation.PlayMode.NORMAL);
    }

<<<<<<< HEAD
    public void move(float amount, Player other) {
        if (currentState != State.ATTACKING) {
            float nextX = x + amount;

            // Lógica para evitar que se encimen
            float myCenterX = nextX + (60 * scale) / 2;
            float otherCenterX = other.getCenterX();
            float minDistance = (getBodyWidth() + other.getBodyWidth()) / 2 + (5 * scale);

            // Solo bloqueamos el movimiento si se están acercando demasiado
            boolean movingTowards = (amount > 0 && myCenterX < otherCenterX) || (amount < 0 && myCenterX > otherCenterX);

            if (!movingTowards || Math.abs(myCenterX - otherCenterX) >= minDistance) {
                x = nextX;
            }
=======
    public void move(float amount) {
        if (currentState != State.ATTACKING) {
            x += amount;
>>>>>>> 5d27d9d8d06851bbc3c32233a94225195e326e56
            currentState = State.WALK;
        }
    }

    public void updateDirection(boolean right, boolean left) {
        if (currentState != State.ATTACKING) {
            if (right) facingRight = true;
            if (left) facingRight = false;

            if (!right && !left && currentState == State.WALK) {
                currentState = State.IDLE;
            }
        }
    }

    public void attack(AttackType type) {
        if (currentState != State.ATTACKING) {
            if (type == AttackType.SPECIAL && comboCharge < MAX_COMBO_CHARGE) return;

            currentState = State.ATTACKING;
            currentAttackType = type;
            attackTime = 0;

            if (type == AttackType.SPECIAL) comboCharge = 0;
        }
    }

    public void takeDamage(float amount) {
        currentHealth -= amount;
        if (currentHealth < 0) currentHealth = 0;
    }

    public void addCharge(float amount) {
        comboCharge += amount;
        if (comboCharge > MAX_COMBO_CHARGE) comboCharge = MAX_COMBO_CHARGE;
    }

    public boolean isAttacking() {
        return currentState == State.ATTACKING;
    }

<<<<<<< HEAD
    public float getCenterX() {
        return x + (60 * scale) / 2;
    }

    public float getBodyWidth() {
        return 40 * scale;
    }

    public boolean canHit(Player other) {
        float bodyWidth = getBodyWidth();
        float centerX = getCenterX();

        float otherBodyWidth = other.getBodyWidth();
        float otherCenterX = other.getCenterX();

        float reach;
        switch (currentAttackType) {
            case KICK: reach = 80 * scale; break;
            case SPECIAL: reach = 120 * scale; break;
            case PUNCH:
            default: reach = 60 * scale; break;
        }

        float attackAreaLeft, attackAreaRight;

        if (facingRight) {
            attackAreaLeft = centerX;
            attackAreaRight = centerX + reach;
        } else {
            attackAreaLeft = centerX - reach;
            attackAreaRight = centerX;
        }

        float enemyLeft = otherCenterX - (otherBodyWidth / 2);
        float enemyRight = otherCenterX + (otherBodyWidth / 2);

        boolean overlapX = (enemyLeft < attackAreaRight) && (enemyRight > attackAreaLeft);
        boolean overlapY = Math.abs(y - other.y) < (60 * scale);
=======
    public State getState() {
        return currentState;
    }

    public boolean canHit(Player other) {
        float bodyWidth = 60 * scale;
        float otherBodyWidth = 60 * other.scale;

        float reach;
        switch (currentAttackType) {
            case KICK: reach = 70 * scale; break;
            case SPECIAL: reach = 100 * scale; break;
            case PUNCH:
            default: reach = 50 * scale; break;
        }

        float attackAreaStart, attackAreaEnd;

        // Si mira a la derecha, el área de ataque nace en el cuerpo y se extiende a la derecha
        if (facingRight) {
            attackAreaStart = x + (bodyWidth * 0.3f);
            attackAreaEnd = x + bodyWidth + reach;
        } else {
            // Si mira a la izquierda, nace en el cuerpo y se extiende a la izquierda
            attackAreaStart = x - reach;
            attackAreaEnd = x + (bodyWidth * 0.7f);
        }

        // Hitbox del enemigo (su cuerpo real)
        float enemyLeft = other.x;
        float enemyRight = other.x + otherBodyWidth;

        // Comprobación de colisión: el área del golpe debe tocar el cuerpo del enemigo
        boolean overlapX = (enemyLeft < attackAreaEnd) && (enemyRight > attackAreaStart);
        boolean overlapY = Math.abs(y - other.y) < (100 * scale);
>>>>>>> 5d27d9d8d06851bbc3c32233a94225195e326e56

        return overlapX && overlapY;
    }

    public void draw(SpriteBatch batch) {
        float delta = Gdx.graphics.getDeltaTime();
        stateTime += delta;

        TextureRegion frame;

        if (currentState == State.ATTACKING) {
            attackTime += delta;
            Animation<TextureRegion> anim;
            switch (currentAttackType) {
                case KICK: anim = kickAnim; break;
                case SPECIAL: anim = specialAnim; break;
                default: anim = punchAnim; break;
            }

            frame = anim.getKeyFrame(attackTime);
            if (anim.isAnimationFinished(attackTime)) {
                currentState = State.IDLE;
            }
        } else if (currentState == State.WALK) {
            frame = walkAnim.getKeyFrame(stateTime);
        } else {
            frame = idleAnim.getKeyFrame(stateTime);
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
            batch.draw(frame, x + width, y, -width, height);
        }
    }

    public void dispose() {
        atlas.dispose();
    }
}
