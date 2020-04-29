package me.yukino.fireprocess.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.lang.NonNull;

/**
 * @author Hoshiiro Yukino
 */

@Data
@AllArgsConstructor
public class BurningCellVo {

    private Integer x;
    private Integer y;
    private Integer z;

    @NonNull
    public static BurningCellVo fromCell(@NonNull Cell cell){
        return new BurningCellVo(cell.getX(), cell.getY(), cell.getZ());
    }

}
