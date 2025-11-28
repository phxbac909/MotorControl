package com.example.motorcontrol;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class HelloController implements Initializable {




    int throttle = 0;

    SerialService serialService = new SerialService("COM5");

    @FXML
    private Slider speed;

    @FXML
    private Slider motor1;

    @FXML
    VBox root;

    @FXML
    private Slider motor2;

    @FXML
    private Slider motor3;

    @FXML
    private Slider motor4;

    @FXML
    private HBox joy;

    @FXML
    private Button ascend;

    @FXML
    private Button descend;

    @FXML
    void sendCommandSIgnal(ActionEvent event) {
        Button button = (Button) event.getSource();

        serialService.send(button.getText());
    }


    @FXML
    void back(ActionEvent event) {

    }

    @FXML
    void forward(ActionEvent event) {

    }

    @FXML
    void left(ActionEvent event) {

    }

    @FXML
    void right(ActionEvent event) {

    }

    @FXML
    void rotateClockside(ActionEvent event) {

    }

    @FXML
    void rotateCounterCllockside(ActionEvent event) {

    }

    @FXML
    void stop(ActionEvent event) {
        serialService.send("s");
        speed.setValue(0 );
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {




        Joystick joystick1 = new Joystick();

        joystick1.setBaseRadius(100);
        joystick1.setHandleRadius(20);

        joy.getChildren().add(joystick1);


        final AtomicReference<CommandControl>[] lastCommand = new AtomicReference[]{new AtomicReference<>(new CommandControl(0, 0, 0, 0))};
        final long[] lastSendTime = {0};

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->{
            try {


                CommandControl newCommand = new CommandControl(speed.getValue(),0,
                        joystick1.getXValue()*45, joystick1.getYValue()*45);


                CommandControl lastSent = lastCommand[0].get();
                long currentTime = System.currentTimeMillis();

                // Gửi nếu: thay đổi > 5% HOẶC 500ms chưa gửi gì
                if (newCommand.compare(lastSent) > 50 ||
                        (currentTime - lastSendTime[0]) > 500) {

                    serialService.send(newCommand.toString());
//                    System.out.println(newCommand);
                    lastCommand[0].set(newCommand);
                    lastSendTime[0] = currentTime;
                }

            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        },0, 25, TimeUnit.MILLISECONDS);

//        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(()->{
//            try {
//                throttle = 0;
//                if(ascend.isPressed()) throttle = (int) speed.getValue();
//                if(descend.isPressed()) throttle = -(int) speed.getValue();
//
//                CommandControl newCommand = new CommandControl(throttle,0,
//                        joystick1.getXValue(), joystick1.getYValue());
//
//                CommandControl lastSent = lastCommand[0].get();
//                long currentTime = System.currentTimeMillis();
//
//                // Gửi nếu: thay đổi > 5 HOẶC 500ms chưa gửi gì
//                if (newCommand.compare(lastSent) > 5 ||
//                        (currentTime - lastSendTime[0]) > 500) {
//
//                    serialService.send(newCommand.toString());
//                    lastCommand[0].set(newCommand);
//                    lastSendTime[0] = currentTime;
//                }
//
//            } catch (Exception e) {
//                System.err.println("Error: " + e.getMessage());
//            }
//        },0, 25, TimeUnit.MILLISECONDS);




    }
}
