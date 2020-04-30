package me.yukino.fireprocess.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;

import java.io.File;

/**
 * @author Hoshiiro Yukino
 */

public class PrintPredictUtil {

    public static void savePredict(long currentModelTime, String content){
        File file = FileUtil.file("../fireprocess-log/predict/"+currentModelTime+".txt");
        FileUtil.touch(file);
        if (!file.canWrite()){
            file.setWritable(true);
        }
        FileWriter writer = new FileWriter(file);
        writer.write(content);
    }

}
