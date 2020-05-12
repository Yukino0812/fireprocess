package me.yukino.fireprocess.model;

import cn.hutool.core.io.FileUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import me.yukino.fireprocess.config.CellPredictConfig;
import me.yukino.fireprocess.enumeration.CellBurningStatus;
import me.yukino.fireprocess.util.CellCoordinateConvertor;
import me.yukino.fireprocess.vo.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Hoshiiro Yukino
 */

@SpringBootTest
public class CellPredictModelTest {

    private static CellPredictModel cellPredictModel;
    private static List<SmokeDetectorVo> smokeDetectorVos;

    @BeforeAll
    public static void init() {
        cellPredictModel = new CellPredictModel();
        cellPredictModel.init(CellPredictConfig.dl, initCells());
        initSmokeDetector();
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
        JsonNode fireWallsNode = node.get("firewalls");
        Set<Cell> cellSet = new HashSet<>();

        initFireDoors(cellSet);
        initFireWalls(fireWallsNode, cellSet);
        initWalls(wallsNode, cellSet);
        initRooms(roomsNode, cellSet);
        initOthers(cellSet);

        return new ArrayList<>(cellSet);
    }

    private static void initFireDoors(Set<Cell> cells) {
        File file = FileUtil.file("testRoute.json");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node;
        try {
            node = objectMapper.readTree(file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        node = node.get("node");
        NodeVo[] nodes;
        try {
            nodes = objectMapper.readValue(node.toString(), NodeVo[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }

        Set<Cell> syncCellSet = Collections.synchronizedSet(cells);
        Arrays.stream(nodes).parallel()
                .filter(nodeVo -> "Fd".equals(nodeVo.getName()))
                .forEach(nodeVo -> {
                    int safeRange = 5;
                    for (int iX = 0; iX < safeRange; ++iX) {
                        for (int iY = 0; iY < safeRange; ++iY) {
                            for (int iZ = 0; iZ < safeRange; ++iZ) {
                                int x = CellCoordinateConvertor.toCellIndex(nodeVo.getX());
                                int y = CellCoordinateConvertor.toCellIndex(nodeVo.getY());
                                int z = CellCoordinateConvertor.toCellIndex(nodeVo.getZ());
                                Cell cell = new Cell(x + iX - 2, y + iY - 2, z + iZ - 2,
                                        CellPredictConfig.DEFAULT_V,
                                        CellPredictConfig.DEFAULT_M,
                                        CellPredictConfig.DEFAULT_BURNING_RATE,
                                        CellBurningStatus.NON_COMBUSTIBLE,
                                        CellPredictConfig.DEFAULT_PROPAGATE_PROBABILITY);
                                syncCellSet.add(cell);
                            }
                        }
                    }
                });
    }

    private static void initFireWalls(JsonNode fireWallsNode, Set<Cell> cells) {
        ObjectMapper objectMapper = new ObjectMapper();
        VertexVo[] vertexex;
        try {
            vertexex = objectMapper.readValue(fireWallsNode.toString(), VertexVo[].class);
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
                            for (Cell cell : cellZ) {
                                cell.setBurningStatus(CellBurningStatus.NON_COMBUSTIBLE);
                                syncCellSet.add(cell);
                            }
                        }
                    }
                });
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

    /**
     * 其他位置暂时均视为过道
     *
     * @param cells
     */
    private static void initOthers(Set<Cell> cells) {
        Set<Cell> syncCellSet = Collections.synchronizedSet(cells);
        Cell[][][] cellArray = getCellsBetweenTwoVertexex(globalMinX, globalMinY, globalMinZ, globalMaxX, globalMaxY, globalMaxZ, false);
        for (Cell[][] cellYZ : cellArray) {
            for (Cell[] cellZ : cellYZ) {
                for (Cell cell : cellZ) {
                    cell.setPropagateProbability(0.1);
                    syncCellSet.add(cell);
                }
            }
        }
    }

    private static int globalMinX = Integer.MAX_VALUE;
    private static int globalMinY = Integer.MAX_VALUE;
    private static int globalMinZ = Integer.MAX_VALUE;
    private static int globalMaxX = Integer.MIN_VALUE;
    private static int globalMaxY = Integer.MIN_VALUE;
    private static int globalMaxZ = Integer.MIN_VALUE;

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
        globalMinX = Math.min(globalMinX, minX);
        globalMinY = Math.min(globalMinY, minY);
        globalMinZ = Math.min(globalMinZ, minZ);
        globalMaxX = Math.max(globalMaxX, minX + rangeX);
        globalMaxY = Math.max(globalMaxY, minY + rangeY);
        globalMaxZ = Math.max(globalMaxZ, minZ + rangeZ);
        Cell[][][] cells = new Cell[rangeX + 1][rangeY + 1][rangeZ + 1];
        double propagateProbability = room ? CellPredictConfig.DEFAULT_PROPAGATE_PROBABILITY : 0.01;
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

    private static void initSmokeDetector() {
        smokeDetectorVos = new ArrayList<>();
        File file = FileUtil.file("testMain.json");
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode node;
        try {
            node = objectMapper.readTree(file);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        node = node.get("smokeDetector");
        SmokeDetectorVo[] vos;
        try {
            vos = objectMapper.readValue(node.toString(), SmokeDetectorVo[].class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return;
        }
        smokeDetectorVos = new ArrayList<>(Arrays.asList(vos));
    }

    @Test
    public void testPredict() {
        Random random = new Random();
        int index = random.nextInt(smokeDetectorVos.size());
        SmokeDetectorVo vo = smokeDetectorVos.get(index);
        BurningCellVo burningCellVo = new BurningCellVo(
                CellCoordinateConvertor.toCellIndex(vo.getX()),
                CellCoordinateConvertor.toCellIndex(vo.getY()-2),
                CellCoordinateConvertor.toCellIndex(vo.getZ()));
        List<BurningCellVo> burningCellVos = new ArrayList<>();
        burningCellVos.add(burningCellVo);
        cellPredictModel.fixBurningCells(burningCellVos);
        cellPredictModel.predict(10 * 60 * 1000);
    }

}
