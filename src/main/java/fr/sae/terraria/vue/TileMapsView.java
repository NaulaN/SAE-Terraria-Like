package fr.sae.terraria.vue;

import fr.sae.terraria.modele.Environment;
import fr.sae.terraria.modele.TileMaps;
import fr.sae.terraria.modele.entities.Rabbit;
import fr.sae.terraria.modele.entities.Slime;
import fr.sae.terraria.modele.entities.blocks.*;
import fr.sae.terraria.vue.entities.RabbitView;
import fr.sae.terraria.vue.entities.SlimeView;
import javafx.collections.ListChangeListener;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * <h1>Tile maps View</h1>
 * <h2>Une classes qui se gère de l'affichage de la carte et de ces élèments aditionnel </h2>
 * <h3><u>Description:</u></h3>
 * <p>Cette classe permet d'afficher, de supprimer les élèments visible à l'écran</p>
 *
 * @author CHRZASZCZ Naulan
 */
public class TileMapsView
{
    private final List<ImageView> rabbitsView;
    private final List<ImageView> blocksView;
    private final List<ImageView> slimesView;
    // Cette liste marche differement à cause de la classes Group, cette liste conserve un groupe d'images
    // La classes Group contient plus ou moins d'images selon du fait de comment il génère l'arbre.
    private final List<Group> treesView;

    private final Image torchImg;
    private final Image floorTopImg;
    private final Image floorLeftImg;
    private final Image floorRightImg;
    private final Image treeImg;
    private final Image stoneImg;
    private final Image dirtImg;
    private final Image tallGrassImg;

    private final Pane displayHostileBeings;
    private final Pane display;

    private final Environment environment;

    private int tileWidth;
    private int tileHeight;


    /**
     * @param environment Avoir des infos relatives à son environment
     * @param displayTileMap Affiche la carte tuilée
     * @param displayHostileBeings Affiche les entités mouvantes (Animal, Joueur, ...)
     */
    public TileMapsView(Environment environment,
                        Pane displayTileMap,
                        Pane displayHostileBeings,
                        double scaleMultiplicatorWidth,
                        double scaleMultiplicatorHeight)
    {
        super();
        this.environment = environment;
        this.display = displayTileMap;
        this.displayHostileBeings = displayHostileBeings;

        this.tileHeight = (int) (TileMaps.TILE_DEFAULT_SIZE * scaleMultiplicatorHeight);
        this.tileWidth = (int) (TileMaps.TILE_DEFAULT_SIZE * scaleMultiplicatorWidth);

        this.rabbitsView = new ArrayList<>();
        this.blocksView = new ArrayList<>();
        this.slimesView = new ArrayList<>();
        this.treesView = new ArrayList<>();

        // Génération des images
        this.floorTopImg = View.loadAnImage("tiles/floor-top.png", tileWidth, tileHeight);
        this.floorLeftImg = View.loadAnImage("tiles/floor-left.png", tileWidth, tileHeight);
        this.floorRightImg = View.loadAnImage("tiles/floor-right.png", tileWidth, tileHeight);
        this.stoneImg = View.loadAnImage("tiles/rock-fill.png", tileWidth, tileHeight);
        this.dirtImg = View.loadAnImage("tiles/dirt-top.png", tileWidth, tileHeight);
        this.torchImg = View.loadAnImage("tiles/torch.png", tileWidth, tileHeight);
        this.treeImg = View.loadAnImage("sprites/tree-sheet.png", scaleMultiplicatorWidth, scaleMultiplicatorHeight);
        this.tallGrassImg = View.loadAnImage("tiles/tall-grass.png",tileWidth,tileHeight);

        // Ajoute et supprime les elements de l'écran qui concernent les blocks
        this.environment.getBlocks().addListener((ListChangeListener<? super Block>) this::updatesBlocksView);
        // Ajoute et supprime les elements de l'écran qui concernent les lapins
        this.environment.getRabbits().addListener((ListChangeListener<? super Rabbit>) this::updatesRabbitView);
        // Ajoute et supprime les elements de l'écran qui concernent les slimes
        this.environment.getSlimes().addListener((ListChangeListener<? super Slime>) this::updatesSlimeView);
        // Ajoute et supprime un groupe d'élèments de l'écran qui concerne les arbres
        this.environment.getTrees().addListener((ListChangeListener<? super Tree>) this::updatesTreeView);
    }

