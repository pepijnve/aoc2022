import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;

public class Day24 {
    private record Position(int x, int y) {
        public int distance(Position p) {
            return Math.abs(p.x - x) + Math.abs(p.y - y);
        }
    }

    private enum Direction {
        UP, DOWN, LEFT, RIGHT, WAIT
    }

    private record Blizzard(Direction direction, Position position) {
        public int encode() {
            return encodeBlizzard(position.x, position.y, direction);
        }
    }

    public static int encodeBlizzard(int x, int y, Direction direction) {
        return (direction.ordinal() << 16) | x << 8 | y;
    }

    public static int x(int encoded) {
        return (encoded >> 8) & 0xFF;
    }

    public static int y(int encoded) {
        return encoded & 0xFF;
    }

    public static Direction direction(int encoded) {
        return Direction.values()[(encoded >> 16) & 0xFF];
    }

    private static class Valley {
        private final int width;
        private final int height;
        private final List<int[]> blizzards;

        public Valley(int width, int height, int[] initial) {
            this.width = width;
            this.height = height;
            this.blizzards = new ArrayList<>();
            this.blizzards.add(initial);
        }

        public int width() {
            return width;
        }

        public int height() {
            return height;
        }

        public int[] blizzards(int steps) {
            if (steps < blizzards.size()) {
                return blizzards.get(steps);
            }

            int[] previous = blizzards(steps - 1);
            int[] next = simulate(this, previous);
            blizzards.add(next);
            return next;
        }
    }

    private record Situation(Valley valley, Position player, int steps) {
        public int score(Position goal) {
            return steps + goal.distance(player);
        }

        public int[] blizzards() {
            return valley.blizzards(steps);
        }
    }

    private static void print(Situation s) {
        Valley v = s.valley();
        System.out.print('#');
        for (int i = 0; i < v.width; i++) {
            if (i == 0) {
                if (s.player.equals(new Position(0, -1))) {
                    System.out.print('E');
                } else {
                    System.out.print('.');
                }
            } else {
                System.out.print('#');
            }
        }
        System.out.println('#');

        for (int y = 0; y < v.height; y++) {
            System.out.print('#');
            for (int x = 0; x < v.width; x++) {
                Position p = new Position(x, y);
                if (s.player.equals(p)) {
                    System.out.print('E');
                } else {
                    List<Blizzard> blizzards = Arrays.stream(s.blizzards())
                            .filter(b -> x(b) == p.x && y(b) == p.y)
                            .mapToObj(b -> new Blizzard(direction((b)), new Position(x(b), y(b))))
                            .toList();
                    if (blizzards.size() == 0) {
                        System.out.print('.');
                    } else if (blizzards.size() == 1) {
                        System.out.print(switch (blizzards.get(0).direction()) {
                            case UP -> '^';
                            case DOWN -> 'v';
                            case LEFT -> '<';
                            case RIGHT -> '>';
                            case WAIT -> 'X';
                        });
                    } else {
                        System.out.print(blizzards.size());
                    }
                }
            }
            System.out.println('#');
        }

        System.out.print('#');
        for (int i = 0; i < v.width; i++) {
            if (i == v.width - 1) {
                System.out.print('.');
            } else {
                System.out.print('#');
            }
        }
        System.out.println('#');
    }

