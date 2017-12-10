package ru.serbis.mnvp.mix;

import ru.serbis.mnvp.general.LogsController;

import java.io.FileOutputStream;
import java.io.IOException;


public interface Log {
    default void log(String message, int level, String nodeLabel) {
        LogsController.getInstance(nodeLabel).log(message, level);


    }

    default void lognnl(String message) {
        //String messageForLog = message;
        //message = repColCodes(message);

       // System.out.print(message);
    }

    /*default void writeToLogFile(String message) {
        try {
            Global.getInstanse().getLogFos().write(message.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