    /**
     * Supprime ou affiche les blocs qui sont ajouté dans la liste d'entité <code>List<Block> blocks;</code>
     *  Une fois affiché, la fonction range l'imageView crée dans la liste <code>List<ImageView> blocksView;</code> de cette classes
     *
     *  @param c Cet argument est present car on passe cette fonction en tant que reference (this::updatesBlocksView),
     *          l'argument est là pour obtenir les informations concernant les modifications dans liste <code>List<Block> blocks;</code>
     */
    private void updatesBlocksView(ListChangeListener.Change<? extends Block> c)
    {
        while (c.next()) {
            if (c.wasAdded()) {
                ImageView blockView = new ImageView();
                Block block = c.getAddedSubList().get(0);

                blockView.setImage(null);
                if (block instanceof Dirt) {
                    if (((Dirt) block).getTypeOfFloor() == TileMaps.FLOOR_TOP)
                        blockView.setImage(this.floorTopImg);
                    else if (((Dirt) block).getTypeOfFloor() == TileMaps.FLOOR_LEFT)
                        blockView.setImage(this.floorLeftImg);
                    else if (((Dirt) block).getTypeOfFloor() == TileMaps.FLOOR_RIGHT)
                        blockView.setImage(this.floorRightImg);
                    else if (((Dirt) block).getTypeOfFloor() == TileMaps.DIRT)
                        blockView.setImage(this.dirtImg);
                    else if (((Dirt) block).getTypeOfFloor() == 0)
                        blockView.setImage(this.floorTopImg);
                } else if (block instanceof Stone) {
                    blockView.setImage(this.stoneImg);
                } else if (block instanceof TallGrass) {
                    blockView.setImage(this.tallGrassImg);
                    ((TallGrass) block).getTallGrassGrowthProperty().addListener((observable, oldValue, newValue) -> blockView.setViewport(new Rectangle2D(0, ((int) ((this.tallGrassImg.getHeight() / (TallGrass.GROWTH_TALL_GRASS_STEP) * newValue.intValue()) - tallGrassImg.getHeight())), this.tallGrassImg.getWidth(), this.tallGrassImg.getHeight())));
                } else if (block instanceof Torch) {
                    blockView.setImage(this.torchImg);
                }

                if (!Objects.isNull(blockView.getImage())) {
                    blockView.translateXProperty().bind(block.xProperty());
                    blockView.translateYProperty().bind(block.yProperty());

                    this.display.getChildren().add(blockView);
                    this.blocksView.add(blockView);
                }
            }

            if (c.wasRemoved()) {
                this.display.getChildren().remove(this.blocksView.get(c.getTo()));
                this.blocksView.remove(c.getTo());
            }
        }
    }

    /**
     * Supprime ou affiche les lapins qui sont ajouté dans la liste d'entité <code>List<Rabbit> rabbits;</code>
     *  Une fois affiché, la fonction range l'imageView crée dans la liste <code>List<ImageView> rabbitsView;</code> de cette classe
     *
     *  @param c Cet argument est present car on passe cette fonction en tant que reference (this::updatesRabbitsView),
     *          l'argument est là pour obtenir les informations concernant les modifications dans liste <code>List<Block> rabbits;</code>
     */
    private void updatesRabbitView(ListChangeListener.Change<? extends Rabbit> c)
    {
        while (c.next()) {
            if (c.wasAdded()) {
                RabbitView rabbitView = new RabbitView(c.getAddedSubList().get(0), environment.scaleMultiplicatorWidth, environment.scaleMultiplicatorHeight);
                rabbitView.display(this.displayHostileBeings);
                this.rabbitsView.add(rabbitView.getImgView());
            }

            if (c.wasRemoved()) {
                this.displayHostileBeings.getChildren().remove(this.rabbitsView.get(c.getTo()));
                this.rabbitsView.remove(c.getTo());
            }
        }
    }

