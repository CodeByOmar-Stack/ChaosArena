package io.github.chaosarena;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class Player {

    public enum AttackType {
        PUNCH, KICK, SPECIAL
    }

    public enum State {
        IDLE, WALK, ATTACKING, HURT
    }

    private TextureAtlas atlas;
    private Texture fallbackTexture;

    private Animation<TextureRegion> walkAnim, idleAnim, punchAnim, kickAnim, specialAnim, hurtAnim, jumpAnim;

    private State currentState = State.IDLE;
    private AttackType currentAttackType = AttackType.PUNCH;

    private float stateTime, attackTime, hurtTime;

    public float x, y, velocityY = 0;
    private final float GRAVITY = -2500f;
    private final float JUMP_FORCE = 1150f;
    public float groundY;

    // --- AJUSTES DE ESCALADO Y POSICIÓN ---
    public float scale = 1.0f;
    public float drawOffsetX = 0f;
    public float drawOffsetY = 0f;
    // --------------------------------------

    public String name;
    public String charType;
    public String charPrefix;
    public float maxHealth = 250, currentHealth = 250, comboCharge = 0;
    public final float MAX_COMBO_CHARGE = 100;
    public float damageMultiplier = 1.0f;
    public boolean facingRight;
    public Player opponent;
    public boolean hasHitInCurrentAttack = false;

    public Player(String name, String atlasPath, String charPrefix, float x, float y, boolean facingRight,
            float worldHeight) {
        this.name = name;
        this.charType = name;
        this.charPrefix = charPrefix;
        this.x = x;
        this.y = y;
        this.groundY = y;
        this.facingRight = facingRight;
        setAtlas(atlasPath, charPrefix, worldHeight);
    }

    private void loadAnimations() {
        if (atlas == null || atlas.getRegions().size == 0) {
            createEmptyAnimations();
            return;
        }

        idleAnim = buildAnimation(charPrefix + "_idle", 0.12f, Animation.PlayMode.LOOP);
        walkAnim = buildAnimation(charPrefix + "_run", 0.1f, Animation.PlayMode.LOOP);
        if (walkAnim == null) walkAnim = idleAnim;

        punchAnim = buildAnimation(charPrefix + "_attack1", 0.08f, Animation.PlayMode.NORMAL);
        kickAnim = buildAnimation(charPrefix + "_attack2", 0.1f, Animation.PlayMode.NORMAL);
        if (kickAnim == null) kickAnim = punchAnim;

        specialAnim = buildAnimation(charPrefix + "_attack3", 0.08f, Animation.PlayMode.NORMAL);
        if (specialAnim == null) specialAnim = buildAnimation(charPrefix + "_attack1", 0.08f, Animation.PlayMode.NORMAL);

        hurtAnim = buildAnimation(charPrefix + "_takehit", 0.12f, Animation.PlayMode.NORMAL);
        if (hurtAnim == null) hurtAnim = buildAnimation(charPrefix + "_hurt", 0.12f, Animation.PlayMode.NORMAL);

        jumpAnim = buildAnimation(charPrefix + "_jump", 0.1f, Animation.PlayMode.NORMAL);
        if (jumpAnim == null) jumpAnim = buildAnimation(charPrefix + "_goingup", 0.1f, Animation.PlayMode.NORMAL);
        if (jumpAnim == null) jumpAnim = idleAnim;

        if (idleAnim == null) createEmptyAnimations();
    }

    private void createEmptyAnimations() {
        if (fallbackTexture != null) fallbackTexture.dispose();
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        fallbackTexture = new Texture(pixmap);
        pixmap.dispose();
        TextureRegion tr = new TextureRegion(fallbackTexture);
        Array<TextureRegion> frames = new Array<>();
        frames.add(tr);
        Animation<TextureRegion> anim = new Animation<>(0.1f, frames, Animation.PlayMode.LOOP);
        idleAnim = walkAnim = punchAnim = kickAnim = specialAnim = hurtAnim = jumpAnim = anim;
    }

    private Animation<TextureRegion> buildAnimation(String prefix, float dur, Animation.PlayMode mode) {
        if (atlas == null) return null;
        Array<TextureAtlas.AtlasRegion> regions = new Array<>();
        for (TextureAtlas.AtlasRegion r : atlas.getRegions()) {
            if (r.name.startsWith(prefix)) {
                regions.add(r);
            }
        }
        if (regions.size == 0) return null;

        regions.sort(new java.util.Comparator<TextureAtlas.AtlasRegion>() {
            @Override
            public int compare(TextureAtlas.AtlasRegion o1, TextureAtlas.AtlasRegion o2) {
                return o1.name.compareTo(o2.name);
            }
        });

        Array<TextureRegion> list = new Array<>();
        for (TextureAtlas.AtlasRegion r : regions) {
            list.add(r);
        }
        return new Animation<TextureRegion>(dur, list, mode);
    }

    public void setAtlas(String path, String charPrefix, float worldHeight) {
        this.charPrefix = charPrefix;
        try {
            if (atlas != null) atlas.dispose();
            if (Gdx.files.internal(path).exists()) {
                atlas = new TextureAtlas(Gdx.files.internal(path));
                loadAnimations();
                calibrateScale(worldHeight);
            } else {
                createEmptyAnimations();
            }
        } catch (Exception e) {
            createEmptyAnimations();
        }
    }

    private void calibrateScale(float worldHeight) {
        if (charPrefix.contains("samurai")) {
            this.scale = 13.0f;
            this.drawOffsetX = 0f;
            this.drawOffsetY = -100f;
        } else if (charPrefix.contains("martial3")) {
            this.scale = 11.0f;
            this.drawOffsetX = 0f;
            this.drawOffsetY = -100f;
        } else {
            // martial1 (Shadow Fist)
            this.scale = 10.0f;
            this.drawOffsetX = 0f;
            this.drawOffsetY = -130f;
        }
    }

    public void update(float delta) {
        velocityY += GRAVITY * delta;
        y += velocityY * delta;
        if (y <= groundY) {
            y = groundY;
            velocityY = 0;
        }

        if (currentState == State.HURT) {
            hurtTime += delta;
            if (hurtAnim.isAnimationFinished(hurtTime))
                currentState = State.IDLE;
        }
    }

    public void jump() {
        if (isGrounded() && currentState != State.HURT)
            velocityY = JUMP_FORCE;
    }

    public void move(float amount) {
        if (currentState != State.ATTACKING && currentState != State.HURT) {
            x += amount;
            if (isGrounded()) currentState = State.WALK;
        }
    }

    public void updateDirection(boolean r, boolean l) {
        if (currentState != State.ATTACKING && currentState != State.HURT) {
            if (r) facingRight = true;
            if (l) facingRight = false;
            if (!r && !l && currentState == State.WALK) currentState = State.IDLE;
        }
    }

    public void attack(AttackType type) {
        if (currentState != State.ATTACKING && currentState != State.HURT) {
            if (type == AttackType.SPECIAL && comboCharge < MAX_COMBO_CHARGE) return;

            currentState = State.ATTACKING;
            currentAttackType = type;
            attackTime = 0;
            hasHitInCurrentAttack = false;

            if (type == AttackType.SPECIAL) comboCharge = 0;
        }
    }

    public void takeDamage(float amount, float knockback) {
        currentHealth = Math.max(0, currentHealth - amount);
        currentState = State.HURT;
        hurtTime = 0;
        
        // Determinar empuje visual: Si el oponente me pega, el knockback me aleja de él
        float pushDir = (opponent != null && opponent.x < this.x) ? 1f : -1f;
        x += (knockback * pushDir);
    }

    public void addCharge(float a) {
        comboCharge = Math.min(MAX_COMBO_CHARGE, comboCharge + a);
    }

    public boolean isAttacking() {
        return currentState == State.ATTACKING;
    }

    public boolean isHurt() {
        return currentState == State.HURT;
    }

    public boolean isGrounded() {
        return y <= groundY + 5;
    }

    public Rectangle getHitbox() {
        TextureRegion reg = idleAnim.getKeyFrame(0);
        float hbW = (reg.getRegionWidth() * scale) * 0.35f;
        float hbH = (reg.getRegionHeight() * scale) * 0.80f;
        return new Rectangle(x - hbW / 2f, y + drawOffsetY, hbW, hbH);
    }

    public boolean canHit(Player other) {
        if (currentState != State.ATTACKING) return false;
        Rectangle body = getHitbox();
        float reach = (currentAttackType == AttackType.KICK ? 400 : currentAttackType == AttackType.SPECIAL ? 500 : 300);
        float attackX = facingRight ? body.x + body.width : body.x - reach;
        Rectangle attackArea = new Rectangle(attackX, body.y, reach, body.height);
        return attackArea.overlaps(other.getHitbox());
    }

    public void draw(SpriteBatch batch, float delta) {
        stateTime += delta;
        TextureRegion frame = null;
        boolean blinking = false;

        if (currentState == State.HURT) {
            frame = hurtAnim.getKeyFrame(hurtTime);
            hurtTime += delta; // Increment hurtTime inside draw if delta > 0
            if ((int) (hurtTime * 15) % 2 == 0) {
                batch.setColor(Color.RED);
                blinking = true;
            }
        } else if (currentState == State.ATTACKING) {
            attackTime += delta;
            float totalDuration = punchAnim.getAnimationDuration();
            if (currentAttackType == AttackType.SPECIAL) {
                totalDuration = punchAnim.getAnimationDuration() + kickAnim.getAnimationDuration();
            } else if (currentAttackType == AttackType.KICK) {
                totalDuration = kickAnim.getAnimationDuration();
            }

            if (attackTime >= totalDuration) {
                currentState = State.IDLE;
                hasHitInCurrentAttack = false;
                currentAttackType = null;
            } else {
                if (!hasHitInCurrentAttack && canHit(opponent)) {
                    float hitTime = (currentAttackType == AttackType.SPECIAL)
                            ? 0.15f // Impacto temprano: a los 0.15s de iniciar la animación (casi de inmediato)
                            : totalDuration / 2f;

                    if (attackTime > hitTime) {
                        float baseDmg = (currentAttackType == AttackType.SPECIAL) ? 45f : (currentAttackType == AttackType.KICK) ? 15f : 10f;
                        float knockback = (currentAttackType == AttackType.SPECIAL) ? 120f : (currentAttackType == AttackType.KICK) ? 80f : 45f;
                        float dmg = baseDmg * damageMultiplier;
                        if (currentAttackType != AttackType.SPECIAL) addCharge(dmg);
                        opponent.takeDamage(dmg, knockback);
                        hasHitInCurrentAttack = true;
                    }
                }
            }
            if (currentAttackType == AttackType.SPECIAL) {
                if (attackTime < punchAnim.getAnimationDuration()) frame = punchAnim.getKeyFrame(attackTime, false);
                else frame = kickAnim.getKeyFrame(attackTime - punchAnim.getAnimationDuration(), false);
            } else {
                Animation<TextureRegion> anim = (currentAttackType == AttackType.KICK) ? kickAnim : punchAnim;
                frame = anim.getKeyFrame(attackTime, false);
            }
        } else if (!isGrounded()) frame = jumpAnim.getKeyFrame(stateTime);
        else if (currentState == State.WALK) frame = walkAnim.getKeyFrame(stateTime);
        else frame = idleAnim.getKeyFrame(stateTime);

        if (frame != null) {
            float w = frame.getRegionWidth() * scale;
            float h = frame.getRegionHeight() * scale;
            TextureRegion idleFrame = idleAnim.getKeyFrame(0);
            float pivotHalfW = (idleFrame.getRegionWidth() * scale) / 2f;

            if (facingRight) batch.draw(frame, x - pivotHalfW + drawOffsetX, y + drawOffsetY, w, h);
            else batch.draw(frame, x + pivotHalfW - drawOffsetX, y + drawOffsetY, -w, h);
        }

        if (blinking) batch.setColor(Color.WHITE);
    }

    public void dispose() {
        if (atlas != null) atlas.dispose();
        if (fallbackTexture != null) fallbackTexture.dispose();
    }
}
