package me.yukino.fireprocess.controller;

import me.yukino.fireprocess.alarm.FireAlarmer;
import me.yukino.fireprocess.model.CellPredictModelProvider;
import me.yukino.fireprocess.model.ModelPredictMessageQueue;
import me.yukino.fireprocess.sensor.MockSensorProcessor;
import me.yukino.fireprocess.util.CellCoordinateConvertor;
import me.yukino.fireprocess.util.DataUtil;
import me.yukino.fireprocess.vo.BurningCellVo;
import me.yukino.fireprocess.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

/**
 * @author Hoshiiro Yukino
 */

@Validated
@RestController
public class FireProcessController {

    private MockSensorProcessor mockSensorProcessor;
    private ModelPredictMessageQueue messageQueue;

    @Autowired
    public FireProcessController(MockSensorProcessor mockSensorProcessor,
                                 ModelPredictMessageQueue messageQueue){
        this.mockSensorProcessor = mockSensorProcessor;
        this.messageQueue = messageQueue;
    }

    @PostMapping("/mock/ignition")
    public ResponseVo mockIgnition(){
        mockSensorProcessor.mockAlertFire();
        messageQueue.requirePredict();
        return new ResponseVo(0,"Mock success");
    }

    @PostMapping("/alarm/fire")
    public ResponseVo manuallyAlarmFire(@RequestParam @NotNull Double x,
                                        @RequestParam @NotNull Double y,
                                        @RequestParam @NotNull Double z){
        int cellX = CellCoordinateConvertor.toCellIndex(x);
        int cellY = CellCoordinateConvertor.toCellIndex(y);
        int cellZ = CellCoordinateConvertor.toCellIndex(z);
        BurningCellVo burningCellVo = new BurningCellVo(cellX, cellY, cellZ);
//        FireAlarmer.alarmFire(burningCellVo);
        return new ResponseVo(0,"Manually alarm fire success");
    }

    @PostMapping("/model/reset")
    public ResponseVo resetModel(){
        CellPredictModelProvider.getCellPredictModel().reset();
        DataUtil.removeAllPredictData();
        return new ResponseVo(0,"Reset success");
    }

    @PostMapping("/model/data")
    public ResponseVo getAvailableData(){
        return new ResponseVo(0,"Get data success", DataUtil.getAvailablePredictData());
    }

}
