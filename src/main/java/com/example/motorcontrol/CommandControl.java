package com.example.motorcontrol;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class CommandControl {
    private double throttle;
    private double yaw;
    private double pitch;
    private double roll;

    public CommandControl(double throttle, double yaw, double pitch, double roll) {
        this.throttle = throttle;
        this.yaw = yaw;
        this.pitch = pitch;
        this.roll = roll;
    }


    double compare(CommandControl commandControl2) {

        double deltaThrottle = Math.abs(commandControl2.throttle - throttle);
        double deltaYaw = Math.abs(commandControl2.yaw - yaw);
        double deltaPitch = Math.abs(commandControl2.pitch - pitch);
        double deltaRoll = Math.abs(commandControl2.roll - roll);

        return Collections.min(Arrays.asList(deltaThrottle, deltaYaw, deltaPitch, deltaRoll));
    }

    public double getThrottle() {
        return throttle;
    }

    public void setThrottle(double throttle) {
        this.throttle = throttle;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    @Override
    public String toString() {
        return String.format("c%.2f %.2f %.2f %.2f", throttle, yaw, pitch, roll);
    }
}
