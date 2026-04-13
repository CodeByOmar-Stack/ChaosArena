package io.github.chaosarena;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * Clase para gestionar el menú principal (Extraído para uso futuro)
 */
public class MenuPrincipal {
    /*
    private void setupMenuUI() {
        menuTable = new Table();
        menuTable.setFillParent(true);

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle();
        style.font = font;
        style.up = new TextureRegionDrawable(whiteTexture).tint(new Color(0.1f, 0.1f, 0.1f, 0.8f));
        style.down = new TextureRegionDrawable(whiteTexture).tint(Color.GRAY);

        Label title = new Label("CHAOS ARENA", new Label.LabelStyle(bigFont, Color.GOLD));
        TextButton newGameBtn = new TextButton("NUEVA PARTIDA", style);
        TextButton continueBtn = new TextButton("CONTINUAR PARTIDA", style);
        TextButton duelsBtn = new TextButton("DUELOS", style);

        newGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { startNewGame(); }
        });
        continueBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { if (prefs.contains("p1_hp")) loadSavedGame(); else startNewGame(); }
        });
        duelsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) { startDuel(); }
        });

        menuTable.add(title).padBottom(80).row();
        menuTable.add(newGameBtn).size(500, 100).padBottom(20).row();
        menuTable.add(continueBtn).size(500, 100).padBottom(20).row();
        menuTable.add(duelsBtn).size(500, 100);
        stage.addActor(menuTable);
    }
    */
}
