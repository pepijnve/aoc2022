import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Day3 {
    public record Item(int c) {
        public int priority() {
            if (c >= 'a' && c <= 'z') {
                return c - 'a' + 1;
            } else if (c >= 'A' && c <= 'Z') {
                return c - 'A' + 27;
            } else {
                throw new IllegalArgumentException();
            }
        }
    }

    public record RuckSack(int group, List<Item> c1, List<Item> c2) {
        List<Item> contents() {
            List<Item> c = new ArrayList<>(c1);
            c.addAll(c2);
            return c;
        }

        public Item incorrectItem() {
            HashSet<Item> set = new HashSet<>(c1);
            set.retainAll(c2);
            return set.iterator().next();
        }

        public static Item commonItem(List<RuckSack> ruckSacks) {
            Set<Item> items = new HashSet<>(ruckSacks.get(0).contents());

            for (int i = 1; i < ruckSacks.size(); i++) {
                items.retainAll(ruckSacks.get(i).contents());
            }

            if (items.size() != 1) {
                throw new IllegalArgumentException();
            }

            return items.iterator().next();
        }
    }

    public static class Grouper {
        private final int groupSize;
        private int group = 1;
        private int count = 0;

        public Grouper(int groupSize) {
            this.groupSize = groupSize;
        }

        public int group() {
            if (count == groupSize) {
                count = 0;
                group++;
            }
            count++;
            return group;
        }
    }

    public static void main(String[] args) throws IOException {
        Grouper grouper = new Grouper(3);

        Map<Integer, List<RuckSack>> groups = Files.readAllLines(Paths.get("day3_input.txt"))
                .stream()
                .map(line -> Day3.parseLine(line, grouper))
                .collect(Collectors.groupingBy(RuckSack::group));

        int sumOfIncorrectItems = groups.values()
                .stream()
                .flatMap(Collection::stream)
                .map(RuckSack::incorrectItem)
                .mapToInt(Item::priority)
                .sum();

        int sumOfBadges = groups.values()
              .stream()
                .map(RuckSack::commonItem)
                .mapToInt(Item::priority)
                .sum();

        System.out.println("sumOfIncorrectItems = " + sumOfIncorrectItems);
        System.out.println("sumOfBadges = " + sumOfBadges);
    }

    public static RuckSack parseLine(String line, Grouper grouping) {
        int split = line.length() / 2;
        String c1 = line.substring(0, split);
        String c2 = line.substring(split);
        if (c1.length() != c2.length()) {
            throw new IllegalArgumentException();
        }

        return new RuckSack(
                grouping.group(),
                parseCompartment(c1),
                parseCompartment(c2)
        );
    }

    private static List<Item> parseCompartment(String compartment) {
        return compartment.chars().mapToObj(Item::new).toList();
    }
}
