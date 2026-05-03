package io.github.chaosarena;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.ScreenUtils;

public class MainMenuScreen extends ScreenAdapter {
    private final Main game;
    private final Stage stage;
    private Table mainMenuTable, newGameTable, continueTable, rankingTable;

    public MainMenuScreen(Main game) {
        this.game = game;
        this.stage = new Stage(game.viewport, game.batch);

        mainMenuTable = new Table(); mainMenuTable.setFillParent(true);
        newGameTable = new Table(); newGameTable.setFillParent(true);
        continueTable = new Table(); continueTable.setFillParent(true);
        rankingTable = new Table(); rankingTable.setFillParent(true);

        stage.addActor(mainMenuTable);
        stage.addActor(newGameTable);
        stage.addActor(continueTable);
        stage.addActor(rankingTable);

        buildMainMenu();
        showMainMenu();
    }

    private void hideAllMenus() {
        mainMenuTable.setVisible(false);
        newGameTable.setVisible(false);
        continueTable.setVisible(false);
        rankingTable.setVisible(false);
    }

    private void buildMainMenu() {
        mainMenuTable.clear();
        mainMenuTable.center().padTop(180); 
        
        TextButton btnNew = new TextButton("MODO HISTORIA", game.redStyle());
        TextButton btnContinue = new TextButton("CONTINUAR", game.fightStyle());
        TextButton btnDuel = new TextButton("MODO DUELO", game.goldStyle());
        TextButton btnArcade = new TextButton("MODO ARCADE", game.fightStyle()); 
        TextButton btnRanking = new TextButton("RANKING ARCADE", game.goldStyle());

        btnNew.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { showNewGameMenu(); } });
        btnContinue.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { showContinueMenu(); } });
        btnDuel.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { game.setScreen(new CharacterSelectScreen(game, -1)); } });
        btnArcade.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { game.setScreen(new CharacterSelectScreen(game, -2)); } });
        btnRanking.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { showRankingMenu(); } });

        Table box = new Table();
        box.setBackground(new TextureRegionDrawable(game.whiteTexture).tint(new Color(0,0,0,0.5f)));
        box.pad(30);

        box.add(btnNew).size(500, 80).padBottom(10).row();
        box.add(btnContinue).size(500, 80).padBottom(10).row();
        box.add(btnDuel).size(500, 80).padBottom(10).row();
        box.add(btnArcade).size(500, 80).padBottom(10).row();
        box.add(btnRanking).size(500, 80);
        
        mainMenuTable.add(box);
    }

    private void showMainMenu() {
        hideAllMenus();
        mainMenuTable.setVisible(true);
    }

    private void showRankingMenu() {
        hideAllMenus();
        rankingTable.clear();
        rankingTable.center();
        
        rankingTable.add(new Label("TOP 5 SUPERVIVIENTES ARCADE", new Label.LabelStyle(game.bigFont, Color.YELLOW))).colspan(3).padBottom(40).row();
        
        for (int i = 0; i < 5; i++) {
            float time = game.prefs.getFloat("arcade_time_" + i, 0f);
            int enemies = game.prefs.getInteger("arcade_enemies_" + i, 0);
            
            if (time == 0f && enemies == 0) continue;
            
            String text = String.format("#%d - %d enemigos - %.1f segundos", (i+1), enemies, time);
            rankingTable.add(new Label(text, new Label.LabelStyle(game.font, Color.WHITE))).padBottom(20).row();
        }
        
        TextButton btnBack = new TextButton("VOLVER", game.dangerStyle());
        btnBack.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { showMainMenu(); } });
        rankingTable.add(btnBack).size(300, 80).padTop(30);
        
        rankingTable.setVisible(true);
    }

    private void showNewGameMenu() {
        hideAllMenus(); newGameTable.clear(); newGameTable.center();
        for (int i = 0; i < Main.MAX_SLOTS; i++) {
            final int slot = i;
            String label = slotExists(i) ? "SOBRESCRIBIR PARTIDA " + (i+1) : "NUEVA PARTIDA " + (i+1);
            TextButton btn = new TextButton(label, game.fightStyle());
            btn.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) {
                game.setScreen(new CharacterSelectScreen(game, slot));
            }});
            newGameTable.add(btn).size(700, 90).padBottom(15).row();
        }
        TextButton btnBack = new TextButton("VOLVER", game.dangerStyle());
        btnBack.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { showMainMenu(); } });
        newGameTable.add(btnBack).size(300, 80).padTop(10); newGameTable.setVisible(true);
    }

    private void showContinueMenu() {
        hideAllMenus(); continueTable.clear(); continueTable.center();
        for (int i = 0; i < Main.MAX_SLOTS; i++) {
            if (!slotExists(i)) continue;
            final int slot = i;
            TextButton btnLoad = new TextButton(slotLabel(i), game.fightStyle());
            btnLoad.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) {
                int lvl = game.prefs.getInteger("slot_" + slot + "_level", 1);
                String p1 = game.prefs.getString("slot_" + slot + "_char", "Shadow Fist");
                game.setScreen(new GameScreen(game, lvl, slot, p1, "Iron Claw", -1));
            } });
            continueTable.add(btnLoad).size(700, 90).padBottom(15).row();
        }
        TextButton btnBack = new TextButton("VOLVER", game.dangerStyle());
        btnBack.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { showMainMenu(); } });
        continueTable.add(btnBack).size(300, 80).padTop(10); continueTable.setVisible(true);
    }

    private boolean slotExists(int s) { return game.prefs.getBoolean("slot_" + s + "_exists", false); }
    private String slotLabel(int s) { return "Ranura " + (s+1) + " | " + game.prefs.getString("slot_"+s+"_char", "") + " | Nivel " + game.prefs.getInteger("slot_"+s+"_level", 1); }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }

    @Override public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();
        game.batch.draw(game.bgTitle, 0, 0, game.viewport.getWorldWidth(), game.viewport.getWorldHeight());

        game.layout.setText(game.bigFont, "CHAOS ARENA");
        game.bigFont.setColor(Color.BLACK);
        game.bigFont.draw(game.batch, "CHAOS ARENA", (game.viewport.getWorldWidth() - game.layout.width) / 2f + 5, game.viewport.getWorldHeight() - 65);
        game.bigFont.setColor(Color.ORANGE);
        game.bigFont.draw(game.batch, "CHAOS ARENA", (game.viewport.getWorldWidth() - game.layout.width) / 2f, game.viewport.getWorldHeight() - 70);
        game.batch.end();

        stage.act(delta);
        stage.draw();
    }

    @Override public void resize(int width, int height) { game.viewport.update(width, height, true); }
    @Override public void dispose() { stage.dispose(); }
}
