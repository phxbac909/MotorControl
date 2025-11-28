package com.example.motorcontrol;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

public class VehicleCounter {
    static {
        nu.pattern.OpenCV.loadLocally(); // Hoặc System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    private CascadeClassifier carClassifier;

    public VehicleCounter() {
        // Sử dụng classifier có sẵn cho xe hơi
        carClassifier = new CascadeClassifier();
        // Có thể tải file XML classifier từ resources
        // carClassifier.load("path/to/cars.xml");
    }

    public int countVehiclesFromStream(ByteArrayInputStream videoStream) {
        int vehicleCount = 0;
        List<Mat> frames = new ArrayList<>();

        try {
            // Chuyển ByteArrayInputStream thành dữ liệu video OpenCV có thể xử lý
            // Lưu ý: Cần chuyển đổi dữ liệu stream thành format phù hợp
            byte[] videoData = videoStream.readAllBytes();

            // Tạo VideoCapture từ dữ liệu byte
            // OpenCV không trực tiếp hỗ trợ ByteArrayInputStream, cần giải pháp trung gian
            MatOfByte matOfByte = new MatOfByte(videoData);
            Mat frame = Imgcodecs.imdecode(matOfByte, Imgcodecs.IMREAD_UNCHANGED);

            if (!frame.empty()) {
                // Xử lý frame để đếm phương tiện
                vehicleCount = processFrame(frame);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return vehicleCount;
    }

    private int processFrame(Mat frame) {
        int count = 0;

        try {
            // Chuyển sang ảnh xám để xử lý
            Mat gray = new Mat();
            Imgproc.cvtColor(frame, gray, Imgproc.COLOR_BGR2GRAY);

            // Phát hiện phương tiện sử dụng Haar Cascade
            MatOfRect vehicles = new MatOfRect();

            // Sử dụng classifier cho xe hơi (cần có file XML)
            // carClassifier.detectMultiScale(gray, vehicles, 1.1, 3, 0, new Size(30, 30));

            // Hoặc sử dụng phương pháp background subtraction đơn giản
            count = detectWithBackgroundSubtraction(gray);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }

    private int detectWithBackgroundSubtraction(Mat grayFrame) {
        int count = 0;

        try {
            // Phương pháp đơn giản: sử dụng threshold để phát hiện chuyển động
            Mat binary = new Mat();
            Imgproc.threshold(grayFrame, binary, 100, 255, Imgproc.THRESH_BINARY);

            // Tìm contours
            List<MatOfPoint> contours = new ArrayList<>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(binary, contours, hierarchy,
                    Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

            // Lọc contours theo kích thước (giả sử là phương tiện)
            for (MatOfPoint contour : contours) {
                double area = Imgproc.contourArea(contour);
                if (area > 500 && area < 50000) { // Ngưỡng kích thước cho phương tiện
                    count++;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;
    }
}