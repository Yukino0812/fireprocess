package me.yukino.fireprocess.model;

import cn.hutool.core.io.FileUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.yukino.fireprocess.config.CellPredictConfig;
import me.yukino.fireprocess.enumeration.CellBurningStatus;
import me.yukino.fireprocess.util.CellCoordinateConvertor;
import me.yukino.fireprocess.vo.Cell;
import me.yukino.fireprocess.vo.VertexVo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Hoshiiro Yukino
 */

@SpringBootTest
public class CellPredictModelTest {

    private static CellPredictModel cellPredictModel;

    @BeforeAll
    public static void init() {
        cellPredictModel = new CellPredictModel();
        cellPredictModel.init(CellPredictConfig.dl, initCells());
    }

    private static List<Cell> initCells() {
        File file = FileUtil.file("testRoom.json");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node;
        try {
            node = objectMapper.readTree(file);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        JsonNode roomsNode = node.get("rooms");
        JsonNode wallsNode = node.get("walls");
        Set<Cell> cellSet = new HashSet<>();

        initRooms(roomsNode, cellSet);
        initWalls(wallsNode, cellSet);

        return new ArrayList<>(cellSet);
    }

    private static void initRooms(JsonNode roomsNode, Set<Cell> cells) {
        ObjectMapper objectMapper = new ObjectMapper();
        VertexVo[] vertexex;
        try {
            vertexex = objectMapper.readValue(roomsNode.toString(), VertexVo[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }
        Set<Cell> syncCellSet = Collections.synchronizedSet(cells);
        Arrays.stream(vertexex).parallel()
                .forEach(vo -> {
                    Cell[][][] cellArray = getCellsBetweenTwoVertexex(vo.getV1()[0], vo.getV1()[1], vo.getV1()[2], vo.getV2()[0], vo.getV2()[1], vo.getV2()[2], true);
                    for (Cell[][] cellYZ : cellArray) {
                        for (Cell[] cellZ : cellYZ) {
                            syncCellSet.addAll(Arrays.asList(cellZ));
                        }
                    }
                });
    }

    private static void initWalls(JsonNode wallsNode, Set<Cell> cells) {
        ObjectMapper objectMapper = new ObjectMapper();
        VertexVo[] vertexex;
        try {
            vertexex = objectMapper.readValue(wallsNode.toString(), VertexVo[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }
        Set<Cell> syncCellSet = Collections.synchronizedSet(cells);
        Arrays.stream(vertexex).parallel()
                .forEach(vo -> {
                    Cell[][][] cellArray = getCellsBetweenTwoVertexex(vo.getV1()[0], vo.getV1()[1], vo.getV1()[2], vo.getV2()[0], vo.getV2()[1], vo.getV2()[2], false);
                    for (Cell[][] cellYZ : cellArray) {
                        for (Cell[] cellZ : cellYZ) {
                            syncCellSet.addAll(Arrays.asList(cellZ));
                        }
                    }
                });
    }

    private static Cell[][][] getCellsBetweenTwoVertexex(double x1, double y1, double z1, double x2, double y2, double z2, boolean room) {
        int cellX1 = CellCoordinateConvertor.toCellIndex(x1);
        int cellY1 = CellCoordinateConvertor.toCellIndex(y1);
        int cellZ1 = CellCoordinateConvertor.toCellIndex(z1);
        int cellX2 = CellCoordinateConvertor.toCellIndex(x2);
        int cellY2 = CellCoordinateConvertor.toCellIndex(y2);
        int cellZ2 = CellCoordinateConvertor.toCellIndex(z2);
        int minX = Math.min(cellX1, cellX2);
        int minY = Math.min(cellY1, cellY2);
        int minZ = Math.min(cellZ1, cellZ2);
        int rangeX = Math.abs(cellX1 - cellX2);
        int rangeY = Math.abs(cellY1 - cellY2);
        int rangeZ = Math.abs(cellZ1 - cellZ2);
        Cell[][][] cells = new Cell[rangeX + 1][rangeY + 1][rangeZ + 1];
        double propagateProbability = room ? CellPredictConfig.DEFAULT_PROPAGATE_PROBABILITY : 0.2;
        for (int iX = 0; iX <= rangeX; ++iX) {
            for (int iY = 0; iY <= rangeY; ++iY) {
                for (int iZ = 0; iZ <= rangeZ; ++iZ) {
                    cells[iX][iY][iZ] = new Cell(minX + iX, minY + iY, minZ + iZ,
                            CellPredictConfig.DEFAULT_V,
                            CellPredictConfig.DEFAULT_M,
                            CellPredictConfig.DEFAULT_BURNING_RATE,
                            CellBurningStatus.IGNITION_POSSIBLE,
                            propagateProbability);
                }
            }
        }
        return cells;
    }

    @Test
    public void testPredict() {
        cellPredictModel.predict(60 * 1000);
    }

}
