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
     * 正交元胞以2倍计算，对角元胞为1倍
     * 即 f = [2n+] + [nx]
     */
    private AtomicInteger countBurningCellNearing;

    /**
     * 元胞状态
     * {@link me.yukino.fireprocess.enumeration.CellBurningStatus}
     */
    private int burningStatus;

    public Cell(Integer x, Integer y, Integer z, Double v, Double m, Double burningRate, int burningStatus) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.v = v;
        this.m = m;
        this.burningRate = burningRate;
        this.countBurningCellNearing = new AtomicInteger(0);
        this.burningStatus = burningStatus;
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
