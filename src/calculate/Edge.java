/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package calculate;

import java.io.Serializable;
import javafx.scene.paint.Color;

/**
 *
 * @author Peter Boots
 */
public class Edge implements Serializable{
    public double X1, Y1, X2, Y2, hue, saturation, brightness;
    
    public Edge(double X1, double Y1, double X2, double Y2, double hue, double saturation, double brightness) {
        this.X1 = X1;
        this.Y1 = Y1;
        this.X2 = X2;
        this.Y2 = Y2;
        this.hue = hue;
        this.saturation = saturation;
        this.brightness = brightness;
    }
    
    @Override
    public String toString(){
        return this.X1 + "," + this.X2 + "," + this.Y1 + "," + this.Y2 + "," + this.hue + "," + this.saturation + "," + this.brightness;
    }
}
