package io.github.chaosarena;

import com.badlogic.gdx.math.MathUtils;

/**
 * Inteligencia Artificial avanzada para el enemigo.
 * Controla al NPC de forma autónoma, buscando distancias y atacando estratégicamente.
 */
public class EnemyAI {
    private final Player npc;
    private final Player target;

    private float decisionTimer = 0;
    private float attackCooldown = 0;
    private String currentStrategy = "APPROACH"; // APPROACH, RETREAT, IDLE, ATTACK

    public EnemyAI(Player npc, Player target) {
        this.npc = npc;
        this.target = target;
    }

    public void update(float delta) {
        if (npc.currentHealth <= 0 || target.currentHealth <= 0) return;

        // Distancia entre centros para mayor precisión
        float dist = target.getCenterX() - npc.getCenterX();
        float absDist = Math.abs(dist);

        // 1. Orientación: Siempre mira al jugador si no está atacando
        if (!npc.isAttacking()) {
            npc.facingRight = dist > 0;
        }

        if (decisionTimer > 0) decisionTimer -= delta;
        if (attackCooldown > 0) attackCooldown -= delta;

        // 2. Selección de Estrategia
        if (decisionTimer <= 0) {
            decisionTimer = 0.3f + MathUtils.random(0.4f);

            // Ajuste de distancias para evitar que se encimen
            if (absDist > 320) {
                currentStrategy = "APPROACH";
            } else if (absDist < 200) { // Retrocede si está demasiado cerca (antes 120)
                currentStrategy = "RETREAT";
            } else {
                // En el "punto dulce" de ataque, se queda quieto o ataca
                currentStrategy = MathUtils.randomBoolean(0.8f) ? "ATTACK" : "IDLE";
            }
        }

        // 3. Ejecución de Movimiento
        if (!npc.isAttacking()) {
            float speed = 360 * delta;
            if (currentStrategy.equals("APPROACH")) {
                npc.move(dist > 0 ? speed : -speed, target);
            } else if (currentStrategy.equals("RETREAT")) {
                npc.move(dist > 0 ? -speed : speed, target);
            }
        }

        // 4. Lógica de Ataque
        // Activamos el ataque desde más lejos (350px) para que no necesite estar pegado
        if (absDist < 300 && attackCooldown <= 0 && !npc.isAttacking()) {
            executeProAttack(absDist);
        }
    }

    private void executeProAttack(float distance) {
        Player.AttackType type;

        // Prioridad: Especial si está cargado
        if (npc.comboCharge >= npc.MAX_COMBO_CHARGE) {
            type = Player.AttackType.SPECIAL;
        } else {
            // Elige el golpe según la distancia
            if (distance > 260) {
                // Si está lejos, solo llega la patada
                type = Player.AttackType.KICK;
            } else {
                // Si está cerca, puede usar ambos
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
            } else {
                target.takeDamage(20);
            }
        }

        // Cooldown aleatorio entre ataques para que no sea spammer
        attackCooldown = 0.4f + MathUtils.random(0.6f);
    }
}
