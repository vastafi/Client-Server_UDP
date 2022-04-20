package Server;

import Client.Sender;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ServerSender implements Runnable {

    private InetAddress inetAddress = null;
    ArrayList<ByteBuffer> list = new ArrayList<ByteBuffer>();

    public ServerSender() throws IOException {
        new Thread(this).start();
    }

    public BufferedImage scale(BufferedImage imgToScale) {
        BufferedImage resizeImage = new BufferedImage(1024, 728, imgToScale.getType());
        Graphics2D graphics = resizeImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(imgToScale, 0, 0, 1024, 728, 0, 0, imgToScale.getWidth(),
                imgToScale.getHeight(), null);
        graphics.dispose();

        return resizeImage;
    }

    private BufferedImage takeScreen() throws HeadlessException, AWTException {
        BufferedImage image = new Robot().createScreenCapture(new Rectangle(Toolkit.getDefaultToolkit().getScreenSize()));

        BufferedImage scaledImage = scale(image);
        return scaledImage;
    }

    private void splitScreenshot(BufferedImage screenshot) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ImageIO.write(screenshot, "png", byteArrayOutputStream);
        byteArrayOutputStream.flush();

        byte[] buffer = byteArrayOutputStream.toByteArray();
        Checksum checksum = new CRC32();

        int size = buffer.length;
        int loadSize = 64000;
        int lastPayloadSize = buffer.length % loadSize;
        int actualPayloadSize = loadSize;

        ByteBuffer byteBuffer = ByteBuffer.allocate(loadSize + 20);

        int number = 1;
        int index = 0;

        checksum.update(buffer, index, loadSize);
        byteBuffer.putLong(checksum.getValue());

        byteBuffer.putInt(number);
        byteBuffer.putInt(actualPayloadSize);
        byteBuffer.putInt(size);

        for (int i = 0; i < buffer.length; i++) {

            byteBuffer.put(buffer[i]);
            if ((i + 1) % 64000 == 0) {
                list.add(byteBuffer);
                byteBuffer.clear();
                checksum.reset();
                byteBuffer = ByteBuffer.allocate(loadSize + 20);

                index += actualPayloadSize;

                if (buffer.length - i == lastPayloadSize + 1) {
                    actualPayloadSize = lastPayloadSize;
                } else {
                    actualPayloadSize = loadSize;
                }
                number++;
                checksum.update(buffer, index, actualPayloadSize);
                byteBuffer.putLong(checksum.getValue());
                byteBuffer.putInt(number);
                byteBuffer.putInt(actualPayloadSize);
                byteBuffer.putInt(size);
            }
        }

        list.add(byteBuffer);

        byteBuffer.clear();
        byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(-1);
        checksum.reset();
        checksum.update(byteBuffer.array(), 0, 4);

        ByteBuffer byteBuffer1 = ByteBuffer.allocate(12);
        byteBuffer1.putLong(checksum.getValue());

        byteBuffer1.putInt(-1);
        list.add(byteBuffer1);
    }

    private void sendData() throws IOException {
        DatagramPacket sendPacket;
        if (Listener.receiversPorts.isEmpty()) {
            return;
        }

        for (ByteBuffer byteBuffer : list) {
            for (String name : Listener.arrayList) {
                InetAddress address = Listener.receiverAddress.get(name);
                int port = Listener.receiversPorts.get(name);
                sendPacket = new DatagramPacket(byteBuffer.array(), byteBuffer.capacity(), address, port);
                Listener.datagramSocket.send(sendPacket);
            }

            long start = System.currentTimeMillis();
            long end = start + 5;
            while (System.currentTimeMillis() < end) {
            }
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                list.clear();
                BufferedImage screenshot = takeScreen();
                Sender.jLabel.setIcon(new ImageIcon(screenshot));
                splitScreenshot(screenshot);
                sendData();
            } catch (Exception exception) {
                System.out.println("Error in server thread!");
            }

        }
    }
}