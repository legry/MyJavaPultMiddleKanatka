
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.IntStream;

import static java.lang.Math.round;

public class Oscil extends Canvas {
    private int[] points;

    Oscil(int[] points) {
        this.points = points;
        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Oscil.super.repaint();
            }
        };
        timer.schedule(task, 100, 100);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        Image buffer = createImage(getWidth(), getHeight());
        Graphics2D g2d = (Graphics2D)buffer.getGraphics();
        g2d.drawPolyline(IntStream.rangeClosed(0, points.length-1).map(x -> x + 10).toArray(),
                IntStream.of(points).map(x -> x + round(super.getHeight()/2)).toArray(),
                points.length);

        g.drawImage(buffer, 0, 0, null);


    }

    public void addPoints(int[] points) {
        this.points = ((this.points.length + points.length) <= super.getWidth()) ?
                IntStream.concat(IntStream.of(this.points), IntStream.of(points)).toArray() :
                IntStream.concat(IntStream.of(this.points).skip(points.length), IntStream.of(points)).toArray();
    }
}
