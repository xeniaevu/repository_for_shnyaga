package mousemaze.ui;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import javax.imageio.ImageIO;
import mousemaze.env.Environment;
import mousemaze.env.RewardConfig;
import mousemaze.generator.MazeFileIO;
import mousemaze.generator.MazeGenerator;
import mousemaze.model.Action;
import mousemaze.model.CellType;
import mousemaze.model.Maze;
import mousemaze.model.Position;
import mousemaze.training.EpisodeResult;
import mousemaze.training.Simulation;
import mousemaze.training.Trainer;
import mousemaze.training.TrainingConfig;

/**
 * Главное окно: слева настройки (лабиринт, редактор схемы, награды, обучение),
 * в центре лабиринт, справа результаты обучения и просмотр эпизодов.
 */
public class MainApp extends Application {

    // --- лабиринт ---
    private final Spinner<Integer> roomRows = intSpinner(2, 40, 6);
    private final Spinner<Integer> roomCols = intSpinner(2, 40, 6);
    private final Spinner<Integer> loops = intSpinner(0, 60, 0);
    private final Spinner<Integer> waterCount = intSpinner(0, Environment.MAX_WATER, 4);
    private final Spinner<Integer> trapCount = intSpinner(0, 50, 3);
    private final TextField seedField = new TextField();

    // --- редактор ---
    private final ToggleButton editMode = new ToggleButton("Режим редактирования");
    private final ComboBox<CellType> brush = new ComboBox<>();

    // --- награды ---
    private final Spinner<Double> cheeseReward = doubleSpinner(1, 10000, 100, 10);
    private final Spinner<Double> waterReward = doubleSpinner(0, 1000, 10, 1);
    private final Spinner<Double> trapPenalty = doubleSpinner(0, 1000, 30, 5);
    private final Spinner<Double> stepCost = doubleSpinner(0, 100, 1, 0.5);
    private final Spinner<Double> wallPenalty = doubleSpinner(0, 100, 5, 1);

    // --- обучение ---
    private final Spinner<Integer> episodes = intSpinner(1, 100000, 500);
    private final Spinner<Integer> maxSteps = intSpinner(10, 100000, 800);
    private final Spinner<Double> alpha = doubleSpinner(0.01, 1, 0.5, 0.05);
    private final Spinner<Double> gamma = doubleSpinner(0, 1, 0.99, 0.01);
    private final Spinner<Double> epsStart = doubleSpinner(0, 1, 1.0, 0.05);
    private final Spinner<Double> epsEnd = doubleSpinner(0, 1, 0.05, 0.01);
    private final Spinner<Double> exploreShare = doubleSpinner(0.05, 1, 0.7, 0.05);
    private final Button trainButton = new Button("Обучить мышь");
    private final ProgressBar progress = new ProgressBar(0);

    // --- результаты ---
    private final Label statEpisode = new Label("—");
    private final Label statSteps = new Label("—");
    private final Label statReward = new Label("—");
    private final Label statEpsilon = new Label("—");
    private final Label statCheese = new Label("—");
    private final Label statWater = new Label("—");
    private final Label statTraps = new Label("—");
    private final Label statWalls = new Label("—");
    private final Slider episodeSlider = new Slider(1, 1, 1);
    private final Label episodeSliderLabel = new Label("Эпизод: —");
    private final Slider speed = new Slider(1, 60, 15);
    private final CheckBox showPolicy = new CheckBox("Стрелки политики (лучшее действие по Q)");
    private final CheckBox showNumbers = new CheckBox("Номера шагов");
    private final Label status = new Label();
    private final Label mazeInfo = new Label();

    private final MazeCanvas mazeView = new MazeCanvas();

    /** Панели, которые блокируются на время обучения. */
    private final List<Node> settingsLock = new java.util.ArrayList<>();
    private Stage stage;
    private Maze maze;
    private Trainer trainer;
    private List<EpisodeResult> history;
    private EpisodeResult bestRun;
    private EpisodeResult shown;
    private int shownSteps;
    private Timeline animation;
    private Task<List<EpisodeResult>> trainingTask;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        BorderPane root = new BorderPane();
        root.setLeft(buildSettings());
        root.setCenter(buildCenter());
        root.setRight(buildResults());
        status.setPadding(new Insets(4, 10, 6, 10));
        status.setWrapText(true);
        root.setBottom(status);

