package me.yukino.fireprocess.test;

import me.yukino.fireprocess.vo.BurningCellVo;
import me.yukino.fireprocess.vo.ResponseVo;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hoshiiro Yukino
 */

@RequestMapping("/test")
@RestController
public class TestController {

    /**
     * test
     * @return
     */
    @GetMapping("/get")
    public String getTest() {
        return "get test success";
    }

    @PostMapping("/post")
    public String postTest(@RequestParam String param) {
        return "post test success, param = " + param;
    }

    @PostMapping("/json")
    public ResponseVo postJsonTest(@RequestParam String param) {
        List<BurningCellVo> burningCellVos = new ArrayList<>();
        burningCellVos.add(new BurningCellVo(1,2,3));
        burningCellVos.add(new BurningCellVo(4,5,6));
        burningCellVos.add(new BurningCellVo(7,8,9));
        Map<String, Object> infoPoints = new HashMap<>(16);
        infoPoints.put("fire", burningCellVos);
        return new ResponseVo(0, param, infoPoints);
    }

}