    /**
     * Supprime ou affiche les slimes qui sont ajoutés dans la liste d'entité <code>List<Slime> slimes;</code>
     *  Une fois affiché, la fonction range l'imageView crée dans la liste <code>List<ImageView> slimesView;</code> de cette classe
     *
     *  @param c Cet argument est present car on passe cette fonction en tant que reference (this::updatesSlimesView),
     *          l'argument est là pour obtenir les informations concernant les modifications dans liste <code>List<Slime> slimes;</code>
     */
    private void updatesSlimeView(ListChangeListener.Change<? extends Slime> c)
    {
        while (c.next()) {
            if (c.wasAdded()) {
                SlimeView slimeView = new SlimeView(c.getAddedSubList().get(0), environment.scaleMultiplicatorWidth, environment.scaleMultiplicatorHeight);
                slimeView.display(this.displayHostileBeings);
                this.slimesView.add(slimeView.getImgView());
            }

            if (c.wasRemoved()) {
                this.displayHostileBeings.getChildren().remove(this.slimesView.get(c.getTo()));
                this.slimesView.remove(c.getTo());
            }
        }
    }

    /**
     * Supprime ou affiche les groupes d'image de l'arbre qui sont ajouté dans la liste d'entité <code>List<Tree> trees;</code>
     * Génère une image plus ou moins grande selon le <code>Math.random</code> obtenue.
     *  Une fois affiché, la fonction range l'imageView crée dans la liste <code>List<ImageView> treesView;</code> de cette classe
     *
     *  @param c Cet argument est present car on passe cette fonction en tant que reference (this::updatesRabbitsView),
     *          l'argument est là pour obtenir les informations concernant les modifications dans liste <code>List<Block> rabbits;</code>
     */
    private void updatesTreeView(ListChangeListener.Change<? extends Tree> c)
    {
        while (c.next()) {
            if (c.wasAdded()) {
                Group group = new Group();

                // La hauteur des feuillages
                int nbFoliage = ((int) (Math.random()*2))+1;
                // La hauteur du tronc
                int nbTrunk = ((int) (Math.random()*3))+1;

                Rectangle2D viewportFirstFrame = new Rectangle2D(0, 0, this.tileWidth, this.tileHeight);
                ImageView firstFrameView = new ImageView();
                firstFrameView.setImage(this.treeImg);
                firstFrameView.setViewport(viewportFirstFrame);
                group.getChildren().add(firstFrameView);

                for (int f = 0; f < nbFoliage; f++) {
                    Rectangle2D viewportSecondFrame = new Rectangle2D(0, this.tileHeight, this.tileWidth, this.tileHeight);
                    ImageView secondFrameView = new ImageView();
                    secondFrameView.setImage(this.treeImg);
                    secondFrameView.setViewport(viewportSecondFrame);
                    group.getChildren().add(secondFrameView);
                }

                if (nbTrunk > 1) {
                    Rectangle2D viewportEndTrunk = new Rectangle2D(0, (this.tileHeight*2), this.tileWidth, this.tileHeight);
                    ImageView endTrunkView = new ImageView();
                    endTrunkView.setImage(this.treeImg);
                    endTrunkView.setViewport(viewportEndTrunk);
                    group.getChildren().add(endTrunkView);

                    for (int t = 0; t < nbTrunk-1; t++) {
                        Rectangle2D viewportTrunk = new Rectangle2D(this.tileWidth, (this.tileHeight*2), this.tileWidth, this.tileHeight);
                        ImageView trunkView = new ImageView();
                        trunkView.setImage(this.treeImg);
                        trunkView.setViewport(viewportTrunk);
                        group.getChildren().add(trunkView);
                    }
                } else {
                    Rectangle2D viewportTrunk = new Rectangle2D(0, (this.tileHeight*2), this.tileWidth, this.tileHeight);
                    ImageView trunkView = new ImageView();
                    trunkView.setImage(this.treeImg);
                    trunkView.setViewport(viewportTrunk);
                    group.getChildren().add(trunkView);
                }
                Rectangle2D viewportTrunkFoot = new Rectangle2D(0, (this.tileHeight*3), this.tileWidth, this.tileHeight);
                ImageView trunkFootView = new ImageView();
                trunkFootView.setImage(this.treeImg);
                trunkFootView.setViewport(viewportTrunkFoot);
                group.getChildren().add(trunkFootView);

                Tree tree = c.getAddedSubList().get(0);
                for (int i = 0; i < group.getChildren().size(); i++) {
                    ImageView treeView = (ImageView) group.getChildren().get(i);

                    treeView.setY((int) (((tree.getY() + this.tileHeight) + (i * this.tileHeight)) - (this.tileHeight * group.getChildren().size())));
                    treeView.setX((int) tree.getX());
                }
                tree.setRect(this.tileWidth, (2+nbFoliage+nbTrunk)*this.tileHeight);
                tree.getRect().updates(tree.getX(), tree.getY() - ((2+nbFoliage+nbTrunk)*this.tileHeight) + this.tileHeight);

                this.display.getChildren().add(group);
                this.treesView.add(group);
            }

            if (c.wasRemoved()) {
                this.display.getChildren().remove(this.treesView.get(c.getTo()));
                this.treesView.remove(c.getTo());
            }
        }

    }

