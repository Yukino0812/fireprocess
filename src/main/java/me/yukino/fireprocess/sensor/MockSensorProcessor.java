package me.yukino.fireprocess.sensor;

import cn.hutool.core.io.FileUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.yukino.fireprocess.alarm.FireAlarmer;
import me.yukino.fireprocess.util.CellCoordinateConvertor;
import me.yukino.fireprocess.util.DataUtil;
import me.yukino.fireprocess.vo.BurningCellVo;
import me.yukino.fireprocess.vo.SensorVo;
import me.yukino.fireprocess.vo.SmokeDetectorVo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author Hoshiiro Yukino
 */

@Component
public class MockSensorProcessor {

    private static final double CALIBRATE_PPM = 20;
    private static final double RL = 5;
    private static final double INIT_RS = 2;
    private static double R0;
    private static double ADC = 2947.16;

    private static final double ppmAlertThreshold = 200;
    private static final int RETRY_ALERT_TIME = 3;

    private List<SmokeDetectorVo> smokeDetectorVos;

    public MockSensorProcessor() {
        initR0();
        initSmokeDetector();
    }

    @Scheduled(cron = "0/10 * * * * ?")
    public void fetchSmokeDetectorPPMData() {
        List<SensorVo> sensorVos = smokeDetectorVos.parallelStream()
                .map(smokeDetectorVo -> SensorVo.fromSmokeDetector(smokeDetectorVo, getSmokeSensorPPMValueWithAlert(smokeDetectorVo)))
                .collect(Collectors.toList());

        DataUtil.saveSensorData(System.currentTimeMillis(), sensorVos);
    }

    private void initR0() {
        R0 = INIT_RS / Math.pow(CALIBRATE_PPM / 613.9, 1.0 / -2.074);
    }

    private void initSmokeDetector() {
        smokeDetectorVos = new ArrayList<>();
        File file = FileUtil.file("testMain.json");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node;
        try {
            node = objectMapper.readTree(file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        node = node.get("smokeDetector");
        SmokeDetectorVo[] vos;
        try {
            vos = objectMapper.readValue(node.toString(), SmokeDetectorVo[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }
        smokeDetectorVos = new ArrayList<>(Arrays.asList(vos));
    }

    public double getSmokeSensorPPMValueWithAlert(SmokeDetectorVo smokeDetectorVo) {
        double ppm = 0;
        for (int i = 0; i < RETRY_ALERT_TIME; ++i) {
            ppm = getSmokeSensorPPMValue(smokeDetectorVo);
            if (ppm <= ppmAlertThreshold) {
                return ppm;
            }
        }
//        alertFire(smokeDetectorVo);
        return ppm;
    }

    public void mockAlertFire() {
        Random random = new Random();
        int index = random.nextInt(smokeDetectorVos.size());
        SmokeDetectorVo vo = smokeDetectorVos.get(index);
        alertFire(vo);
    }

    private void alertFire(SmokeDetectorVo smokeDetectorVo) {
        BurningCellVo burningCellVo = new BurningCellVo(
                CellCoordinateConvertor.toCellIndex(smokeDetectorVo.getX()),
                CellCoordinateConvertor.toCellIndex(smokeDetectorVo.getY() - 2),
                CellCoordinateConvertor.toCellIndex(smokeDetectorVo.getZ()));
        FireAlarmer.alarmFire(burningCellVo);
    }

    public double getSmokeSensorPPMValue(SmokeDetectorVo smokeDetectorVo) {
        double vrl = 3.3 * getMockADCValue(0) / 4095;
        double RS = (3.3 - vrl) / vrl * RL;
        double ppm = 613.9 * Math.pow(RS / R0, -2.074);
        ppm = CALIBRATE_PPM;
        return ppm;
    }

    private double getMockADCValue(int sensorId) {
        return ADC;
    }

}
