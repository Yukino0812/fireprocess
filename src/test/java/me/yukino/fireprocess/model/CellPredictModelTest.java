package me.yukino.fireprocess.model;

import me.yukino.fireprocess.enumeration.CellBurningStatus;
import me.yukino.fireprocess.vo.Cell;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hoshiiro Yukino
 */

@SpringBootTest
public class CellPredictModelTest {

    private static CellPredictModel cellPredictModel;
    private final static double dl = 0.5;

    @BeforeAll
    public static void init() {
        cellPredictModel = new CellPredictModel();
        List<Cell> cells = new ArrayList<>();
        for (int x = 0; x < 100; ++x) {
            for (int y = 0; y < 100; ++y) {
                Cell cell = new Cell(x, y, 0, 0.005, 1.0, 0.005, CellBurningStatus.IGNITION_POSSIBLE);
                if (x == 0 || y == 0 || x == 99 || y == 99){
                    cell.setBurningStatus(CellBurningStatus.NON_COMBUSTIBLE);
                }
                if (x == 49 && y == 49) {

                    cell.setBurningStatus(CellBurningStatus.BURNING);
                }
                cells.add(cell);
            }
        }
        cellPredictModel.init(dl, cells);
    }

    @Test
    public void testPredict(){
        cellPredictModel.predict(10 * 60*1000);
    }

}
