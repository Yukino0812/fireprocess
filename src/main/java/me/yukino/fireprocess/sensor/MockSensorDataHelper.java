package me.yukino.fireprocess.sensor;

/**
 * @author Hoshiiro Yukino
 */

public class MockSensorDataHelper {

    private static final double CALIBRATE_PPM = 20;
    private static final double RL = 5;
    private static final double INIT_RS = 2;
    private static double R0;
    private static double ADC = 0;

    public static void init() {
        R0 = INIT_RS / Math.pow(CALIBRATE_PPM / 613.9, 1.0 / -2.074);
    }

    public static double getSmokeSensorPPMValue(int sensorId) {
        double vrl = 3.3 * getMockADCValue(sensorId) / 4095;
        double RS = (3.3 - vrl) / vrl * RL;
        double ppm = 613.9 * Math.pow(RS / R0, -2.074);
        ppm = CALIBRATE_PPM;
        return ppm;
    }

    private static double getMockADCValue(int sensorId) {
        return ADC;
    }

}
