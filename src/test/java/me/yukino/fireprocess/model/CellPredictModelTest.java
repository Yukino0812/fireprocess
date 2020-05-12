package me.yukino.fireprocess.model;

import cn.hutool.core.io.FileUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.yukino.fireprocess.util.CellCoordinateConvertor;
import me.yukino.fireprocess.vo.BurningCellVo;
import me.yukino.fireprocess.vo.SmokeDetectorVo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * @author Hoshiiro Yukino
 */

@SpringBootTest
public class CellPredictModelTest {

    private static ICellPredictModel cellPredictModel;
    private static List<SmokeDetectorVo> smokeDetectorVos;

    @BeforeAll
    public static void init() {
        CellPredictModelProvider.initModel();
        cellPredictModel = CellPredictModelProvider.getCellPredictModel();
        initSmokeDetector();
    }

    private static void initSmokeDetector() {
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

    @Test
    public void testPredict() {
        Random random = new Random();
        int index = random.nextInt(smokeDetectorVos.size());
        SmokeDetectorVo vo = smokeDetectorVos.get(index);
        BurningCellVo burningCellVo = new BurningCellVo(
                CellCoordinateConvertor.toCellIndex(vo.getX()),
                CellCoordinateConvertor.toCellIndex(vo.getY() - 2),
                CellCoordinateConvertor.toCellIndex(vo.getZ()));
        List<BurningCellVo> burningCellVos = new ArrayList<>();
        burningCellVos.add(burningCellVo);
        cellPredictModel.fixBurningCells(burningCellVos);
        cellPredictModel.predict(10 * 60 * 1000);
    }

}
