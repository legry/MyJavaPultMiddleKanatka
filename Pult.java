import jssc.SerialPort;
import jssc.SerialPortException;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.*;
import java.util.Timer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.Math.*;


class Pult extends JFrame {
    private SerialPort serialPort;
    private ComPortList comPortList = new ComPortList();
    private JSlider ust = new JSlider(JSlider.HORIZONTAL, 5, 50, 20);
    private JRadioButton[] radioButton = new JRadioButton[3];
    volatile private byte[] data = new byte[2];
    private Random rnd = new Random();
    //volatile private boolean setOk = true, mseUst = false;
    Pult() throws HeadlessException {
        super("Пульт");
        this.setBounds(300, 300, 550, 400);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new MigLayout());
        add(comPortList, "gap");
        JButton openport = new JButton("Открыть порт");
        JButton closeport = new JButton("Закрыть порт");
        closeport.setEnabled(false);
        //JLabel amperaj = new JLabel("0.0");
        byte[] dataAccume = new byte[0];
        ArrayList<ArrayList<Integer>> curves = new ArrayList<>(1);
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
                    if (serialPortEvent.isRXCHAR()) {
                        try {
                            byte[] inData = serialPort.readBytes(serialPortEvent.getEventValue());

                        } catch (SerialPortException e1) {
                            e1.printStackTrace();
                        }
                    }
                });
                openport.setEnabled(false);
                closeport.setEnabled(true);
            } catch (SerialPortException e1) {
                e1.printStackTrace();
            }
        });
        add(openport, "gap 10mm");
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
        /*Font font = new Font("Times New Roman", Font.PLAIN, 72);
        amperaj.setFont(font);*/

        JSlider ustfreq = new JSlider(JSlider.HORIZONTAL, 1, 10, 5);
        ustfreq.setMinorTickSpacing(1);
        ustfreq.setMajorTickSpacing(1);
        ustfreq.setPaintTicks(true);
        ustfreq.setPaintLabels(true);
        ustfreq.addMouseWheelListener(e -> ustfreq.setValue(ustfreq.getValue() + e.getWheelRotation()));
        JPopupMenu popup;
        popup = new JPopupMenu();
        popup.add(new JMenuItem("Свойства графиков"));
        Oscil oscil = new Oscil();
        oscil.setSize(getWidth()-100,100);
        JPanel panOscil = new JPanel(new MigLayout());
        panOscil.setSize(getWidth(), 150);
        panOscil.add(oscil, "align 50% 50%");
        panOscil.setComponentPopupMenu(popup);
        Border etched = BorderFactory.createEtchedBorder();
        Border titled = BorderFactory.createTitledBorder(etched, "Осциллограф");
        panOscil.setBorder(titled);
        add(panOscil, "span, align 50%");
        ust.setMinorTickSpacing(1);
        ust.setMajorTickSpacing(5);
        ust.setPaintTicks(true);
        ust.setPaintLabels(true);
        ust.addMouseWheelListener(e -> ust.setValue(ust.getValue() + e.getWheelRotation()));

        JLabel ampl = new JLabel("Изменение амплитуды");
        ampl.setLabelFor(ust);
        add(ampl, "wrap");
        add(ust, "span, align 50%");

        JLabel freq = new JLabel("Изменение частоты");
        freq.setLabelFor(ustfreq);
        add(freq, "wrap");
        add(ustfreq, "span, align 50%");

        JSlider ustrazv = new JSlider(JSlider.HORIZONTAL, 0, 100, 5);
        ustrazv.setMinorTickSpacing(5);
        ustrazv.setMajorTickSpacing(10);
        ustrazv.setPaintTicks(true);
        ustrazv.setPaintLabels(true);
        ustrazv.addMouseWheelListener(e -> ustrazv.setValue(ustrazv.getValue() + e.getWheelRotation()));

        JLabel razr = new JLabel("Изменение развертки");
        razr.setLabelFor(ustrazv);
        add(razr, "wrap");
        add(ustrazv, "span, align 50%");

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
/*                if (serialPort != null) {
                    if (serialPort.isOpened()) {
                        data[0] = (byte) floor(150*ust.getValue()/50);
                        try {
                            if (setOk) {
                                serialPort.writeBytes(data);
                            } else {
                                setOk = false;
                            }
                        } catch (SerialPortException e) {
                            e.printStackTrace();
                        }
                    }
                }*/
                oscil.setRazv(ustrazv.getValue());
                ArrayList<ArrayList<Integer>> curves = new ArrayList<>();

                curves.add(IntStream.rangeClosed(0, 99).
                        map(x -> (int) round(abs(ust.getValue()*sin(2*PI*ustfreq.getValue()*((double) x/100))))).
                        boxed().
                        collect(Collectors.toCollection(ArrayList::new)));
                curves.add(IntStream.rangeClosed(0, 99).
                        map(x -> (int) round(ust.getValue()*cos(2*PI*ustfreq.getValue()*((double) x/100)))).
                        boxed().
                        collect(Collectors.toCollection(ArrayList::new)));
                curves.add(IntStream.rangeClosed(0, 99).
                        map(x -> (int) round(ust.getValue()*sin(2*PI*(ustfreq.getValue()/2)*((double) x/100)))).
                        boxed().
                        collect(Collectors.toCollection(ArrayList::new)));
                oscil.addPoints(curves);
            }
        };
        timer.schedule(timerTask, 0, 300);
        pack();
        setResizable(false);
        setVisible(true);
    }

}
