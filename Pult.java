import jssc.SerialPort;
import jssc.SerialPortException;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

class Pult extends JFrame {
    private SerialPort serialPort;
    private ComPortList comPortList = new ComPortList();
    private JSlider ust = new JSlider(JSlider.HORIZONTAL, 10, 50, 20);
    private JRadioButton[] radioButton = new JRadioButton[3];
    private byte[] data = new byte[2];
    private boolean setOk = false;
    Pult() throws HeadlessException {
        super("Пульт");
        this.setBounds(300, 300, 550, 400);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new MigLayout());
        add(comPortList, "gap");
        JButton openport = new JButton("Открыть порт");
        JLabel amperaj = new JLabel("0.0");
        openport.addActionListener(e -> {
            serialPort = new SerialPort((String) comPortList.getSelectedItem());
            try {
                serialPort.openPort();
                serialPort.setParams(
                        SerialPort.BAUDRATE_38400,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);
                serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
                serialPort.addEventListener(serialPortEvent -> {
                    if (serialPortEvent.isRXCHAR() && (serialPortEvent.getEventValue() == 3)) {
                        try {
                            byte[] inData = serialPort.readBytes(3);
                            try {
                                int amps = (new DataInputStream(new ByteArrayInputStream(inData, 0, 2))).readUnsignedShort();
                                amperaj.setText(String.valueOf(amps));
                                setOk = true;
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        } catch (SerialPortException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            } catch (SerialPortException e1) {
                e1.printStackTrace();
            }
        });
        add(openport, "gap 10mm");
        JButton closeport = new JButton("Закрыть порт");
        closeport.addActionListener(e -> {
            if (serialPort != null) {
                if (serialPort.isOpened()) {
                    try {
                        serialPort.closePort();
                    } catch (SerialPortException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        add(closeport, "gap, wrap");
        Font font = new Font("Times New Roman", Font.PLAIN, 72);
        amperaj.setFont(font);
        add(amperaj, "span, align 50%");
        ust.setMinorTickSpacing(1);
        ust.setMajorTickSpacing(5);
        ust.setPaintTicks(true);
        ust.setPaintLabels(true);
        add(ust, "span, align 50%");
        JPanel hodPanel = new JPanel();
        hodPanel.setLayout(new MigLayout());
        String[] hodTitles = new String[]{"Вперед", "Нейтраль", "Назад"};
        ActionListener hodListener = e -> {
                data[1] = (byte) ((radioButton[0].isSelected()) ? data[1] | (1 << 1) : data[1] & ~(1 << 1));
                data[1] = (byte) ((radioButton[2].isSelected()) ? data[1] | (1 << 2) : data[1] & ~(1 << 2));

        };
        ButtonGroup hod = new ButtonGroup();
        for (int i = 0; i < 3; i++) {
            radioButton[i] = new JRadioButton(hodTitles[i]);
            if (i == 1) radioButton[i].setSelected(true);
            hod.add(radioButton[i]);
            if (i < 2)
            hodPanel.add(radioButton[i], "wrap");
            else hodPanel.add(radioButton[i]);
            radioButton[i].addActionListener(hodListener);
        }
        JPanel valanPanel = new JPanel();
        valanPanel.setLayout(new MigLayout());
        JButton start = new JButton("Пуск");
        start.addActionListener(e -> data[1] |= 1);
        valanPanel.add(start, "wrap");
        JButton stop = new JButton("Стоп");
        stop.addActionListener(e -> data[1] &= ~1);
        valanPanel.add(stop);
        add(hodPanel, "span 2");
        add(valanPanel, "span 2");
        Timer timer = new Timer(true);
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (serialPort != null) {
                    if (serialPort.isOpened()) {
                        data[0] = (byte) ust.getValue();
                        if (!setOk) {
                            data[1] |= 0x06;
                        } else {
                            setOk = false;
                        }
                        try {
                            serialPort.writeBytes(data);
                        } catch (SerialPortException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        timer.schedule(timerTask, 0, 1000);
        pack();
        setResizable(false);
        setVisible(true);
    }

}
