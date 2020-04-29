package me.yukino.fireprocess.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Hoshiiro Yukino
 */

@Data
@AllArgsConstructor
public class ResponseVo {

    private Integer code;
    private String msg;
    private Object data;

    public ResponseVo(Integer code) {
        this.code = code;
    }

    public ResponseVo(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
