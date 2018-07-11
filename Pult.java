import jssc.SerialPort;
import jssc.SerialPortException;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

import static java.lang.Math.round;

class Pult extends JFrame {
    private int amp;
    private byte[] inData = new byte[3];
    private SerialPort serialPort;
    private ComPortList comPortList = new ComPortList();
    private JSlider ust = new JSlider(JSlider.HORIZONTAL, 5, 50, 20);
    private JRadioButton[] radioButton = new JRadioButton[3];
    private byte data;
    private boolean wrtOk = false;
    private float val;
    private int ampval;
    private Properties properties = new Properties();
    private OutputStream os = null;

    Pult() throws HeadlessException {
        super("Пульт");
        this.setBounds(300, 300, 550, 400);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new MigLayout());
        add(comPortList, "gap");
        JButton openport = new JButton("Открыть порт");
        JButton closeport = new JButton("Закрыть порт");
        JLabel amperaj = new JLabel("0.0");
        JPopupMenu ampCorr = new JPopupMenu();
        InputStream is = null;
        File file = new File("configPult.txt");
        try {
            if (file.exists() || file.createNewFile()) {
                is = new FileInputStream(file);
                properties.load(is);
                if (properties.stringPropertyNames().isEmpty()) {
                    properties.setProperty("val", String.valueOf(val));
                    properties.setProperty("ampval", String.valueOf(ampval));
                } else {
                    val = Float.parseFloat(properties.getProperty("val"));
                    ampval = Integer.parseInt(properties.getProperty("ampval"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        ampCorr.add("Калибровка").addActionListener(e -> {
            String res = JOptionPane.showInputDialog("Введите значения", 0);
            val = Float.parseFloat(res);
            ampval = amp;
            try {
                os = new FileOutputStream("configPult.txt");
                properties.setProperty("val", String.valueOf(val));
                properties.setProperty("ampval", String.valueOf(ampval));
                properties.store(os, "");
            } catch (IOException e1) {
                e1.printStackTrace();
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
            }
        });
        amperaj.setComponentPopupMenu(ampCorr);
        Thread wrt = new Thread(() -> {
            while (true) {
                if (wrtOk) {
                    try {
                        amp = new
                                DataInputStream(new ByteArrayInputStream(inData, 0, 2)).readUnsignedShort();

                        amperaj.setText(String.format("%.3f", (float) (amp * val / ampval)));
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    int minustamp = round((float) (ust.getValue() - 0.5) * ampval / val);
                    int maxustamp = round((float) (ust.getValue() + 0.5) * ampval / val);
                    data = (byte) ((radioButton[0].isSelected()) ? data | (1 << 1) : data & ~(1 << 1));
                    data = (byte) ((radioButton[2].isSelected()) ? data | (1 << 2) : data & ~(1 << 2));
                    try {
                        Thread.sleep(10);
                        if (serialPort.isOpened()) {
                            serialPort.writeByte((byte) (minustamp >> 8));
                            serialPort.writeByte((byte) minustamp);
                            serialPort.writeByte((byte) (maxustamp >> 8));
                            serialPort.writeByte((byte) maxustamp);
                            serialPort.writeByte(data);
                        }
                    } catch (SerialPortException | InterruptedException e) {
                        e.printStackTrace();
                    }
                    wrtOk = false;
                }
            }
        });
        wrt.setDaemon(true);
        wrt.start();
        closeport.setEnabled(false);
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
                    if (serialPortEvent.isRXCHAR() || (serialPortEvent.getEventValue() == 3)) {
                        try {
                            inData = serialPort.readBytes(3);
                            wrtOk = true;
                        } catch (SerialPortException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
                wrtOk = true;
                openport.setEnabled(false);
                closeport.setEnabled(true);
            } catch (SerialPortException e1) {
                e1.printStackTrace();
            }
        });
        add(openport, "gap");
        closeport.addActionListener(e -> {
            if (serialPort != null) {
                if (serialPort.isOpened()) {
                    try {
                        serialPort.closePort();
                        openport.setEnabled(true);
                        closeport.setEnabled(false);
                    } catch (SerialPortException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        add(closeport, "gap, wrap");
        Font font = new Font("Times New Roman", Font.PLAIN, 72);
        amperaj.setFont(font);
        add(amperaj, "span, align 50% 50%, wrap");
        ust.setMinorTickSpacing(1);
        ust.setMajorTickSpacing(5);
        ust.setPaintTicks(true);
        ust.setPaintLabels(true);
        ust.addMouseWheelListener(e -> ust.setValue(ust.getValue() + e.getWheelRotation()));
        add(ust, "span, align 50% 50%, wrap");
        JPanel hodPanel = new JPanel();
        hodPanel.setLayout(new MigLayout());
        String[] hodTitles = new String[]{"Вперед", "Нейтраль", "Назад"};
        ButtonGroup hod = new ButtonGroup();
        for (int i = 0; i < 3; i++) {
            radioButton[i] = new JRadioButton(hodTitles[i]);
            if (i == 1) radioButton[i].setSelected(true);
            hod.add(radioButton[i]);
            if (i < 2)
                hodPanel.add(radioButton[i], "wrap");
            else hodPanel.add(radioButton[i]);
        }
        JPanel valanPanel = new JPanel();
        valanPanel.setLayout(new MigLayout());
        JButton start = new JButton("Пуск");
        start.addActionListener(e -> data |= 1);
        valanPanel.add(start, "wrap");
        JButton stop = new JButton("Стоп");
        stop.addActionListener(e -> data &= ~1);
        valanPanel.add(stop);
        add(hodPanel, "span 2");
        add(valanPanel, "span 2");
        pack();
        setResizable(false);
        setVisible(true);
    }
}
