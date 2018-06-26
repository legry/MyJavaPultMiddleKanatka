
import javax.swing.plaf.ColorChooserUI;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.IntStream;

import static java.lang.Math.round;

public class Oscil extends Canvas {
    private ArrayList<ArrayList<Integer>> curves;
    private int razv;
    private Image buffer;
    private ArrayList<Color> colors = new ArrayList<>(3);

    Oscil() {
        colors.add(Color.GREEN);
        colors.add(Color.RED);
        colors.add(Color.BLUE);
    }

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
            curves.stream().parallel().forEach(x -> {
                ArrayList<Integer> curve = iCurv.next();
                curve.addAll(x);
                if ((curve.size() - 1) * razv > super.getWidth())
                    curve.removeAll(curve.subList(0, round((float) ((curve.size() - 1) * razv - super.getWidth()) / razv) - 1));

            });
        }
        buffer = createImage(getWidth(), getHeight());
        Graphics2D g2d = (Graphics2D) buffer.getGraphics();
        g2d.drawRect(0, 0, super.getWidth() - 1, super.getHeight() - 1);
        Iterator<Color> iterColors = colors.iterator();
        curves.stream().sequential().
                forEach(x -> {
                    g2d.setColor(iterColors.next());
                    g2d.drawPolyline(IntStream.rangeClosed(0, x.size() - 1).map(xx -> xx * razv).toArray(),
                            x.stream().mapToInt(xx -> xx + round(super.getHeight() / 2)).toArray(), x.size());

                });
        Oscil.super.repaint();
    }

    void setRazv(int razv) {
        this.razv = (razv == 0) ? 1 : razv;
    }
}
