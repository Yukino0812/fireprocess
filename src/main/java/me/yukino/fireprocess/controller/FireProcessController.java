package me.yukino.fireprocess.controller;

import me.yukino.fireprocess.model.CellPredictModelProvider;
import me.yukino.fireprocess.sensor.MockSensorProcessor;
import me.yukino.fireprocess.util.DataUtil;
import me.yukino.fireprocess.vo.ResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Hoshiiro Yukino
 */

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
