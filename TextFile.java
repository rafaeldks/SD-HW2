import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * Класс для хранения файлов и работы с ними.
 */
public class TextFile {
    /**
     * Абсолютный путь к файлу.
     */
    private final String path;

    /**
     * Уникальный ID для файла с заданным путём.
     */
    private final Integer id;

    /**
     * Статическсий хэш-мап, которые хранит путь к файлу в качестве ключа и уникальный ID в качестве значения.
     */
    private static final HashMap<String, Integer> IDs = new HashMap<>();

    /**
     * Статический счётчик уникальных файлов (с уникальным путём)
     */
    private static Integer fileCounter = 0;

    /**
     * Статический вспомогательный метод для извлечения пути из Require-строки файла.
     *
     * @param requireLine Require-строка в файле.
     * @return Корректный путь к файлу.
     */
    private static String extractPath(String requireLine) {
        return requireLine.substring(9, requireLine.length() - 1);
    }

    /**
     * Конструктор класса.
     *
     * @param path Абсолютный путь к файлу.
     */
    public TextFile(String path) {
        if (IDs.containsKey(path)) {
            id = IDs.get(path);
        } else {
            id = fileCounter;
            IDs.put(path, fileCounter);
            fileCounter++;
        }
        this.path = path;
    }

    /**
     * Вспомогательный статический метод для получения пути к файлу по его ID.
     *
     * @param id ID файла.
     * @return Абсолютный путь к файлу с заданным ID.
     */
    public static String getPathByID(Integer id) {
        for (var key : IDs.keySet()) {
            if (Objects.equals(IDs.get(key), id)) {
                return key;
            }
        }
        return null;
    }

    /**
     * Getter пути файла.
     *
     * @return Абсолютный путь к файлу.
     */
    public String getPath() {
        return path;
    }

    /**
     * Getter ID файла.
     *
     * @return ID файла.
     */
    public Integer getID() {
        return id;
    }

    /**
     * Функция, возвращающая текстовое содержимое всего файла.
     *
     * @return StringBuilder с содержимым файла.
     * @throws IOException Исключение в случае наличия проблем с чтением из файла (например, такого пути не существует).
     */
    public StringBuilder read() throws IOException {
        StringBuilder text = new StringBuilder();
        BufferedReader reader;
        reader = new BufferedReader(new FileReader(path));
        String line = reader.readLine();
        while (line != null) {
            text.append(line).append("\n");
            line = reader.readLine();
        }
        reader.close();
        return text;
    }

    /**
     * Функция для получения списка путей ко всем файлам, запрашиваемых в данном файле.
     *
     * @return Список из путей к запрашиваемым файлам.
     * @throws IOException Исключение в случае наличия проблем с чтением из файла (например, такого пути не существует).
     */
    public ArrayList<String> getRequiredFiles() throws IOException {
        BufferedReader reader;
        ArrayList<String> paths = new ArrayList<>(0);
        reader = new BufferedReader(new FileReader(path));
        String line = reader.readLine();
        while (line != null) {
            if (line.startsWith("require ")) {
                paths.add(extractPath(line));
            }
            line = reader.readLine();
        }
        reader.close();
        return paths;
    }

    /**
     * Переопределение функции equals (для хэш-мапа)
     *
     * @param obj Объект, с которым происходит сравнение.
     * @return True - если объекты равны; False - иначе.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        return ((TextFile) obj).getPath().equals(path);
    }

    /**
     * Переопределение функции hashCode (для хэш-мапа)
     *
     * @return Значение хэш-функции
     */
    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
