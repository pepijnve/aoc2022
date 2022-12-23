import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class Day23 {
    private static class Elf {
        public Position position;
        public Position nextPosition;

        public Elf(Position position) {
            this.position = position;
        }

        public Position planNextPosition(Deque<BiFunction<Position[], boolean[], Position>> computeNext, Set<Position> currentPositions) {
            Position nextPosition = null;

            Position[] possiblePositions = position.possibleMovements();
            boolean[] freePositions = new boolean[possiblePositions.length];
            boolean allFree = true;
            for (int i = 0; i < freePositions.length; i++) {
                boolean free = !currentPositions.contains(possiblePositions[i]);
                freePositions[i] = free;
                if (i != Position.X) {
                    allFree &= free;
                }
            }

            if (!allFree) {
                for (BiFunction<Position[], boolean[], Position> next : computeNext) {
                    nextPosition = next.apply(possiblePositions, freePositions);
                    if (nextPosition != null) {
                        break;
                    }
                }
            }

            this.nextPosition = nextPosition;
            return nextPosition;
        }

        public boolean move() {
            if (nextPosition != null) {
                position = nextPosition;
                nextPosition = null;
                return true;
            } else {
                return false;
            }
        }
    }

    private record Position(int x, int y) {
        private static final int NW = 0;
        private static final int N = 1;
        private static final int NE = 2;

        private static final int W = 3;
        private static final int X = 4;
        private static final int E = 5;

        private static final int SW = 6;
        private static final int S = 7;
        private static final int SE = 8;
        public Position[] possibleMovements() {
            int n = y - 1;
            int e = x + 1;
            int s = y + 1;
            int w = x - 1;

            return new Position[]{
                    new Position(w, n), new Position(x, n), new Position(e, n),
                    new Position(w, y), this, new Position(e, y),
                    new Position(w, s), new Position(x, s), new Position(e, s)
            };
        }

        public static Position north(Position[] possiblePositions, boolean[] free) {
            if (free[NW] && free[N] && free[NE]) {
                return possiblePositions[N];
            }

            return null;
        }

        public static Position east(Position[] possiblePositions, boolean[] free) {
            if (free[NE] && free[E] && free[SE]) {
                return possiblePositions[E];
            }

            return null;
        }

        public static Position south(Position[] possiblePositions, boolean[] free) {
            if (free[SW] && free[S] && free[SE]) {
                return possiblePositions[S];
            }

            return null;
        }

        public static Position west(Position[] possiblePositions, boolean[] free) {
            if (free[NW] && free[W] && free[SW]) {
                return possiblePositions[W];
            }

            return null;
        }
    }

    public static void print(List<Elf> elves) {
        Position p = elves.get(0).position;
        int minX = p.x;
        int maxX = p.x;
        int minY = p.y;
        int maxY = p.y;
        for (int i = 1; i < elves.size(); i++) {
            p = elves.get(i).position;
            int x = p.x;
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }

            int y = p.y;
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
        }

        print(elves, minX, maxX, minY, maxY);
    }

    private static void print(List<Elf> elves, int minX, int maxX, int minY, int maxY) {
        Set<Position> positions = elves.stream().map(e -> e.position).collect(Collectors.toSet());

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                if (positions.contains(new Position(x, y))) {
                    System.out.print('#');
                } else {
                    System.out.print('.');
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args) throws IOException {
        List<Position> initialPositions = new ArrayList<>();

        List<String> lines = Files.readAllLines(Paths.get("day23_input.txt"));
        for (int y = 0; y < lines.size(); y++) {
            char[] chars = lines.get(y).toCharArray();
            for (int x = 0; x < chars.length; x++) {
                char c = chars[x];
                if (c == '#') {
                    initialPositions.add(new Position(x, y));
                }
            }
        }

        simulate(initialPositions, 10);
        simulate(initialPositions, Integer.MAX_VALUE);
    }

    private static void simulate(List<Position> initialPositions, int maxRounds) {
        List<Elf> elves = initialPositions.stream().map(Elf::new).toList();

        Deque<BiFunction<Position[], boolean[], Position>> computeNext = new ArrayDeque<>();
        computeNext.addLast(Position::north);
        computeNext.addLast(Position::south);
        computeNext.addLast(Position::west);
        computeNext.addLast(Position::east);

        Set<Position> currentPositions = new HashSet<>();

        Map<Position, Elf> collisions = new HashMap<>();

        int round = 0;

        while(round < maxRounds) {
            currentPositions.clear();
            for (Elf elf : elves) {
                currentPositions.add(elf.position);
            }

            collisions.clear();

            for (Elf elf : elves) {
                Position next = elf.planNextPosition(computeNext, currentPositions);

                if (next != null) {
                    Elf previousElf = collisions.put(next, elf);
                    if (previousElf != null) {
                        elf.nextPosition = null;
                        previousElf.nextPosition = null;
                    }
                }
            }

            boolean anyoneMoved = false;
            for (Elf elf : elves) {
                anyoneMoved |= elf.move();
            }

            computeNext.addLast(computeNext.removeFirst());
            round++;

            if (!anyoneMoved) {
                break;
            }
        }

        System.out.println("round = " + round);
        System.out.println("empty = " + calculateFreeTiles(elves));
    }

    private static int calculateFreeTiles(List<Elf> elves) {
        Position p = elves.get(0).position;
        int minX = p.x;
        int maxX = p.x;
        int minY = p.y;
        int maxY = p.y;
        for (int i = 1; i < elves.size(); i++) {
            p = elves.get(i).position;
            int x = p.x;
            if (x < minX) {
                minX = x;
            }
            if (x > maxX) {
                maxX = x;
            }

            int y = p.y;
            if (y < minY) {
                minY = y;
            }
            if (y > maxY) {
                maxY = y;
            }
        }

        int area = (maxX - minX + 1) * (maxY - minY + 1);
        return area - elves.size();
    }
}
