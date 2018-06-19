
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

import static java.lang.Math.*;

public class Oscil extends Canvas {
    private int[] xp, yp;

    Oscil(int[] xp, int[] yp) {
        this.xp = xp;
        this.yp = yp;
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Oscil.super.repaint();
            }
        };
        timer.schedule(task, 500, 500);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawPolyline(xp, yp, xp.length);
    }

    public void addPoints(int[] xp, int[] yp) {
        this.xp = xp;
        this.yp = yp;
    }
}
