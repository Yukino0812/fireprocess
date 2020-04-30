package me.yukino.fireprocess.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import me.yukino.fireprocess.vo.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Hoshiiro Yukino
 */

public class PrintPredictUtil {

    public static void savePredict(long currentModelTime, List<Cell> cells){
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
        }
        writer.write(rows);
        writer.close();
    }

}
