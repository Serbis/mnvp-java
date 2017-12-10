package ru.serbis.mnvp.exceptions;

/**
 * Исключение инициализации сети mnvp. Возникает в случае ошибок при
 * инициализации окружения. Пример - запуск узла до  установки конфига,
 * или с заведомо конфликтующими или отствующими параметрами.
 */
public class MnvpInitializeException extends MnvpExecption {
    public MnvpInitializeException(String msg) {
        super(msg);
    }
}
