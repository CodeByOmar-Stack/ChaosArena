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
        // Si alguien ha muerto, la IA deja de procesar
        if (npc.currentHealth <= 0 || target.currentHealth <= 0) return;

        // Calculamos la distancia basándonos en la posición X
        // Nota: Si no tienes implementado getCenterX(), usa (target.x - npc.x)
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
            decisionTimer = 0.3f + MathUtils.random(0.4f);

            // Calibración de distancias para el mapa de 1920px
            if (absDist > 300) {
                currentStrategy = "APPROACH";
            } else if (absDist < 150) {
                currentStrategy = "RETREAT";
            } else {
                // En el "punto dulce" de ataque, decide si atacar o esperar
                currentStrategy = MathUtils.randomBoolean(0.8f) ? "ATTACK" : "IDLE";
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
        // Ataca si está a menos de 300px y no está en enfriamiento
        if (absDist < 300 && attackCooldown <= 0 && !npc.isAttacking()) {
            executeProAttack(absDist);
        }
    }

    private void executeProAttack(float distance) {
        Player.AttackType type;

        // Prioridad: Ataque Especial si tiene la barra llena
        if (npc.comboCharge >= npc.MAX_COMBO_CHARGE) {
            type = Player.AttackType.SPECIAL;
        } else {
            // Elige el golpe según la distancia: Patada para lejos, mezcla para cerca
            if (distance > 220) {
                type = Player.AttackType.KICK;
            } else {
                type = MathUtils.randomBoolean() ? Player.AttackType.PUNCH : Player.AttackType.KICK;
            }
        }

        npc.attack(type);

        // Si el golpe conecta, aplicamos el daño
        if (npc.canHit(target)) {
            if (type == Player.AttackType.PUNCH) {
                target.takeDamage(4);
                npc.addCharge(10);
            } else if (type == Player.AttackType.KICK) {
                target.takeDamage(7);
                npc.addCharge(15);
            } else if (type == Player.AttackType.SPECIAL) {
                target.takeDamage(25);
            }
        }

        // Tiempo de espera entre ataques para que no sea una máquina de spam
        attackCooldown = 0.5f + MathUtils.random(0.7f);
    }
}
