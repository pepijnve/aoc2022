import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class Day17 {
    private static class Shape {
        private final int width;
        private final int height;
        private final int[] shape;

        public Shape(String lines) {
            char[][] grid = Arrays.stream(lines.split("\n")).map(String::toCharArray).toArray(char[][]::new);
            this.shape = new int[grid.length];
            this.width = grid[0].length;
            this.height = grid.length;
            for (int y = 0; y < grid.length; y++) {
                char[] chars = grid[y];
                for (int x = 0; x < chars.length; x++) {
                    char c = chars[x];
                    if (c == '#') {
                        shape[height - y - 1] |= (1 << x);
                    }
                }
            }
        }

        public boolean filled(int x, int y) {
            if (x < 0 || x >= width || y < 0 || y >= height) {
                return false;
            }
            return (shape[y] & (1 << x)) != 0;
        }

        public int asGridRow(int x, int y) {
            return shape[y] << x;
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }
    }

    private static final Shape SHAPE1 = new Shape(
            "####"
    );

    private static final Shape SHAPE2 = new Shape("""
            .#.
            ###
            .#.
            """
    );

    private static final Shape SHAPE3 = new Shape("""
            ..#
            ..#
            ###
            """
    );

    private static final Shape SHAPE4 = new Shape("""
            #
            #
            #
            #
            """
    );

    private static final Shape SHAPE5 = new Shape("""
            ##
            ##
            """
    );


    private static class ShapeSupplier implements Supplier<Shape> {
        private final List<Shape> shapes;
        private int index;

        public ShapeSupplier(Shape... shapes) {
            this.shapes = List.of(shapes);
        }

        public int index() {
            return index;
        }

        @Override
        public Shape get() {
            Shape s = shapes.get(index);
            index = index + 1;
            if (index >= shapes.size()) {
                index = 0;
            }
            return s;
        }

        public int size() {
            return shapes.size();
        }
    }

    private static class JetSupplier {
        private final byte[] jets;
        private int index;

        public JetSupplier(byte[] jets) {
            this.jets = jets.clone();
        }

        public int index() {
            return index;
        }

        public byte get() {
            byte j = jets[index];
            index = index + 1;
            if (index >= jets.length) {
                index = 0;
            }
            return j;
        }

        public int size() {
            return jets.length;
        }
    }

    private static class Grid {
        private final long longMask;
        private final int intMask;
        private final int fullRow;
        private long minY;
        private long maxY;
        int[] grid;

        public Grid() {
            this.grid = new int[4096];
            longMask = this.grid.length - 1;
            intMask = this.grid.length - 1;
            fullRow = 0b1111111;

            grid[0] = fullRow;
        }

        public long minY() {
            return minY;
        }

        public long maxY() {
            return maxY;
        }

        public boolean overlaps(int x, long y, Shape shape) {
            if (x < 0 || x + shape.width() > 7) {
                return true;
            }

            if (y > maxY) {
                return false;
            }

            int height = shape.height();

            long gridY = y;
            for (int shapeY = 0; shapeY < height; shapeY++, gridY++) {
                if (gridY > maxY) {
                    return false;
                }

                int shapeRow = shape.asGridRow(x, shapeY);
                int gridRow = grid[(int) (gridY & longMask)];
                if ((shapeRow & gridRow) != 0) {
                    return true;
                }
            }

            return false;
        }

        public void add(int x, long y, Shape shape) {
            ensureCapacity(y, shape);
            addShape(x, y, shape);
        }

        private void addShape(int x, long y, Shape shape) {
            long cutoffY = -1;
            int gridY = (int)(y & longMask);
            for (int shapeY = 0; shapeY < shape.height(); shapeY++) {
                int rowIndex = (gridY + shapeY) & intMask;

                int shapeRow = shape.asGridRow(x, shapeY);
                int newRow = grid[rowIndex] | shapeRow;

                grid[rowIndex] = newRow;
                if (newRow == fullRow) {
                    cutoffY = y + shapeY;
                }
            }

            if (cutoffY != -1) {
                minY = cutoffY;
            }
        }

        private void ensureCapacity(long y, Shape shape) {
            long maxY = y + shape.height() - 1;

            long newRows = maxY - this.maxY;
            if (newRows > 0) {
                int gridY = (int)((this.maxY + 1) & longMask);

                for (int i = 0; i < newRows; i++) {
                     grid[(gridY + i) & intMask] = 0;
                }

                this.maxY = maxY;
            }
        }

        public boolean filled(int x, long y) {
            if (x < 0 || x > 6 || y < minY || y > maxY) {
                return false;
            }

            return (grid[(int) (y & longMask)] & (1 << x)) != 0;
        }

        public String topRows(int rowCount) {
            StringBuilder b = new StringBuilder(7 * rowCount);
            for (int i = 0; i < rowCount; i++) {
                long y = maxY - i;
                if (y < minY) {
                    break;
                }
                for (int x = 0; x < 7; x++) {
                    b.append(filled(x, y) ? '.' : '#');
                }
            }

            return b.toString();
        }

        public void shift(long deltaHeight) {
            long newMinY = minY + deltaHeight;
            long newMaxY = maxY + deltaHeight;
            int[] newGrid = new int[grid.length];
            for (long oldY = minY, newY = newMinY; oldY <= maxY; oldY++, newY++) {
                newGrid[(int)(newY & longMask)] = grid[(int)(oldY & longMask)];
            }

            minY = newMinY;
            maxY = newMaxY;
            grid = newGrid;
        }
    }

    private static void print(String header, Grid grid, int blockX, long blockY, Shape shape, long maxRows) {
        System.out.println(header);

        long minY = grid.minY();
        long y = Math.max(shape != null ? blockY + shape.height() - 1 : 0, grid.maxY());
        long rows = 0;

        while (y >= minY && rows < maxRows) {
            System.out.printf("%5d ", y);
            System.out.print("|");
            for (int x = 0; x < 7; x++) {
                if (shape != null && y >= blockY && shape.filled(x - blockX, (int)(y - blockY))) {
                    System.out.print("@");
                } else if (grid.filled(x, y)) {
                    System.out.print("#");
                } else {
                    System.out.print(".");
                }
            }
            System.out.println("|");
            y--;
            rows++;
        }

        if (y < 0) {
            System.out.println("      +-------+");
        }
        System.out.println();
    }

    private record Key(int shapeIndex, int jetIndex, String grid) {
    }

    private record Value(long blockCount, long maxY) {
    }

    public static void main(String[] args) throws IOException {
        ShapeSupplier shapes = new ShapeSupplier(SHAPE1, SHAPE2, SHAPE3, SHAPE4, SHAPE5);
        JetSupplier jets = new JetSupplier(Files.readAllBytes(Paths.get("day17_input.txt")));

        Map<Key, Value> cache = new HashMap<>();

//        long nbBlocksToSimulate = 2022;
        long nbBlocksToSimulate = 1000000000000L;

        long nbFallenBlocks = 0;

        Grid fallenBlocks = new Grid();

        int currentX = 2;
        long currentY = fallenBlocks.maxY() + 4;
        int rowsToCache = 100;
        Key key = new Key(
                shapes.index,
                jets.index,
                fallenBlocks.topRows(rowsToCache)
        );
        cache.put(key, new Value(0, fallenBlocks.maxY()));
        Shape currentShape = shapes.get();

        // print("Rock 0 begins falling:", fallenBlocks, currentBlock);
        while (nbFallenBlocks < nbBlocksToSimulate) {

            byte jet = jets.get();

            int newX;
            if (jet == '<') {
                newX = currentX - 1;
            } else {
                newX = currentX + 1;
            };

            if (!fallenBlocks.overlaps(newX, currentY, currentShape)) {
                currentX = newX;
            }

            long newY = currentY - 1;
            if (!fallenBlocks.overlaps(currentX, newY, currentShape)) {
                currentY = newY;
            } else {
                fallenBlocks.add(currentX, currentY, currentShape);
                nbFallenBlocks++;

                // print("Rock " + nbFallenBlocks + " falls 1 unit, causing it to come to rest:", fallenBlocks, null);

                key = new Key(
                        shapes.index,
                        jets.index,
                        fallenBlocks.topRows(rowsToCache)
                );

                Value previousValue = cache.get(key);
                if (previousValue != null) {
                    long deltaBlocks = nbFallenBlocks - previousValue.blockCount();
                    long deltaHeight = fallenBlocks.maxY() - previousValue.maxY();

                    long remainingBlocks = nbBlocksToSimulate - nbFallenBlocks;
                    long numberOfCycles = remainingBlocks / deltaBlocks;

                    nbFallenBlocks += numberOfCycles * deltaBlocks;
                    fallenBlocks.shift(numberOfCycles * deltaHeight);
                } else {
                    cache.put(key, new Value(nbFallenBlocks, fallenBlocks.maxY()));
                }

                currentX = 2;
                currentY = fallenBlocks.maxY() + 4;
                currentShape = shapes.get();

                // print("Rock " + nbFallenBlocks + " begins falling:", fallenBlocks, currentBlock);
            }
        }

        print("End", fallenBlocks, 0, 0, null, Long.MAX_VALUE);
        System.out.println("maxY = " + fallenBlocks.maxY());
    }
}
