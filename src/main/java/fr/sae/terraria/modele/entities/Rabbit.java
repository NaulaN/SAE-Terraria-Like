package fr.sae.terraria.modele.entities;

import javafx.beans.property.SimpleDoubleProperty;

import fr.sae.terraria.modele.entities.entity.Entity;


public class Rabbit extends Entity
{
    private int frame;


    public Rabbit(int x, int y, double pv, double velocity)
    {
        super(x, y);

        this.pv = new SimpleDoubleProperty(pv);
        this.velocity = velocity;
    }

    public void updates()
    {
        this.setX(this.x.get() + this.offset[0] * this.velocity);
        this.setY(this.y.get() + this.offset[1] * this.velocity);

        if(frame == 4) {
            frame = 0;
        }//TODO faire les frame

    }

}




