import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Day10 {
    private enum OpCode {
        NOOP,
        ADDX;

        OpCode() {
        }
    }

    private record Instruction(OpCode opcode, int argument) {
    }

    public static void main(String[] args) throws IOException {
        List<Instruction> instructions = new ArrayList<>();
        List<String> lines = Files.readAllLines(Paths.get("day10_input.txt"));
        for (String line : lines) {
            if (line.equals("noop")) {
                instructions.add(new Instruction(OpCode.NOOP, 0));
            } else if (line.startsWith("addx ")) {
                instructions.add(new Instruction(OpCode.NOOP, 0));
                instructions.add(new Instruction(OpCode.ADDX, Integer.parseInt(line.substring(5))));
            } else {
                throw new IllegalArgumentException(line);
            }
        }

        int signalSum = 0;
        int cycle = 1;
        int x = 1;
        for (Instruction instruction : instructions) {
            int position = cycle % 40 - 1;
            if (x - 1 <= position && position <= x + 1) {
                System.out.print("#");
            } else {
                System.out.print(" ");
            }

            switch (instruction.opcode) {
                case NOOP -> {
                    // nothing to do
                }
                case ADDX -> {
                    x += instruction.argument();
                }
            }
            if (cycle % 40 == 0) {
                System.out.println();
            }
            cycle++;
        }
        System.out.println("signalSum = " + signalSum);
    }
}
