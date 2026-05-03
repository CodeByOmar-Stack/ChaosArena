package io.github.chaosarena;

import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;
import java.util.Map;

public class StageDef {
    public String name;
    public Texture bgTexture;
    public float floorVisualOffset;
    public float groundY;
    // Mapa para guardar ajustes de altura específicos por personaje en este nivel
    private Map<String, Float> charYOffsets = new HashMap<>();

    public StageDef(String name, Texture bgTexture, float floorVisualOffset, float groundY) {
        this.name = name;
        this.bgTexture = bgTexture;
        this.floorVisualOffset = floorVisualOffset;
        this.groundY = groundY;
    }

    public StageDef addOffset(String charName, float offset) {
        charYOffsets.put(charName, offset);
        return this;
    }

    public float getOffsetFor(String charName) {
        return charYOffsets.getOrDefault(charName, 0f);
    }
}
