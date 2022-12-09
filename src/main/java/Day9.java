import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Day9 {
    public record Position(int x, int y) {
        public Position add(Position p) {
            return new Position(x + p.x(), y + p.y());
        }

        public Position catchUpTo(Position head) {
            int dx = head.x() - x();
            int dy = head.y() - y();
            if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1) {
                return this;
            }

            return new Position(
                    x() + (int) Math.signum(dx),
                    y() + (int) Math.signum(dy)
            );
        }
    }

    public static final Position LEFT = new Position(-1, 0);
    public static final Position RIGHT = new Position(1, 0);
    public static final Position UP = new Position(0, 1);
    public static final Position DOWN = new Position(0, -1);

    public static void main(String[] args) throws IOException {
        Position[] knots = new Position[10];
        for (int i = 0; i < knots.length; i++) {
            knots[i] = new Position(0, 0);
        }
        Set<Position> tailPositions = new HashSet<>();
        tailPositions.add(knots[knots.length - 1]);

        List<String> lines = Files.readAllLines(Paths.get("day9_input.txt"));
        for (String line : lines) {
            String[] directionCount = line.split(" ");
            Position direction = switch (directionCount[0]) {
                case "L" -> LEFT;
                case "R" -> RIGHT;
                case "U" -> UP;
                case "D" -> DOWN;
                default -> throw new IllegalArgumentException(directionCount[0]);
            };

            int count = Integer.parseInt(directionCount[1]);
            for (int i = 0; i < count; i++) {
                knots[0] = knots[0].add(direction);
                for (int j = 1; j < knots.length; j++) {
                    knots[j] = knots[j].catchUpTo(knots[j - 1]);
                }
                tailPositions.add(knots[knots.length - 1]);
            }
        }

        System.out.println(tailPositions.size());
    }
}
