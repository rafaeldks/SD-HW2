import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Класс, хранящий корневую папку, который позволяет работать с её содержимым.
 */
public class Root {
    /**
     * Путь до корневой папки.
     */
    private final String path;

    /**
     * Количество всех файлов в корневой папке (в том числе и в её подпапках).
     */
    private int filesCounter;

    /**
     * Матрица смежности графа.
     */
    private final int[][] adjacencyMatrix;

    /**
     * Массив с цветами вершин графа.
     */
    private static int[] color;

    /**
     * Стэк, использующийся при топологической сортировке вершин графа.
     */
    private static final Stack<Integer> stack = new Stack<>();

    /**
     * Конструктор класса.
     *
     * @param path Абсолютный путь к корневой папке.
     * @throws IOException Исключение при наличии проблем с файлами (например, несуществующий путь).
     */
    public Root(String path) throws IOException {
        this.path = path;
        filesCounter = countFiles();
        adjacencyMatrix = new int[filesCounter][filesCounter];
        color = new int[filesCounter];
        fillAdjacencyMatrix();
    }

    /**
     * Статический метод, возвращающий массив с названиями всех подпапок в заданной папке.
     *
     * @param path Путь к папке.
     * @return Массив строк - названий подпапок.
     */
    static private String[] getFolders(String path) {
        File file = new File(path);
        return file.list((current, name) -> new File(current, name).isDirectory());
    }

    /**
     * Вспомогательный статический метод для подсчёта общего количества файлов в папке и её подпапках.
     *
     * @param path Путь к папке.
     * @return Количество всех файлов в папке и её подпапках.
     */
    static private int recursiveCount(String path) {
        String[] subfolders = getFolders(path);
        int result = getFileNames(path).size();
        for (var folder : subfolders) {
//            System.out.println(path + folder + "/");
            result += recursiveCount(path + folder + "/");
        }
        return result;
    }

    /**
     * Статический рекурсивный метод для получения путей ко всем файлам в папке и её подпапках.
     *
     * @param path  Путь к папке.
     * @param paths Текущий список путей (используется для обновления).
     * @return Список путей ко всем файлам в данной папке и её подпапках.
     */
    static private ArrayList<String> recursiveSearch(String path, ArrayList<String> paths) {
        String[] subfolders = getFolders(path);
        for (var file : getFileNames(path)) {
            paths.add(path + file);
        }
        for (var folder : subfolders) {
            paths = recursiveSearch(path + folder + "/", paths);
        }
        return paths;
    }

    /**
     * Статический метод для получения названий всех файлов в текущей папке и её подпапках.
     *
     * @param path Путь к папке.
     * @return Список из строк с названиями всех файлов в текущей папке и её подпапках.
     */
    static private ArrayList<String> getFileNames(String path) {
        Set<String> set = Stream.of(Objects.requireNonNull(new File(path).listFiles()))
                .filter(file -> !file.isDirectory())
                .map(File::getName)
                .collect(Collectors.toSet());
        ArrayList<String> array = new ArrayList<>();
        for (var directory : set) {
            if (directory.endsWith(".txt")) array.add(directory);
        }
        return array;
    }

    /**
     * Вспомогательный метод для подсчёта всех файлов в данной папке и её подпапках.
     *
     * @return Количество всех .txt файлов в данной папке и её подпапках.
     */
    private int countFiles() {
        filesCounter = recursiveCount(path);
        return filesCounter;
    }

    /**
     * Метод для получения списка путей ко всем файлам в данной папке и её подпапках.
     *
     * @return Список путей ко всем файлам в данной папке и её подпапках.
     */
    private ArrayList<String> getFilesPaths() {
        ArrayList<String> paths = new ArrayList<>();
        paths = recursiveSearch(path, paths);
        return paths;
    }

    /**
     * Процедура для заполнения матрицы смежности текущей корневой папки.
     *
     * @throws IOException Исключение в случае проблем с файлами (например, указан несуществующий путь).
     */
    private void fillAdjacencyMatrix() throws IOException {
        ArrayList<String> paths = getFilesPaths();
        for (var directory : paths) {
            TextFile file = new TextFile(directory);
            ArrayList<String> requiredFiles = file.getRequiredFiles();
            for (var requiredPath : requiredFiles) {
                TextFile requiredFile = new TextFile(path + requiredPath + ".txt");
                adjacencyMatrix[file.getID()][requiredFile.getID()] = 1;
            }
        }
    }

    /**
     * Процедура запуска обхода в глубину с топологической сортировкой.
     *
     * @param fileID Номер вершины, в качестве которого выступает уникальный ID файла.
     */
    private void dfs(int fileID) {
        if (color[fileID] == 2) {
            return;
        }
        color[fileID] = 1;
        for (int j = 0; j < filesCounter; j++) {
            if (adjacencyMatrix[fileID][j] == 1) {
                dfs(j);
            }
        }
        color[fileID] = 2;
        stack.push(fileID);
    }

    /**
     * Функция для получения списка компонент связности из файлов.
     *
     * @return Список списка строк, где хранятся каждая компонента связности находится в отдельном списке.
     */
    private ArrayList<ArrayList<String>> getList() {
        ArrayList<ArrayList<String>> result = new ArrayList<>();
        for (int i = 0; i < filesCounter; i++) {
            if (isSatisfyingNode(i)) {
                if (color[i] == 0) {
                    dfs(i);
                    ArrayList<String> component = new ArrayList<>();
                    int size = stack.size();
                    for (int j = 0; j < size; j++) {
                        component.add(TextFile.getPathByID(stack.pop()));
                    }
                    Collections.reverse(component);
                    result.add(component);
                }
            }
        }
        color = new int[filesCounter];
        return result;
    }

    /**
     * Вспомогательнаая булева функция для проверки вершины на доступность при посещении в dfs.
     *
     * @param n Номер вершины.
     * @return True - вершину можно посетить; False - нельзя.
     */
    private boolean isSatisfyingNode(int n) {
        for (int i = 0; i < filesCounter; i++) {
            if (adjacencyMatrix[i][n] == 1) return false;
        }
        return true;
    }

    /**
     * Процедура вывода списка зависимостей файлов в консоль.
     */
    public void printList() {
        var list = getList();
        for (var component : list) {
            for (var path : component) {
                System.out.println(path + " ");
            }
            System.out.println();
        }
    }

    /**
     * Процедура склеивания содержимого всех файлов в данной корневой папке и её подпапках в нужном порядке.
     *
     * @throws IOException Исключение в случае наличия проблем с записью в файл "Result.txt".
     */
    public void concatenateFiles() throws IOException {
        var list = getList();
        PrintWriter writer = new PrintWriter("Result.txt", StandardCharsets.UTF_8);
        for (var component : list) {
            for (var path : component) {
                TextFile textFile = new TextFile(path);
                writer.println(textFile.read());
            }
        }
        writer.close();
    }

    /**
     * Булева функция для проверки на наличие циклов в зависимостях файлов.
     *
     * @return True - циклы есть; False - иначе.
     */
    public boolean hasLoops() {
        var list = getList();
        int sum = 0;
        for (var component : list) {
            sum += component.size();
        }
        return sum != filesCounter;
    }
}
