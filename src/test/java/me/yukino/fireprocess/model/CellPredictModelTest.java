package me.yukino.fireprocess.model;

import me.yukino.fireprocess.sensor.MockSensorProcessor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author Hoshiiro Yukino
 */

@SpringBootTest
public class CellPredictModelTest {

    private static ICellPredictModel cellPredictModel;

    @Autowired
    private MockSensorProcessor mockSensorProcessor;

    @Test
    public void testPredict() {
        mockSensorProcessor.mockAlertFire();
        cellPredictModel = CellPredictModelProvider.getCellPredictModel();
        cellPredictModel.predict(10 * 60 * 1000);
    }

}
