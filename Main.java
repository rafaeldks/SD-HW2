import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Класс Main с точкой входа и вспомогательными функциями.
 */
public class Main {
    /**
     * Вспомогательная функция для ввода пользователем пути до корневой папки.
     *
     * @return Строка - путь до корневой папки.
     * @throws IOException Исключение в случае проблем с чтением из консоли.
     */
    public static String getPath() throws IOException {
        System.out.println("Введите абсолютный путь до корневой папки: ");
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String path = reader.readLine();
        if (!path.endsWith("/")) path += "/";
        return path;
    }

    /**
     * Процедура для красивого вывода списка файлов, отсортированных по зависимостям.
     *
     * @param root Экземпляр класса Root - корневая папка.
     */
    public static void printList(Root root) {
        System.out.println("Отсортированный по зависимостям список файлов: ");
        System.out.println("(Разные компоненты связности выводятся через \\n) \n");
        root.printList();
    }

    /**
     * Процедура проверки на наличие циклов в зависимостях файлов. При наличии циклов завершает работу программы.
     *
     * @param root Экземпляр класса Root - корневая папка.
     */
    public static void checkLoops(Root root) {
        try {
            if (root.hasLoops()) {
                System.out.println("В зависимостях имеются циклы. Завершение работы...");
                System.exit(1);
            }
            System.out.println("В зависимостях не обнаружено циклов.");
        } catch (StackOverflowError ex) {
            System.out.println("Ошибка: в зависимостях имеются циклы...");
            System.exit(1);
        }
    }

    /**
     * Точка входа.
     *
     * @param args Аргументы командной строки
     */
    public static void main(String[] args) {
        Root root = null;
        try {
            root = new Root(getPath());
        } catch (IOException | ArrayIndexOutOfBoundsException ex) {
            System.out.println("Ошибка: какого-либо из файлов, запрашиваемых в require, не существует...");
            System.exit(1);
        } catch (StackOverflowError ex) {
            System.out.println("Ошибка: в зависимостях имеются циклы...");
            System.exit(1);
        } catch (Exception ex) {
            System.out.println("Ошибка: неверно указан путь к корневой папке...");
            System.exit(1);
        }
        checkLoops(root);
        printList(root);
        try {
            root.concatenateFiles();
            System.out.println("Содержимое файлов склеено в файл 'Result.txt'");
        } catch (Exception e) {
            System.out.println("При записи в файл 'Result.txt' произошла ошибка...");
        }
    }
}