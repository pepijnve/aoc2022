import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day16 {
    private static final Pattern ENTRY = Pattern.compile("Valve (?<name>[^ ]+) has flow rate=(?<rate>[0-9]+); tunnels? leads? to valves? (?<tunnels>.*)");

    private static class Valve {

        private final String name;
        private final int rate;
        private final Set<Tunnel> tunnels = new HashSet<>();

        private int index;
        private int[] dist;

        public Valve(String name, int rate) {
            this.name = name;
            this.rate = rate;
        }

        public String name() {
            return name;
        }

        public int flowRate() {
            return rate;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }

            if (!(obj instanceof Valve)) {
                return false;
            }

            return index == ((Valve) obj).index;
        }

        @Override
        public int hashCode() {
            return Integer.hashCode(index);
        }

        @Override
        public String toString() {
            return name() + "(" + flowRate() + ")";
        }

        public int costToTravelTo(Valve v) {
            return dist[v.index];
        }
    }

    private record Tunnel(Valve v1, Valve v2, int cost) {
        private Tunnel(Valve v1, Valve v2, int cost) {
            if (v1.name().compareTo(v2.name()) < 0) {
                this.v1 = v1;
                this.v2 = v2;
            } else {
                this.v1 = v2;
                this.v2 = v1;
            }
            this.cost = cost;
        }

        public Valve otherValve(Valve endpoint) {
            return v1().equals(endpoint) ? v2() : v1();
        }
    }

    private static List<Valve> parseInput(Path path) throws IOException {
        List<String> lines = Files.readAllLines(path);

        Map<String, Valve> valves = new HashMap<>();
        Map<String, String[]> connections = new HashMap<>();
        Set<Tunnel> tunnels = new HashSet<>();

        int index = 0;
        for (String line : lines) {
            Matcher m = ENTRY.matcher(line);
            if (!m.matches()) {
                throw new IllegalArgumentException(line);
            }

            String name = m.group("name");
            int rate = Integer.parseInt(m.group("rate"));

            Valve valve = new Valve(name, rate);
            valve.index = index++;
            valves.put(name, valve);

            connections.put(name, m.group("tunnels").split(", "));
        }

        for (Map.Entry<String, String[]> entry : connections.entrySet()) {
            Valve from = valves.get(entry.getKey());
            for (String toName : entry.getValue()) {
                tunnels.add(new Tunnel(from, valves.get(toName), 1));
            }
        }

        // Eliminate irrelevant nodes.
        Iterator<Map.Entry<String, Valve>> valveIterator = valves.entrySet().iterator();
        while (valveIterator.hasNext()) {
            Map.Entry<String, Valve> entry = valveIterator.next();
            Valve v = entry.getValue();
            if (v.flowRate() == 0) {
                List<Tunnel> outgoingTunnels = tunnels.stream().filter(t -> t.v1().equals(v) || t.v2().equals(v)).toList();
                if (outgoingTunnels.size() == 2) {
                    valveIterator.remove();
                    tunnels.removeAll(outgoingTunnels);
                    Tunnel t1 = outgoingTunnels.get(0);
                    Tunnel t2 = outgoingTunnels.get(1);
                    tunnels.add(new Tunnel(
                            t1.otherValve(v),
                            t2.otherValve(v),
                            t1.cost() + t2.cost()
                    ));
                }
            }
        }

        for (Tunnel tunnel : tunnels) {
            tunnel.v1().tunnels.add(tunnel);
            tunnel.v2().tunnels.add(tunnel);
        }
        List<Valve> valveList = new ArrayList<>(valves.values());
        for (int i = 0; i < valveList.size(); i++) {
            valveList.get(i).index = i;
        }
        return valveList;
    }

    public static void main(String[] args) throws IOException {
        List<Valve> valves = parseInput(Paths.get("day16_input.txt"));

        // Create a complete graph
        for (Valve v1 : valves) {
            v1.dist = dijkstra(valves, v1);
        }

        System.out.println("score = " + maxScore(valves, "AA"));
        System.out.println("scoreAlt = " + maxScoreAlt(valves, "AA"));
    }

    private static int maxScore(List<Valve> valves, String startingPoint) {
        Valve[] remaining = valves.toArray(Valve[]::new);
        Valve start = null;
        for (Valve valve : remaining) {
            if (valve.name().equals(startingPoint)) {
                start = valve;
                break;
            }
        }
        remaining[start.index] = null;
        int timeLeft = 30;
        return maxScore(remaining, start, timeLeft, 0);
    }

    private static int maxScore(Valve[] remainingValves, Valve current, int timeLeft, int score) {
        int bestScore = score;

        for (Valve otherValve : remainingValves) {
            if (otherValve == null) {
                continue;
            }

            int costToOpen = current.costToTravelTo(otherValve) + 1;
            int timeOpen = timeLeft - costToOpen;
            if (timeOpen > 0) {
                remainingValves[otherValve.index] = null;

                int newScore = maxScore(remainingValves, otherValve, timeOpen, score + timeOpen * otherValve.flowRate());
                if (newScore > bestScore) {
                    bestScore = newScore;
                }

                remainingValves[otherValve.index] = otherValve;
            }
        }

        return bestScore;
    }

    private static int maxScoreAlt(List<Valve> valves, String startingPoint) {
        Valve[] remaining = valves.toArray(Valve[]::new);
        Valve start = null;
        for (Valve valve : remaining) {
            if (valve.name().equals(startingPoint)) {
                start = valve;
                break;
            }
        }
        remaining[start.index] = null;
        int timeLeft = 26;
        return maxScoreAlt(remaining, start, timeLeft, start, timeLeft, 0);
    }

    private static int maxScoreAlt(Valve[] remainingValves, Valve current1, int timeLeft1, Valve current2, int timeLeft2, int score) {
        int bestScore = score;

        for (Valve o1 : remainingValves) {
            if (o1 == null) {
                continue;
            }

            int costToOpen1 = current1.costToTravelTo(o1) + 1;
            int timeOpen1 = timeLeft1 - costToOpen1;
            if (timeOpen1 > 0) {
                for (Valve o2 : remainingValves) {
                    if (o2 == o1 || o2 == null) {
                        continue;
                    }

                    int costToOpen2 = current2.costToTravelTo(o2) + 1;
                    int timeOpen2 = timeLeft2 - costToOpen2;
                    int newScore;
                    if (timeOpen2 > 0) {
                        remainingValves[o1.index] = null;
                        remainingValves[o2.index] = null;
                        newScore = maxScoreAlt(remainingValves, o1, timeOpen1, o2, timeOpen2, score + timeOpen1 * o1.flowRate() + timeOpen2 * o2.flowRate());
                        remainingValves[o1.index] = o1;
                        remainingValves[o2.index] = o2;
                    } else {
                        remainingValves[o1.index] = null;
                        newScore = maxScoreAlt(remainingValves, o1, timeOpen1, current2, timeLeft2, score + timeOpen1 * o1.flowRate());
                        remainingValves[o1.index] = o1;
                    }

                    if (newScore > bestScore) {
                        bestScore = newScore;
                    }
                }
            } else {
                int costToOpen2 = current2.costToTravelTo(o1) + 1;
                int timeOpen2 = timeLeft2 - costToOpen2;
                if (timeOpen2 > 0) {
                    remainingValves[o1.index] = null;
                    int newScore = maxScoreAlt(remainingValves, current1, timeLeft1, o1, timeOpen2, score + timeOpen2 * o1.flowRate());
                    if (newScore > bestScore) {
                        bestScore = newScore;
                    }
                    remainingValves[o1.index] = o1;
                }
            }
        }

        return bestScore;
    }

    private static int[] dijkstra(List<Valve> graph, Valve source) {
        Set<Valve> queue = new HashSet<>(graph);
        int[] dist = new int[graph.size()];
        for (Valve valve : graph) {
            dist[valve.index] = Integer.MAX_VALUE;
        }
        dist[source.index] = 0;

        while(!queue.isEmpty()) {
            Valve u = queue.stream().min(Comparator.comparingInt(v -> dist[v.index])).orElse(null);
            queue.remove(u);

            for (Tunnel t : u.tunnels) {
                Valve v = t.otherValve(u);
                if (!queue.contains(v)) {
                    continue;
                }

                int alt = dist[u.index] + t.cost();
                if (alt < dist[v.index]) {
                    dist[v.index] = alt;
                }
            }
        }

        return dist;
    }
}
