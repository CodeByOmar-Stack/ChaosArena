package io.github.chaosarena;

import com.badlogic.gdx.math.MathUtils;

/**
 * Inteligencia Artificial avanzada para el enemigo (FSM).
 * Controla al NPC de forma autónoma, buscando distancias y atacando estratégicamente.
 */
public class EnemyAI {

    public enum State {
        IDLE, CHASE, ATTACK, RETREAT, COOLDOWN
    }

    private final Player npc;
    private final Player target;

    private State currentState = State.IDLE;
    private float stateTimer = 0f;
    private float attackCooldownTimer = 0f;

    // ── Parámetros de dificultad (configurables por nivel desde Main) ──────────
    private float speed      = 350f;
    private float damage     = 5f;
    private float attackRate = 1.8f; // segundos entre ataques

    public EnemyAI(Player npc, Player target) {
        this.npc    = npc;
        this.target = target;
    }

    public void setSpeed(float speed)           { this.speed = speed; }
    public void setDamage(float damage)         { 
        this.damage = damage; 
        this.npc.damageMultiplier = damage / 10f; // Escala el daño base del Player (golpe base es 10)
    }
    public void setAttackRate(float attackRate) { this.attackRate = attackRate; }

    public float getSpeed()      { return speed; }
    public float getDamage()     { return damage; }
    public float getAttackRate() { return attackRate; }

    public void update(float delta) {
        if (npc.currentHealth <= 0 || target.currentHealth <= 0) return;

        float dist    = target.x - npc.x;
        float absDist = Math.abs(dist);

        if (stateTimer > 0) stateTimer -= delta;
        if (attackCooldownTimer > 0) attackCooldownTimer -= delta;

        // Orientación: siempre mira al jugador si no está atacando
        if (!npc.isAttacking() && currentState != State.RETREAT) {
            npc.facingRight = dist > 0;
        } else if (!npc.isAttacking() && currentState == State.RETREAT) {
            // Retrocediendo pero sin dar la espalda en un juego de lucha 2D
            npc.facingRight = dist > 0;
        }

        // Interrupción: si el NPC fue golpeado
        if (npc.isHurt()) {
            currentState = State.IDLE;
            stateTimer = 0.2f;
            return;
        }

        // Nueva mecánica PRO: "Esquiva/bloqueo dinámico"
        // Si el jugador está atacando
        if (target.isAttacking()) {
            boolean isPlayerSpecial = target.getCurrentAttackType() == Player.AttackType.SPECIAL;
            
            if (isPlayerSpecial) {
                // El ataque especial tiene mucho rango, la IA intentará retroceder siempre
                if (currentState != State.RETREAT) {
                    currentState = State.RETREAT;
                    stateTimer = 0.5f;
                }
            } else if (absDist < 250 && currentState != State.RETREAT && attackCooldownTimer > 0) {
                if (MathUtils.randomBoolean(0.7f)) { // 70% de chance de reaccionar como un pro
                    currentState = State.RETREAT;
                    stateTimer = 0.4f;
                }
            }
        }

        // --- MÁQUINA DE ESTADOS FINITOS (FSM) ---
        switch (currentState) {
            case IDLE:
                if (stateTimer <= 0) {
                    decideNextState(absDist);
                }
                break;
                
            case CHASE:
                if (absDist < 250) {
                    if (attackCooldownTimer <= 0) {
                        currentState = State.ATTACK;
                    } else {
                        // Espera "timing humano" antes de decidir otra cosa
                        currentState = State.IDLE;
                        stateTimer = 0.2f + MathUtils.random(0.3f);
                    }
                } else if (stateTimer <= 0) {
                    decideNextState(absDist);
                } else if (!npc.isAttacking()) {
                    npc.move(dist > 0 ? speed * delta : -speed * delta);
                }
                break;

            case RETREAT:
                if (stateTimer <= 0) {
                    currentState = State.IDLE;
                    stateTimer = 0.1f + MathUtils.random(0.2f);
                } else if (!npc.isAttacking()) {
                    // Moverse en dirección opuesta al objetivo
                    npc.move(dist > 0 ? -speed * delta : speed * delta);
                }
                break;

            case ATTACK:
                if (!npc.isAttacking()) {
                    executeProAttack(absDist);
                    currentState = State.COOLDOWN;
                    stateTimer = attackRate + MathUtils.random(-0.2f, 0.4f);
                    attackCooldownTimer = stateTimer;
                }
                break;
                
            case COOLDOWN:
                if (stateTimer <= 0) {
                    currentState = State.IDLE;
                } else if (!npc.isAttacking()) {
                    // "Timing humano": Ocasionalmente retroceder mientras está en cooldown para ser menos predecible
                    if (MathUtils.randomBoolean(0.015f)) { // Probabilidad por frame (~90% chance of triggering at 60fps within a second)
                        currentState = State.RETREAT;
                        stateTimer = MathUtils.random(0.3f, 0.6f);
                    }
                }
                break;
        }
    }

    private void decideNextState(float absDist) {
        if (absDist > 350) {
            currentState = State.CHASE;
            stateTimer = MathUtils.random(0.2f, 0.6f); // Más rápido
        } else if (absDist < 180) {
            // Está muy cerca: 30% de probabilidad de retroceder para hacer spacing
            if (MathUtils.randomBoolean(0.30f)) {
                currentState = State.RETREAT;
                stateTimer = MathUtils.random(0.2f, 0.4f);
            } else if (attackCooldownTimer <= 0) {
                currentState = State.ATTACK;
            } else {
                currentState = State.IDLE;
                stateTimer = 0.1f;
            }
        } else {
            // Distancia media (rango de pateo o aproximación)
            if (attackCooldownTimer <= 0) {
                currentState = MathUtils.randomBoolean(0.8f) ? State.ATTACK : State.CHASE; // Más agresivo
            } else {
                currentState = MathUtils.randomBoolean(0.4f) ? State.RETREAT : State.IDLE;
                stateTimer = MathUtils.random(0.1f, 0.3f);
            }
        }
    }

    private void executeProAttack(float distance) {
        Player.AttackType type;

        if (npc.comboCharge >= npc.MAX_COMBO_CHARGE) {
            type = Player.AttackType.SPECIAL;
        } else if (distance > 200) {
            // A cierta distancia solo usa patadas por el mayor alcance
            type = Player.AttackType.KICK;
        } else {
            // Muy cerca puede combinar patadas o puños
            type = MathUtils.randomBoolean(0.6f) ? Player.AttackType.PUNCH : Player.AttackType.KICK;
        }

        npc.attack(type);
        // El daño ahora se aplica automáticamente en el frame correcto de la animación en Player.java (escalado por damageMultiplier)
    }
}
