import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Day1 {
    public static void main(String[] args) throws IOException {
        List<Elf> elves = new ArrayList<>();

        try (BufferedReader r = Files.newBufferedReader(Paths.get("day1_input1.txt"))) {
            int elfNumber = 1;
            int totalCalories = 0;
            String line;
            while((line = r.readLine()) != null) {
                if (line.isBlank()) {
                    elves.add(new Elf(elfNumber++, totalCalories));
                    totalCalories = 0;
                } else {
                    int calories = Integer.parseInt(line);
                    totalCalories += calories;
                }
            }
            elves.add(new Elf(elfNumber, totalCalories));
        }

        int maxCalories = elves.stream().max(Comparator.comparing(Elf::calories)).get().calories();
        System.out.println("maxCalories = " + maxCalories);

        int topThreeCalories = elves.stream().sorted(Comparator.comparing(Elf::calories).reversed()).mapToInt(Elf::calories).limit(3).sum();
        System.out.println("topThreeCalories = " + topThreeCalories);
    }

    public record Elf(int number, int calories) {
    }
}
