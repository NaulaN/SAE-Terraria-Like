package fr.sae.terraria.modele;

import fr.sae.terraria.Terraria;
import fr.sae.terraria.modele.entities.Rabbit;
import fr.sae.terraria.modele.entities.Slime;
import fr.sae.terraria.modele.entities.blocks.Block;
import fr.sae.terraria.modele.entities.blocks.Tree;
import fr.sae.terraria.modele.entities.entity.Entity;
import fr.sae.terraria.modele.entities.entity.ReproductiveObjectType;
import fr.sae.terraria.modele.entities.items.Vodka;
import fr.sae.terraria.modele.entities.player.Player;
import fr.sae.terraria.modele.entities.tools.MaterialSet;
import fr.sae.terraria.modele.entities.tools.Tool;
import fr.sae.terraria.modele.entities.tools.ToolSet;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Duration;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.io.BufferedInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Environment
{
    private final ObservableList<Tree> trees;
    private final ObservableList<Block> blocks;
    // Permet d'update toutes les entités avec une seule boucle.
    private final ObservableList<Entity> entities;
    // Range des entitiés en plus pour avoir le nombre d'entités sur la carte et limiter leurs apparitions
    private final ObservableList<Rabbit> rabbits;
    private final ObservableList<Slime> slimes;
    // Permet d'update facilement les lumières des torches sur le filtre
    private final ObservableList<Block> torches;

    private final TileMaps tileMaps;
    private final Player player;
    private final Clock clock;
    private Timeline loop;

    public double scaleMultiplicatorWidth;
    public double scaleMultiplicatorHeight;

    private int previousDays;
    public int widthTile;
    public int heightTile;
    private int ticks;


    public Environment(double scaleMultiplicatorWidth, double scaleMultiplicatorHeight)
    {
        super();

        this.scaleMultiplicatorWidth = scaleMultiplicatorWidth;
        this.scaleMultiplicatorHeight = scaleMultiplicatorHeight;
        this.widthTile = (int) (scaleMultiplicatorWidth * TileMaps.TILE_DEFAULT_SIZE);
        this.heightTile = (int) (scaleMultiplicatorHeight * TileMaps.TILE_DEFAULT_SIZE);

        this.tileMaps = new TileMaps();
        this.tileMaps.load( "maps/map_0.json");

        this.clock = new Clock();
        this.previousDays = -1;
        this.ticks = 0;

        this.entities = FXCollections.observableArrayList();
        this.rabbits = FXCollections.observableArrayList();
        this.slimes = FXCollections.observableArrayList();
        this.torches = FXCollections.observableArrayList();
        this.blocks = FXCollections.observableArrayList();
        this.trees = FXCollections.observableArrayList();

        // Fait apparaitre le joueur.
        this.player = new Player(this);
        this.player.setVelocity(5);
        this.player.setPv(4);
        this.player.spawn(5*widthTile, 3*heightTile);

        // Détecte si le joueur n'est pas dans un bloc lorsqu'il met un block au sol
        this.blocks.addListener((ListChangeListener<? super Block>) c -> {
            while (c.next()) if (c.wasAdded()) {
                Block block = c.getAddedSubList().get(0);
                if (!Objects.isNull(block.getRect())) {
                    // Si on le pose sur le joueur
                    boolean isIntoABlock = player.getRect().collideRect(block.getRect());
                    boolean isCollideBlock = Block.isDirt(block) || Block.isRock(block) || Block.isFloorLeft(block) || Block.isFloorRight(block) || Block.isFloorTop(block);
                    if (isCollideBlock && isIntoABlock) {
                        // Place le joueur au-dessus du block posé.
                        player.setY(block.getY() - player.getRect().getHeight());
                        player.getGravity().yInit = player.getY();
                        player.getGravity().xInit = player.getX();
                    }
                }
            }
        });

        Environment.playSound("sound/Bird_Sound.wav", true);
        gameLoop();
    }

    /** La boucle principale du jeu  */
    private void gameLoop()
    {
        // TODO TEST
        boolean[] caught = new boolean[] { false };

        this.loop = new Timeline();
        this.loop.setCycleCount(Animation.INDEFINITE);

        List<Entity> entitiesAtAdded = new ArrayList<>();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(Terraria.TARGET_FPS), (ev -> {
            // TODO TEST
            if (!caught[0]) {
                this.player.pickup(new Tool(ToolSet.PICKAXE, MaterialSet.IRON));
                this.player.pickup(new Vodka(this));
                this.player.pickup(new Tool(ToolSet.SWORD, MaterialSet.IRON));

                caught[0] = true;
            }

            // Ajoute les entités ReproductiveObjectType
            for (Entity entity : entitiesAtAdded) {
                this.entities.add(entity);
                if (entity instanceof Block)
                    this.blocks.add((Block) entity);
            }
            entitiesAtAdded.clear();

            // Génère aléatoirement des entités
            boolean dayTime = this.clock.getMinutes() < (Clock.MINUTES_IN_A_DAY/2);
            boolean nightTime = this.clock.getMinutes() > (Clock.MINUTES_IN_A_DAY)/2;
            boolean weHaveChangedDay = this.previousDays != this.clock.getDays();
            if (weHaveChangedDay)
                for (int i = 0; i < 15; i++) // Génère par jour, 3 arbres
                    GenerateEntity.treeRandomly(this);
            if (dayTime) {  // Génère certaines entités uniquement pendant le jour
                GenerateEntity.rabbitRandomly(this);
                GenerateEntity.tallGrassRandomly(this);
            } else if (nightTime)    // Génère certaines entités uniquement pendant le soir et la nuit
                GenerateEntity.slimeRandomly(this);

            // Updates toutes les entités
            for (Entity entity : this.entities) {
                // ajoute les enfants des entités parent.
                if (entity instanceof ReproductiveObjectType)
                    entitiesAtAdded.addAll(((ReproductiveObjectType) entity).reproduction(this));
                entity.updates();
            }
            this.player.updates();

            this.previousDays = this.clock.getDays();
            this.clock.updates(this.ticks);
            this.ticks++;
        }));

        this.loop.getKeyFrames().add(keyFrame);
        this.loop.play();
    }

    public static Clip playSound(String path, boolean loop)
    {
        Clip clip = null;
        try {
            clip = AudioSystem.getClip();
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(Terraria.class.getResourceAsStream(path)));
            clip.open(inputStream);
        } catch (Exception e) { e.printStackTrace(); }

        if (!Objects.isNull(clip)) {
            clip.loop(loop ? Clip.LOOP_CONTINUOUSLY : 0);
            clip.start();
        }

        return clip;
    }


    public ObservableList<Block> getBlocks() { return this.blocks; }
    public ObservableList<Tree> getTrees() { return this.trees; }
    public ObservableList<Entity> getEntities() { return this.entities; }
    public ObservableList<Rabbit> getRabbits() { return this.rabbits; }
    public ObservableList<Block> getTorches() { return this.torches; }
    public ObservableList<Slime> getSlimes() { return this.slimes; }
    public TileMaps getTileMaps() { return this.tileMaps; }
    public Player getPlayer() { return this.player; }
    public Clock getGameClock() { return this.clock; }
    public Timeline getLoop() { return this.loop; }
    public int getTicks() { return this.ticks; }
}