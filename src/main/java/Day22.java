import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day22 {
    public enum Direction {
        RIGHT('>'),
        DOWN('v'),
        LEFT('<'),
        UP('^');

        private final char value;

        Direction(char c) {
            value = c;
        }

        public Direction rotateRight() {
            Direction[] values = values();
            int ordinal = ordinal() + 1;
            if (ordinal >= values.length) {
                ordinal = 0;
            }
            return  values[ordinal];
        }

        public Direction rotateLeft() {
            Direction[] values = values();
            int ordinal = ordinal() - 1;
            if (ordinal < 0) {
                ordinal = values.length - 1;
            }
            return  values[ordinal];
        }
    }

    public static class Position {
        public int x;
        public int y;

        public Direction direction;

        public Position(int x, int y, Direction d) {
            this.x = x;
            this.y = y;
            this.direction = d;
        }
    }

    public interface Surface {
        default void set(Position p, char value) {
            set(p.x, p.y, value);
        }

        void set(int x, int y, char value);

        default char get(Position p) {
            return get(p.x, p.y);
        }

        char get(int x, int y);

        boolean move(Position from, Position out);

        void print(PrintStream out);
    }

    public static class Grid implements Surface {
        private final char[] grid;
        private final int width;
        private final int height;

        public Grid(int width, int height) {
            this.width = width;
            this.height = height;
            grid = new char[width * height];
            Arrays.fill(grid, ' ');
        }

        @Override
        public char get(int x, int y) {
            return grid[y * width + x];
        }

        @Override
        public void set(int x, int y, char value) {
            grid[y * width + x] = value;
        }

        @Override
        public boolean move(Position from, Position out) {
            int x = from.x;
            int y = from.y;
            Direction direction = from.direction;

            switch (direction) {
                case LEFT -> {
                    x -= 1;
                    if (x < 0 || get(x, y) == ' ') {
                        x = width - 1;
                        while (get(x, y) == ' ') {
                            x -= 1;
                        }
                    }
                }
                case RIGHT -> {
                    x += 1;
                    if (x >= width || get(x, y) == ' ') {
                        x = 0;
                        while (get(x, y) == ' ') {
                            x += 1;
                        }
                    }
                }
                case UP -> {
                    y -= 1;
                    if (y < 0 || get(x, y) == ' ') {
                        y = height - 1;
                        while (get(x, y) == ' ') {
                            y -= 1;
                        }
                    }
                }
                case DOWN -> {
                    y += 1;
                    if (y >= height || get(x, y) == ' ') {
                        y = 0;
                        while (get(x, y) == ' ') {
                            y += 1;
                        }
                    }
                }
            }

            if (get(x, y) != '#') {
                out.x = x;
                out.y = y;
                out.direction = direction;
                return true;
            } else {
                return false;
            }
        }

        public void print(PrintStream out) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    System.out.print(get(x, y));
                }
                System.out.println();
            }
        }
    }

    private static final int[][] FACE_ADJACENCY = new int[][] {
            {2, 1, 4, 3},
            {2, 5, 4, 0},
            {3, 5, 1, 0},
            {4, 5, 2, 0},
            {1, 5, 3, 0},
            {2, 3, 4, 1},
    };

    public static class Face {
        private final int index;
        private final int faceX;
        private final int faceY;
        private Map<Direction, Integer> neighbours = new EnumMap<>(Direction.class);

        public Face(int index, int faceX, int faceY) {
            this.index = index;
            this.faceX = faceX;
            this.faceY = faceY;
        }
    }

    public static class Cube implements Surface {
        private final Grid grid;
        private final int faceSize;
        private final int[] faceIndex;
        private final Face[] faces;
        private final int faceIndexWidth;
        private final int faceIndexHeight;

        public Cube(Grid grid) {
            this.grid = new Grid(grid.width, grid.height);
            System.arraycopy(grid.grid, 0, this.grid.grid, 0, grid.grid.length);

            if (grid.width > grid.height) {
                faceSize = grid.width / 4;
            } else {
                faceSize = grid.height / 4;
            }

            faceIndexWidth = (grid.width / faceSize);
            faceIndexHeight = (grid.height / faceSize);
            faceIndex = new int[faceIndexWidth * faceIndexHeight];
            Arrays.fill(faceIndex, -1);

            faces = new Face[6];

            int faceY = 0;
            int faceX = 0;
            for (; faceX < faceIndexWidth; faceX++) {
                 if (get(faceX * faceSize, faceY) != ' ') {
                     break;
                 }
            }

            Face f = new Face(0, faceX, faceY);
            f.neighbours.put(Direction.UP, FACE_ADJACENCY[0][0]);
            f.neighbours.put(Direction.RIGHT, FACE_ADJACENCY[0][1]);
            f.neighbours.put(Direction.DOWN, FACE_ADJACENCY[0][2]);
            f.neighbours.put(Direction.LEFT, FACE_ADJACENCY[0][3]);

            faceIndex[faceY * faceIndexHeight + faceX] = f.index;
            faces[f.index] = f;

            populateNeighbours(f);
        }

        private void populateNeighbours(Face face) {
            populateNeighbour(face.faceX - 1, face.faceY, face.neighbours.get(Direction.LEFT), Direction.RIGHT, face.index);
            populateNeighbour(face.faceX + 1, face.faceY, face.neighbours.get(Direction.RIGHT), Direction.LEFT, face.index);
            populateNeighbour(face.faceX, face.faceY - 1, face.neighbours.get(Direction.UP), Direction.DOWN, face.index);
            populateNeighbour(face.faceX, face.faceY + 1, face.neighbours.get(Direction.DOWN), Direction.UP, face.index);
        }

        private void populateNeighbour(int faceX, int faceY, int index, Direction edge, int neighbour) {
            if (faces[index] != null) {
                return;
            }

            if (faceX < 0 || faceX >= faceIndexWidth) {
                return;
            }

            if (faceY < 0 || faceY >= faceIndexHeight) {
                return;
            }

            if (get(faceX * faceSize, faceY * faceSize) == ' ') {
                return;
            }

            Face f = new Face(index, faceX, faceY);
            faces[index] = f;
            faceIndex[faceY * faceIndexWidth + faceX] = index;

            int[] adjacency = FACE_ADJACENCY[index];
            int adjacencyIndex = -1;
            for (int i = 0; i < adjacency.length; i++) {
                if (adjacency[i] == neighbour) {
                    adjacencyIndex = i;
                    break;
                }
            }

            for (int i = 0; i < 4; i++) {
                f.neighbours.put(edge, adjacency[(adjacencyIndex + i) % 4]);
                edge = edge.rotateRight();
            }

            populateNeighbours(f);
        }

        @Override
        public char get(int x, int y) {
            return grid.get(x, y);
        }

        @Override
        public void set(int x, int y, char value) {
            grid.set(x, y, value);
        }

        @Override
        public boolean move(Position from, Position out) {
            int faceX = from.x % faceSize;
            int faceY = from.y % faceSize;
            Direction direction = from.direction;

            return switch (direction) {
                case LEFT -> faceX == 0 ? crossEdge(direction, from, out) : grid.move(from, out);
                case RIGHT -> faceX == faceSize - 1 ? crossEdge(direction, from, out) : grid.move(from, out);
                case UP -> faceY == 0 ? crossEdge(direction, from, out) : grid.move(from, out);
                case DOWN -> faceY == faceSize - 1 ? crossEdge(direction, from, out) : grid.move(from, out);
            };
        }

        private boolean crossEdge(Direction outgoingEdge, Position from, Position out) {
            int relX = from.x % faceSize;
            int relY = from.y % faceSize;

            Face currentFace = faces[faceIndex[(from.y / faceSize) * faceIndexWidth + (from.x / faceSize)]];
            Face nextFace = faces[currentFace.neighbours.get(outgoingEdge)];
            Direction incomingEdge = null;
            for (Map.Entry<Direction, Integer> entry : nextFace.neighbours.entrySet()) {
                if (entry.getValue() == currentFace.index) {
                    incomingEdge = entry.getKey();
                    break;
                }
            }

            int distanceAlongEdge = switch (outgoingEdge) {
                case UP -> faceSize - relX - 1;
                case RIGHT -> faceSize - relY - 1;
                case DOWN -> relX;
                case LEFT -> relY;
            };

            int newRelX = 0;
            int newRelY = 0;
            switch (incomingEdge) {
                case UP -> {
                    newRelX = distanceAlongEdge;
                    newRelY = 0;
                }
                case RIGHT -> {
                    newRelX = faceSize - 1;
                    newRelY = distanceAlongEdge;
                }
                case DOWN -> {
                    newRelX = faceSize - 1 - distanceAlongEdge;
                    newRelY = faceSize - 1;
                }
                case LEFT -> {
                    newRelX = 0;
                    newRelY = faceSize - 1 - distanceAlongEdge;
                }
            }

            int newX = nextFace.faceX * faceSize + newRelX;
            int newY = nextFace.faceY * faceSize + newRelY;

            if (grid.get(newX, newY) != '#') {
                out.x = newX;
                out.y = newY;
                out.direction = incomingEdge.rotateRight().rotateRight();
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void print(PrintStream out) {
            grid.print(out);
        }
    }

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("day22_input.txt"));
        String path = lines.remove(lines.size() - 1);
        // blank
        lines.remove(lines.size() - 1);

        int width = lines.stream().mapToInt(String::length).max().orElse(0);
        int height = lines.size();
        Grid g = new Grid(width, height);
        for (int y = 0; y < lines.size(); y++) {
            String row = lines.get(y);
            for (int x = 0; x < row.length(); x++) {
                char c = row.charAt(x);
                if (c != ' ') {
                     g.set(x, y, c);
                 }
            }
        }
        Cube c = new Cube(g);

        System.out.println("part1 = " + getPassword(followPath(g, path)));
        System.out.println("part2 = " + getPassword(followPath(c, path)));
    }

    private static int getPassword(Position p) {
        int column = p.x + 1;
        int row = p.y + 1;
        int facing = p.direction.ordinal();
        return 1000 * row + 4 * column + facing;
    }

    private static Position followPath(Surface s, String path) {
        Position p = new Position(0, 0, Direction.RIGHT);

        while(s.get(p) == ' ') {
            p.x++;
        }
        s.set(p, p.direction.value);

        Matcher step = Pattern.compile("R|L|[0-9]+").matcher(path);
        while(step.find()) {
            String value = step.group();
            char c = value.charAt(0);
            if (c == 'R') {
                p.direction = p.direction.rotateRight();
                s.set(p, p.direction.value);
            } else if (c == 'L') {
                p.direction = p.direction.rotateLeft();
                s.set(p, p.direction.value);
            } else {
                int steps = Integer.parseInt(value);
                for (int i = 0; i < steps; i++) {
                    if (s.move(p, p)) {
                        s.set(p, p.direction.value);
                    } else {
                        break;
                    }
                }
            }
        }
        return p;
    }
}
