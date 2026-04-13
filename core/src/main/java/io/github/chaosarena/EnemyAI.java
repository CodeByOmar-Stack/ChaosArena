package io.github.chaosarena;

import com.badlogic.gdx.math.MathUtils;

/**
 * Inteligencia Artificial avanzada para el enemigo.
 * Controla al NPC de forma autónoma, buscando distancias y atacando estratégicamente.
 */
public class EnemyAI {
    private Player npc;
    private Player target;

    private float decisionTimer = 0;
    private float attackCooldown = 0;
    private String currentStrategy = "APPROACH"; // APPROACH, RETREAT, IDLE, ATTACK

    public EnemyAI(Player npc, Player target) {
        this.npc = npc;
        this.target = target;
    }

    public void update(float delta) {
        if (npc.currentHealth <= 0 || target.currentHealth <= 0) return;

        float dist = target.x - npc.x;
        float absDist = Math.abs(dist);

        if (decisionTimer > 0) decisionTimer -= delta;
        if (attackCooldown > 0) attackCooldown -= delta;

        // 1. Orientación: Siempre mira al jugador si no está atacando
        if (!npc.isAttacking()) {
            npc.facingRight = dist > 0;
        }

        // 2. Selección de Estrategia
        if (decisionTimer <= 0) {
            decisionTimer = 0.4f + MathUtils.random(0.3f);
            if (absDist > 180) {
                currentStrategy = "APPROACH";
            } else if (absDist < 80) {
                currentStrategy = "RETREAT";
            } else {
                // En rango de ataque, decide si atacar o esperar
                currentStrategy = MathUtils.randomBoolean(0.7f) ? "ATTACK" : "IDLE";
            }
        }

        // 3. Ejecución de Movimiento
        if (!npc.isAttacking()) {
            float speed = 360 * delta;
            if (currentStrategy.equals("APPROACH")) {
                npc.move(dist > 0 ? speed : -speed);
            } else if (currentStrategy.equals("RETREAT")) {
                npc.move(dist > 0 ? -speed : speed);
            }
        }

        // 4. Lógica de Ataque
        if (absDist < 165 && attackCooldown <= 0 && !npc.isAttacking()) {
            executeProAttack(absDist);
        }
    }

    private void executeProAttack(float distance) {
        Player.AttackType type;

        // Prioridad: Especial si está cargado
        if (npc.comboCharge >= npc.MAX_COMBO_CHARGE) {
            type = Player.AttackType.SPECIAL;
        } else {
            // Elige patada si está a media distancia, puñetazo si está cerca
            if (distance > 110) {
                type = Player.AttackType.KICK;
            } else {
                type = MathUtils.randomBoolean() ? Player.AttackType.PUNCH : Player.AttackType.KICK;
            }
        }

        npc.attack(type);

        // Si conecta, aplica daño y carga combo
        if (npc.canHit(target)) {
            if (type == Player.AttackType.PUNCH) {
                target.takeDamage(4);
                npc.addCharge(10);
            } else if (type == Player.AttackType.KICK) {
                target.takeDamage(7);
                npc.addCharge(15);
            } else if (type == Player.AttackType.SPECIAL) {
                target.takeDamage(20);
                // El método attack(SPECIAL) en Player ya resetea el comboCharge
            }
        }

        // Cooldown aleatorio entre ataques
        attackCooldown = 0.6f + MathUtils.random(0.8f);
    }
}
