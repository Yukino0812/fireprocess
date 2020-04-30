package me.yukino.fireprocess.enumeration;

/**
 * @author Hoshiiro Yukino
 */

public class CellBurningStatus {

    /**
     * 不可燃
     */
    public static final int NON_COMBUSTIBLE = 0;

    /**
     * 可燃
     */
    public static final int IGNITION_POSSIBLE = 1;

    /**
     * 正在燃烧
     */
    public static final int BURNING = 2;

    /**
     * 燃尽
     */
    public static final int BURNING_FINISH = 3;

}
