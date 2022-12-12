import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Day12 {
    private record Position(int x, int y) {
        public Position left() {
            return new Position(x - 1, y);
        }

        public Position right() {
            return new Position(x + 1, y);
        }

        public Position up() {
            return new Position(x, y - 1);
        }

        public Position down() {
            return new Position(x, y + 1);
        }

        public int manhattanDistance(Position position) {
            return Math.abs(position.x() - x()) + Math.abs(position.y() - y());
        }
    }

    private static class Grid {
        private final int[] grid;
        private final int width;
        private final int height;

        public Grid(int width, int height, int initialValue) {
            grid = new int[width * height];
            Arrays.fill(grid, initialValue);
            this.width = width;
            this.height = height;
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }

        public boolean isValidPosition(Position p) {
            return p.x >= 0 && p.x < width && p.y >= 0 && p.y < height;
        }

        public void set(Position p, int value) {
            grid[p.y * width + p.x] = value;
        }

        public int get(Position p) {
            return get(p.x, p.y);
        }

        public int get(int x, int y) {
            return grid[y * width + x];
        }
    }

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("day12_input.txt"));

        int width = lines.get(0).length();
        int height = lines.size();

        Grid grid = new Grid(width, height, 0);
        Position start = null;
        Position end = null;

        for (int y = 0; y < height; y++) {
            char[] line = lines.get(y).toCharArray();
            for (int x = 0; x < width; x++) {
                Position p = new Position(x, y);
                char c = line[x];
                if (c == 'S') {
                    start = p;
                    grid.set(p, 0);
                } else if (c == 'E') {
                    end = p;
                    grid.set(p, 25);
                } else {
                    grid.set(p, c - 'a');
                }
            }
        }

        int shortestPathFromStart = find(grid, start, end);

        int shortestPathFromAnyA = Integer.MAX_VALUE;
        for (int y = 0; y < grid.height(); y++) {
            for (int x = 0; x < grid.width(); x++) {
                int value = grid.get(x, y);
                if (value == 0) {
                    int length = find(grid, new Position(x, y), end);
                    if (length != -1 && length < shortestPathFromAnyA) {
                        shortestPathFromAnyA = length;
                    }
                }
            }
        }

        System.out.println("shortestPathFromStart = " + shortestPathFromStart);
        System.out.println("shortestPathFromAnyA = " + shortestPathFromAnyA);
    }

    private static int find(Grid heightMap, Position start, Position end) {
        Grid pathLengths = new Grid(heightMap.width(), heightMap.height(), Integer.MAX_VALUE);

        Queue<Position> candidates = new PriorityQueue<>(Comparator.comparingInt(p -> p.manhattanDistance(end) + pathLengths.get(p)));
        candidates.add(start);

        pathLengths.set(start, 0);

        while (!candidates.isEmpty()) {
            Position p = candidates.remove();
            if (p.equals(end)) {
                return pathLengths.get(p);
            }

            addIfValidStep(heightMap, p, p.left(), candidates, pathLengths);
            addIfValidStep(heightMap, p, p.right(), candidates, pathLengths);
            addIfValidStep(heightMap, p, p.up(), candidates, pathLengths);
            addIfValidStep(heightMap, p, p.down(), candidates, pathLengths);
        }

        return -1;
    }

    private static void addIfValidStep(Grid heightMap, Position current, Position next, Collection<Position> candidates, Grid pathLengths) {
        if (!heightMap.isValidPosition(next)) {
            return;
        }

        int currentLength = pathLengths.get(current);
        int nextLength = currentLength + 1;
        int existingLength = pathLengths.get(next);
        if (existingLength <= nextLength) {
            return;
        }

        int currentHeight = heightMap.get(current);
        int nextHeight = heightMap.get(next);
        if (nextHeight - currentHeight > 1) {
            return;
        }

        pathLengths.set(next, nextLength);
        candidates.add(next);
    }
}
