package io.github.chaosarena;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class Main extends Game {
    public static final int MAX_LEVELS = 3;
    public static final int MAX_SLOTS = 3;

    public static final float[] LEVEL_ENEMY_HEALTH = { 250f,  250f,  250f }; // Similar al jugador
    public static final float[] LEVEL_ENEMY_SPEED  = { 300f,  400f,  500f }; // Más rápidos
    public static final float[] LEVEL_ENEMY_DAMAGE = {   5f,    5f,    5f }; // Daño normal y equitativo
    public static final float[] LEVEL_ATTACK_RATE  = {   1.6f,  1.0f,  0.6f }; // Muchísimo más listos y agresivos

    public static final String[] LEVEL_ENEMY_NAMES = { "Dark Fighter", "Ronin Boss", "Shadow Master" };
    public static final String[] CHAR_NAMES = { "Shadow Fist", "Iron Claw", "Ronin" };

    public static final String[] LEVEL_LORE = {
        "Nivel 1: El Torneo Comienza\n\nHas llegado a las puertas del Chaos Arena. Tu primer rival es un guerrero sin nombre que busca robar tu honor. ¡Derrótalo!",
        "Nivel 2: El Honor del Ronin\n\nBajo el sol abrasador, un maestro de la espada te espera. Dice que tu estilo es débil... demuéstrale que se equivoca.",
        "Nivel 3: El Maestro de las Sombras\n\nLa ciudad cyberpunk es el fin del camino. El Shadow Master te observa desde lo alto. Si vences, serás leyenda."
    };

    public SpriteBatch batch;
    public ExtendViewport viewport;
    public Preferences prefs;
    public BitmapFont font, bigFont;
    public Texture bgForest, bgCity, bgDesert, bgTitle, whiteTexture, circleTexture;
    public Texture joystickBg, joystickKnob;
    public Music music;
    public StageDef[] stages;
    public final GlyphLayout layout = new GlyphLayout();

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new ExtendViewport(1920, 1080);
        prefs = Gdx.app.getPreferences("ChaosArenaPrefs");
        setupFonts();
        setupTextures();
        try {
            music = Gdx.audio.newMusic(Gdx.files.internal("sounds/Techno_Syndrome.mp3"));
            music.setLooping(true); music.setVolume(0.8f); music.play();
        } catch (Exception e) {}
        setScreen(new MainMenuScreen(this));
    }

    private void setupFonts() {
        font = new BitmapFont(); font.getData().setScale(3.5f);
        bigFont = new BitmapFont(); bigFont.getData().setScale(8f);
    }

    private void setupTextures() {
        Pixmap p1 = new Pixmap(1, 1, Pixmap.Format.RGBA8888); p1.setColor(Color.WHITE); p1.fill();
        whiteTexture = new Texture(p1); p1.dispose();
        Pixmap p2 = new Pixmap(256, 256, Pixmap.Format.RGBA8888); p2.setColor(Color.WHITE); p2.fillCircle(128, 128, 128);
        circleTexture = new Texture(p2); p2.dispose();

        bgForest = new Texture("backgrounds/forest_bg.png");
        bgCity = new Texture("backgrounds/city_bg.png");
        bgDesert = new Texture("backgrounds/desert_bg.png");
        bgTitle = new Texture("backgrounds/title_bg.png");
        joystickBg = new Texture("joystick/AIR_joystick_bg600.png");
        joystickKnob = new Texture("joystick/AIR_joystick_stick600.png");

        // En Main.java, dentro de setupTextures()
        stages = new StageDef[] {
            // --- MAPA 1: BOSQUE ---
            new StageDef("Bosque", bgForest, -40f, 160f)
                .addOffset("Shadow Fist", -750f)
                .addOffset("Iron Claw", -500f)
                .addOffset("Ronin", -175f),

            // --- MAPA 2: DESIERTO ---
            new StageDef("Desierto", bgDesert, 0f, 130f)
                .addOffset("Shadow Fist", -750f)
                .addOffset("Iron Claw", -450f)
                .addOffset("Ronin", -175f),

            // --- MAPA 3: CIUDAD ---
            new StageDef("Ciudad", bgCity, 40f, 180f)
                .addOffset("Shadow Fist", -750f)
                .addOffset("Iron Claw", -500f)
                .addOffset("Ronin", -175f)
        };
    }

    public String getAtlasForChar(String name) {
        if (name.contains("Iron Claw")) return "sprites/player/player_atlas/martial3_atlas.atlas";
        if (name.contains("Ronin")) return "sprites/player/player_atlas/samurai_atlas.atlas";
        return "sprites/player/player_atlas/martial1_atlas.atlas";
    }

    public String getPrefixForChar(String name) {
        if (name.contains("Iron Claw")) return "martial3";
        if (name.contains("Ronin")) return "samurai";
        return "martial1";
    }

    private TextButton.TextButtonStyle makeStyle(Color bg) {
        TextButton.TextButtonStyle s = new TextButton.TextButtonStyle();
        s.font = font;
        s.up = new TextureRegionDrawable(whiteTexture).tint(bg);
        return s;
    }

    public TextButton.TextButtonStyle fightStyle() { return makeStyle(new Color(0.1f, 0.1f, 0.1f, 0.7f)); }
    public TextButton.TextButtonStyle redStyle() { return makeStyle(new Color(0.55f, 0.05f, 0.05f, 0.9f)); }
    public TextButton.TextButtonStyle goldStyle() { return makeStyle(new Color(0.45f, 0.35f, 0.0f, 0.9f)); }
    public TextButton.TextButtonStyle dangerStyle() { return makeStyle(new Color(0.25f, 0.0f, 0.0f, 0.85f)); }

    @Override public void dispose() { super.dispose(); batch.dispose(); }
}
