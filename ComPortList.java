import jssc.SerialPortList;

import javax.swing.*;
import java.util.Arrays;

class ComPortList extends JComboBox<String> {
    ComPortList() {
        super();
        Thread thread = new Thread(() -> {
            String[] newList, oldList = new String[0];
            while (true) {
                newList = SerialPortList.getPortNames();
                if (!Arrays.equals(newList, oldList)) {
                    this.removeAllItems();
                    oldList = newList;
                    for (char i = 0; i < newList.length; i++) {
                        this.addItem(oldList[i]);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
