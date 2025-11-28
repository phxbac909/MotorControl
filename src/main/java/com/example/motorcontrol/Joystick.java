package com.example.motorcontrol;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.input.MouseEvent;
import javafx.event.EventHandler;

public class Joystick extends Pane {
    private Circle base;
    private Circle handle;
    private double baseRadius = 50;
    private double handleRadius = 20;
    private double centerX, centerY;

    public interface JoystickListener {
        void onJoystickMoved(double x, double y);
    }

    private JoystickListener listener;

    public Joystick() {
        initializeJoystick();
        setupEventHandlers();
    }

    private void initializeJoystick() {
        base = new Circle(baseRadius, Color.LIGHTGRAY);
        base.setStroke(Color.DARKGRAY);
        base.setStrokeWidth(2);

        handle = new Circle(handleRadius, Color.DARKBLUE);
        handle.setStroke(Color.BLACK);
        handle.setStrokeWidth(1);

        this.getChildren().addAll(base, handle);
        this.setPrefSize(baseRadius * 2, baseRadius * 2);
    }

    private void setupEventHandlers() {
        // Xử lý kéo handle
        handle.setOnMouseDragged(event -> {
            moveHandle(event.getX() + handle.getLayoutX(), event.getY() + handle.getLayoutY());
        });

        // Xử lý kéo base
        this.setOnMouseDragged(event -> {
            if (event.getTarget() == base || event.getTarget() == this) {
                moveHandle(event.getX(), event.getY());
            }
        });

        // Xử lý thả chuột
        this.setOnMouseReleased(event -> {
            resetHandle();
        });
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        centerX = getWidth() / 2;
        centerY = getHeight() / 2;

        base.setCenterX(centerX);
        base.setCenterY(centerY);

        // Chỉ reset handle về trung tâm nếu chưa được kéo
        if (handle.getCenterX() == 0 && handle.getCenterY() == 0) {
            handle.setCenterX(centerX);
            handle.setCenterY(centerY);
        }
    }

    private void moveHandle(double targetX, double targetY) {
        // Tính vector từ tâm đến vị trí target
        double deltaX = targetX - centerX;
        double deltaY = targetY - centerY;

        // Tính khoảng cách thực tế
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Giới hạn handle trong phạm vi base
        if (distance > baseRadius - handleRadius) {
            double scale = (baseRadius - handleRadius) / distance;
            deltaX *= scale;
            deltaY *= scale;
        }

        // Cập nhật vị trí handle
        handle.setCenterX(centerX + deltaX);
        handle.setCenterY(centerY + deltaY);

        // Tính giá trị chuẩn hóa (-1 đến 1)
        double normalizedX = deltaX / (baseRadius - handleRadius);
        double normalizedY = deltaY / (baseRadius - handleRadius);

        // Gọi callback
        if (listener != null) {
            listener.onJoystickMoved(normalizedX, normalizedY);
        }
    }

    private void resetHandle() {
        // Tạo hiệu ứng mượt mà khi reset
        javafx.animation.KeyValue kvX = new javafx.animation.KeyValue(handle.centerXProperty(), centerX);
        javafx.animation.KeyValue kvY = new javafx.animation.KeyValue(handle.centerYProperty(), centerY);
        javafx.animation.KeyFrame kf = new javafx.animation.KeyFrame(javafx.util.Duration.millis(150), kvX, kvY);
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(kf);
        timeline.play();

        // Thông báo vị trí reset
        if (listener != null) {
            listener.onJoystickMoved(0, 0);
        }
    }

    public double getXValue() {
        if (centerX == 0) return 0;
        return (handle.getCenterX() - centerX) / (baseRadius - handleRadius);
    }

    public double getYValue() {
        if (centerY == 0) return 0;
        return (handle.getCenterY() - centerY) / (baseRadius - handleRadius);
    }

    public void setJoystickListener(JoystickListener listener) {
        this.listener = listener;
    }

    public void setBaseColor(Color color) {
        base.setFill(color);
    }

    public void setHandleColor(Color color) {
        handle.setFill(color);
    }

    public void setBaseRadius(double radius) {
        this.baseRadius = radius;
        base.setRadius(radius);
        this.setPrefSize(radius * 2, radius * 2);
        requestLayout();
    }

    public void setHandleRadius(double radius) {
        this.handleRadius = radius;
        handle.setRadius(radius);
    }
}