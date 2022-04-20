package Client;

import Server.ServerListener;
import Server.ServerSender;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class ClientSender {
    private JFrame frame;
    public static JLabel jLabel;

    ServerListener listener;
    ServerSender sender;

    public static void main(String[] args) throws IOException {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    ClientSender clientSender = new ClientSender();
                    clientSender.frame.setVisible(true);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    public ClientSender() {
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

        JMenu jMenu = new JMenu("File");
        jMenuBar.add(jMenu);

        JMenuItem Start = new JMenuItem("Share Screen");
        Start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    listener = new ServerListener();
                    sender = new ServerSender();
                    JOptionPane.showMessageDialog(null, "You can minimize the screen, screen casting has started successfully. ", "Successful", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(null, "Something is wrong!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        jMenu.add(Start);
    }
}