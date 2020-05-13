package me.yukino.fireprocess.vo;

import lombok.Data;

/**
 * @author Hoshiiro Yukino
 */

@Data
public class SensorVo {

    private String name;
    private Double x;
    private Double y;
    private Double z;
    private Double val;

    public static SensorVo fromSmokeDetector(SmokeDetectorVo smokeDetectorVo, double val){
        SensorVo vo = new SensorVo();
        vo.setName(smokeDetectorVo.getName());
        vo.setX(smokeDetectorVo.getX());
        vo.setY(smokeDetectorVo.getY());
        vo.setZ(smokeDetectorVo.getZ());
        vo.setVal(val);
        return vo;
    }

}
