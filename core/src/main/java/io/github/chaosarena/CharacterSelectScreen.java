package io.github.chaosarena;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;

public class CharacterSelectScreen extends ScreenAdapter {
    private final Main game;
    private final Stage stage;
    private Table rootTable;
    private Label lblP1sel, lblP2sel, lblStageSel;
    private int duelP1 = -1;
    private int duelP2 = -1;
    private int selectedStage = 0;
    private int targetSlot; // -1 para Duelo, 0-2 para Historia

    public CharacterSelectScreen(Main game, int slot) {
        this.game = game;
        this.targetSlot = slot;
        this.stage = new Stage(game.viewport, game.batch);

        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        showUI();
    }

    private void showUI() {
        rootTable.clear();
        rootTable.center();

        String title = (targetSlot == -1) ? "MODO DUELO" : (targetSlot == -2 ? "MODO ARCADE" : "MODO HISTORIA");
        rootTable.add(new Label(title, new Label.LabelStyle(game.bigFont, Color.YELLOW))).colspan(3).padBottom(30).row();

        lblP1sel = new Label("TU HEROE: ?", new Label.LabelStyle(game.font, Color.ORANGE));
        lblP2sel = new Label(targetSlot == -1 || targetSlot == -2 ? "RIVAL: ?" : "IA: Dark Fighter", new Label.LabelStyle(game.font, Color.CYAN));

        rootTable.add(lblP1sel).padRight(50);
        rootTable.add(new Label("VS", new Label.LabelStyle(game.bigFont, Color.WHITE)));
        rootTable.add(lblP2sel).padLeft(50).row();

        // Cuadrícula de personajes
        Table charGrid = new Table();
        for (int i = 0; i < Main.CHAR_NAMES.length; i++) {
            final int idx = i;
            TextButton btn = new TextButton(Main.CHAR_NAMES[i], game.fightStyle());
            btn.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { onCharSelected(idx); } });
            charGrid.add(btn).size(350, 100).pad(10);
            if ((i+1) % 2 == 0) charGrid.row();
        }
        rootTable.add(charGrid).colspan(3).padTop(20).row();

        // Selección de Escenario
        rootTable.add(new Label("ESCENARIO:", new Label.LabelStyle(game.font, Color.WHITE))).colspan(3).padTop(30).row();
        lblStageSel = new Label(game.stages[selectedStage].name, new Label.LabelStyle(game.font, Color.GOLD));

        Table stageControls = new Table();
        TextButton btnPrev = new TextButton("<", game.fightStyle());
        TextButton btnNext = new TextButton(">", game.fightStyle());

        btnPrev.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { changeStage(-1); } });
        btnNext.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { changeStage(1); } });

        stageControls.add(btnPrev).size(100, 100);
        stageControls.add(lblStageSel).padLeft(30).padRight(30);
        stageControls.add(btnNext).size(100, 100);

        rootTable.add(stageControls).colspan(3).padTop(10).row();

        // Botones de acción
        TextButton btnFight = new TextButton("¡A LUCHAR!", game.redStyle());
        btnFight.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                if (targetSlot >= 0 && duelP1 >= 0) {
                    saveSlotFull(targetSlot, Main.CHAR_NAMES[duelP1], 0, 1);
                    game.setScreen(new GameScreen(game, 1, targetSlot, Main.CHAR_NAMES[duelP1], "Dark Fighter", selectedStage));
                } else if (targetSlot == -2 && duelP1 >= 0) {
                    game.setScreen(new GameScreen(game, 1, -2, Main.CHAR_NAMES[duelP1], "Random", selectedStage));
                } else if(targetSlot == -1 && duelP1 >= 0 && duelP2 >= 0) {
                    game.setScreen(new GameScreen(game, 1, -1, Main.CHAR_NAMES[duelP1], Main.CHAR_NAMES[duelP2], selectedStage));
                }
            }
        });

        TextButton btnBack = new TextButton("VOLVER", game.dangerStyle());
        btnBack.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { game.setScreen(new MainMenuScreen(game)); }
        });

        rootTable.add(btnFight).size(550, 130).colspan(3).padTop(40).row();
        rootTable.add(btnBack).size(350, 90).colspan(3).padTop(20);
    }

    private void changeStage(int delta) {
        selectedStage = (selectedStage + delta + game.stages.length) % game.stages.length;
        lblStageSel.setText(game.stages[selectedStage].name);
    }

    private void onCharSelected(int idx) {
        if (targetSlot != -1) {
            duelP1 = idx;
            lblP1sel.setText("ERES: " + Main.CHAR_NAMES[idx]);
        } else {
            if (duelP1 == -1) { duelP1 = idx; lblP1sel.setText("J1: " + Main.CHAR_NAMES[idx]); }
            else if (duelP2 == -1) { duelP2 = idx; lblP2sel.setText("J2: " + Main.CHAR_NAMES[idx]); }
            else { duelP1 = idx; duelP2 = -1; lblP1sel.setText("J1: " + Main.CHAR_NAMES[idx]); lblP2sel.setText("J2: ?"); }
        }
    }

    private void saveSlotFull(int s, String n, int w, int l) {
        game.prefs.putBoolean("slot_"+s+"_exists", true);
        game.prefs.putString("slot_"+s+"_char", n);
        game.prefs.putInteger("slot_"+s+"_level", l);
        game.prefs.flush();
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }
    @Override public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        game.viewport.apply();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();
        game.batch.draw(game.stages[selectedStage].bgTexture, 0, 0, game.viewport.getWorldWidth(), game.viewport.getWorldHeight());
        game.batch.end();
        stage.act(delta); stage.draw();
    }
    @Override public void resize(int w, int h) { game.viewport.update(w, h, true); }
    @Override public void dispose() { stage.dispose(); }
}
