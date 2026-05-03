package io.github.chaosarena;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen extends ScreenAdapter {
    private final Main game;
    private final Stage stage;

    private Player player1, player2;
    private EnemyAI enemyAI;

    private int currentLevel;
    private int activeSlot;
    private boolean isGameOver = false;
    private boolean isPaused = false;
    private boolean showingLore = false;
    private boolean lastBattleWon = false;
    
    private float timeLeft = 90f;
    private int enemiesDefeated = 0;
    private float totalTimeSurvived = 0f;
    private float feedbackTimer = 0f;
    private String feedbackMsg = "";
    private Label lblTime;
    private Label lblEnemies;
    private Label lblFeedback;

    private Touchpad joystick;
    private Table uiRootTable;
    private Table resultTable;
    private Table pauseTable;
    private Table loreTable;

    private Label lblResultTitle, lblResultSub;
    private TextButton btnResultAction, btnResultMenu;
    private StageDef currentStage;

    // --- ROGUELIKE ---
    private RunManager runManager;
    private Table buffSelectionTable;

    public GameScreen(Main game, int level, int slot, String p1Name, String p2Name, int stageIdx) {
        this.game = game;
        this.currentLevel = level;
        this.activeSlot = slot;
        this.stage = new Stage(game.viewport, game.batch);

        if (stageIdx >= 0 && stageIdx < game.stages.length) {
            currentStage = game.stages[stageIdx];
        } else {
            // En modo historia, el escenario depende del nivel
            if (slot >= 0) currentStage = game.stages[Math.min(level - 1, game.stages.length - 1)];
            else currentStage = game.stages[MathUtils.random(0, game.stages.length - 1)];
        }

        // Aplicar altura del suelo + offset individual por personaje en este mapa
        float p1Y = currentStage.groundY + currentStage.getOffsetFor(p1Name);
        float p2Y = currentStage.groundY + currentStage.getOffsetFor(p2Name);

        player1 = new Player(p1Name, game.getAtlasForChar(p1Name), game.getPrefixForChar(p1Name), 400, p1Y, true, game.viewport.getWorldHeight());
        player1.charType = p1Name;
        player2 = new Player(p2Name, game.getAtlasForChar(p2Name), game.getPrefixForChar(p2Name), game.viewport.getWorldWidth() - 400, p2Y, false, game.viewport.getWorldHeight());
        player2.charType = p2Name;

        player1.opponent = player2;
        player2.opponent = player1;
        enemyAI = new EnemyAI(player2, player1);

        setupGameUI();
        setupPauseMenu();
        setupBuffUI();

        if (slot >= 0 || slot == -2) {
            if (slot == -2) {
                runManager = new RunManager(player1);
                applyArcadeConfig();
            } else {
                applyLevelConfig();
                setupLoreUI();
                showingLore = true;
            }
        } else {
            player1.maxHealth = 600f; player2.maxHealth = 600f;
            player1.currentHealth = 600f; player2.currentHealth = 600f;
            enemyAI.setSpeed(500f); enemyAI.setDamage(12f); enemyAI.setAttackRate(1.2f);
        }
    }

    private void applyLevelConfig() {
        int idx = Math.min(currentLevel - 1, Main.LEVEL_ENEMY_NAMES.length - 1);
        String enemyName = Main.LEVEL_ENEMY_NAMES[idx];
        player2.name = enemyName;

        // El rival depende del lore (nivel)
        String charType;
        if (idx == 0) charType = "Shadow Fist";
        else if (idx == 1) charType = "Ronin";
        else charType = "Iron Claw"; // Shadow Master usa el de Iron Claw por ejemplo

        player2.setAtlas(game.getAtlasForChar(charType), game.getPrefixForChar(charType), game.viewport.getWorldHeight());

        player2.charType = charType;

        // Recalcular Y del enemigo tras cambiar su atlas por si el offset cambia
        player2.y = currentStage.groundY + currentStage.getOffsetFor(charType);
        player2.groundY = player2.y;

        player2.maxHealth = Main.LEVEL_ENEMY_HEALTH[idx];
        player2.currentHealth = player2.maxHealth;
        enemyAI.setSpeed(Main.LEVEL_ENEMY_SPEED[idx]);
        enemyAI.setDamage(Main.LEVEL_ENEMY_DAMAGE[idx]);
        enemyAI.setAttackRate(Main.LEVEL_ATTACK_RATE[idx]);
    }

    private void applyArcadeConfig() {
        String charType = Main.CHAR_NAMES[com.badlogic.gdx.math.MathUtils.random(0, Main.CHAR_NAMES.length - 1)];
        player2.name = charType;
        player2.charType = charType;
        player2.setAtlas(game.getAtlasForChar(charType), game.getPrefixForChar(charType), game.viewport.getWorldHeight());
        player2.y = currentStage.groundY + currentStage.getOffsetFor(charType);
        player2.groundY = player2.y;

        player2.maxHealth = 250f;
        player2.currentHealth = player2.maxHealth;
        
        // Base normal, el RunManager escala luego
        enemyAI.setSpeed(350f);
        enemyAI.setDamage(10f); 
        enemyAI.setAttackRate(1.5f);

        runManager.applyEnemyScaling(enemyAI, player2);
    }

    private void setupLoreUI() {
        if (loreTable != null) {
            loreTable.remove();
        }
        loreTable = new Table();
        loreTable.setFillParent(true);
        loreTable.setBackground(new TextureRegionDrawable(game.whiteTexture).tint(new Color(0,0,0,0.85f)));

        String loreText = Main.LEVEL_LORE[Math.min(currentLevel-1, Main.LEVEL_LORE.length-1)];
        Label lblLore = new Label(loreText, new Label.LabelStyle(game.font, Color.WHITE));
        lblLore.setWrap(true);
        lblLore.setAlignment(1);

        TextButton btnStart = new TextButton("EMPEZAR COMBATE", game.redStyle());
        btnStart.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) {
                showingLore = false;
                loreTable.setVisible(false);
                uiRootTable.setVisible(true);
            }
        });

        loreTable.add(lblLore).width(1200).padBottom(100).row();
        loreTable.add(btnStart).size(600, 150);
        stage.addActor(loreTable);
        uiRootTable.setVisible(false);
    }

    private void setupGameUI() {
        uiRootTable = new Table();
        uiRootTable.setFillParent(true);
        
        Table hudTable = new Table();
        hudTable.setFillParent(true);
        hudTable.top().padTop(20);
        
        lblTime = new Label("", new Label.LabelStyle(game.font, Color.WHITE));
        lblEnemies = new Label("", new Label.LabelStyle(game.font, Color.WHITE));
        lblFeedback = new Label("", new Label.LabelStyle(game.font, Color.GREEN));
        
        hudTable.add(lblFeedback).padTop(10).row();
        
        uiRootTable.addActor(hudTable);

        // Botón de Pausa (Arriba Centro)
        TextButton btnPause = new TextButton("PAUSA", game.fightStyle());
        btnPause.addListener(new ClickListener() {
            @Override public void clicked(InputEvent e, float x, float y) { if(!isGameOver && !showingLore) togglePause(); }
        });

        // Contenedor para Pausa + Enemigos + Timer debajo
        Table centerTopTable = new Table();
        centerTopTable.add(btnPause).size(300, 100).padTop(20).row();
        centerTopTable.add(lblEnemies).padTop(10).row();
        centerTopTable.add(lblTime).padTop(5);

        // Estilo Joystick
        Touchpad.TouchpadStyle jsStyle = new Touchpad.TouchpadStyle();
        jsStyle.background = new TextureRegionDrawable(game.joystickBg);
        jsStyle.knob       = new TextureRegionDrawable(game.joystickKnob);
        jsStyle.knob.setMinWidth(180); jsStyle.knob.setMinHeight(180);
        joystick = new Touchpad(20, jsStyle);

        // Tabla de botones de ataque
        Table attackTable = new Table();
        TextButton.TextButtonStyle styleY = createBtnStyle(new Color(0.1f, 0.7f, 0.1f, 0.9f)); // ^
        TextButton.TextButtonStyle styleSq = createBtnStyle(new Color(0.8f, 0.4f, 0.8f, 0.9f)); // A1
        TextButton.TextButtonStyle styleO = createBtnStyle(new Color(0.8f, 0.1f, 0.1f, 0.9f)); // A2
        TextButton.TextButtonStyle styleX = createBtnStyle(new Color(0.1f, 0.4f, 0.8f, 0.9f)); // S

        TextButton btnJump    = new TextButton("^", styleY);
        TextButton btnPunch   = new TextButton("A1", styleSq);
        TextButton btnKick    = new TextButton("A2", styleO);
        TextButton btnSpecial = new TextButton("S", styleX);

        btnJump.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { if (!isGameOver && !isPaused && !showingLore) player1.jump(); } });
        btnPunch.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { if (!isGameOver && !isPaused && !showingLore) player1.attack(Player.AttackType.PUNCH); } });
        btnKick.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { if (!isGameOver && !isPaused && !showingLore) player1.attack(Player.AttackType.KICK); } });
        btnSpecial.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { if (!isGameOver && !isPaused && !showingLore) player1.attack(Player.AttackType.SPECIAL); } });

        int bs = 160; int p = 15;
        attackTable.add().size(bs);
        attackTable.add(btnJump).size(bs).padBottom(p).row();
        attackTable.add(btnPunch).size(bs).padRight(p);
        attackTable.add().size(bs);
        attackTable.add(btnKick).size(bs).padLeft(p).row();
        attackTable.add().size(bs);
        attackTable.add(btnSpecial).size(bs).padTop(p);

        uiRootTable.add(centerTopTable).colspan(3).top().row();
        uiRootTable.add(joystick).size(380).left().pad(80).expandY().bottom();
        uiRootTable.add().expandX();
        uiRootTable.add(attackTable).right().pad(80).bottom();

        resultTable = new Table(); resultTable.setFillParent(true); resultTable.center();
        lblResultTitle = new Label("", new Label.LabelStyle(game.bigFont, Color.YELLOW));
        lblResultSub = new Label("", new Label.LabelStyle(game.font, Color.WHITE));
        btnResultAction = new TextButton("", game.fightStyle());
        btnResultMenu = new TextButton("MENU PRINCIPAL", game.dangerStyle());

        btnResultAction.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { resetCombat(); } });
        btnResultMenu.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { game.setScreen(new MainMenuScreen(game)); } });

        resultTable.add(lblResultTitle).padBottom(20).row();
        resultTable.add(lblResultSub).padBottom(50).row();
        resultTable.add(btnResultAction).size(600, 150).padBottom(20).row();
        resultTable.add(btnResultMenu).size(600, 120);
        resultTable.setVisible(false);

        stage.addActor(uiRootTable);
        stage.addActor(resultTable);
    }

    private void setupPauseMenu() {
        pauseTable = new Table();
        pauseTable.setFillParent(true);
        pauseTable.center();
        pauseTable.setBackground(new TextureRegionDrawable(game.whiteTexture).tint(new Color(0,0,0,0.6f)));

        Label lblPause = new Label("PAUSA", new Label.LabelStyle(game.bigFont, Color.YELLOW));
        TextButton btnResume = new TextButton("CONTINUAR", game.fightStyle());
        ImageButton btnMute = new ImageButton(game.soundButtonStyle());
        btnMute.setChecked(game.prefs.getBoolean("is_muted", false));
        TextButton btnExit = new TextButton("SALIR AL MENU", game.dangerStyle());

        btnResume.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { togglePause(); } });
        btnMute.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) {
            boolean isMuted = game.prefs.getBoolean("is_muted", false);
            game.prefs.putBoolean("is_muted", !isMuted);
            game.prefs.flush();
            btnMute.setChecked(!isMuted);
            if (game.music != null) {
                game.music.setVolume(!isMuted ? 0f : 0.8f);
            }
        } });
        btnExit.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { game.setScreen(new MainMenuScreen(game)); } });

        pauseTable.add(lblPause).padBottom(60).row();
        pauseTable.add(btnResume).size(600, 150).padBottom(30).row();
        pauseTable.add(btnMute).size(150, 150).padBottom(30).row();
        pauseTable.add(btnExit).size(600, 150);
        pauseTable.setVisible(false);
        stage.addActor(pauseTable);


    }

    private void setupBuffUI() {
        buffSelectionTable = new Table();
        buffSelectionTable.setFillParent(true);
        buffSelectionTable.center();
        buffSelectionTable.setBackground(new TextureRegionDrawable(game.whiteTexture).tint(new Color(0,0,0,0.85f)));
        buffSelectionTable.setVisible(false);
        stage.addActor(buffSelectionTable);
    }

    private void showBuffSelection() {
        isGameOver = true;
        uiRootTable.setVisible(false);
        buffSelectionTable.clear();
        
        Label title = new Label("ELIGE UNA MEJORA", new Label.LabelStyle(game.bigFont, Color.GOLD));
        Label subtitle = new Label("Las mejoras que NO elijas se las quedará el enemigo...", new Label.LabelStyle(game.font, Color.RED));
        buffSelectionTable.add(title).padBottom(20).row();
        buffSelectionTable.add(subtitle).padBottom(50).row();

        final java.util.List<Buff> choices = runManager.getRandomBuffChoices();
        for (final Buff buff : choices) {
            String text = buff.getDescription() + " (" + (int)(buff.value * (buff.value < 1 ? 100 : 1)) + (buff.value < 1 ? "%" : "") + ")";
            
            Color fontColor = Color.WHITE;
            if (buff.rarity == Buff.Rarity.EPIC) {
                fontColor = Color.YELLOW;
            } else if (buff.rarity == Buff.Rarity.RARE) {
                fontColor = Color.CYAN;
            }

            TextButton btn = new TextButton(text, game.buffStyle(fontColor));
            btn.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    runManager.applyBuffToPlayer(buff);
                    // Los que no elegiste van para el enemigo
                    for (Buff other : choices) {
                        if (other != buff) {
                            runManager.addPendingEnemyBuff(other);
                        }
                    }
                    buffSelectionTable.setVisible(false);
                    checkNextEventOrContinue();
                }
            });
            // Apilamos uno debajo del otro y ocupamos buen ancho
            buffSelectionTable.add(btn).size(600, 100).padBottom(15).row();
        }
        buffSelectionTable.setVisible(true);
    }

    private void checkNextEventOrContinue() {
        if (runManager.shouldTriggerEvent()) {
            Event event = runManager.generateRandomEvent();
            event.apply(runManager);
            
            buffSelectionTable.clear();
            Label title = new Label("EVENTO ESPECIAL", new Label.LabelStyle(game.bigFont, Color.MAGENTA));
            Label desc = new Label(event.description, new Label.LabelStyle(game.font, Color.WHITE));
            TextButton btnOk = new TextButton("CONTINUAR", game.fightStyle());
            
            btnOk.addListener(new ClickListener() {
                @Override public void clicked(InputEvent e, float x, float y) {
                    buffSelectionTable.setVisible(false);
                    resetCombat();
                }
            });
            
            buffSelectionTable.add(title).padBottom(30).row();
            buffSelectionTable.add(desc).padBottom(50).row();
            buffSelectionTable.add(btnOk).size(500, 150);
            buffSelectionTable.setVisible(true);
        } else {
            resetCombat();
        }
    }

    private void togglePause() {
        isPaused = !isPaused;
        pauseTable.setVisible(isPaused);
        uiRootTable.setVisible(!isPaused);
    }

    private TextButton.TextButtonStyle createBtnStyle(Color color) {
        TextButton.TextButtonStyle s = new TextButton.TextButtonStyle();
        s.font = game.bigFont;
        s.up = new TextureRegionDrawable(game.circleTexture).tint(color);
        return s;
    }

    private void saveArcadeScore() {
        int[] topEnemies = new int[6];
        float[] topTimes = new float[6];
        for (int i = 0; i < 5; i++) {
            topEnemies[i] = game.prefs.getInteger("arcade_enemies_" + i, 0);
            topTimes[i] = game.prefs.getFloat("arcade_time_" + i, 0f);
        }
        topEnemies[5] = enemiesDefeated;
        topTimes[5] = totalTimeSurvived;
        
        for (int i = 0; i < 6; i++) {
            for (int j = i + 1; j < 6; j++) {
                if (topTimes[j] > topTimes[i] || (topTimes[j] == topTimes[i] && topEnemies[j] > topEnemies[i])) {
                    float tempT = topTimes[i]; topTimes[i] = topTimes[j]; topTimes[j] = tempT;
                    int tempE = topEnemies[i]; topEnemies[i] = topEnemies[j]; topEnemies[j] = tempE;
                }
            }
        }
        
        for (int i = 0; i < 5; i++) {
            game.prefs.putInteger("arcade_enemies_" + i, topEnemies[i]);
            game.prefs.putFloat("arcade_time_" + i, topTimes[i]);
        }
        game.prefs.flush();
    }

    private void showResult(boolean won) {
        player1.forceIdle();
        player2.forceIdle();
        lastBattleWon = won;
        isGameOver = true; uiRootTable.setVisible(false);
        
        if (activeSlot == -2) {
            if (won) {
                enemiesDefeated++;
                runManager.onFightWon();
                lblResultTitle.setText("¡RIVAL DERROTADO!");
                lblResultTitle.setColor(Color.GOLD);
                btnResultAction.setText("ELEGIR MEJORA");
                
                // Redefinimos listener temporalmente
                btnResultAction.clearListeners();
                btnResultAction.addListener(new ClickListener() { 
                    @Override public void clicked(InputEvent e, float x, float y) { 
                        resultTable.setVisible(false);
                        showBuffSelection();
                    } 
                });
            } else {
                lblResultTitle.setText("FIN DEL JUEGO");
                lblResultTitle.setColor(Color.RED);
                btnResultAction.setText("GUARDAR Y SALIR");
                btnResultAction.clearListeners();
                btnResultAction.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { resetCombat(); } });
                saveArcadeScore();
            }
        } else {
            lblResultTitle.setText(won ? "¡VICTORIA!" : "DERROTA");
            lblResultTitle.setColor(won ? Color.GOLD : Color.RED);
            
            btnResultAction.clearListeners();
            btnResultAction.addListener(new ClickListener() { @Override public void clicked(InputEvent e, float x, float y) { resetCombat(); } });
            
            if (won && activeSlot >= 0) {
                if (currentLevel >= Main.MAX_LEVELS) {
                    btnResultAction.setText("COMPLETADO - SALIR");
                } else {
                    btnResultAction.setText("SIGUIENTE COMBATE");
                }
            } else {
                btnResultAction.setText(won ? "OTRO DUELO" : "REINTENTAR");
            }
        }
        
        resultTable.setVisible(true);
    }

    private void resetCombat() {
        if (activeSlot == -2 && !lastBattleWon) {
            game.setScreen(new MainMenuScreen(game));
            return;
        }

        if (lastBattleWon) {
            if (activeSlot >= 0 || activeSlot == -2) {
                if (activeSlot >= 0 && currentLevel >= Main.MAX_LEVELS) {
                    game.setScreen(new MainMenuScreen(game));
                    return;
                }
                currentLevel++;
                if (activeSlot >= 0) {
                    game.prefs.putInteger("slot_" + activeSlot + "_level", currentLevel);
                    game.prefs.flush();
                } else if (activeSlot == -2) {
                    timeLeft += 30f;
                    feedbackTimer = 2f;
                    feedbackMsg = "+30s | Siguiente pelea";
                }
            }
        }
        
        isGameOver = false; isPaused = false;
        resultTable.setVisible(false); pauseTable.setVisible(false); uiRootTable.setVisible(true);
        if (buffSelectionTable != null) buffSelectionTable.setVisible(false);
        
        if (activeSlot >= 0 || activeSlot == -2) {
            if (activeSlot == -2) {
                currentStage = game.stages[com.badlogic.gdx.math.MathUtils.random(0, game.stages.length - 1)];
                applyArcadeConfig();
            } else {
                applyLevelConfig();
                currentStage = game.stages[Math.min(currentLevel - 1, game.stages.length - 1)];
            }
        }
        
        if (activeSlot == -2 && lastBattleWon) {
            player1.comboCharge = 0;
            // La salud no se regenera automáticamente un 15% como antes
            // Ahora dependemos de Buffs y Eventos para la curación (RunManager)
        } else {
            player1.currentHealth = player1.maxHealth; 
            player1.comboCharge = 0;
        }
        player2.currentHealth = player2.maxHealth; player2.comboCharge = 0;
        player1.x = 400; player2.x = game.viewport.getWorldWidth() - 400;
        player1.y = currentStage.groundY + currentStage.getOffsetFor(player1.charType);
        player1.groundY = player1.y;
        player2.y = currentStage.groundY + currentStage.getOffsetFor(player2.charType);
        player2.groundY = player2.y;
        
        if (activeSlot >= 0) {
            setupLoreUI();
            showingLore = true;
            loreTable.setVisible(true);
            uiRootTable.setVisible(false);
        } else {
            showingLore = false;
        }
    }

    @Override public void show() { Gdx.input.setInputProcessor(stage); }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        float fDelta = (isGameOver || isPaused || showingLore) ? 0 : delta;

        if (fDelta > 0) {
            if (activeSlot == -2) {
                timeLeft -= fDelta;
                totalTimeSurvived += fDelta;
                if (timeLeft <= 0) {
                    timeLeft = 0;
                    if (player1.currentHealth > 0 && player2.currentHealth > 0) {
                        showResult(false);
                    }
                }
                lblTime.setText(String.format("%02d", (int)timeLeft));
                lblEnemies.setText("DERROTADOS: " + enemiesDefeated);
                
                if (feedbackTimer > 0) {
                    feedbackTimer -= fDelta;
                    lblFeedback.setText(feedbackMsg);
                } else {
                    lblFeedback.setText("");
                }
            } else {
                lblTime.setText("");
                lblEnemies.setText("");
            }

            float ix = joystick.getKnobPercentX();
            if (ix > 0.2f) player1.move(850 * player1.speedMultiplier * delta); if (ix < -0.2f) player1.move(-850 * player1.speedMultiplier * delta);
            player1.updateDirection(ix > 0.2f, ix < -0.2f);
            player1.update(delta); player2.update(delta); enemyAI.update(delta);
            
            // Límite de mapa: Nadie se sale de la pantalla
            float minX = 150f;
            float maxX = game.viewport.getWorldWidth() - 150f;
            player1.x = com.badlogic.gdx.math.MathUtils.clamp(player1.x, minX, maxX);
            player2.x = com.badlogic.gdx.math.MathUtils.clamp(player2.x, minX, maxX);

            if (player1.currentHealth <= 0) showResult(false); else if (player2.currentHealth <= 0) showResult(true);
        }

        if (player1.isAttacking() && player1.getCurrentAttackType() == Player.AttackType.SPECIAL) {
            game.viewport.getCamera().position.x = game.viewport.getWorldWidth()/2 + com.badlogic.gdx.math.MathUtils.random(-15f, 15f);
            game.viewport.getCamera().position.y = game.viewport.getWorldHeight()/2 + com.badlogic.gdx.math.MathUtils.random(-15f, 15f);
        } else {
            game.viewport.getCamera().position.set(game.viewport.getWorldWidth()/2, game.viewport.getWorldHeight()/2, 0);
        }
        game.viewport.getCamera().update();
        game.batch.setProjectionMatrix(game.viewport.getCamera().combined);
        game.batch.begin();
        
        float pShiftX = (player1.x / game.viewport.getWorldWidth()) * 300f;
        game.batch.draw(currentStage.bgTexture, -pShiftX, currentStage.floorVisualOffset, game.viewport.getWorldWidth() + 300f, game.viewport.getWorldHeight());
        
        if (player1.isAttacking() && player1.getCurrentAttackType() == Player.AttackType.SPECIAL) {
            game.batch.setColor(new Color(0.5f, 0.0f, 0.0f, 0.5f));
            game.batch.draw(game.whiteTexture, -500, -500, game.viewport.getWorldWidth() + 1000, game.viewport.getWorldHeight() + 1000);
            game.batch.setColor(Color.WHITE);
        }
        
        player1.draw(game.batch, fDelta); player2.draw(game.batch, fDelta);
        drawBars();
        game.batch.end();
        stage.act(delta); stage.draw();
    }

    private void drawBars() {
        float bw = 700, bh = 45, m = 80, y = game.viewport.getWorldHeight() - 100;
        drawBar(m, y, bw, bh, player1.currentHealth / player1.maxHealth, Color.GREEN);
        Color c1 = Color.GOLD;
        if (player1.comboCharge >= player1.MAX_COMBO_CHARGE) if ((System.currentTimeMillis() / 200) % 2 == 0) c1 = Color.WHITE;
        drawBar(m, y - 30, bw, 12, player1.comboCharge / 100f, c1);

        float x2 = game.viewport.getWorldWidth() - m - bw;
        drawBar(x2, y, bw, bh, player2.currentHealth / player2.maxHealth, Color.GREEN);
        Color c2 = Color.GOLD;
        if (player2.comboCharge >= player2.MAX_COMBO_CHARGE) if ((System.currentTimeMillis() / 200) % 2 == 0) c2 = Color.WHITE;
        drawBar(x2, y - 30, bw, 12, player2.comboCharge / 100f, c2);
    }

    private void drawBar(float x, float y, float w, float h, float p, Color c) {
        game.batch.setColor(Color.BLACK); game.batch.draw(game.whiteTexture, x, y, w, h);
        game.batch.setColor(c); game.batch.draw(game.whiteTexture, x, y, w * Math.max(0, p), h);
        game.batch.setColor(Color.WHITE);
    }

    @Override public void resize(int w, int h) { game.viewport.update(w, h, true); }
    @Override public void dispose() { stage.dispose(); player1.dispose(); player2.dispose(); }
}
