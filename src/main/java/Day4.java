import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Day4 {
    public record SectionRange(int start, int end) {
        public boolean contains(SectionRange other) {
            return other.start >= start && other.end <= end;
        }

        public boolean overlaps(SectionRange other) {
            return start <= other.end && end >= other.start;
        }
    }

    public record Pair(SectionRange e1, SectionRange e2) {
        public boolean rangesFullyOverlap() {
            return e1.contains(e2) || e2.contains(e1);
        }

        public boolean rangesPartiallyOverlap() {
            return e1.overlaps(e2);
        }
    }

    public static void main(String[] args) throws IOException {
        long overlappingCount = Files.readAllLines(Paths.get("day4_input.txt"))
                .stream()
                .map(Day4::parsePair)
                .filter(Pair::rangesPartiallyOverlap)
                .count();

        System.out.println("overlappingCount = " + overlappingCount);
    }

    private static Pair parsePair(String s) {
        String[] ranges = s.split(",");
        return new Pair(
                parseRange(ranges[0]),
                parseRange(ranges[1])
        );
    }

    private static SectionRange parseRange(String range) {
        String[] startEnd = range.split("-");
        return new SectionRange(
                Integer.parseInt(startEnd[0]),
                Integer.parseInt(startEnd[1])
        );
    }
}
