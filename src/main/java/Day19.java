import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Day19 {
    private static final Pattern BLUEPRINT = Pattern.compile(
            "Blueprint (?<id>[0-9]+): " +
                    "Each ore robot costs (?<orore>[0-9]+) ore. " +
                    "Each clay robot costs (?<clore>[0-9]+) ore. " +
                    "Each obsidian robot costs (?<obore>[0-9]+) ore and (?<obclay>[0-9]+) clay. " +
                    "Each geode robot costs (?<geore>[0-9]+) ore and (?<geobs>[0-9]+) obsidian."
    );

    private record Blueprint(int id, int oreRobotOre, int clayRobotOre, int obsidianRobotOre, int obsidianRobotClay,
                             int geodeRobotOre, int geodeRobotObsidian, int oreDemand, int clayDemand,
                             int obsidianDemand) {
        private Blueprint(int id, int oreRobotOre, int clayRobotOre, int obsidianRobotOre, int obsidianRobotClay, int geodeRobotOre, int geodeRobotObsidian) {
            this(
                    id,
                    oreRobotOre,
                    clayRobotOre,
                    obsidianRobotOre, obsidianRobotClay,
                    geodeRobotOre, geodeRobotObsidian,
                    Math.max(Math.max(Math.max(oreRobotOre, clayRobotOre), obsidianRobotOre), geodeRobotOre),
                    obsidianRobotClay,
                    geodeRobotObsidian
            );

        }
    }

    private enum Robot {
        ORE,
        CLAY,
        OBSIDIAN,
        GEODE
    }

    private record Inventory(int time, int ore, int clay, int obsidian, int geode, int oreRobots, int clayRobots,
                             int obsidianRobots, int geodeRobots) {
        public Inventory() {
            this(0, 0, 0, 0, 0, 1, 0, 0, 0);
        }

        public boolean canBuild(Blueprint bp, Robot robot) {
            return switch (robot) {
                case ORE -> ore >= bp.oreRobotOre();
                case CLAY -> ore >= bp.clayRobotOre();
                case OBSIDIAN -> clay >= bp.obsidianRobotClay() && ore >= bp.obsidianRobotOre();
                case GEODE -> obsidian >= bp.geodeRobotObsidian() && ore >= bp.geodeRobotOre();
            };
        }

        public Inventory simulate(Blueprint bp, Robot building) {
            int ore = ore();
            int clay = clay();
            int obsidian = obsidian();
            int geode = geode();
            int oreRobots = oreRobots();
            int clayRobots = clayRobots();
            int obsidianRobots = obsidianRobots();
            int geodeRobots = geodeRobots();

            if (building != null) {
                switch (building) {
                    case ORE -> {
                        ore -= bp.oreRobotOre();
                    }
                    case CLAY -> {
                        ore -= bp.clayRobotOre();
                    }
                    case OBSIDIAN -> {
                        ore -= bp.obsidianRobotOre();
                        clay -= bp.obsidianRobotClay();
                    }
                    case GEODE -> {
                        ore -= bp.geodeRobotOre();
                        obsidian -= bp.geodeRobotObsidian();
                    }
                }
            }

            // Collect resources
            if (oreRobots > 0) {
                ore += oreRobots;
            }
            if (clayRobots > 0) {
                clay += clayRobots;
            }
            if (obsidianRobots > 0) {
                obsidian += obsidianRobots;
            }
            if (geodeRobots > 0) {
                geode += geodeRobots;
            }

            // Finish building
            if (building != null) {
                switch (building) {
                    case ORE -> {
                        oreRobots++;
                    }
                    case CLAY -> {
                        clayRobots++;
                    }
                    case OBSIDIAN -> {
                        obsidianRobots++;
                    }
                    case GEODE -> {
                        geodeRobots++;
                    }
                }
            }

            return new Inventory(time() + 1, ore, clay, obsidian, geode, oreRobots, clayRobots, obsidianRobots, geodeRobots);
        }
    }

    public static void main(String[] args) throws IOException {
        List<Blueprint> blueprints = Files.readAllLines(Paths.get("day19_input.txt"))
                .stream()
                .map(Day19::parseBlueprint)
                .toList();

        int part1 = blueprints.stream().mapToInt(bp -> {
            int geodes = calculateMaxGeodeCount(bp, 24);
            System.out.println("BP " + bp.id() + " -> " + geodes);
            return bp.id() * geodes;
        }).sum();
        System.out.println("part1 = " + part1);

        int part2 = blueprints.stream().limit(3).mapToInt(bp -> {
            int geodes = calculateMaxGeodeCount(bp, 32);
            System.out.println("BP " + bp.id() + " -> " + geodes);
            return geodes;
        }).reduce(1, (l, r) -> l * r);
        System.out.println("part2 = " + part2);
    }

    private static Blueprint parseBlueprint(String s) {
        Matcher m = BLUEPRINT.matcher(s);
        if (!m.matches()) {
            throw new IllegalArgumentException(s);
        }

        return new Blueprint(
                Integer.parseInt(m.group("id")),
                Integer.parseInt(m.group("orore")),
                Integer.parseInt(m.group("clore")),
                Integer.parseInt(m.group("obore")),
                Integer.parseInt(m.group("obclay")),
                Integer.parseInt(m.group("geore")),
                Integer.parseInt(m.group("geobs"))
        );
    }

    private static int calculateMaxGeodeCount(Blueprint bp, int runtime) {
        return calculateMaxGeodeCount(bp, new Inventory(), runtime);
    }

    private static int calculateMaxGeodeCount(Blueprint bp, Inventory i, int runtime) {
        if (i.time() == runtime) {
            return i.geode();
        }

        if (i.canBuild(bp, Robot.GEODE)) {
            return calculateMaxGeodeCount(bp, i.simulate(bp, Robot.GEODE), runtime);
        } else if (i.canBuild(bp, Robot.OBSIDIAN) && i.obsidianRobots < bp.obsidianDemand) {
            return calculateMaxGeodeCount(bp, i.simulate(bp, Robot.OBSIDIAN), runtime);
        }

        int best = 0;



        if (i.canBuild(bp, Robot.CLAY) && i.clayRobots < bp.clayDemand) {
            int score = calculateMaxGeodeCount(bp, i.simulate(bp, Robot.CLAY), runtime);
            if (score > best) {
                best = score;
            }
        }

        if (i.canBuild(bp, Robot.ORE) && i.oreRobots < bp.oreDemand) {
            int score = calculateMaxGeodeCount(bp, i.simulate(bp, Robot.ORE), runtime);
            if (score > best) {
                best = score;
            }
        }

        int score = calculateMaxGeodeCount(bp, i.simulate(bp, null), runtime);
        if (score > best) {
            best = score;
        }

        return best;
    }
}
