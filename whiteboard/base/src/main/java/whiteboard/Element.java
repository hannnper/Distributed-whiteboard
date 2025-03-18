// Element: class for communicating whiteboard actions
package whiteboard;

import java.io.Serializable;
import java.awt.Color;

public class Element implements Serializable {
    public String tool;
    public int x1, x2, y1, y2;
    public int size;
    public Color drawColor;
    public Color fillColor;
    public String inputText;

    public Element(String tool, int x1, int y1, int x2, int y2, int size, Color drawColor, Color fillColor, String inputText) {
        this.tool = tool;
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.size = size;
        this.drawColor = drawColor;
        this.fillColor = fillColor;
        this.inputText = inputText;
    }
}
