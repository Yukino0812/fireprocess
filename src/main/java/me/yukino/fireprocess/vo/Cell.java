package me.yukino.fireprocess.vo;

import lombok.*;

/**
 * @author Hoshiiro Yukino
 */

@Data
@AllArgsConstructor
public class Cell {

    private Integer x;
    private Integer y;
    private Integer z;
    /**
     * 火势扩散线速度，与墙壁或地板材料有关
     * 0.005m/s 5mm/s
     */
    private Double v;
    /**
     * 剩余可燃负载，以kg计算
     */
    private Double m;

}
