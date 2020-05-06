package me.yukino.fireprocess.vo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Hoshiiro Yukino
 */

@Getter
@Setter
@ToString
@NoArgsConstructor
public class Cell {

    private Integer x;
    private Integer y;
    private Integer z;
    /**
     * 火势扩散线速度，与墙壁或地板材料有关
     * 以m/s为单位，默认0.005m/s = 5mm/s
     */
    private Double v;
    /**
     * 剩余可燃负载，以kg计算
     */
    private Double m;

    /**
     * 燃烧速率，以kg/s计算
     */
    private Double burningRate;

    /**
     * 该参数表征相邻的在燃烧中元胞数量
     * 值越大越容易起燃
     */
    private AtomicInteger countBurningCellNearing;

    /**
     * 元胞状态
     * {@link me.yukino.fireprocess.enumeration.CellBurningStatus}
     */
    private int burningStatus;

    /**
     * 传播概率 0.0 - 1.0
     * 用于对材质是否传播火势进行限制，与是否起燃无必然关联
     * 值为0时该元胞不对外传播火势
     */
    private double propagateProbability;

    private long ignitionTime;

    public Cell(Integer x, Integer y, Integer z, Double v, Double m, Double burningRate, int burningStatus, double propagateProbability) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.v = v;
        this.m = m;
        this.burningRate = burningRate;
        this.countBurningCellNearing = new AtomicInteger(0);
        this.burningStatus = burningStatus;
        this.propagateProbability = propagateProbability;
        this.ignitionTime = 0;
    }

    /**
     * 仅判断坐标是否相同
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return x.equals(cell.x) &&
                y.equals(cell.y) &&
                z.equals(cell.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
