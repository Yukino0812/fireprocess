package me.yukino.fireprocess.alarm;

import me.yukino.fireprocess.model.CellPredictModelProvider;
import me.yukino.fireprocess.vo.BurningCellVo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hoshiiro Yukino
 */

public class FireAlarmer {

    public static void alarmFire(BurningCellVo vo) {
        List<BurningCellVo> list = new ArrayList<>();
        list.add(vo);
        CellPredictModelProvider.getCellPredictModel().fixBurningCells(list);
    }

}
