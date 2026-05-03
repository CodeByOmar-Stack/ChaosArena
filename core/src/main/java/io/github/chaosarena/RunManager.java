package io.github.chaosarena;

import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.List;

public class RunManager {
    private Player player;
    private int fightsWon;
    private List<Buff> activeBuffs;
    
    private boolean harderCombatNext;
    private boolean enemyHealNext;
    private float globalDamageMultiplier;
    private List<Buff> pendingEnemyBuffs;

    public RunManager(Player player) {
        this.player = player;
        this.fightsWon = 0;
        this.activeBuffs = new ArrayList<>();
        this.pendingEnemyBuffs = new ArrayList<>();
        this.harderCombatNext = false;
        this.enemyHealNext = false;
        this.globalDamageMultiplier = 1.0f;
    }

    public void setPlayer(Player player) {
        this.player = player;
        reapplyBuffs();
    }

    public void onFightWon() {
        fightsWon++;
        harderCombatNext = false;
        enemyHealNext = false;
        globalDamageMultiplier = 1.0f; // Se reinicia el multiplicador global
    }

    public boolean shouldTriggerEvent() {
        return fightsWon > 0 && fightsWon % 3 == 0; 
    }

    public Event generateRandomEvent() {
        int r = MathUtils.random(0, 4);
        switch(r) {
            case 0: return new Event(Event.EventType.HEAL, "Te curas un 30% de tu salud máxima.");
            case 1: return new Event(Event.EventType.HARDER_COMBAT, "¡Combate más difícil!\nEl enemigo será un 50% más fuerte.");
            case 2: return new Event(Event.EventType.GLOBAL_MODIFIER, "Modificador global:\nDaño 1.5x en la próxima pelea.");
            case 3: return new Event(Event.EventType.POISON, "¡Envenenado!\nPierdes un 20% de tu vida actual.");
            case 4: return new Event(Event.EventType.ENEMY_HEAL, "El próximo enemigo vendrá con más vida extra.");
        }
        return new Event(Event.EventType.HEAL, "Te curas un 30% de tu salud máxima.");
    }

    public List<Buff> getRandomBuffChoices() {
        List<Buff> choices = new ArrayList<>();
        for(int i = 0; i < 3; i++) {
            BuffType type = BuffType.values()[MathUtils.random(0, BuffType.values().length - 1)];
            
            // Determinar rareza
            float r = MathUtils.random();
            Buff.Rarity rarity = Buff.Rarity.COMMON;
            float multiplier = 1.0f;
            if (r > 0.9f) { rarity = Buff.Rarity.EPIC; multiplier = 2.5f; }
            else if (r > 0.6f) { rarity = Buff.Rarity.RARE; multiplier = 1.5f; }

            float value = 0;
            switch(type) {
                case DAMAGE: value = 0.15f * multiplier; break; 
                case SPEED: value = 0.10f * multiplier; break; 
                case HEALTH: value = 30f * multiplier; break; 
                case LIFESTEAL: value = 0.03f * multiplier; break; 
                case ARMOR: value = 0.05f * multiplier; break; // 5% a 12.5% reduccion
                case CRITICAL: value = 0.10f * multiplier; break; // 10% a 25% chance
                case REGEN: value = 2f * multiplier; break; // 2 a 5 hp per sec
            }
            choices.add(new Buff(type, value, true, rarity));
        }
        return choices;
    }

    public void applyBuffToPlayer(Buff buff) {
        if (activeBuffs.size() < 6) {
            activeBuffs.add(buff);
            applyBuffDirectly(player, buff);
        }
    }

    public void addPendingEnemyBuff(Buff buff) {
        pendingEnemyBuffs.add(buff);
    }

    private void reapplyBuffs() {
        // Al reiniciar el personaje o curarse por completo, re-aplicar stats extra de buffs
        for (Buff buff : activeBuffs) {
            applyBuffDirectly(player, buff);
        }
    }

    private void applyBuffDirectly(Player p, Buff buff) {
        switch(buff.type) {
            case DAMAGE:
                p.damageMultiplier += buff.value;
                break;
            case SPEED:
                p.speedMultiplier += buff.value;
                break;
            case HEALTH:
                p.maxHealth += buff.value;
                p.currentHealth += buff.value;
                break;
            case LIFESTEAL:
                p.lifestealPercent += buff.value;
                break;
            case ARMOR:
                p.armorPercent += buff.value;
                break;
            case CRITICAL:
                p.criticalChance += buff.value;
                break;
            case REGEN:
                p.regenPerSec += buff.value;
                break;
        }
    }

    public void applyEnemyScaling(EnemyAI enemyAI, Player enemyPlayer) {
        int cycle = fightsWon / 3;
        float baseDmg = enemyAI.getDamage();
        float baseSpd = enemyAI.getSpeed();
        
        if (harderCombatNext) {
            baseDmg *= 1.5f;
            baseSpd *= 1.2f;
            enemyPlayer.maxHealth *= 1.5f;
            enemyPlayer.currentHealth = enemyPlayer.maxHealth;
        }

        if (enemyHealNext) {
            enemyPlayer.maxHealth += 100f;
            enemyPlayer.currentHealth = enemyPlayer.maxHealth;
        }
        
        enemyAI.setDamage(baseDmg * globalDamageMultiplier);
        enemyAI.setSpeed(baseSpd);
        enemyPlayer.damageMultiplier = globalDamageMultiplier;
        
        // Aplica los buffs rechazados por el jugador al enemigo
        for (Buff b : pendingEnemyBuffs) {
            applyEnemyBuff(enemyAI, enemyPlayer, b.type);
        }
        pendingEnemyBuffs.clear(); // Limpiamos para el próximo turno

        // Escalado progresivo de enemigos basado en wins
        int buffsToApply = 0;
        if (fightsWon >= 6) buffsToApply = 2; // Late game
        else if (fightsWon >= 3) buffsToApply = 1; // Mid game
        
        for (int i = 0; i < buffsToApply; i++) {
            BuffType type = BuffType.values()[MathUtils.random(0, BuffType.values().length - 1)];
            applyEnemyBuff(enemyAI, enemyPlayer, type);
        }
    }

    private void applyEnemyBuff(EnemyAI enemyAI, Player enemyPlayer, BuffType type) {
        switch(type) {
            case DAMAGE: enemyPlayer.damageMultiplier += 0.2f; break;
            case SPEED: enemyAI.setSpeed(enemyAI.getSpeed() * 1.15f); break;
            case HEALTH: 
                enemyPlayer.maxHealth += 50; 
                enemyPlayer.currentHealth = enemyPlayer.maxHealth; 
                break;
            case LIFESTEAL: enemyPlayer.lifestealPercent += 0.05f; break;
            case ARMOR: enemyPlayer.armorPercent += 0.10f; break;
            case CRITICAL: enemyPlayer.criticalChance += 0.15f; break;
            case REGEN: enemyPlayer.regenPerSec += 2f; break;
        }
    }

    public Player getPlayer() { return player; }
    public void setHarderCombatNext(boolean harder) { this.harderCombatNext = harder; }
    public void setEnemyHealNext(boolean heal) { this.enemyHealNext = heal; }
    public void setGlobalDamageMultiplier(float mult) { this.globalDamageMultiplier = mult; }
    public float getGlobalDamageMultiplier() { return globalDamageMultiplier; }
    public int getFightsWon() { return fightsWon; }
    public List<Buff> getActiveBuffs() { return activeBuffs; }
}
