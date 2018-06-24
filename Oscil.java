
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.IntStream;

import static java.lang.Math.round;

public class Oscil extends Canvas {
    private ArrayList<ArrayList<Integer>> curves;
    private int razv;
    private Image buffer;

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(buffer, 0, 0, null);
    }

    void addPoints(ArrayList<ArrayList<Integer>> curves) {
        if (this.curves == null) {
            this.curves = new ArrayList<>(1);
            this.curves.addAll(curves);
        } else {
            Iterator<ArrayList<Integer>> iCurv = this.curves.iterator();
            curves.stream().sequential().forEach(x -> {
                ArrayList<Integer> curve = iCurv.next();
                curve.addAll(x);
                if (curve.size() * razv > super.getWidth())
                    curve.removeAll(curve.subList(0, round((float) (curve.size() * razv - super.getWidth()) / razv)));
            });
        }
        buffer = createImage(getWidth(), getHeight());
        Graphics2D g2d = (Graphics2D) buffer.getGraphics();
        g2d.drawRect(0, 0, super.getWidth() - 1, super.getHeight() - 1);
        curves.stream().sequential().
                forEach(x -> g2d.drawPolyline(IntStream.rangeClosed(0, x.size() - 1).map(xx -> xx * razv).toArray(),
                        x.stream().mapToInt(xx -> xx + round(super.getHeight() / 2)).toArray(), x.size()));
        Oscil.super.repaint();
    }

    void setRazv(int razv) {
        this.razv = (razv == 0) ? 1 : razv;
    }
}
