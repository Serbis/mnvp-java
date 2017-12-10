package ru.serbis.mnvp.mix;

import java.util.List;

/**
 * Утилитные методы для работы со списками
 */
public interface ArrayUtils {
    /**
     * Преобразует список байт в массив
     *
     * @param buffer список байт для преобразования
     * @return массив байт
     */
    default byte[] byteListToArray(List<Byte> buffer) {
        byte[] bf = new byte[buffer.size()]; //Преобразовать список в массив байт
        for (int i = 0; i < buffer.size(); i++) {
            bf[i] = buffer.get(i);
        }

        return bf;
    }
}
