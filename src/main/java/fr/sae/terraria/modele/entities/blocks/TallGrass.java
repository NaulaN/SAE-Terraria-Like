package fr.sae.terraria.modele.entities.blocks;

import fr.sae.terraria.modele.Environment;
import fr.sae.terraria.modele.TileMaps;
import fr.sae.terraria.modele.entities.entity.Entity;
import fr.sae.terraria.modele.entities.entity.ReproductiveObjectType;
import fr.sae.terraria.modele.entities.entity.SpawnableObjectType;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.util.ArrayList;
import java.util.List;


public class TallGrass extends Block implements ReproductiveObjectType, SpawnableObjectType
{
    public static final int WHEN_SPAWN_A_TALL_GRASS = 2_500;
    public static final double TALL_GRASS_SPAWN_RATE = .8;
    public static final double REPRODUCTION_RATE = 1_250;
    public static final double GROWTH_SPEED = .01;
    public static final int GROWTH_TALL_GRASS_STEP = 6;
    public static final int LOOTS_FIBRE_MAX = 3;

    private final DoubleProperty tallGrassGrowth;

    private final Environment environment;


    public TallGrass(final Environment environment, int x, int y)
    {
        super(BlockSet.TALL_GRASS, environment, x, y);
        this.environment = environment;

        this.tallGrassGrowth = new SimpleDoubleProperty(0);
    }

    public TallGrass(Environment environment) { this(environment, 0, 0); }

    @Override public void updates()
    {
        // L'animation de pousse
        if (((int) (tallGrassGrowth.get())) < TallGrass.GROWTH_TALL_GRASS_STEP)
            tallGrassGrowth.set(tallGrassGrowth.get() + TallGrass.GROWTH_SPEED);
    }

    /** Reproduit des hautes herbes à gauche et à droite de celle parente */
    @Override public List<Entity> reproduction(Environment environment)
    {
        List<Entity> children = new ArrayList<>();

        boolean tallGrassMustReproduce = environment.getTicks()%TallGrass.REPRODUCTION_RATE == 0;
        if (tallGrassMustReproduce) {
            final TileMaps tileMaps = environment.getTileMaps();
            List<Entity> entities = environment.getEntities();
            int heightTile = environment.heightTile;
            int widthTile = environment.widthTile;
            int widthMaps = tileMaps.getWidth();
            int heightMaps = tileMaps.getHeight();

            int x = -1;
            int y = (int) (getY()/heightTile)+1;

            // Check si aucune entité ne se trouve à coté
            int left = 0; int right = 0;
            for (Entity entity : entities) {
                boolean haveAnEntityOnLeft = entity.getX() == (getX() - widthTile) && entity.getY() == getY();
                boolean haveAnEntityOnRight = entity.getX() == (getX() + widthTile) && entity.getY() == getY();

                if (haveAnEntityOnLeft) left++;
                if (haveAnEntityOnRight) right++;
            }

            // Si les casses à côté sont libres
            boolean leftIsAvailable = (left == 0);
            boolean rightIsAvailable = (right == 0);
            if (leftIsAvailable)
                x = (int) (getX() - widthTile)/widthTile;
            else if (rightIsAvailable)
                x = (int) (getX() + widthTile)/widthTile;

            // La place sur la carte
            boolean isntOutTheMap = (x >= 0 && x < widthMaps) && (y >= 0 && y < heightMaps);
            if (isntOutTheMap) {
                boolean dontHaveTile = !tileMaps.isSkyTile(x, y);

                if (dontHaveTile) {
                    int xTallGrassChildren = (int) ((left == 0) ? (getX() - widthTile) : (getX() + widthTile));
                    int yTallGrassChildren = (int) getY();

                    TallGrass tallGrassChildren = new TallGrass(environment, xTallGrassChildren, yTallGrassChildren);
                    tallGrassChildren.setRect(widthTile, heightTile);
                    children.add(tallGrassChildren);
                }
            }
        }

        return children;
    }

    @Override public void spawn(int x, int y)
    {
        this.setX(x);
        this.setY(y);
        this.setRect(this.environment.widthTile, this.environment.heightTile);

        this.environment.getEntities().add(this);
        this.environment.getBlocks().add(this);
    }


    public DoubleProperty getTallGrassGrowthProperty() { return this.tallGrassGrowth; }
}
