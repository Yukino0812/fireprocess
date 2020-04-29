package me.yukino.fireprocess.model;

import me.yukino.fireprocess.vo.Cell;

import java.util.List;

/**
 * @author Hoshiiro Yukino
 */

public interface ICellPredictModel {

    void init(double dl);

    void predict(long timeInMillis);

    void tick();

    void tick(int times);

    void fixBurningCells(List<Cell> cellsBurning);

    /**
     * 可燃
     * @return
     */
    List<Cell> getIgnitionPossibleCells();


    /**
     * 正在燃烧
     * @return
     */
    List<Cell> getBurningCells();

    /**
     * 燃尽
     * @return
     */
    List<Cell> getBurningFinishCells();

    /**
     * 不可燃
     * @return
     */
    List<Cell> getNonCombustibleCells();

}
