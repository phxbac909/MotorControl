package com.example.motorcontrol;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VideoService {

    private DatagramSocket socket;
    private boolean running = false;
    private Thread thread;
    private ImageView imageView;
    private ByteArrayOutputStream frameBuffer;
    private List<Byte> frameData = new ArrayList<>();
    private int lastIndex = -1;

    public VideoService(ImageView imageView) {
        this.imageView = imageView;
        this.frameBuffer = new ByteArrayOutputStream();

        if (running) {
            System.out.println("Video service already running");
            return;
        }

        try {
            socket = new DatagramSocket(8888);
            socket.setSoTimeout(1000); // Timeout 1 giây
            running = true;

            thread = new Thread(() -> {
                byte[] buffer = new byte[2048]; // Kích thước packet từ ESP32-CAM

                while (running) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        socket.receive(packet);

                        // Xử lý packet nhận được
                        processPacket(packet.getData(), packet.getLength());

                    }  catch (Exception e) {
                        if (running) System.out.println("Receive error: " + e.getMessage());
                    }
                }
            });

            thread.setDaemon(true);
            thread.start();
            System.out.println("Video service started");

        } catch (Exception e) {
            System.out.println("Failed to start: " + e.getMessage());
            stop();
        }
    }

    private void processPacket(byte[] data, int length) {
        // Lấy index từ 4 byte đầu
        int index = ((data[0] & 0xFF) << 24) |
                ((data[1] & 0xFF) << 16) |
                ((data[2] & 0xFF) << 8) |
                (data[3] & 0xFF);

        // Logic chính
        if (index <= lastIndex && lastIndex != -1) {
            displayFrame();
            saveImageToFile();
            frameData.clear();
        }

        // Thêm dữ liệu ảnh vào List
        byte[] imageData = Arrays.copyOfRange(data, 4, length);
        for (byte b : imageData) {
            frameData.add(b);
        }

        lastIndex = index;
    }

    private void saveImageToFile() {
        try {
            // Tạo folder nếu chưa tồn tại
            File savedDir = new File("saved");
            if (!savedDir.exists()) {
                savedDir.mkdirs();
            }

            // Tạo tên file với timestamp để tránh trùng lặp
            String timestamp = String.valueOf(System.currentTimeMillis());
            String filename = "image_" + timestamp + ".jpg"; // hoặc .png tùy định dạng ảnh
            File imageFile = new File(savedDir, filename);

            // Chuyển List<Byte> thành mảng byte[]
            byte[] imageBytes = new byte[frameData.size()];
            for (int i = 0; i < frameData.size(); i++) {
                imageBytes[i] = frameData.get(i);
            }

            // Ghi dữ liệu ảnh vào file
            Files.write(imageFile.toPath(), imageBytes);

            System.out.println("Đã lưu ảnh: " + imageFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Lỗi khi lưu ảnh: " + e.getMessage());
            e.printStackTrace();
        }
    }

        private void displayFrame() {
        if (frameData.isEmpty()) return;

        // Chuyển List<Byte> sang byte[]
        byte[] imageBytes = new byte[frameData.size()];
        for (int i = 0; i < frameData.size(); i++) {
            imageBytes[i] = frameData.get(i);
        }

        // Hiển thị ảnh
        Platform.runLater(() -> {
            try {
                imageView.setImage(new Image(new java.io.ByteArrayInputStream(imageBytes)));
            } catch (Exception e) {
                System.out.println("Display error");
            }
        });

        System.out.println("Displayed frame: " + frameData.size() + " bytes");
    }
    public void stop() {
        running = false;

        if (thread != null) {
            thread.interrupt();
        }

        if (socket != null) {
            socket.close();
        }

        System.out.println("Video service stopped");
    }

    public boolean isRunning() {
        return running;
    }
}