package com.example.motorcontrol;

import javafx.scene.image.ImageView;
import purejavacomm.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Enumeration;

public class SerialService {

    private SerialPort serialPort;
    private volatile boolean running = false;
    private Thread readThread;
    ImageView imageView;

    public SerialService(String portName)  {
        if (portName == null) {
            portName = "COM5";
        }
        if (running) {
            System.out.println("---------------------------------------------------------------");
            System.out.println("Serial service is already running on port: " + portName);
            System.out.println("---------------------------------------------------------------");
            return;
        }

        try {
            // Tìm và mở cổng serial
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

            // Mở cổng với timeout 2000ms
            CommPort commPort = portIdentifier.open("MotorControl", 2000);

            // Ép kiểu sang SerialPort
            if (commPort instanceof SerialPort) {
                serialPort = (SerialPort) commPort;

                // Cấu hình tham số cổng serial
                serialPort.setSerialPortParams(3000000,
                        SerialPort.DATABITS_8,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_NONE);

                // Cấu hình flow control
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);

                running = true;
                System.out.println("Serial service started on port: " + portName);

                // Tạo luồng đọc dữ liệu
                readThread = new Thread(() -> {
                    try {
                        InputStream input = serialPort.getInputStream();
                        StringBuilder buffer = new StringBuilder();
                        byte[] readBuffer = new byte[1024];

                        while (running && !Thread.currentThread().isInterrupted()) {
                            int available = input.available();
                            if (available > 0) {
                                int bytesRead = input.read(readBuffer, 0, Math.min(available, readBuffer.length));
                                for (int i = 0; i < bytesRead; i++) {
                                    byte b = readBuffer[i];
                                    if (b == '\n') {
                                        String message = buffer.toString().trim();
                                        if (!message.isEmpty()) {
                                            System.out.println("Received message: " + message);
                                            synchronized (this) {
                                                handleMessage(message);
                                            }
                                        }
                                        buffer.setLength(0);
                                    } else if (b != '\r') { // Bỏ qua ký tự carriage return
                                        buffer.append((char) b);
                                    }
                                }
                            }
                            Thread.sleep(50); // Giảm thời gian chờ để tăng responsiveness
                        }
                    } catch (Exception e) {
                        if (running) {
                            System.out.println("Error reading from serial port: " + e.getMessage());
                        }
                    } finally {
                        stop();
                    }
                });
                readThread.setDaemon(true); // Đặt là daemon thread
                readThread.start();

            } else {
                throw new RuntimeException("Port is not a serial port: " + portName);
            }

        } catch (Exception e) {
            System.out.println("Failed to start serial service: " + e.getMessage());
            e.printStackTrace();
            stop();

//            throw new RuntimeException("Failed to start serial service: " + e.getMessage());
        }
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;

        if (readThread != null && readThread.isAlive()) {
            readThread.interrupt();
            try {
                readThread.join(1000); // Chờ thread kết thúc trong 1 giây
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (serialPort != null) {
            try {
                serialPort.close();
                System.out.println("Serial port closed");
            } catch (Exception e) {
                System.out.println("Error closing serial port: " + e.getMessage());
            }
        }
        serialPort = null;
    }

    public synchronized void send(String command) {
        if (!running || serialPort == null) {
            System.out.println("Cannot send command: Serial port is not open");
            throw new IllegalStateException("Serial port is not open");
        }
        try {
            OutputStream output = serialPort.getOutputStream();
            String fullCommand = command.endsWith("\n") ? command : command + "\n";
            output.write(fullCommand.getBytes());
            output.flush();
            System.out.println("------------Command: " + command.trim() + "-------------");
        } catch (Exception e) {
            System.out.println("Error sending command: " + e.getMessage());
            throw new RuntimeException("Failed to send command: " + e.getMessage());
        }
    }

    private void handleMessage(String message) {
        // Xử lý message nhận được từ serial
        // Thêm logic xử lý của bạn ở đây
        // Ví dụ:
        // if (message.startsWith("POS:")) { ... }
        // if (message.equals("READY")) { ... }
    }

    public static String[] listPorts() {
        try {
            Enumeration<CommPortIdentifier> portEnum = CommPortIdentifier.getPortIdentifiers();
            java.util.List<String> portList = new java.util.ArrayList<>();

            while (portEnum.hasMoreElements()) {
                CommPortIdentifier portIdentifier = portEnum.nextElement();
                if (portIdentifier.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                    String portInfo = portIdentifier.getName();
                    try {
                        portInfo += " - " + portIdentifier.getCurrentOwner();
                    } catch (Exception e) {
                        portInfo += " - Available";
                    }
                    portList.add(portInfo);
                }
            }

            return portList.toArray(new String[0]);
        } catch (Exception e) {
            System.out.println("Error listing ports: " + e.getMessage());
            return new String[0];
        }
    }

    // Thêm phương thức kiểm tra trạng thái
    public boolean isRunning() {
        return running;
    }

    // Thêm phương thức get port name
    public String getPortName() {
        return serialPort != null ? serialPort.getName() : "Not connected";
    }

    // Phương thức helper để kiểm tra cổng có tồn tại không
    public static boolean isPortAvailable(String portName) {
        try {
            CommPortIdentifier.getPortIdentifier(portName);
            return true;
        } catch (NoSuchPortException e) {
            return false;
        }
    }
}