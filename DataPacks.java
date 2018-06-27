import java.util.ArrayList;

import static java.lang.Math.round;

class DataPacks {
    private boolean begOk = false, endOk = false;
    private ArrayList<Byte> bytes = new ArrayList<>(1);
    private ArrayList<ArrayList<Integer>> curves;

    DataPacks(ArrayList<ArrayList<Integer>> curves) {
        this.curves = curves;
    }

    void addByte(byte bt) {
        if (bt == '/') {
            if (!begOk) {
                begOk = true;
            } else {
                if (!endOk) {
                    endOk = true;
                    for (int i = 0; i < bytes.size(); i += 2) {
                        if ((curves.size() - 1) < round(i/2)) {
                            ArrayList<Integer> curve = new ArrayList<>(1);
                            curve.add((bytes.get(i) << 8) + bytes.get(i + 1));
                            curves.add(curve);
                        } else {
                            curves.get(round(i/2)).add((bytes.get(i) << 8) + bytes.get(i + 1));
                        }
                    }
                }
            }
        } else if (begOk) {
            this.bytes.add(bt);
        }
    }

    boolean isEndOk() {
        return endOk;
    }
}
