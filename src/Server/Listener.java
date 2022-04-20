package Server;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class Listener implements Runnable {

    static DatagramSocket datagramSocket = null;
    static ArrayList<String> arrayList = new ArrayList<String>();
    static HashMap<String, Integer> receiversPorts = new HashMap<String, Integer>();
    static HashMap<String, InetAddress> receiverAddress = new HashMap<String, InetAddress>();

    public Listener() throws SocketException, UnknownHostException {
        datagramSocket = new DatagramSocket(8086);
        new Thread(this).start();
    }

    public void requestHandler(int port, String name, InetAddress address) {
        if (receiversPorts.containsKey(name)) {
            System.out.println("Warning!" + name + " is already connected to the server.");
            ByteBuffer sendData = ByteBuffer.allocate(4);
            sendData.putInt(500);
            DatagramPacket sendPacket = new DatagramPacket(sendData.array(), sendData.capacity(), address, port);
            try {
                datagramSocket.send(sendPacket);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        } else {
            System.out.println(name + " connected to the server.");
            ByteBuffer sendData = ByteBuffer.allocate(4);
            sendData.putInt(200);
            DatagramPacket sendPacket = new DatagramPacket(sendData.array(), sendData.capacity(), address, port);
            try {
                datagramSocket.send(sendPacket);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            byte[] bytes = new byte[100];
            DatagramPacket receivePacket = new DatagramPacket(bytes, bytes.length);
            try {
                datagramSocket.receive(receivePacket);
            } catch (IOException exception) {
                exception.printStackTrace();
            }
            int port = receivePacket.getPort();
            String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
            InetAddress address = receivePacket.getAddress();
            System.out.println("Recceived: " + sentence + " from " + port);
            String[] message = sentence.split(" ");
            if (message[0].equals("Hello")) {
                requestHandler(port,message[1], address);
            } else if (message[0].equals("Ready")) {
                receiversPorts.put(message[1],port);
                receiverAddress.put(message[1],address);
                arrayList.add(message[1]);
            }
        }
    }
}


