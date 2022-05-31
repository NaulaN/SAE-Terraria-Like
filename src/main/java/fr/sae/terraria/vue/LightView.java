package fr.sae.terraria.vue;

import fr.sae.terraria.modele.Clock;
import fr.sae.terraria.modele.TileMaps;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

public class LightView {
    private static final double opacityIter = 0.0017;

    private Clock clock;
    private Pane filterPane;
    private SimpleDoubleProperty opacityNightAir;
    private SimpleDoubleProperty opacityNightFade;
    private Shape air;
    private Shape fade;
    private Shape tunnel;

    public LightView(Clock clock, Pane filterPane, double scaleMultiplicatorHeight, double scaleMultiplicatorWidth) {

        this.clock = clock;
        this.filterPane = filterPane;
        opacityNightAir = new SimpleDoubleProperty(0.0);
        opacityNightFade = new SimpleDoubleProperty(0.8143);

        air = new Rectangle(scaleMultiplicatorWidth* TileMaps.TILE_DEFAULT_SIZE*30 ,scaleMultiplicatorHeight*TileMaps.TILE_DEFAULT_SIZE*19);
        fade =  new Rectangle(scaleMultiplicatorWidth* TileMaps.TILE_DEFAULT_SIZE*30,scaleMultiplicatorHeight*TileMaps.TILE_DEFAULT_SIZE+1);
        tunnel = new Rectangle(scaleMultiplicatorWidth* TileMaps.TILE_DEFAULT_SIZE*30,scaleMultiplicatorHeight*TileMaps.TILE_DEFAULT_SIZE);

        air.setFill(Color.web("#0d0d38"));
        Stop[] stops = new Stop[] { new Stop(0,new Color(0,0,0,0) ), new Stop(1, Color.web("#0d0d38"))};
        LinearGradient lg1 = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        fade.setFill(lg1);

        tunnel.setFill(Color.web("#0d0d38"));

        fade.setLayoutY(scaleMultiplicatorHeight*TileMaps.TILE_DEFAULT_SIZE*14);
        tunnel.setLayoutY(scaleMultiplicatorHeight*TileMaps.TILE_DEFAULT_SIZE*15);

        tunnel.opacityProperty().bind(opacityNightFade);
        fade.opacityProperty().bind(opacityNightFade);
        air.opacityProperty().bind(opacityNightAir);
        filterPane.getChildren().addAll(air,fade,tunnel);
    }

    public void setLightElements() {
        clock.minutesProperty().addListener(((obs, oldV, newV) -> updateOpacity(newV.intValue())));
    }

    private void updateOpacity(int minutes) {
        if (minutes >= Clock.FOUR_PM_INGAME || minutes <= Clock.EIGHT_AM_INGAME) {
            if (minutes >= Clock.FOUR_PM_INGAME && minutes < Clock.ONE_DAY_INGAME) {
                opacityNightAir.setValue(opacityNightAir.getValue()+opacityIter);
                opacityNightFade.setValue(opacityNightFade.getValue()-opacityIter);
            }
            else if (minutes > Clock.MIDNIGHT_INGAME && minutes <= Clock.EIGHT_AM_INGAME) {
                opacityNightAir.setValue(opacityNightAir.getValue()-opacityIter);
                opacityNightFade.setValue(opacityNightFade.getValue()+opacityIter);
            }
        }
    }
}
