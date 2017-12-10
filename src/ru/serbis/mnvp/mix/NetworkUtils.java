package ru.serbis.mnvp.mix;

import ru.serbis.mnvp.general.NodeVars;

/**
 * Утилитные методы для работы с сетью
 */
public interface NetworkUtils {

    /**
     * Возвращает новый идентификатор сообщения инкрементируя счетчик сообщений
     * узла контролем переполнения.
     *
     * @param  nodeLabel метка узла
     * @return идетификатор
     */
    default int getNewMsgId(String nodeLabel) {
        int nm = NodeVars.getInstance(nodeLabel).getMsgCounter();
        NodeVars.getInstance(nodeLabel).setMsgCounter(nm + 1);

        return nm;
    }
}
