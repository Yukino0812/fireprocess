package me.yukino.fireprocess.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.yukino.fireprocess.vo.Cell;
import me.yukino.fireprocess.vo.PrintCellVo;
import me.yukino.fireprocess.vo.SensorVo;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author Hoshiiro Yukino
 */

public class DataUtil {

    public static void savePredict(long currentModelTime, List<Cell> cells) {
        File file = FileUtil.file("../../../fireprocess-log/predict/" + currentModelTime + ".json");
        if (file.exists()) {
            file.delete();
        }
        FileUtil.touch(file);
        if (!file.canWrite()) {
            file.setWritable(true);
        }

        Map<Double, Map<Double, Map<Double, PrintCellVo>>> mapXYZ = new ConcurrentHashMap<>(16);
        cells.parallelStream()
                .forEach(cell -> {
                    double x = CellCoordinateConvertor.toBuildingModelCoordinate(cell.getX());
                    Map<Double, Map<Double, PrintCellVo>> mapYZ;
                    if (!mapXYZ.containsKey(x)) {
                        mapYZ = new ConcurrentHashMap<>(16);
                        mapXYZ.put(x, mapYZ);
                    } else {
                        mapYZ = mapXYZ.get(x);
                    }

                    double y = CellCoordinateConvertor.toBuildingModelCoordinate(cell.getY());
                    Map<Double, PrintCellVo> mapZ;
                    if (!mapYZ.containsKey(y)) {
                        mapZ = new ConcurrentHashMap<>(16);
                        mapYZ.put(y, mapZ);
                    } else {
                        mapZ = mapYZ.get(y);
                    }

                    double z = CellCoordinateConvertor.toBuildingModelCoordinate(cell.getZ());
                    PrintCellVo vo = new PrintCellVo(cell.getIgnitionTime(), cell.getBurningFinishTime());
                    mapZ.put(z, vo);
                });

        ObjectMapper objectMapper = new ObjectMapper();
        String resultJson = "";
        try {
            resultJson = objectMapper.writeValueAsString(mapXYZ);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println(resultJson);
        FileWriter writer = FileWriter.create(file);
        writer.write(resultJson);
    }

    public static void removeAllPredictData() {
        File file = FileUtil.file("../../../fireprocess-log/predict");
        FileUtil.del(file);
        file.mkdirs();
    }

    public static List<String> getAvailablePredictData() {
        File file = FileUtil.file("../../../fireprocess-log/predict");
        File[] files = file.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(files)
                .map(File::getName)
                .collect(Collectors.toList());
    }

    public static void saveSensorData(long currentTime, List<SensorVo> sensorVos) {
        File file = FileUtil.file("../../../fireprocess-log/sensor/" + currentTime + ".json");
        if (file.exists()) {
            file.delete();
        }
        FileUtil.touch(file);
        if (!file.canWrite()) {
            file.setWritable(true);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String resultJson = "";
        try {
            resultJson = objectMapper.writeValueAsString(sensorVos);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        System.out.println(resultJson);
        FileWriter writer = FileWriter.create(file);
//        writer.write(resultJson);
    }

    /**
     * 备份用
     *
     * @param currentModelTime
     * @param cells
     */
    @Deprecated
    private static void savePredictToExcel(long currentModelTime, List<Cell> cells) {
        File file = FileUtil.file("../../../fireprocess-log/predict/" + currentModelTime + ".xlsx");
        if (file.exists()) {
            file.delete();
        }
        FileUtil.touch(file);
        if (!file.canWrite()) {
            file.setWritable(true);
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            XSSFWorkbook workbook = new XSSFWorkbook();
            workbook.write(fileOutputStream);
        } catch (Exception e) {
            e.printStackTrace();
        }

        ExcelWriter writer = ExcelUtil.getWriter(file);
        List<List<Integer>> rows = new ArrayList<>();
        for (Cell cell : cells) {
            List<Integer> row = new ArrayList<>();
            row.add(cell.getX());
            row.add(cell.getY());
            row.add(cell.getZ());
            rows.add(row);

            System.out.println(cell.getX() + " " + cell.getY() + " " + cell.getZ());
        }
        System.out.println("========================");
        writer.write(rows);
        writer.close();
    }

}
