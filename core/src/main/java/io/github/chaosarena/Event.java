package io.github.chaosarena;

public class Event {
    public enum EventType {
        HEAL,
        HARDER_COMBAT,
        GLOBAL_MODIFIER,
        POISON,
        ENEMY_HEAL
    }

    public EventType type;
    public String description;

    public Event(EventType type, String description) {
        this.type = type;
        this.description = description;
    }

    public void apply(RunManager runManager) {
        switch(type) {
            case HEAL:
                if (runManager.getPlayer() != null) {
                    Player p = runManager.getPlayer();
                    p.currentHealth = Math.min(p.maxHealth, p.currentHealth + (p.maxHealth * 0.3f));
                }
                break;
            case HARDER_COMBAT:
                runManager.setHarderCombatNext(true);
                break;
            case GLOBAL_MODIFIER:
                runManager.setGlobalDamageMultiplier(1.5f);
                break;
            case POISON:
                if (runManager.getPlayer() != null) {
                    Player p = runManager.getPlayer();
                    p.currentHealth = Math.max(1, p.currentHealth - (p.maxHealth * 0.2f));
                }
                break;
            case ENEMY_HEAL:
                runManager.setEnemyHealNext(true);
                break;
        }
    }
}
