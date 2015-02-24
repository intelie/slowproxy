package net.intelie.slowproxy;

import java.net.Socket;
import java.util.concurrent.Future;
public class Util {
    public static void silentlyClose(Socket localSocket) {
        try {
            localSocket.close();
        } catch (Throwable ignored) {

        }
    }

    public static <V> V getSilently(Future<V> future) {
        boolean interrupted = false;
        try {
            while (true) {
                try {
                    return future.get();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } catch (Throwable ignored) {
            return null;
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }
}
