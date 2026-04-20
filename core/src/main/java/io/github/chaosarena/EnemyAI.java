package io.github.chaosarena;

import com.badlogic.gdx.math.MathUtils;

/**
 * Inteligencia Artificial avanzada para el enemigo.
 * Controla al NPC de forma autónoma, buscando distancias y atacando estratégicamente.
 */
public class EnemyAI {
    private final Player npc;
    private final Player target;

    private float decisionTimer  = 0;
    private float attackCooldown = 0;
    private String currentStrategy = "APPROACH"; // APPROACH, RETREAT, IDLE, ATTACK

    // ── Parámetros de dificultad (configurables por nivel desde Main) ──────────
    private float speed      = 350f;
    private float damage     = 5f;
    private float attackRate = 1.8f; // segundos entre ataques

    public EnemyAI(Player npc, Player target) {
        this.npc    = npc;
        this.target = target;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  GETTERS / SETTERS  (llamados desde Main.applyLevelConfig)
    // ══════════════════════════════════════════════════════════════════════════

    public void setSpeed(float speed)           { this.speed = speed; }
    public void setDamage(float damage)         { this.damage = damage; }
    public void setAttackRate(float attackRate) { this.attackRate = attackRate; }

    public float getSpeed()      { return speed; }
    public float getDamage()     { return damage; }
    public float getAttackRate() { return attackRate; }

    // ══════════════════════════════════════════════════════════════════════════
    //  BUCLE PRINCIPAL
    // ══════════════════════════════════════════════════════════════════════════

    public void update(float delta) {
        // Si alguien ha muerto, la IA deja de procesar
        if (npc.currentHealth <= 0 || target.currentHealth <= 0) return;

        float dist    = target.x - npc.x;
        float absDist = Math.abs(dist);

        if (decisionTimer  > 0) decisionTimer  -= delta;
        if (attackCooldown > 0) attackCooldown -= delta;

        // 1. Orientación: siempre mira al jugador si no está atacando
        if (!npc.isAttacking()) {
            npc.facingRight = dist > 0;
        }

        // 2. Selección de estrategia
        if (decisionTimer <= 0) {
            decisionTimer = 0.3f + MathUtils.random(0.4f);

            if (absDist > 300) {
                currentStrategy = "APPROACH";
            } else if (absDist < 150) {
                currentStrategy = "RETREAT";
            } else {
                currentStrategy = MathUtils.randomBoolean(0.8f) ? "ATTACK" : "IDLE";
            }
        }

        // 3. Movimiento — usa la variable speed (píxeles/seg × delta)
        if (!npc.isAttacking()) {
            float step = speed * delta;
            if (currentStrategy.equals("APPROACH")) {
                npc.move(dist > 0 ? step : -step);
            } else if (currentStrategy.equals("RETREAT")) {
                npc.move(dist > 0 ? -step : step);
            }
        }

        // 4. Ataque — el cooldown base ahora viene de attackRate
        if (absDist < 300 && attackCooldown <= 0 && !npc.isAttacking()) {
            executeProAttack(absDist);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  LÓGICA DE ATAQUE
    // ══════════════════════════════════════════════════════════════════════════

    private void executeProAttack(float distance) {
        Player.AttackType type;

        if (npc.comboCharge >= npc.MAX_COMBO_CHARGE) {
            type = Player.AttackType.SPECIAL;
        } else if (distance > 220) {
            type = Player.AttackType.KICK;
        } else {
            type = MathUtils.randomBoolean() ? Player.AttackType.PUNCH : Player.AttackType.KICK;
        }

        npc.attack(type);

        // Daño escalado por nivel: damage es el valor base configurado desde Main
        if (npc.canHit(target)) {
            if (type == Player.AttackType.PUNCH) {
                target.takeDamage(damage);         // antes: 4 fijo
                npc.addCharge(10);
            } else if (type == Player.AttackType.KICK) {
                target.takeDamage(damage * 1.75f); // antes: 7 fijo (~damage × 1.75)
                npc.addCharge(15);
            } else if (type == Player.AttackType.SPECIAL) {
                target.takeDamage(damage * 6.25f); // antes: 25 fijo (~damage × 6.25)
            }
        }

        // Cooldown entre ataques: base = attackRate con variación aleatoria de ±0.25s
        attackCooldown = attackRate + MathUtils.random(-0.25f, 0.25f);
    }
}
