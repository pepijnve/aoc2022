import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Day18 {
    public record Coordinate(int x, int y, int z) {
        public List<Coordinate> neighbours() {
            return List.of(
                    new Coordinate(x, y + 1, z),
                    new Coordinate(x, y - 1, z),
                    new Coordinate(x - 1, y, z),
                    new Coordinate(x + 1, y, z),
                    new Coordinate(x, y, z + 1),
                    new Coordinate(x, y, z - 1)
            );
        }
    }

    public static void main(String[] args) throws IOException {
        List<Coordinate> coordinates = Files.readAllLines(Paths.get("day18_input.txt"))
                .stream()
                .map(Day18::parse)
                .toList();

        System.out.println("non touching = " + nonTouchingSurfaces(coordinates));
        System.out.println("exposed = " + exposedSurfaces(coordinates));
    }

    private static int nonTouchingSurfaces(List<Coordinate> coordinates) {
        Set<Coordinate> coordSet = new HashSet<>(coordinates);
        int surface = 0;
        for (Coordinate c : coordSet) {
            for (Coordinate neighbour : c.neighbours()) {
                if (!coordSet.contains(neighbour)) {
                    surface++;
                }
            }
        }
        return surface;
    }

    private static int exposedSurfaces(List<Coordinate> coordinates) {
        int minX = coordinates.stream().mapToInt(Coordinate::x).min().orElse(0) - 1;
        int maxX = coordinates.stream().mapToInt(Coordinate::x).max().orElse(0) + 1;
        int minY = coordinates.stream().mapToInt(Coordinate::y).min().orElse(0) - 1;
        int maxY = coordinates.stream().mapToInt(Coordinate::y).max().orElse(0) + 1;
        int minZ = coordinates.stream().mapToInt(Coordinate::z).min().orElse(0) - 1;
        int maxZ = coordinates.stream().mapToInt(Coordinate::z).max().orElse(0) + 1;

        Set<Coordinate> coords = new HashSet<>(coordinates);
        Set<Coordinate> water = new HashSet<>();
        Queue<Coordinate> queue = new LinkedList<>();
        Coordinate seed = new Coordinate(minX, minY, minZ);
        queue.add(seed);
        water.add(seed);

        while (!queue.isEmpty()) {
            Coordinate c = queue.remove();
            for (Coordinate neighbour : c.neighbours()) {
                if (neighbour.x() < minX || neighbour.x() > maxX
                        || neighbour.y() < minY || neighbour.y() > maxY
                        || neighbour.z() < minZ || neighbour.z() > maxZ) {
                    continue;
                }
                if (!coords.contains(neighbour) && !water.contains(neighbour)) {
                    water.add(neighbour);
                    queue.add(neighbour);
                }
            }
        }

        int surface = 0;
        for (Coordinate c : coords) {
            for (Coordinate neighbour : c.neighbours()) {
                if (water.contains(neighbour)) {
                    surface++;
                }
            }
        }
        return surface;
    }

    private static Coordinate parse(String s) {
        String[] parts = s.split(",");
        return new Coordinate(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])
        );
    }
}
