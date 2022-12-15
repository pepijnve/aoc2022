import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day15 {
    public static void main(String[] args) throws IOException {
        List<Entry> entries = Files.readAllLines(Paths.get("day15_input.txt"))
                .stream()
                .map(Day15::parse)
                .sorted(Comparator.comparingInt(Entry::minX))
                .toList();

        int row = 2000000;
        int minX = entries.stream().mapToInt(Entry::minX).min().orElse(0);
        int maxX = entries.stream().mapToInt(Entry::maxX).max().orElse(0);
        int locationCount = 0;
        for (int x = minX; x <= maxX; x++) {
            Point location = new Point(x, row);
            if (entries.stream().anyMatch(e -> e.withinSensorReach(location) && !e.closestBeacon().location().equals(location))) {
                locationCount++;
            }
        }
        System.out.println("locationCount = " + locationCount);

        for (int y = 0; y <= 4000000; y++) {
            for (int x = 0; x <= 4000000; x++) {
                Entry overlappingEntry = null;

                for (Entry entry : entries) {
                    if (entry.withinSensorReach(x, y)) {
                        overlappingEntry = entry;
                        break;
                    }
                }

                if (overlappingEntry != null) {
                    int radius = overlappingEntry.radius();
                    int deltaY = Math.abs(y - overlappingEntry.sensor().location().y());
                    int deltaX = radius - deltaY;
                    x = overlappingEntry.sensor().location().x() + deltaX;
                } else {
                    System.out.println("x = " + x);
                    System.out.println("y = " + y);
                    long frequency = 4000000L * x + y;
                    System.out.println("frequency = " + frequency);
                }
            }
        }
    }

    private record Point(int x, int y) {
        int distance(Point other) {
            return distance(other.x, other.y);
        }

        public int distance(int x, int y) {
            return Math.abs(this.x - x) + Math.abs(this.y - y);
        }
    }

    private record Sensor(Point location) {
    }

    private record Beacon(Point location) {
    }

    private record Entry(Sensor sensor, Beacon closestBeacon, int radius) {
        private Entry(Sensor sensor, Beacon closestBeacon) {
            this(
                    sensor,
                    closestBeacon,
                    sensor.location().distance(closestBeacon.location())
            );
        }

        public int minX() {
            return minX(sensor.location().y());
        }

        public int minX(int y) {
            int radius = radius();
            int deltaY = Math.abs(y - sensor().location().y());
            int deltaX = Math.max(0, radius - deltaY);
            return sensor().location().x() - deltaX;
        }

        public int maxX() {
            return maxX(sensor.location().y());
        }

        public int maxX(int y) {
            int radius = radius();
            int deltaY = Math.abs(y - sensor().location().y());
            int deltaX = Math.max(0, radius - deltaY);
            return sensor().location().x() + deltaX;
        }

        public boolean withinSensorReach(Point location) {
            return withinSensorReach(location.x(), location.y());
        }

        public boolean withinSensorReach(int x, int y) {
            int distance = sensor().location().distance(x, y);
            return distance <= this.radius();
        }
    }

    private static final Pattern ENTRY = Pattern.compile("Sensor at x=(?<sensorx>-?[0-9]+), y=(?<sensory>-?[0-9]+): closest beacon is at x=(?<beaconx>-?[0-9]+), y=(?<beacony>-?[0-9]+)");

    private static Entry parse(String s) {
        Matcher matcher = ENTRY.matcher(s);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(s);
        }
        return new Entry(
                new Sensor(
                        new Point(
                                Integer.parseInt(matcher.group("sensorx")),
                                Integer.parseInt(matcher.group("sensory"))
                        )
                ),
                new Beacon(
                        new Point(
                                Integer.parseInt(matcher.group("beaconx")),
                                Integer.parseInt(matcher.group("beacony"))
                        )
                )
        );
    }
}
