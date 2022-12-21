import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day21 {
    private record Monkey(String name, Function<Map<String, Monkey>, Expression> getExpression) {
    }

    private interface Expression {
        long evaluate();

        boolean containsUnknown();
    }

    private static class Unknown implements Expression  {
        @Override
        public long evaluate() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsUnknown() {
            return true;
        }

        @Override
        public String toString() {
            return "?";
        }
    }

    private record Value(long value) implements Expression  {
        @Override
        public long evaluate() {
            return value();
        }

        @Override
        public boolean containsUnknown() {
            return false;
        }

        @Override
        public String toString() {
            return Long.toString(value());
        }
    }

    private record Calculation(Expression lhs, Operator operator, Expression rhs) implements Expression  {
        @Override
        public long evaluate() {
            long l = lhs.evaluate();
            long r = rhs.evaluate();
            return switch (operator()) {
                case ADD -> l + r;
                case SUBTRACT -> l - r;
                case MULTIPLY -> l * r;
                case DIVIDE -> l / r;
            };
        }

        @Override
        public boolean containsUnknown() {
            return lhs.containsUnknown() || rhs.containsUnknown();
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append('(');
            b.append(lhs);
            b.append(')');
            b.append(switch (operator()) {
                case ADD -> " + ";
                case SUBTRACT -> " - ";
                case MULTIPLY -> " * ";
                case DIVIDE -> " / ";
            });
            b.append('(');
            b.append(rhs);
            b.append(')');
            return b.toString();
        }
    }

    private enum Operator {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE;

        public Operator inverse() {
            return switch (this) {
                case ADD -> SUBTRACT;
                case SUBTRACT -> ADD;
                case MULTIPLY -> DIVIDE;
                case DIVIDE -> MULTIPLY;
            };
        }

        public boolean commutative() {
            return switch (this) {
                case ADD, MULTIPLY -> true;
                case SUBTRACT, DIVIDE -> false;
            };
        }
    }

    public static void main(String[] args) throws IOException {
        Map<String, Monkey> monkeys = new HashMap<>();

        Files.readAllLines(Paths.get("day21_input.txt"))
                .stream()
                .map(Day21::parse)
                .forEach(m -> monkeys.put(m.name(), m));

        Monkey root = monkeys.get("root");

        long part1 = root.getExpression().apply(monkeys).evaluate();
        System.out.println("part1 = " + part1);

        monkeys.put("humn", new Monkey("humn", m -> new Unknown()));
        Expression expression = root.getExpression().apply(monkeys);
        Calculation rootCalculation = (Calculation) expression;
        Expression lhs = rootCalculation.lhs();
        Expression rhs = rootCalculation.rhs();
        Expression unknownExpression;
        if (rhs.containsUnknown()) {
            unknownExpression = extractUnknown(rhs, lhs);
        } else {
            unknownExpression = extractUnknown(lhs, rhs);
        }
        long part2 = unknownExpression.evaluate();
        System.out.println(part2);
    }

    private static Expression extractUnknown(Expression lhs, Expression rhs) {
        if (lhs instanceof Unknown) {
            return rhs;
        } else if (lhs instanceof Value) {
            throw new IllegalStateException();
        }

        Calculation calculation = (Calculation) lhs;
        if (calculation.lhs().containsUnknown()) {
            return extractUnknown(
                    calculation.lhs(),
                    new Calculation(
                        rhs,
                        calculation.operator().inverse(),
                        calculation.rhs()
                    )
            );
        } else {
            if (calculation.operator().commutative()) {
                return extractUnknown(
                        calculation.rhs(),
                        new Calculation(
                                rhs,
                                calculation.operator().inverse(),
                                calculation.lhs()
                        )
                );
            } else {
                return extractUnknown(
                        calculation.rhs(),
                        new Calculation(
                                calculation.lhs(),
                                calculation.operator(),
                                rhs
                        )
                );
            }
        }
    }

    private static Monkey parse(String line) {
        String[] parts = line.split(": ");
        String name = parts[0];
        String value = parts[1];
        try {
            long intValue = Long.parseLong(value);
            return new Monkey(name, monkeys -> new Value(intValue));
        } catch (NumberFormatException e) {
            // Ignore
        }

        Matcher matcher = Pattern.compile("(?<op1>[a-z]+) (?<operator>[+\\-*/]) (?<op2>[a-z]+)").matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(value);
        }

        String monkey1 = matcher.group("op1");
        String monkey2 = matcher.group("op2");
        String op = matcher.group("operator");

        Operator operator = switch (op) {
            case "+" -> Operator.ADD;
            case "-" -> Operator.SUBTRACT;
            case "*" -> Operator.MULTIPLY;
            case "/" -> Operator.DIVIDE;
            default -> throw new IllegalArgumentException(op);
        };

        return new Monkey(name, monkeys -> {
            Expression lhs = monkeys.get(monkey1).getExpression().apply(monkeys);
            Expression rhs = monkeys.get(monkey2).getExpression().apply(monkeys);
            return new Calculation(lhs, operator, rhs);
        });
    }
}
