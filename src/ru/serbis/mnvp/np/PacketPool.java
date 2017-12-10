package ru.serbis.mnvp.np;

import ru.serbis.mnvp.mix.Log;
import ru.serbis.mnvp.structs.general.Packet;

import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.Semaphore;

/**
 * Created by serbis on 05.12.17.
 */
public class PacketPool implements Log {
    private Stack<Packet> pool;
    private Semaphore accessSemaphore;
    private String nodeLabel;

    public PacketPool(String nodeLabel) {
        this.nodeLabel = nodeLabel;
        pool = new Stack<>();
        accessSemaphore = new Semaphore(1);
    }

    public synchronized void put(Packet packet) {
        try {
            accessSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (pool.size() == 2) {
            System.out.println("x");
        }
        pool.push(packet);
        log(String.format("<blue>[%s] Пул - размещен пакет -> %s ps -> %d<nc>", nodeLabel, packet.toString(), pool.size()), 3, nodeLabel);
        accessSemaphore.release();
    }

    public synchronized Packet get() {
        try {
            accessSemaphore.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (pool.size() == 3) {
            System.out.println("x");
        }
        if (pool.size() == 0) {
            accessSemaphore.release();

            return null;
        }


        Packet packet = pool.pop();
        log(String.format("<blue>[%s] Пул - выбран пакет -> %s ps -> %d<nc>", nodeLabel, packet.toString(), pool.size()), 3, nodeLabel);

        accessSemaphore.release();


        return packet;
    }
}
