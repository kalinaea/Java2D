package panels;

import app.Point;
import app.Task;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.humbleui.jwm.Event;
import io.github.humbleui.jwm.EventMouseButton;
import io.github.humbleui.jwm.Window;
import io.github.humbleui.skija.Canvas;
import misc.CoordinateSystem2d;
import misc.CoordinateSystem2i;
import misc.Vector2d;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Панель рисования
 */
public class PanelRendering extends GridPanel {
    /**
     * Представление проблемы
     */
    public static Task task;

    /**
     * Панель управления
     *
     * @param window     окно
     * @param drawBG     флаг, нужно ли рисовать подложку
     * @param color      цвет подложки
     * @param padding    отступы
     * @param gridWidth  кол-во ячеек сетки по ширине
     * @param gridHeight кол-во ячеек сетки по высоте
     * @param gridX      координата в сетке x
     * @param gridY      координата в сетке y
     * @param colspan    кол-во колонок, занимаемых панелью
     * @param rowspan    кол-во строк, занимаемых панелью
     */
    public PanelRendering(
            Window window, boolean drawBG, int color, int padding, int gridWidth, int gridHeight,
            int gridX, int gridY, int colspan, int rowspan
    ) {
        super(window, drawBG, color, padding, gridWidth, gridHeight, gridX, gridY, colspan, rowspan);

        // ОСК от [-10.0,-10.0] до [10.0,10.0]
        CoordinateSystem2d cs = new CoordinateSystem2d(
                new Vector2d(-10.0, -10.0), new Vector2d(10.0, 10.0)
        );

        // создаём массив случайных точек
        ArrayList<Point> points = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            // получаем случайное множество
            Point.PointSet pointSet = ThreadLocalRandom.current().nextBoolean() ?
                    Point.PointSet.FIRST_SET : Point.PointSet.SECOND_SET;
            // добавляем точку в случайном месте ОСК в указанное множество
            points.add(new Point(cs.getRandomCoords(), pointSet));
        }
        task = new Task(cs, points);

        // создаём задачу без точек
        task = new Task(cs, new ArrayList<>());
        // добавляем в нё 10 случайных
        task.addRandomPoints(10);
    }

    /**
     * Обработчик событий
     *
     * @param e событие
     */
    @Override
    public void accept(Event e) {
        // вызов обработчика предка
        super.accept(e);
        // если событие - это клик мышью
        if (e instanceof EventMouseButton ee) {
            // если последнее положение мыши сохранено и курсор был внутри
            if (lastMove != null && lastInside){
                // если событие - нажатие мыши
                if (ee.isPressed())
                // обрабатываем клик по задаче
                task.click(lastWindowCS.getRelativePos(lastMove), ee.getButton());
            }
        }
    }

    /**
     * Метод под рисование в конкретной реализации
     *
     * @param canvas   область рисования
     * @param windowCS СК окна
     */
    @Override
    public void paintImpl(Canvas canvas, CoordinateSystem2i windowCS) {
        task.paint(canvas, windowCS);
    }


    /**
     * Сохранить файл
     */
    public static void save() {
        String path = "src/main/resources/conf.json";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(new File(path), task);
            PanelLog.success("Файл " + path + " успешно сохранён");
        } catch (IOException e) {
            PanelLog.error("не получилось записать файл \n" + e);
        }
    }


    /**
     * Загружаем из файла
     *
     * @param path путь к файлу
     */
    public static void loadFromFile(String path) {
        // создаём загрузчик JSON
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // считываем систему координат
            task = objectMapper.readValue(new File(path), Task.class);
            PanelLog.success("Файл " + path + " успешно загружен");
        } catch (IOException e) {
            PanelLog.error("Не получилось прочитать файл " + path + "\n" + e);
        }
    }

    /**
     * Загрузить файл
     */
    public static void load() {
        String path = "src/main/resources/conf.json";
        PanelLog.info("load from " + path);
        loadFromFile(path);
    }
}