package me.yukino.fireprocess.model;

import me.yukino.fireprocess.enumeration.CellBurningStatus;
import me.yukino.fireprocess.util.PrintPredictUtil;
import me.yukino.fireprocess.vo.Cell;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Hoshiiro Yukino
 */

public class CellPredictModel implements ICellPredictModel {

    private Cell[][][] cells;
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
     * 输出记录时间间隔
     * 以毫秒ms为单位
     */
    private final long printInterval = 60 * 1000;

    /**
     * 全局时间记录，用于tick后输出cells记录
     * 以毫秒ms为单位
     */
    private long globalTimeCount = 0;

    /**
     * 全局时间记录，用于保存上一次输出记录的时刻
     * 以毫秒ms为单位
     */
    private long lastPrintTimeCount = 0;


    @Override
    public void init(double dl, List<Cell> cells) {
        this.cellsIgnitionPossible = new ArrayList<>();
        this.cellsBurning = new ArrayList<>();
        this.cellsBurningFinish = new ArrayList<>();
        this.cellsNonCombustible = new ArrayList<>();
        this.dl = dl;

        AtomicInteger maxX = new AtomicInteger();
        AtomicInteger maxY = new AtomicInteger();
        AtomicInteger maxZ = new AtomicInteger();
        Collections.synchronizedList(cells).parallelStream()
                .forEach(cell -> {
                    if (cell.getX() > maxX.get()) maxX.set(cell.getX());
                    if (cell.getY() > maxY.get()) maxY.set(cell.getY());
                    if (cell.getZ() > maxZ.get()) maxZ.set(cell.getZ());

                    switch (cell.getBurningStatus()) {
                        case CellBurningStatus.NON_COMBUSTIBLE:
                            cellsNonCombustible.add(cell);
                            break;
                        case CellBurningStatus.IGNITION_POSSIBLE:
                            cellsIgnitionPossible.add(cell);
                            break;
                        case CellBurningStatus.BURNING:
                            cellsBurning.add(cell);
                            break;
                        case CellBurningStatus.BURNING_FINISH:
                            cellsBurningFinish.add(cell);
                            break;
                        default:
                            cellsIgnitionPossible.add(cell);
                    }
                });
        this.cells = new Cell[maxX.get()+1][maxY.get()+1][maxZ.get()+1];
        Collections.synchronizedList(cells).parallelStream()
                .forEach(cell -> this.cells[cell.getX()][cell.getY()][cell.getZ()] = cell);
    }

    @Override
    public void predict(long timeInMillis) {
        tick((int) (timeInMillis / stepSize));
    }

    @Override
    public void tick() {
        Set<Cell> cellsToBurn = Collections.synchronizedSet(new HashSet<>());
        List<Cell> tempList = new ArrayList<>();

        // 计算正在燃烧的元胞
        Collections.synchronizedList(cellsBurning).parallelStream()
                .filter(Objects::nonNull)
                .forEach(cell -> {
                    // 计算相邻未开始燃烧的可燃元胞
                    for (int i = 0; i < OFFSET.length; ++i) {
                        int x = cell.getX() + OFFSET[i][0];
                        int y = cell.getY() + OFFSET[i][1];
                        int z = cell.getZ() + OFFSET[i][2];
                        if (x < 0 || x >= cells.length) continue;
                        if (y < 0 || y >= cells[x].length) continue;
                        if (z < 0 || z >= cells[x][y].length) continue;
                        Cell nearingCell = cells[x][y][z];
                        if (nearingCell == null
                                || nearingCell.getBurningStatus() != CellBurningStatus.IGNITION_POSSIBLE
                                || nearingCell.getM() < minM) continue;
                        nearingCell.getCountBurningCellNearing().addAndGet(OFFSET[i][3]);
                        cellsToBurn.add(nearingCell);
                    }

                    // 消耗自身可燃物
                    cell.setM(cell.getM() - cell.getBurningRate() * (double) stepSize / 1000);
                    // 可燃物消耗完后，认为燃尽
                    if (cell.getM() < minM) {
                        cell.setBurningStatus(CellBurningStatus.BURNING_FINISH);
                        tempList.add(cell);
                        Collections.synchronizedList(cellsBurningFinish).add(cell);
                    }
                });

        // 移除燃尽元胞
        tempList.forEach(cellsBurning::remove);
        tempList.clear();

        Random random = new Random();

        // 计算可燃元胞
        cellsToBurn.parallelStream()
                .forEach(cell -> {
                    double probabilityToBurn = cell.getV() * cell.getCountBurningCellNearing().get() * ((double) stepSize / 1000) / (4 * dl);
                    cell.getCountBurningCellNearing().set(0);
                    // 没有起燃
                    if (random.nextDouble() >= probabilityToBurn) return;

                    // 起燃
                    cell.setBurningStatus(CellBurningStatus.BURNING);
                    Collections.synchronizedList(cellsIgnitionPossible).remove(cell);
                    Collections.synchronizedList(cellsBurning).add(cell);
                });
        cellsToBurn.clear();

        // 记录元胞数据
        globalTimeCount += stepSize;
        if (globalTimeCount - lastPrintTimeCount >= printInterval) {
            print(globalTimeCount);
            lastPrintTimeCount = globalTimeCount;
        }
    }

    @Override
    public void tick(int times) {
        for (; times > 0; times--) {
            tick();
        }
    }

    @Override
    public void reset() {
        cellsIgnitionPossible.addAll(cellsBurning);
        cellsIgnitionPossible.addAll(cellsBurningFinish);
        cellsBurning.clear();
        cellsBurningFinish.clear();
        globalTimeCount = 0;
        lastPrintTimeCount = 0;
    }

    private void print(long currentModelTime) {
        PrintPredictUtil.savePredict(currentModelTime, getBurningCells());
    }

    @Override
    public void fixBurningCells(List<Cell> cellsBurning) {
        Collections.synchronizedList(cellsBurning).parallelStream()
                .forEach(cell -> {
                    this.cellsIgnitionPossible.remove(cell);
                    this.cellsBurningFinish.remove(cell);
                    this.cellsNonCombustible.remove(cell);
                    if (!this.cellsBurning.contains(cell)) {
                        this.cellsBurning.add(cell);
                    }
                });
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