    public static void main(String[] args) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("day24_input.txt"));

        int width = lines.get(0).length() - 2;
        int height = lines.size() - 2;

        List<Blizzard> blizzards = new ArrayList<>();

        List<String> innerLines = lines.subList(1, lines.size() - 1);
        for (int y = 0; y < innerLines.size(); y++) {
            String line = innerLines.get(y);
            char[] chars = line.substring(1, line.length() - 1).toCharArray();
            for (int x = 0; x < chars.length; x++) {
                Direction d = switch (chars[x]) {
                    case '<' -> Direction.LEFT;
                    case '>' -> Direction.RIGHT;
                    case '^' -> Direction.UP;
                    case 'v' -> Direction.DOWN;
                    default -> null;
                };

                if (d != null) {
                    blizzards.add(new Blizzard(d, new Position(x, y)));
                }
            }
        }

        Position start = new Position(0, -1);
        Position startGoal = new Position(0, 0);
        Position end = new Position(width - 1, height);
        Position endGoal = new Position(width - 1, height - 1);

        Valley valley = new Valley(width, height, blizzards.stream().mapToInt(Blizzard::encode).toArray());

        Situation initial = new Situation(valley, start, 0);

        Situation phase1 = find(initial, endGoal, end);
        Situation phase2 = find(phase1, startGoal, start);
        Situation phase3 = find(phase2, endGoal, end);

        System.out.println("part1 = " + phase1.steps);
        System.out.println("part2 = " + phase3.steps);
    }

    private static Situation find(Situation initial, Position goal, Position actualGoal) {
        Situation almostThere = find(initial, goal);
        if (almostThere == null) {
            return null;
        }

        return new Situation(almostThere.valley, actualGoal, almostThere.steps + 1);
    }

    private static Situation find(Situation initial, Position goal) {
        Comparator<Situation> comparator = Comparator.comparingInt(s -> s.score(goal));
        PriorityQueue<Situation> candidates = new PriorityQueue<>(comparator);
        Set<Situation> visited = new HashSet<>();
        visited.add(initial);
        candidates.add(initial);

        Consumer<Situation> add = s -> {
            if (!visited.contains(s)) {
                candidates.add(s);
                visited.add(s);
            }
        };

        while (!candidates.isEmpty()) {
            Situation s = candidates.remove();
            if (s.player().equals(goal)) {
                return s;
            }

            int[] blizzards = s.valley().blizzards(s.steps + 1);
            addIfPossible(s, blizzards, Direction.UP, add);
            addIfPossible(s, blizzards, Direction.DOWN, add);
            addIfPossible(s, blizzards, Direction.LEFT, add);
            addIfPossible(s, blizzards, Direction.RIGHT, add);
            addIfPossible(s, blizzards, Direction.WAIT, add);
        }

        return null;
    }

    private static void addIfPossible(Situation oldSituation, int[] blizzards, Direction direction, Consumer<Situation> acceptNew) {
        Position newPlayer = moveIfPossible(oldSituation.valley, oldSituation.player, direction);
        if (newPlayer == null) {
            return;
        }

        for (int blizzard : blizzards) {
            int x = x(blizzard);
            int y = y(blizzard);
            if (newPlayer.x == x && newPlayer.y == y) {
                return;
            }
        }

        Situation newSituation = new Situation(oldSituation.valley, newPlayer, oldSituation.steps + 1);

//        System.out.println("From " + direction);
//        print(oldSituation);
//        System.out.println();
//
//        System.out.println("Minute " + newSituation.steps + ", " + direction);
//        print(newSituation);
//        System.out.println();

        acceptNew.accept(newSituation);
    }

    private static int[] simulate(Valley valley, int[] blizzards) {
        int[] newBlizzards = new int[blizzards.length];
        for (int i = 0; i < blizzards.length; i++) {
            newBlizzards[i] = moveAndWrap(valley, blizzards[i]);
        }
        return newBlizzards;
    }

    private static int moveAndWrap(Valley valley, int encodedBlizzard) {
        Direction direction = direction(encodedBlizzard);
        int oldX = x(encodedBlizzard);
        int oldY = y(encodedBlizzard);

        return switch (direction) {
            case UP -> {
                int y = oldY - 1;
                if (y < 0) {
                    y = valley.height() - 1;
                }
                yield encodeBlizzard(oldX, y, direction);
            }
            case DOWN -> {
                int y = oldY + 1;
                if (y >= valley.height()) {
                    y = 0;
                }
                yield encodeBlizzard(oldX, y, direction);
            }
            case LEFT -> {
                int x = oldX - 1;
                if (x < 0) {
                    x = valley.width() - 1;
                }
                yield encodeBlizzard(x, oldY, direction);
            }
            case RIGHT -> {
                int x = oldX + 1;
                if (x >= valley.width()) {
                    x = 0;
                }
                yield encodeBlizzard(x, oldY, direction);
            }
            case WAIT -> encodedBlizzard;
        };
    }

    private static Position moveIfPossible(Valley valley, Position position, Direction direction) {
        int x = position.x;
        int y = position.y;
        return switch (direction) {
            case UP -> {
                y = y - 1;
                if (x < 0 || x >= valley.width() || y < 0 || y >= valley.height()) {
                    yield null;
                }
                yield new Position(x, y);
            }
            case DOWN -> {
                y = y + 1;
                if (x < 0 || x >= valley.width() || y < 0 || y >= valley.height()) {
                    yield null;
                }
                yield new Position(x, y);
            }
            case LEFT -> {
                x = x - 1;
                if (x < 0 || x >= valley.width() || y < 0 || y >= valley.height()) {
                    yield null;
                }
                yield new Position(x, y);
            }
            case RIGHT -> {
                x = x + 1;
                if (x < 0 || x >= valley.width() || y < 0 || y >= valley.height()) {
                    yield null;
                }
                yield new Position(x, y);
            }
            case WAIT -> position;
        };
    }
}
