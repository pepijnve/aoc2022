import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day5 {
    public static void main(String[] args) throws IOException {
        boolean crateMover9001 = true;

        BufferedReader reader = Files.newBufferedReader(Paths.get("day5_input.txt"));

        List<List<Character>> stacks = readStacks(reader);
        printStacks(stacks);

        Pattern pattern = Pattern.compile("move (?<amount>\\d+) from (?<from>\\d+) to (?<to>\\d+)");
        String movement;
        while ((movement = reader.readLine()) != null) {
            Matcher matcher = pattern.matcher(movement);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(movement);
            }

            System.out.println();
            System.out.println(movement);
            System.out.println();

            int amount = Integer.parseInt(matcher.group("amount"));
            int from = Integer.parseInt(matcher.group("from"));
            int to = Integer.parseInt(matcher.group("to"));
            List<Character> fromStack = stacks.get(from - 1);
            List<Character> toStack = stacks.get(to - 1);

            if (crateMover9001) {
                List<Character> partToMove = fromStack.subList(fromStack.size() - amount, fromStack.size());
                toStack.addAll(partToMove);
                partToMove.clear();
            } else {
                for (int i = 0; i < amount; i++) {
                    Character container = fromStack.remove(fromStack.size() - 1);
                    toStack.add(container);
                }
            }

            printStacks(stacks);
        }

        for (List<Character> stack : stacks) {
            System.out.print(stack.get(stack.size() - 1));
        }
    }

    private static void printStacks(List<List<Character>> stacks) {
        int index = stacks.stream().mapToInt(List::size).max().getAsInt();
        for (int i = index - 1; i >= 0; i--) {
            for (int j = 0; j < stacks.size(); j++) {
                if (j != 0) {
                    System.out.print(" ");
                }

                List<Character> stack = stacks.get(j);
                if (stack.size() > i) {
                    System.out.print("[" + stack.get(i) + "]");
                } else {
                    System.out.print("   ");
                }
            }
            System.out.println();
        }

        for (int i = 0; i < stacks.size(); i++) {
            if (i != 0) {
                System.out.print(" ");
            }

            System.out.print(" " + (i + 1) + " ");
        }
        System.out.println();
    }

    private static List<List<Character>> readStacks(BufferedReader reader) throws IOException {
        String line;

        Deque<String> begin = new ArrayDeque<>();
        while ((line = reader.readLine()) != null && !line.isEmpty()) {
            begin.addLast(line);
        }

        String numbers = begin.removeLast();
        List<List<Character>> stacks = new ArrayList<>();
        List<Integer> positions = new ArrayList<>();
        for (int i = 0; i < numbers.length(); i++) {
             if (Character.isDigit(numbers.charAt(i))) {
                 stacks.add(new ArrayList<>());
                 positions.add(i);
             }
        }

        while (!begin.isEmpty()) {
            String row = begin.removeLast();
            for (int i = 0; i < positions.size(); i++) {
                char c = row.charAt(positions.get(i));
                if (Character.isLetter(c)) {
                    stacks.get(i).add(c);
                }
            }
        }
        return stacks;
    }
}
