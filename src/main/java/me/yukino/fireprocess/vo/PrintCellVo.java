package me.yukino.fireprocess.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Hoshiiro Yukino
 */

@Data
@AllArgsConstructor
public class PrintCellVo {

    private Long ignitionTime;
    private Long burningFinishTime;

}
