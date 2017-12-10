package ru.serbis.mnvp.exceptions;

/**
 * Базовый класс исключения сети mnvp
 */
public class MnvpExecption extends RuntimeException {
    public MnvpExecption(String message) {
        super(message);
    }
}
