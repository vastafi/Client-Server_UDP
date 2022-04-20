package Client;

import Server.Listener;
import Server.ServerSender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class Sender {
    private JFrame frame;
    public static JLabel jLabel;

    Listener listener;
    ServerSender sender;

    public static void main(String[] args) throws IOException {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    Sender sender = new Sender();
                    sender.frame.setVisible(true);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    public Sender() {
        init();
    }

    private void init() {
        frame = new JFrame();
        frame.setBounds(120, 120, 1024, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        jLabel = new JLabel();
        frame.getContentPane().add(jLabel, BorderLayout.CENTER);

        JMenuBar jMenuBar = new JMenuBar();
        frame.setJMenuBar(jMenuBar);

        JMenu jMenu = new JMenu("Share");
        jMenuBar.add(jMenu);

        JMenuItem Start = new JMenuItem("Share Screen");
        Start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    listener = new Listener();
                    sender = new ServerSender();
                    JOptionPane.showMessageDialog(null, "Screen casting has started successfully.You can minimize the screen! ", "Successful", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception exception) {
                    JOptionPane.showMessageDialog(null, "Something is wrong!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        jMenu.add(Start);
    }
}