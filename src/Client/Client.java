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
        System.out.println("Running...");

        datagramSocket = new DatagramSocket();
        datagramSocket.setSoTimeout(15);

        inetAddress = InetAddress.getByName(address);
        send();
        setPath();
        connected = true;
        datagramSocket.setSoTimeout(60);

        new Thread(this).start();
    }

    public boolean send() throws IOException {
        if (counter == 280) {
            JOptionPane.showMessageDialog(null, "Server is down! Exiting...", "Alert", JOptionPane.INFORMATION_MESSAGE);
            System.exit(0);
        }
        counter++;
        byte[] bytes = new byte[500];
        if (isPresent == true) {
            name = JOptionPane.showInputDialog("You name");
        }
        int capacity = 4;
        String sentence = "Hello" + name;
        ByteBuffer byteBuffer = ByteBuffer.allocate(sentence.getBytes().length);
        byteBuffer.put(sentence.getBytes());
        DatagramPacket datagramPackets = new DatagramPacket(byteBuffer.array(), byteBuffer.capacity(), inetAddress, 9876);
        datagramSocket.send(datagramPackets);
        byteBuffer = ByteBuffer.allocate(capacity);
        byteBuffer.clear();
        byteBuffer.rewind();
        DatagramPacket datagram = new DatagramPacket(bytes, bytes.length);

        try {
            datagramSocket.receive(datagram);

            for (int i = 0; i < 4; i++) {
                byteBuffer.put(datagram.getData()[i]);
            }
            int result = byteBuffer.getInt(0);
            if (result == 200) {

            } else if (result == 500) {
                isPresent = true;
                send();
                return false;
            }

        } catch (Exception exception) {
            send();
            return false;
        }

        byteBuffer.clear();
        byteBuffer.rewind();

        sentence = "ready" + name;
        byteBuffer = ByteBuffer.allocate(sentence.getBytes().length);
        byteBuffer.put(sentence.getBytes());

        datagramPackets = new DatagramPacket(byteBuffer.array(), byteBuffer.capacity(), inetAddress, 9876);
        datagramSocket.send(datagramPackets);

        return true;
    }

    public void setPath() {
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        File[] files = fileSystemView.getRoots();
        this.path = fileSystemView.getHomeDirectory().toString();
    }


    public void sendFin() throws IOException {
        String sentence = "fin" + name;
        ByteBuffer data = ByteBuffer.allocate(sentence.getBytes().length);
        data.put(sentence.getBytes());
        DatagramPacket sendPacket = new DatagramPacket(data.array(), data.capacity(), inetAddress, 9876);
        datagramSocket.send(sendPacket);
    }

    public void receive() throws IOException {
        timeCounter++;
        byte[] receiveData = new byte[65000];
        Checksum checkSum = new CRC32();
        int expectedNumber = 1;
        int numberOfPackets = 0;
        ByteBuffer buffer = null;

        while (true) {
            DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                datagramSocket.receive(receivedPacket);
                timeCounter = 0;
            } catch (Exception exception) {
                if (timeCounter == 10000) {
                    JOptionPane.showMessageDialog(null, "Server is down! Exit!", "Information", JOptionPane.INFORMATION_MESSAGE);
                    System.exit(0);
                }
                return;
            }

            int packetLength = receivedPacket.getLength();

            ByteBuffer data = ByteBuffer.allocate(packetLength);
            data.put(receiveData, 0, packetLength);
            data.rewind();

            long checkSumVal = data.getLong();
            int receivedNumber = data.getInt();

            if (receivedNumber == -1) {
                checkSum.reset();
                checkSum.update(data.array(), 8, data.capacity() - 8);
                long calculatedChecksum = checkSum.getValue();

                if ((calculatedChecksum == checkSumVal) && (numberOfPackets + 1 == expectedNumber)) {
                    break;
                }
                return;
            }

            int loadSize = data.getInt();
            int totalSize = data.getInt();

            data.rewind();
            checkSum.reset();
            checkSum.update(data.array(), 20, loadSize);
            long calculatedChecksum = checkSum.getValue();

            if (calculatedChecksum != checkSumVal) {
                System.out.println("Expected: " + calculatedChecksum);
                System.out.println("Expected: " + checkSumVal);
                return;
            }
            if (receivedNumber != expectedNumber) {
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
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buffer.array());
            image = ImageIO.read(byteArrayInputStream);
            Receiver.jLabel.setIcon(new ImageIcon(image));
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                receive();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }
}

