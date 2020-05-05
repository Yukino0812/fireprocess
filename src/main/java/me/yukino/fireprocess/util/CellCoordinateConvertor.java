package me.yukino.fireprocess.util;

import me.yukino.fireprocess.config.CellPredictConfig;

/**
 * @author Hoshiiro Yukino
 */

public class CellCoordinateConvertor {

    public static int toCellIndex(double buildingModelCoordinate) {
        if (buildingModelCoordinate >= 0) {
            return (int) (buildingModelCoordinate / CellPredictConfig.dl);
        }
        return ((int) (buildingModelCoordinate / CellPredictConfig.dl)) - 1;
    }

    public static double toBuildingModelCoordinate(int cellIndex) {
        return cellIndex * CellPredictConfig.dl + 0.5 * CellPredictConfig.dl;
    }

}
