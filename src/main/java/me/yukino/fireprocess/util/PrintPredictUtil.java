package me.yukino.fireprocess.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.yukino.fireprocess.vo.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Hoshiiro Yukino
 */

public class PrintPredictUtil {

    public static void savePredict(long currentModelTime, List<Cell> cells){
        File file = FileUtil.file("../fireprocess-log/predict/"+currentModelTime+".json");
        if (file.exists()){
            file.delete();
        }
        FileUtil.touch(file);
        if (!file.canWrite()){
            file.setWritable(true);
        }

        Map<Double,Map<Double,Map<Double,Long>>> mapXYZ = new ConcurrentHashMap<>(16);
        cells.parallelStream()
                .forEach(cell -> {
                    double x = CellCoordinateConvertor.toBuildingModelCoordinate(cell.getX());
                    Map<Double, Map<Double,Long>> mapYZ;
                    if (!mapXYZ.containsKey(x)){
                        mapYZ = new ConcurrentHashMap<>(16);
                        mapXYZ.put(x,mapYZ);
                    }else {
                        mapYZ = mapXYZ.get(x);
                    }

                    double y = CellCoordinateConvertor.toBuildingModelCoordinate(cell.getY());
                    Map<Double,Long> mapZ;
                    if (!mapYZ.containsKey(y)){
                        mapZ = new ConcurrentHashMap<>(16);
                        mapYZ.put(y,mapZ);
                    }else {
                        mapZ = mapYZ.get(y);
                    }

                    double z = CellCoordinateConvertor.toBuildingModelCoordinate(cell.getZ());
                    mapZ.put(z, cell.getIgnitionTime());
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

    /**
     * 备份用
     * @param currentModelTime
     * @param cells
     */
    @Deprecated
    private static void savePredictToExcel(long currentModelTime, List<Cell> cells){
        File file = FileUtil.file("../fireprocess-log/predict/"+currentModelTime+".xlsx");
        if (file.exists()){
            file.delete();
        }
        FileUtil.touch(file);
        if (!file.canWrite()){
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
        for (Cell cell:cells){
            List<Integer> row = new ArrayList<>();
            row.add(cell.getX());
            row.add(cell.getY());
            row.add(cell.getZ());
            rows.add(row);

            System.out.println(cell.getX()+" "+cell.getY()+" "+cell.getZ());
        }
        System.out.println("========================");
        writer.write(rows);
        writer.close();
    }

}
