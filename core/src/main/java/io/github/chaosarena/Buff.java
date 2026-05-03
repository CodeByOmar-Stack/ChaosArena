package io.github.chaosarena;

public class Buff {
    public enum Rarity { COMMON, RARE, EPIC }

    public BuffType type;
    public float value;
    public boolean isPermanent;
    public Rarity rarity;

    public Buff(BuffType type, float value, boolean isPermanent, Rarity rarity) {
        this.type = type;
        this.value = value;
        this.isPermanent = isPermanent;
        this.rarity = rarity;
    }

    public String getDescription() {
        String r = "[" + rarity.name() + "] ";
        switch (type) {
            case DAMAGE: return r + "+Daño";
            case SPEED: return r + "+Velocidad";
            case HEALTH: return r + "+Vida Máx";
            case LIFESTEAL: return r + "+Robo Vida";
            case ARMOR: return r + "+Defensa";
            case CRITICAL: return r + "+Crítico";
            case REGEN: return r + "+Regen. Vida";
            default: return r;
        }
    }
}
