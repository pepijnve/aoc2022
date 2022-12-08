import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Day8 {
    public static void main(String[] args) throws IOException {
        int[][] grid = readGrid("day8_input.txt");
        System.out.println(countVisibleTreesFromOutside(grid));
        System.out.println(calculateMaxScenicScore(grid));
    }

    private static int[][] readGrid(String fileName) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(fileName));
        int gridHeight = lines.size();
        int gridWidth = lines.get(0).length();
        int[][] grid = new int[gridHeight][gridWidth];
        for (int i = 0; i < grid.length; i++) {
            char[] line = lines.get(i).toCharArray();
            for (int j = 0; j < line.length; j++) {
                grid[i][j] = line[j] - '0';
            }
        }
        return grid;
    }

    private static int countVisibleTreesFromOutside(int[][] grid) {
        int gridHeight = grid.length;
        int gridWidth = grid[0].length;

        boolean[][] visible = new boolean[grid.length][grid[0].length];

        // left-to-right
        for (int r = 0; r < gridHeight; r++) {
            int maxHeight = -1;
            for (int c = 0; c < gridWidth; c++) {
                int treeHeight = grid[r][c];
                if (treeHeight > maxHeight) {
                    visible[r][c] = true;
                    maxHeight = treeHeight;
                }
            }
        }

        // right-to-left
        for (int r = 0; r < gridHeight; r++) {
            int maxHeight = -1;
            for (int c = gridWidth - 1; c >= 0; c--) {
                int treeHeight = grid[r][c];
                if (treeHeight > maxHeight) {
                    visible[r][c] = true;
                    maxHeight = treeHeight;
                }
            }
        }

        // top-to-bottom
        for (int c = 0; c < gridWidth; c++) {
            int maxHeight = -1;
            for (int r = 0; r < gridHeight; r++) {
                int treeHeight = grid[r][c];
                if (treeHeight > maxHeight) {
                    visible[r][c] = true;
                    maxHeight = treeHeight;
                }
            }
        }

        // bottom-to-top
        for (int c = 0; c < gridWidth; c++) {
            int maxHeight = -1;
            for (int r = gridHeight - 1; r >= 0; r--) {
                int treeHeight = grid[r][c];
                if (treeHeight > maxHeight) {
                    visible[r][c] = true;
                    maxHeight = treeHeight;
                }
            }
        }

        int visibleTrees = 0;
        for (int r = 0; r < gridHeight; r++) {
            for (int c = 0; c < gridWidth; c++) {
                if (visible[r][c]) {
                    visibleTrees++;
                }
            }
        }
        return visibleTrees;
    }

    private static int calculateMaxScenicScore(int[][] grid) {
        int gridHeight = grid.length;
        int gridWidth = grid[0].length;

        int maxScore = 0;

        for (int r = 0; r < gridHeight; r++) {
            for (int c = 0; c < gridWidth; c++) {
                int height = grid[r][c];

                int up = 0;
                for (int r_ = r - 1; r_ >= 0; r_--) {
                    int otherHeight = grid[r_][c];
                    if (otherHeight <= height) {
                        up++;
                    }

                    if (otherHeight >= height) {
                        break;
                    }
                }

                int down = 0;
                for (int r_ = r + 1; r_ < gridHeight; r_++) {
                    int otherHeight = grid[r_][c];
                    if (otherHeight <= height) {
                        down++;
                    }

                    if (otherHeight >= height) {
                        break;
                    }
                }

                int left = 0;
                for (int c_ = c - 1; c_ >= 0; c_--) {
                    int otherHeight = grid[r][c_];
                    if (otherHeight <= height) {
                        left++;
                    }

                    if (otherHeight >= height) {
                        break;
                    }
                }

                int right = 0;
                for (int c_ = c + 1; c_ < gridWidth; c_++) {
                    int otherHeight = grid[r][c_];
                    if (otherHeight <= height) {
                        right++;
                    }

                    if (otherHeight >= height) {
                        break;
                    }
                }

                int score = up * down * left * right;
                if (score > maxScore) {
                    maxScore = score;
                }
            }
        }

        return maxScore;
    }
}
