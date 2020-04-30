package me.yukino.fireprocess.model;

import me.yukino.fireprocess.enumeration.CellBurningStatus;
import me.yukino.fireprocess.vo.Cell;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

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
     * 可燃负载最少应具有的值
     * 当元胞可燃负载低于此值，认为元胞不可燃烧或者已经燃烧完毕
     */
    private final double minM = 1e-6;

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
        HashSet<Cell> cellsToBurn = new HashSet<>();
        cellsBurning.parallelStream()
                .forEach(cell -> {
                    // 计算相邻未开始燃烧的可燃元胞
                    for (int i = 0; i < OFFSET.length; ++i) {
                        int x = cell.getX() + OFFSET[i][0];
                        int y = cell.getY() + OFFSET[i][1];
                        int z = cell.getZ() + OFFSET[i][2];
                        if (x < 0 || x >= cells.size()) continue;
                        if (y < 0 || y >= cells.get(x).size()) continue;
                        if (z < 0 || z >= cells.get(x).get(y).size()) continue;
                        Cell nearingCell = cells.get(x).get(y).get(z);
                        if (nearingCell.getBurningStatus() != CellBurningStatus.IGNITION_POSSIBLE
                                || nearingCell.getM() < minM) continue;
                        nearingCell.setCountBurningCellNearing(nearingCell.getCountBurningCellNearing() + OFFSET[i][3]);
                        cellsToBurn.add(nearingCell);
                    }

                    // 消耗自身可燃物
                    cell.setM(cell.getM() - cell.getBurningRate() * (double) stepSize / 1000);
                    // 可燃物消耗完后，认为燃尽
                    if (cell.getM() < minM) {
                        cell.setBurningStatus(CellBurningStatus.BURNING_FINISH);
                        cellsBurning.remove(cell);
                        cellsBurningFinish.add(cell);
                    }
                });

        Random random = new Random();

        cellsToBurn.parallelStream()
                .forEach(cell -> {
                    double probabilityToBurn = cell.getV() * cell.getCountBurningCellNearing() * ((double) stepSize / 1000) / (4 * dl);
                    cell.setCountBurningCellNearing(0);
                    // 没有起燃
                    if (random.nextDouble() >= probabilityToBurn) return;

                    // 起燃
                    cell.setBurningStatus(CellBurningStatus.BURNING);
                    cellsIgnitionPossible.remove(cell);
                    cellsBurning.add(cell);
                });
        cellsToBurn.clear();

        globalTimeCount += stepSize;
        
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

    /**
     * 用于计算相邻元胞
     * x y z n系数(countBurningCellNearing)
     */
    private static final int[][] OFFSET = {
            {1, 0, 0, 2},
            {-1, 0, 0, 2},
            {0, 1, 0, 2},
            {0, -1, 0, 2},
            {0, 0, 1, 2},
            {0, 0, -1, 2},
            {1, 1, 0, 1},
            {1, -1, 0, 1},
            {-1, 1, 0, 1},
            {-1, -1, 0, 1},
            {1, 0, 1, 1},
            {1, 0, -1, 1},
            {-1, 0, 1, 1},
            {-1, 0, -1, 1},
            {0, 1, 1, 1},
            {0, 1, -1, 1},
            {0, -1, 1, 1},
            {0, -1, -1, 1}
    };
}
