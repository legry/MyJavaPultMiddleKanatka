
import java.awt.*;
import java.util.stream.Stream;

public class Oscil extends Canvas {
    @Override
    public void paint(Graphics g) {
        //super.paint(g);
        g.drawPolyline();
        Dimension size = size();
        g.drawRect(0,0, size.width-1, size.height-1);
    }
}
