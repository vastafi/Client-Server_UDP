package Client;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class Client implements Runnable{
    InetAddress inetAddress = null;
    DatagramSocket datagramSocket = null;

    String name = null;
    boolean isPresent = false;
    int counter = 0;
    int timeCounter = 0;

    ArrayList<byte[]> list = new ArrayList<byte[]>();

    static boolean connected = false;
    BufferedImage image;
    static boolean imageFlag = false;
    String path;
    static boolean isBusy = false;

    public Client(String name, String address) throws IOException {
        this.name = name;
        System.out.println("Client running...");

        datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(35);

        inetAddress = InetAddress.getByName(address);

        send();
        setPath();

        connected = true;
        datagramSocket.setSoTimeout(100);

        new Thread(this).start();
    }

    public void setPath() {
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        File[] files = fileSystemView.getRoots();
        this.path = fileSystemView.getHomeDirectory().toString();
    }

    public boolean send() throws IOException {
        if (counter == 100) {
            JOptionPane.showMessageDialog(null, "Server is down! Exiting...", "Information", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
        counter++;

        byte[] bytes = new byte[64008];

        if (isPresent == true) {
            name = JOptionPane.showInputDialog("You name");
        }


        String sentence = "Hello " + name;

        ByteBuffer buffer = ByteBuffer.allocate(sentence.getBytes().length);
        buffer.put(sentence.getBytes());

        DatagramPacket sendPacket = new DatagramPacket(buffer.array(), buffer.capacity(), inetAddress, 9876);
        datagramSocket.send(sendPacket);

        buffer = ByteBuffer.allocate(4);
        buffer.clear();
        buffer.rewind();

        DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);

        try {
            datagramSocket.receive(datagramPacket);

            for (int i = 0; i < 4; i++) {
                buffer.put(datagramPacket.getData()[i]);
            }
            int result = buffer.getInt(0);
            if (result == 200) {

            } else if (result == 500) {
                isPresent = true;
                send();
                return false;
            }

        } catch (Exception ex) {
            send();
            return false;
        }

        buffer.clear();
        buffer.rewind();

        sentence = "ready" + name;
        buffer = ByteBuffer.allocate(sentence.getBytes().length);
        buffer.put(sentence.getBytes());

        sendPacket = new DatagramPacket(buffer.array(), buffer.capacity(), inetAddress, 9876);
        datagramSocket.send(sendPacket);

        return true;
    }

    public void sendFin() throws IOException {
        String sentence = "fin" + name;
        ByteBuffer data = ByteBuffer.allocate(sentence.getBytes().length);
        data.put(sentence.getBytes());
        DatagramPacket sendPacket = new DatagramPacket(data.array(), data.capacity(), inetAddress, 9876);
        datagramSocket.send(sendPacket);
    }

    public void receivePackets() throws IOException {
        timeCounter++;
        byte[] receiveData = new byte[64020];
        Checksum checksum = new CRC32();
        int expectedNumber = 1;
        int numberOfPackets = 0;
        ByteBuffer buffer = null;

        while (true) {

            DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                datagramSocket.receive(receivedPacket);
                timeCounter = 0;
            } catch (Exception ex) {
                if (timeCounter == 300) {
                    JOptionPane.showMessageDialog(null, "Server is down! Exit!", "Information", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
                return;
            }

            int packetLength = receivedPacket.getLength();

            ByteBuffer data = ByteBuffer.allocate(packetLength);
            data.put(receiveData, 0, packetLength);

            data.rewind();

            long checksumVal = data.getLong();
            int receivedChunkNumber = data.getInt();

            if (receivedChunkNumber == -1) {
                checksum.reset();
                checksum.update(data.array(), 8, data.capacity() - 8);
                long calculatedChecksum = checksum.getValue();

                if ((calculatedChecksum == checksumVal) && (numberOfPackets + 1 == expectedNumber)) {
                    break;
                }
                return;
            }

            int loadSize = data.getInt();
            int totalSize = data.getInt();

            data.rewind();
            checksum.reset();
            checksum.update(data.array(), 20, loadSize);
            long calculatedChecksum = checksum.getValue();

            if (calculatedChecksum != checksumVal) {
                System.out.println("Expected: " + calculatedChecksum);
                System.out.println("Expected: " + checksumVal);
                return;
            }
            if (receivedChunkNumber != expectedNumber) {
                System.out.println("Packet loss");
                return;
            }

            if (expectedNumber == 1) {
                buffer = ByteBuffer.allocate(totalSize);
                numberOfPackets = (totalSize / 64000) + 1;
            }
            data.rewind();
            buffer.put(data.array(), 20, loadSize);
            expectedNumber++;
            data.clear();
        }

        if (buffer != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(buffer.array());
            image = ImageIO.read(bais);
            ClientReceiver.jLabel.setIcon(new ImageIcon(image));
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                receivePackets();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}

