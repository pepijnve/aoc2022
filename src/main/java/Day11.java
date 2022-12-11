import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.LongUnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day11 {
    private static class Monkey {
        private final int index;
        private final Deque<Long> items = new LinkedList<>();
        private final LongUnaryOperator operation;

        private final long divisibleBy;
        private final int trueMonkey;
        private final int falseMonkey;

        private int itemsInspected = 0;

        public Monkey(int index, List<Long> startingItems, LongUnaryOperator operation, int divisibleBy, int trueMonkey, int falseMonkey) {
            this.index = index;
            items.addAll(startingItems);
            this.operation = operation;
            this.divisibleBy = divisibleBy;
            this.trueMonkey = trueMonkey;
            this.falseMonkey = falseMonkey;
        }

        public int getItemsInspected() {
            return itemsInspected;
        }

        public void passItems(List<Monkey> monkeyPack, LongUnaryOperator worryReduction) {
            if (items.isEmpty()) {
                return;
            }


            while(!items.isEmpty()) {
                itemsInspected++;
                long worryLevel = items.removeFirst();
                worryLevel = operation.applyAsLong(worryLevel);
                worryLevel = worryReduction.applyAsLong(worryLevel);
                Monkey otherMonkey = monkeyPack.get(worryLevel % divisibleBy == 0 ? trueMonkey : falseMonkey);
                otherMonkey.items.add(worryLevel);
            }
        }

        @Override
        public String toString() {
            return "Monkey " + index + ": " + items;
        }
    }

    private static final Pattern HEADER = Pattern.compile("\\s*Monkey (?<index>\\d+):");
    private static final Pattern STARTING = Pattern.compile("\\s*Starting items: (?<list>.*)");
    private static final Pattern OPERATION = Pattern.compile("\\s*Operation: new = old (?<operation>[*+]) (?<operand>\\d+|old)");
    private static final Pattern TEST = Pattern.compile("\\s*Test: divisible by (?<operand>\\d+)");
    private static final Pattern TRUE = Pattern.compile("\\s*If true: throw to monkey (?<operand>\\d+)");
    private static final Pattern FALSE = Pattern.compile("\\s*If false: throw to monkey (?<operand>\\d+)");

    private static List<Monkey> parseInput(BufferedReader reader) throws IOException {
        List<Monkey> monkeys = new ArrayList<>();

        String line;
        while((line = reader.readLine()) != null) {
            Matcher matcher;

            matcher = HEADER.matcher(line);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(line);
            }
            int index = Integer.parseInt(matcher.group("index"));

            line = reader.readLine();
            matcher = STARTING.matcher(line);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(line);
            }
            List<Long> items = Arrays.stream(matcher.group("list").split(", ")).map(Long::parseLong).toList();

            line = reader.readLine();
            matcher = OPERATION.matcher(line);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(line);
            }
            String operation = matcher.group("operation");
            String operand = matcher.group("operand");
            boolean add = operation.equals("+");

            LongUnaryOperator op;
            if (add) {
                if (operand.equals("old")) {
                    op = worry -> worry + worry;
                } else {
                    long amount = Long.parseLong(operand);
                    op = worry -> worry + amount;
                }
            } else {
                if (operand.equals("old")) {
                    op = worry -> worry * worry;
                } else {
                    long amount = Long.parseLong(operand);
                    op = worry -> worry * amount;
                }
            }

            line = reader.readLine();
            matcher = TEST.matcher(line);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(line);
            }
            int divisibleBy = Integer.parseInt(matcher.group("operand"));

            line = reader.readLine();
            matcher = TRUE.matcher(line);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(line);
            }
            int trueMonkey = Integer.parseInt(matcher.group("operand"));

            line = reader.readLine();
            matcher = FALSE.matcher(line);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(line);
            }
            int falseMonkey = Integer.parseInt(matcher.group("operand"));

            line = reader.readLine();

            monkeys.add(new Monkey(index, items, op, divisibleBy, trueMonkey, falseMonkey));
        }

        return monkeys;
    }
    public static void main(String[] args) throws IOException {
        List<Monkey> monkeys;
        try (var r = Files.newBufferedReader(Paths.get("day11_input.txt"))) {
            monkeys = parseInput(r);
        }


//        int numberOfRounds = 20;
//      LongUnaryOperator worryReduction = worryLevel -> worryLevel / 3;
        int numberOfRounds = 10000;

        // Not really the LCM, but good enough
        long leastCommonMultiple = monkeys.stream().mapToLong(m -> m.divisibleBy).reduce(1, (l, r) -> l * r);
        LongUnaryOperator worryReduction = worryLevel -> worryLevel % leastCommonMultiple;

        for (int round = 0; round < numberOfRounds; round++) {
            for (int i = 0; i < monkeys.size(); i++) {
                monkeys.get(i).passItems(monkeys, worryReduction);
            }

            System.out.println("Round " + round);
            for (Monkey monkey : monkeys) {
                System.out.println(monkey);
            }
            System.out.println();
        }

        long monkeyBusiness = monkeys.stream()
                .sorted(Comparator.comparing(Monkey::getItemsInspected).reversed())
                .limit(2)
                .mapToLong(Monkey::getItemsInspected)
                .reduce(1, (left, right) -> left * right);

        System.out.println("monkeyBusiness = " + monkeyBusiness);
    }
}
