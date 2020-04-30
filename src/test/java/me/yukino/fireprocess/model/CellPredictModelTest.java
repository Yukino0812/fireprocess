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
    private final static double dl = 0.2;

    @BeforeAll
    public static void init() {
        cellPredictModel = new CellPredictModel();
        List<Cell> cells = new ArrayList<>();
        for (int x = 0; x < 1000; ++x) {
            for (int y = 0; y < 1000; ++y) {
                Cell cell = new Cell(x, y, 0, 0.05, 1.0, 0.005, CellBurningStatus.IGNITION_POSSIBLE);
                if (x == 0 || y == 0 || x == 999 || y == 999){
                    cell.setBurningStatus(CellBurningStatus.NON_COMBUSTIBLE);
                }
                if (x == 499 && y == 499) {
                    cell.setBurningStatus(CellBurningStatus.BURNING);
                }
                if (x==480 && y>480 &&y<520){
                    cell.setBurningStatus(CellBurningStatus.NON_COMBUSTIBLE);
                }
                if (x==520&& y>480 &&y<520){
                    cell.setBurningStatus(CellBurningStatus.NON_COMBUSTIBLE);
                }
                if (y==520&&x>=480&&x<=520){
                    cell.setBurningStatus(CellBurningStatus.NON_COMBUSTIBLE);
                }
                cells.add(cell);
            }
        }
        cellPredictModel.init(dl, cells);
    }

    @Test
    public void testPredict(){
        cellPredictModel.predict(5 * 60*1000);
    }

}
