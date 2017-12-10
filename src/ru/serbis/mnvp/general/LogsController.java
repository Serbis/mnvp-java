package ru.serbis.mnvp.general;

import ru.serbis.mnvp.mix.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Контроллер логгирования
 *
 * Данный объект является пулом синглетонов с номерной регистрацией
 *
 */
public class LogsController implements Log {
    /** Файл вывода лога */
    private File logFile;
    /** Выходной поток файла лога */
    private FileOutputStream logOs;
    /** Уровень логгирования */
    private int logLevel;
    /** Форметтер даты */
    private SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss dd.MM.yyyy");

    private static Map<String, LogsController> instancesPool = new HashMap<>();

    /**
     * Регестрирует новый синглетон с заданным номером
     *
     * @param label метка синглетона
     */
    public static void registerInstance(String label) {
        instancesPool.put(label, new LogsController());
    }

    /**
     * Снимает регистрацию синглетона с заданным номером
     *
     * @param label метка синглетона
     */
    public static void unregisterInstance(String label) {
        instancesPool.remove(label);
    }
    /**
     * Экстрактор синглетона
     *
     * @param label метка синглетона
     * @return синглетон
     */
    public static LogsController getInstance(String label) {
        return instancesPool.get(label);
    }

    /**
     * Устанавливает файл вывода лога, проверяя возможность записи в него
     * и создавая выходной поток записи
     *
     * @param logFile файл лога
     */
    public void setLogFile(File logFile) {
        this.logFile = logFile;
        if (!logFile.exists()) {
            try {
                boolean result = logFile.createNewFile();
                if (!result)
                    throw new IOException();
            } catch (IOException e) {
                System.out.println(String.format("Не удалось открыть для записи файл лога %s", logFile.getPath()));
                e.printStackTrace();
                return;
            }
        }

        try {
            logOs = new FileOutputStream(logFile);
        } catch (FileNotFoundException e) {
            System.out.println(String.format("Не удалось открыть для записи файл лога %s", logFile.getPath()));
            e.printStackTrace();
        }


    }

    public void setLogLevel(int logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * Производит запись лога в стандартный вывод и в файл, если задан файл
     * для вывода
     *
     * @param msg текст записи
     * @param logLevel уровень логгирования
     */
    public void log(String msg, int logLevel) {
        if (logLevel > this.logLevel)
            return;

        msg = String.format("[%s] %s", sdf.format(new Date()), msg);

        if (logOs != null) {
            String msgForFile = deleteCodes(msg);
            try {
                logOs.write(String.format("----------> %s\n", msgForFile).getBytes());
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
        }

        msg = repColCodes(msg);
        System.out.println(String.format("----------> %s", msg));
    }

    private String repColCodes(String message) {
        message = message.replaceAll("<red>", (char)27 + "[0;31m");
        message = message.replaceAll("<green>", (char)27 + "[0;32m");
        message = message.replaceAll("<black>", (char)27 + "[0;30m");
        message = message.replaceAll("<yellow>", (char)27 + "[0;33m");
        message = message.replaceAll("<blue>", (char)27 + "[0;34m");
        message = message.replaceAll("<lblue>", (char)27 + "[1;34m");
        message = message.replaceAll("<nc>", (char)27 + "[0;0m");

        return message;
    }

    private String deleteCodes(String message) {
        String nmsg = message;
        nmsg = nmsg.replaceAll("<red>", "");
        nmsg = nmsg.replaceAll("<green>", "");
        nmsg = nmsg.replaceAll("<black>", "");
        nmsg = nmsg.replaceAll("<yellow>", "");
        nmsg = nmsg.replaceAll("<blue>", "");
        nmsg = nmsg.replaceAll("<lblue>", "");
        nmsg = nmsg.replaceAll("<nc>", "");

        return nmsg;
    }
}
