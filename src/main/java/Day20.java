import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Day20 {
    private static class Node {
        public final long value;

        public Node(long value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return Long.toString(value);
        }
    }

    public static void main(String[] args) throws IOException {
        long decryptionKey = 811589153;
        int mixIterations = 10;

        List<Node> nodes = Files.readAllLines(Paths.get("day20_input.txt"))
                .stream()
                .map(line -> new Node(Long.parseLong(line) * decryptionKey))
                .collect(Collectors.toCollection(ArrayList::new));

        List<Node> originalOrder = new ArrayList<>(nodes);

        for (int i = 0; i < mixIterations; i++) {
            mix(nodes, originalOrder);
        }

        Node nodeZero = nodes.stream().filter(n -> n.value == 0).findFirst().orElse(null);
        int index = nodes.indexOf(nodeZero);
        long v1 = nodes.get((index + 1000) % nodes.size()).value;
        long v2 = nodes.get((index + 2000) % nodes.size()).value;
        long v3 = nodes.get((index + 3000) % nodes.size()).value;

        System.out.println("v1 = " + v1);
        System.out.println("v2 = " + v2);
        System.out.println("v3 = " + v3);
        System.out.println("sum = " + (v1 + v2 + v3));
    }

    private static void mix(List<Node> nodes, List<Node> nodeOrder) {
        for (Node node : nodeOrder) {
            int oldIndex = nodes.indexOf(node);
            long value = node.value;
            long newIndex = oldIndex + value;
            int adjustedIndex;
            if (newIndex > nodes.size()) {
                adjustedIndex = (int)(newIndex % (nodes.size() - 1));
            } else if (newIndex < 0) {
                adjustedIndex = nodes.size() - 1 + (int)(newIndex % (nodes.size() - 1));
            } else {
                adjustedIndex = (int)newIndex;
            }

            if (oldIndex != adjustedIndex) {
                nodes.remove(oldIndex);
                nodes.add(adjustedIndex, node);
            }
        }
    }
}