    /** Affiche une erreur si un developer a fait une erreur lors de la saisie d'un tile dans le fichier .json */
    private void errorTile(final int tile) { if (tile != TileMaps.SKY) System.out.println("Le tile '" + tile + "' n'est pas reconnu."); }

    /** Decompose la carte pour afficher une à une les tiles à l'écran */
    public void displayMaps(TileMaps tiles)
    {
        for (int y = 0; y < tiles.getHeight() ; y++)
            for (int x = 0 ; x < tiles.getWidth() ; x++)
                switch (tiles.getTile(x, y)) {
                    case TileMaps.STONE:
                        Stone stoneEntity = new Stone(this.environment, x*this.tileWidth, y*this.tileHeight);
                        stoneEntity.setRect(this.tileWidth, this.tileHeight);

                        this.environment.getEntities().add(stoneEntity);
                        this.environment.getBlocks().add(stoneEntity);
                        break;
                    case TileMaps.DIRT:
                        Dirt dirtSprite = new Dirt(this.environment, x*this.tileWidth, y*this.tileHeight);
                        dirtSprite.setTypeOfFloor(TileMaps.DIRT);
                        dirtSprite.setRect(this.tileWidth, this.tileHeight);

                        this.environment.getEntities().add(dirtSprite);
                        this.environment.getBlocks().add(dirtSprite);
                        break;
                    case TileMaps.FLOOR_TOP:
                    case TileMaps.FLOOR_LEFT:
                    case TileMaps.FLOOR_RIGHT:
                        Dirt floorEntity = new Dirt(this.environment, x*this.tileWidth, y*this.tileHeight);
                        floorEntity.setTypeOfFloor(tiles.getTile(x, y));
                        floorEntity.setRect(this.tileWidth, this.tileHeight);

                        this.environment.getEntities().add(floorEntity);
                        this.environment.getBlocks().add(floorEntity);
                        break;
                    default:
                        this.errorTile(tiles.getTile(x, y));
                        break;
                }
    }
}
