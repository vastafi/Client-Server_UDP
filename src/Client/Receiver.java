package Client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class Receiver {

    private JFrame frame;
    static JLabel jLabel;
    Client receiver;

    public static void main(String[] args) throws IOException {
        EventQueue.invokeLater(() -> {
            try {
                Receiver window = new Receiver();
                window.frame.setVisible(true);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public Receiver() {
        init();
    }

    private void init() {
        frame = new JFrame();
        frame.setBounds(120, 120, 1024, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                try {
                    if (Client.connected) {
                        receiver.sendFin();
                    }
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            }
        });

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu menu = new JMenu("File");
        menuBar.add(menu);

        JMenuItem connect = new JMenuItem("Connect");
        connect.addActionListener(actionEvent -> {
            try {
                String address = JOptionPane.showInputDialog("Sender's IP");
                String name = JOptionPane.showInputDialog("Your name");
                if (address.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Address field cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                } else if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(null, "Name field cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    receiver = new Client(name, address);
                }

            } catch (Exception exception) {
                JOptionPane.showMessageDialog(null, "The IP you entered is invalid!", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        });
        menu.add(connect);

        jLabel = new JLabel();
        frame.getContentPane().add(jLabel, BorderLayout.CENTER);
    }

}