        mazeView.setOnCellPaint(this::paintCell);
        mazeView.setOnCellHover(this::describeCell);

        generateMaze();

        Scene scene = new Scene(root, 1360, 820);
        stage.setTitle("Мышь в лабиринте — обучение с подкреплением (Q-learning)");
        stage.setScene(scene);
        stage.show();
    }

    // =====================================================================
    // Построение интерфейса
    // =====================================================================

    private Node buildSettings() {
        seedField.setPromptText("пусто — случайно");

        Button generate = new Button("Сгенерировать (DFS)");
        generate.setMaxWidth(Double.MAX_VALUE);
        generate.setOnAction(e -> generateMaze());

        GridPane mazeGrid = form(
                "Комнат по вертикали", roomRows,
                "Комнат по горизонтали", roomCols,
                "Петли, % снесённых стен", loops,
                "Клеток с водой", waterCount,
                "Ловушек (ток)", trapCount,
                "Зерно случайности", seedField);
        Label hint = small("0% петель — идеальный лабиринт: между любыми клетками ровно один путь.");
        VBox mazeBox = new VBox(8, mazeGrid, hint, generate);

        brush.getItems().setAll(CellType.values());
        brush.setValue(CellType.WALL);
        brush.setMaxWidth(Double.MAX_VALUE);
        Button blank = new Button("Пустое поле заданного размера");
        blank.setMaxWidth(Double.MAX_VALUE);
        blank.setOnAction(e -> {
            int rows = 2 * roomRows.getValue() + 1;
            int cols = 2 * roomCols.getValue() + 1;
            Maze m = Maze.blankWithBorder(rows, cols);
            m.set(rows - 2, cols - 2, CellType.START);
            m.set(1, 1, CellType.CHEESE);
            setMaze(m, "Пустое поле " + rows + "×" + cols + ". Рисуйте стены и предметы мышью.");
            editMode.setSelected(true);
        });
        Button load = new Button("Загрузить схему…");
        Button save = new Button("Сохранить схему…");
        load.setOnAction(e -> loadMaze());
        save.setOnAction(e -> saveMaze());
        HBox files = new HBox(6, load, save);
        editMode.setMaxWidth(Double.MAX_VALUE);
        VBox editBox = new VBox(8, editMode, labeled("Кисть", brush), blank, files,
                small("ЛКМ — поставить выбранное, ПКМ — сделать проходом. "
                        + "Файл: # стена, . проход, S старт, C сыр, W вода, T ток."));

        GridPane rewardGrid = form(
                "Сыр  +Z", cheeseReward,
                "Вода  +x", waterReward,
                "Ток  −y", trapPenalty,
                "Шаг  −", stepCost,
                "Удар о стену  −", wallPenalty);

        trainButton.setMaxWidth(Double.MAX_VALUE);
        trainButton.setDefaultButton(true);
        trainButton.setOnAction(e -> {
            if (trainingTask != null && trainingTask.isRunning()) {
                trainingTask.cancel();
            } else {
                startTraining();
            }
        });
        progress.setMaxWidth(Double.MAX_VALUE);
        GridPane learnGrid = form(
                "Эпизодов (эпох)", episodes,
                "Лимит шагов в эпизоде", maxSteps,
                "α — скорость обучения", alpha,
                "γ — дисконтирование", gamma,
                "ε в начале", epsStart,
                "ε в конце", epsEnd,
                "Доля эпизодов на спад ε", exploreShare);
        VBox learnBox = new VBox(8, learnGrid, trainButton, progress);

        TitledPane mazePane = titled("Лабиринт", mazeBox);
        TitledPane editPane = titled("Схема лабиринта (редактор)", editBox);
        TitledPane rewardPane = titled("Награды", rewardGrid);
        settingsLock.addAll(List.of(mazePane, editPane, rewardPane, learnGrid));
        VBox box = new VBox(mazePane, editPane, rewardPane, titled("Обучение (Q-learning)", learnBox));
        ScrollPane scroll = new ScrollPane(box);
        scroll.setFitToWidth(true);
        scroll.setPrefWidth(320);
        return scroll;
    }

    private Node buildCenter() {
        mazeInfo.setPadding(new Insets(6, 10, 0, 10));
        VBox center = new VBox(mazeInfo, mazeView);
        VBox.setVgrow(mazeView, Priority.ALWAYS);
        mazeView.setPadding(new Insets(8));
        return center;
    }

    private Node buildResults() {
        GridPane stats = form(
                "Показан", statEpisode,
                "Шагов", statSteps,
                "Сумма выигрыша", statReward,
                "ε (случайные ходы)", statEpsilon,
                "Дошла до сыра", statCheese,
                "Выпито воды", statWater,
                "Ударов током", statTraps,
                "Ударов о стену", statWalls);
        statReward.setStyle("-fx-font-weight: bold;");

        episodeSlider.setDisable(true);
        episodeSlider.setMaxWidth(Double.MAX_VALUE);
        episodeSlider.valueProperty().addListener((o, ov, nv) -> {
            if (history != null && !episodeSlider.isValueChanging()) {
                showEpisode(history.get(nv.intValue() - 1), false);
            }
        });
        episodeSlider.valueChangingProperty().addListener((o, was, now) -> {
            if (!now && history != null) {
                showEpisode(history.get((int) episodeSlider.getValue() - 1), false);
            }
        });

        Button ep1 = new Button("Эпоха 1");
        Button ep50 = new Button("Эпоха 50");
        Button epLast = new Button("Последняя");
        Button best = new Button("Найденный путь (ε = 0)");
        ep1.setOnAction(e -> jumpToEpisode(1));
        ep50.setOnAction(e -> jumpToEpisode(50));
        epLast.setOnAction(e -> jumpToEpisode(Integer.MAX_VALUE));
        best.setOnAction(e -> {
            if (bestRun != null) {
                showEpisode(bestRun, true);
            }
        });
        best.setMaxWidth(Double.MAX_VALUE);

        Button play = new Button("▶ Анимация");
        Button pause = new Button("⏸ Пауза");
        play.setOnAction(e -> playAnimation());
        pause.setOnAction(e -> stopAnimation());
        Label speedLabel = small("Скорость (шагов в секунду)");

        showPolicy.setOnAction(e -> mazeView.setShowPolicy(showPolicy.isSelected()));
        showNumbers.setOnAction(e -> mazeView.setShowStepNumbers(showNumbers.isSelected()));

        Button shotMaze = new Button("Снимок лабиринта…");
        Button shotWindow = new Button("Снимок окна…");
        shotMaze.setOnAction(e -> saveSnapshot(mazeView));
        shotWindow.setOnAction(e -> saveSnapshot(stage.getScene().getRoot()));

        VBox right = new VBox(8,
                header("Результат эпизода"), stats,
                new Separator(),
                header("Просмотр обучения"),
                episodeSliderLabel, episodeSlider,
                new HBox(6, ep1, ep50, epLast), best,
                new HBox(6, play, pause), speedLabel, speed,
                showPolicy, showNumbers,
                new HBox(6, shotMaze, shotWindow));
        right.setPadding(new Insets(10));
        right.setPrefWidth(360);
        ScrollPane scroll = new ScrollPane(right);
        scroll.setFitToWidth(true);
        scroll.setPrefWidth(380);
        return scroll;
    }

    // =====================================================================
    // Лабиринт: генерация, редактирование, файлы
    // =====================================================================

    private void generateMaze() {
        long seed = parseSeed();
        Random rnd = seed == 0 ? new Random() : new Random(seed);
        Maze m = new MazeGenerator(rnd).generate(roomRows.getValue(), roomCols.getValue(),
                loops.getValue() / 100.0, waterCount.getValue(), trapCount.getValue());
        setMaze(m, "Лабиринт сгенерирован поиском в глубину. Нажмите «Обучить мышь».");
    }

    private void setMaze(Maze m, String message) {
        stopAnimation();
        maze = m;
        trainer = null;
        history = null;
        bestRun = null;
        shown = null;
        mazeView.setMaze(maze);
        episodeSlider.setDisable(true);
        episodeSliderLabel.setText("Эпизод: —");
        clearStats();
        updateMazeInfo();
        setStatus(message);
    }

    private void paintCell(Position p, MouseButton button) {
        if (!editMode.isSelected() || maze == null) {
            return;
        }
        CellType type = button == MouseButton.SECONDARY ? CellType.EMPTY : brush.getValue();
        if (maze.get(p) == type) {
            return;
        }
        if (type == CellType.WATER && maze.count(CellType.WATER) >= Environment.MAX_WATER) {
            setStatus("Не больше " + Environment.MAX_WATER + " клеток с водой: каждая удваивает размер Q-таблицы.");
            return;
        }
        // Изменение схемы делает прежнее обучение недействительным.
        Maze edited = maze;
        setMaze(edited, "Схема изменена: " + p + " → " + type.title() + ".");
        maze.set(p, type);
        mazeView.redraw();
        updateMazeInfo();
    }

    private void loadMaze() {
        FileChooser fc = mazeFileChooser();
        File f = fc.showOpenDialog(stage);
        if (f == null) {
            return;
        }
        try {
            Maze m = MazeFileIO.load(f.toPath());
            setMaze(m, "Схема загружена из " + f.getName() + ".");
        } catch (IOException | IllegalArgumentException ex) {
            error("Не удалось загрузить схему", ex.getMessage());
        }
    }

    private void saveMaze() {
        FileChooser fc = mazeFileChooser();
        fc.setInitialFileName("maze.txt");
        File f = fc.showSaveDialog(stage);
        if (f == null) {
            return;
        }
        try {
            MazeFileIO.save(maze, f.toPath());
            setStatus("Схема сохранена в " + f.getName() + ".");
        } catch (IOException ex) {
            error("Не удалось сохранить схему", ex.getMessage());
        }
    }

    private FileChooser mazeFileChooser() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Схема лабиринта");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Текстовая схема", "*.txt"));
        File examples = new File("examples");
        if (examples.isDirectory()) {
            fc.setInitialDirectory(examples);
        }
        return fc;
    }

    private void updateMazeInfo() {
        int shortest = maze.shortestPathLength();
        mazeInfo.setText(String.format("Поле %d×%d клеток · вода: %d · ловушки: %d · кратчайший путь: %s",
                maze.rows(), maze.cols(), maze.count(CellType.WATER), maze.count(CellType.TRAP),
                shortest < 0 ? "нет пути" : shortest + " шагов"));
    }

    // =====================================================================
    // Обучение
    // =====================================================================

    private void startTraining() {
        String problem = maze.validate();
        if (problem != null) {
            error("Лабиринт не готов к обучению", problem);
            return;
        }
        RewardConfig rewards = new RewardConfig(cheeseReward.getValue(), waterReward.getValue(),
                trapPenalty.getValue(), stepCost.getValue(), wallPenalty.getValue());
        TrainingConfig config = new TrainingConfig(episodes.getValue(), maxSteps.getValue(),
                alpha.getValue(), gamma.getValue(), epsStart.getValue(), epsEnd.getValue(),
                exploreShare.getValue(), parseSeed());
        Trainer created;
        try {
            created = Simulation.create(maze, rewards, config);
        } catch (IllegalArgumentException ex) {
            error("Обучение невозможно", ex.getMessage());
            return;
        }
        stopAnimation();
        editMode.setSelected(false);
        settingsLock.forEach(n -> n.setDisable(true));
        final Trainer t = created;

        trainingTask = new Task<>() {
            @Override
            protected List<EpisodeResult> call() {
                return t.train((r, total) -> {
                    if (r.episode() % 10 == 0 || r.episode() == total) {
                        updateProgress(r.episode(), total);
                        updateMessage(String.format("Эпизод %d из %d: шагов %d, сумма %.1f",
                                r.episode(), total, r.steps(), r.totalReward()));
                    }
                }, this::isCancelled);
            }
        };
        progress.progressProperty().bind(trainingTask.progressProperty());
        status.textProperty().bind(trainingTask.messageProperty());
        trainButton.setText("Остановить");

        trainingTask.setOnSucceeded(e -> finishTraining(t, trainingTask.getValue()));
        trainingTask.setOnCancelled(e -> {
            unbindProgress();
            setStatus("Обучение остановлено.");
        });
        trainingTask.setOnFailed(e -> {
            unbindProgress();
            error("Ошибка обучения", String.valueOf(trainingTask.getException()));
        });
        Thread thread = new Thread(trainingTask, "training");
        thread.setDaemon(true);
        thread.start();
    }

    private void unbindProgress() {
        progress.progressProperty().unbind();
        status.textProperty().unbind();
        settingsLock.forEach(n -> n.setDisable(false));
        trainButton.setText("Обучить мышь");
    }

    private void finishTraining(Trainer t, List<EpisodeResult> result) {
        unbindProgress();
        progress.setProgress(1);
        trainer = t;
        history = result;
        bestRun = t.greedyRun();
        mazeView.setModel(t.environment(), t.agent());

        episodeSlider.setMax(history.size());
        episodeSlider.setMajorTickUnit(Math.max(1, history.size() / 5.0));
        episodeSlider.setDisable(false);
        showEpisode(bestRun, true);

        long reached = history.stream().filter(EpisodeResult::reachedCheese).count();
        setStatus(String.format(
                "Обучение завершено: %d эпизодов, сыр найден в %d из них. Найденный путь: %d шагов, сумма %.1f%s.",
                history.size(), reached, bestRun.steps(), bestRun.totalReward(),
                bestRun.reachedCheese() ? "" : " — сыр НЕ найден, увеличьте число эпизодов или γ"));
    }

    // =====================================================================
    // Просмотр эпизодов и анимация
    // =====================================================================

    private void jumpToEpisode(int n) {
        if (history == null) {
            setStatus("Сначала обучите мышь.");
            return;
        }
        int idx = Math.min(n, history.size());
        episodeSlider.setValue(idx);
        showEpisode(history.get(idx - 1), false);
    }

    private void showEpisode(EpisodeResult r, boolean best) {
        stopAnimation();
        shown = r;
        shownSteps = r.steps();
        mazeView.showEpisode(r, shownSteps, best);
        if (best) {
            episodeSliderLabel.setText("Показан контрольный прогон после обучения");
        } else {
            episodeSliderLabel.setText("Эпизод: " + r.episode() + " из " + history.size());
        }
        updateStats();
    }

    private void playAnimation() {
        if (shown == null) {
            setStatus("Сначала обучите мышь и выберите эпизод.");
            return;
        }
        stopAnimation();
        if (shownSteps >= shown.steps()) {
            shownSteps = 0;
        }
        animation = new Timeline(new KeyFrame(Duration.millis(1000.0 / speed.getValue()), e -> {
            shownSteps++;
            mazeView.showEpisode(shown, shownSteps, shown == bestRun);
            updateStats();
            if (shownSteps >= shown.steps()) {
                stopAnimation();
            }
        }));
        animation.setCycleCount(Timeline.INDEFINITE);
        animation.play();
    }

    private void stopAnimation() {
        if (animation != null) {
            animation.stop();
            animation = null;
        }
    }

    private void updateStats() {
        if (shown == null) {
            clearStats();
            return;
        }
        boolean complete = shownSteps >= shown.steps();
        statEpisode.setText(shown.episode() == 0 ? "после обучения" : "эпизод " + shown.episode());
        statSteps.setText(complete ? String.valueOf(shown.steps()) : shownSteps + " / " + shown.steps());
        statReward.setText(String.format("%.1f", shown.rewardUpTo(shownSteps)));
        statEpsilon.setText(String.format("%.2f", shown.epsilon()));
        statCheese.setText(complete ? (shown.reachedCheese() ? "да" : "нет (лимит шагов)") : "…");
        statWater.setText(String.valueOf(shown.waterDrunk()));
        statTraps.setText(String.valueOf(shown.trapHits()));
        statWalls.setText(String.valueOf(shown.wallHits()));
    }

    private void clearStats() {
        for (Label l : new Label[]{statEpisode, statSteps, statReward, statEpsilon,
                statCheese, statWater, statTraps, statWalls}) {
            l.setText("—");
        }
    }

    /** Строка состояния: что мышь знает о клетке под курсором (строка Q-таблицы). */
    private void describeCell(Position p) {
        if (p == null || maze == null || (trainingTask != null && trainingTask.isRunning())) {
            return;
        }
        StringBuilder sb = new StringBuilder("Клетка " + p + ": " + maze.get(p).title());
        if (trainer != null && maze.get(p).isPassable()) {
            Environment env = trainer.environment();
            int state = env.encode(p.row(), p.col(), mazeView.currentMask());
            sb.append("   |   Q(s, a):");
            for (Action a : Action.ALL) {
                sb.append(String.format("  %s %.2f", a.arrow(), trainer.agent().getQ(state, a)));
            }
        }
        setStatus(sb.toString());
    }

    // =====================================================================
    // Вспомогательное
    // =====================================================================

    private void saveSnapshot(Node node) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Сохранить снимок");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG", "*.png"));
        String name = shown == null ? "maze" : shown.episode() == 0 ? "best_path" : "episode_" + shown.episode();
        fc.setInitialFileName(name + ".png");
        File f = fc.showSaveDialog(stage);
        if (f == null) {
            return;
        }
        WritableImage img = node.snapshot(null, null);
        try {
            ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", f);
            setStatus("Снимок сохранён: " + f.getName());
        } catch (IOException ex) {
            error("Не удалось сохранить снимок", ex.getMessage());
        }
    }

    private long parseSeed() {
        String s = seedField.getText().trim();
        if (s.isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return s.hashCode();
        }
    }

    private void setStatus(String text) {
        if (!status.textProperty().isBound()) {
            status.setText(text);
        }
    }

    private void error(String title, String text) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.initOwner(stage);
        a.setHeaderText(title);
        a.setContentText(text);
        a.showAndWait();
    }

    private static Spinner<Integer> intSpinner(int min, int max, int value) {
        Spinner<Integer> s = new Spinner<>(min, max, value);
        s.setEditable(true);
        s.setPrefWidth(110);
        s.focusedProperty().addListener((o, was, now) -> {
            if (!now) {
                commit(s);
            }
        });
        return s;
    }

    private static Spinner<Double> doubleSpinner(double min, double max, double value, double step) {
        Spinner<Double> s = new Spinner<>(min, max, value, step);
        s.setEditable(true);
        s.setPrefWidth(110);
        s.focusedProperty().addListener((o, was, now) -> {
            if (!now) {
                commit(s);
            }
        });
        return s;
    }

    /** Применяет введённый вручную текст; при ошибке ввода возвращает прежнее значение. */
    private static <T> void commit(Spinner<T> s) {
        try {
            T v = s.getValueFactory().getConverter().fromString(s.getEditor().getText());
            if (v != null) {
                s.getValueFactory().setValue(v);
            }
        } catch (RuntimeException ignored) {
            // неверный ввод — оставляем прежнее значение
        }
        s.getEditor().setText(s.getValueFactory().getConverter().toString(s.getValue()));
    }

    private static GridPane form(Object... labelAndControl) {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(6);
        for (int i = 0; i < labelAndControl.length; i += 2) {
            Label l = new Label((String) labelAndControl[i]);
            l.setWrapText(true);
            Node control = (Node) labelAndControl[i + 1];
            grid.add(l, 0, i / 2);
            grid.add(control, 1, i / 2);
            GridPane.setHgrow(l, Priority.ALWAYS);
        }
        return grid;
    }

    private static VBox labeled(String text, Node control) {
        return new VBox(4, new Label(text), control);
    }

    private static TitledPane titled(String title, Node content) {
        TitledPane tp = new TitledPane(title, content);
        tp.setCollapsible(true);
        return tp;
    }

    private static Label small(String text) {
        Label l = new Label(text);
        l.setWrapText(true);
        l.setStyle("-fx-font-size: 11px; -fx-text-fill: #5F5E5A;");
        return l;
    }

    private static Label header(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");
        l.setAlignment(Pos.CENTER_LEFT);
        return l;
    }

    @Override
    public void stop() {
        if (trainingTask != null) {
            trainingTask.cancel();
        }
        Platform.exit();
    }
}