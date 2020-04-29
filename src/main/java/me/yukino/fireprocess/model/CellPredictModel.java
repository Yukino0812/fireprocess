package me.yukino.fireprocess.model;

import me.yukino.fireprocess.vo.Cell;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hoshiiro Yukino
 */

public class CellPredictModel implements ICellPredictModel {

    private List<List<List<Cell>>> cells;
    private List<Cell> cellsIgnitionPossible;
    private List<Cell> cellsBurning;
    private List<Cell> cellsBurningFinish;
    private List<Cell> cellsNonCombustible;

    /**
     * 元胞边长
     */
    private double dl;

    /**
     * 1tick步长，以毫秒ms为单位，默认为1s=1000ms
     */
    private final int stepSize = 1000;

    /**
     * 全局时间记录，用于tick后输出cells记录
     * 以毫秒ms为单位
     */
    private long globalTimeCount = 0;

    @Override
    public void init(double dl) {
        cells = new ArrayList<>();
        cellsIgnitionPossible = new ArrayList<>();
        cellsBurning = new ArrayList<>();
        cellsBurningFinish = new ArrayList<>();
        cellsNonCombustible = new ArrayList<>();

        this.dl = dl;
    }

    @Override
    public void predict(long timeInMillis) {
        tick((int) (timeInMillis / stepSize));
    }

    @Override
    public void tick() {

    }

    @Override
    public void tick(int times) {
        for (; times > 0; times--) {
            tick();
        }
    }

    @Override
    public void fixBurningCells(List<Cell> cellsBurning) {

    }

    @Override
    public List<Cell> getIgnitionPossibleCells() {
        return cellsIgnitionPossible;
    }

    @Override
    public List<Cell> getBurningCells() {
        return cellsBurning;
    }

    @Override
    public List<Cell> getBurningFinishCells() {
        return cellsBurningFinish;
    }

    @Override
    public List<Cell> getNonCombustibleCells() {
        return cellsNonCombustible;
    }
}
