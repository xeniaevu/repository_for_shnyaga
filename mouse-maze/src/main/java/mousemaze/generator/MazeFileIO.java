package mousemaze.generator;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import mousemaze.model.CellType;
import mousemaze.model.Maze;

/**
 * Чтение и запись схемы лабиринта в текстовый файл.
 *
 * Формат: одна строка файла — одна строка лабиринта.
 *   #  стена      .  проход     S  старт
 *   C  сыр        W  вода       T  ловушка (ток)
 * Строки, начинающиеся с ';', считаются комментариями.
 * Короткие строки дополняются стенами справа.
 */
public final class MazeFileIO {

    private MazeFileIO() {
    }

    public static Maze load(Path path) throws IOException {
        return parse(Files.readAllLines(path, StandardCharsets.UTF_8));
    }

    public static Maze parse(List<String> rawLines) {
        List<String> lines = new ArrayList<>();
        for (String line : rawLines) {
            String trimmed = line.stripTrailing();
            if (!trimmed.isEmpty() && !trimmed.startsWith(";")) {
                lines.add(trimmed);
            }
        }
        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Файл не содержит схемы лабиринта");
        }
        int cols = lines.stream().mapToInt(String::length).max().orElse(0);
        Maze maze = new Maze(lines.size(), cols, CellType.WALL);
        for (int r = 0; r < lines.size(); r++) {
            String line = lines.get(r);
            for (int c = 0; c < line.length(); c++) {
                char ch = line.charAt(c);
                CellType type;
                try {
                    type = CellType.fromSymbol(ch);
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException(
                            "Строка " + (r + 1) + ", позиция " + (c + 1) + ": " + e.getMessage());
                }
                maze.set(r, c, type);
            }
        }
        return maze;
    }

    public static void save(Maze maze, Path path) throws IOException {
        String header = "; Схема лабиринта: # стена, . проход, S старт, C сыр, W вода, T ток\n";
        Files.writeString(path, header + maze.toText(), StandardCharsets.UTF_8);
    }
}
