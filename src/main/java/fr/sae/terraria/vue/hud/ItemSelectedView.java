package fr.sae.terraria.vue.hud;

import fr.sae.terraria.modele.TileMaps;
import fr.sae.terraria.modele.entities.blocks.Dirt;
import fr.sae.terraria.modele.entities.blocks.Stone;
import fr.sae.terraria.modele.entities.entity.StowableObjectType;
import fr.sae.terraria.modele.entities.items.*;
import fr.sae.terraria.modele.entities.player.Player;
import fr.sae.terraria.vue.View;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;



public class ItemSelectedView
{
    private ImageView itemSelectedImgView;

    private Image stoneItemImg;
    private Image dirtItemImg;
    private Image coalItemImg;
    private Image fibreItemImg;
    private Image ironItemImg;
    private Image pierreItemImg;
    private Image silexItemImg;
    private Image meatItemImg;
    private Image woodItemImg;

    private Pane display;


    public ItemSelectedView(Pane display, Player player, double scaleMultiplicatorWidth, double scaleMultiplicatorHeight)
    {
        this.display = display;

        int widthTile = (int) (scaleMultiplicatorWidth * TileMaps.TILE_DEFAULT_SIZE);
        int heightTile = (int) (scaleMultiplicatorHeight * TileMaps.TILE_DEFAULT_SIZE);
        int widthItem = widthTile/2;
        int heightItem = heightTile/2;

        this.itemSelectedImgView = new ImageView();
        this.dirtItemImg = View.loadAnImage("tiles/floor-top.png", widthItem, heightItem);
        this.stoneItemImg = View.loadAnImage("tiles/rock-fill.png", widthItem, heightItem);
        this.coalItemImg = View.loadAnImage("loots/coal.png", widthItem, heightItem);
        this.fibreItemImg = View.loadAnImage("loots/fibre.png", widthItem, heightItem);
        this.ironItemImg = View.loadAnImage("loots/iron.png", widthItem, heightItem);
        this.pierreItemImg = View.loadAnImage("loots/pierre.png", widthItem, heightItem);
        this.silexItemImg = View.loadAnImage("loots/silex.png", widthItem, heightItem);
        this.meatItemImg = View.loadAnImage("loots/meat.png", widthItem, heightItem);
        this.woodItemImg = View.loadAnImage("loots/wood.png", widthItem, heightItem);

        player.getInventory().posCursorHorizontallyInventoryBarProperty().addListener((obs, oldItemSelected, newItemSelected) -> {
            StowableObjectType item = player.getItemSelected();

            if (item instanceof Dirt)
                itemSelectedImgView.setImage(dirtItemImg);
            else if (item instanceof Stone)
                itemSelectedImgView.setImage(stoneItemImg);
            else if (item instanceof Coal)
                itemSelectedImgView.setImage(coalItemImg);
            else if (item instanceof Fiber)
                itemSelectedImgView.setImage(fibreItemImg);
            else if (item instanceof Iron)
                itemSelectedImgView.setImage(ironItemImg);
            else if (item instanceof Pierre)
                itemSelectedImgView.setImage(pierreItemImg);
            else if (item instanceof Silex)
                itemSelectedImgView.setImage(silexItemImg);
            else if (item instanceof Meat)
                itemSelectedImgView.setImage(meatItemImg);
            else if (item instanceof Wood)
                itemSelectedImgView.setImage(woodItemImg);
            else itemSelectedImgView.setImage(null);
        });

        display.addEventFilter(MouseEvent.MOUSE_MOVED, mouse -> {
            itemSelectedImgView.setX(mouse.getX());
            itemSelectedImgView.setY(mouse.getY());
        });
    }

    public void display() { display.getChildren().add(itemSelectedImgView); }
}