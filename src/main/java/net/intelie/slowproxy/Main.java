package net.intelie.slowproxy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    @SuppressWarnings("InfiniteLoopStatement")
    public static void main(String[] args) throws Exception {
        final Options options;
        try {
            options = Options.parse(args);
        } catch (Exception e) {
            System.out.println(e.getClass().getName() + ": " + e.getMessage());
            System.out.println("usage: slowproxy [@<speed>] <local port> [<remote host>:<remote port>]...");
            System.out.println("example: slowproxy @56kbps 1234 somehost:80");
            System.exit(1);
            return;
        }

        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        ExecutorService executor = Executors.newCachedThreadPool();
        AtomicInteger currentHost = new AtomicInteger(0);

        executor.submit(new ServerTask(options, scheduled, executor, currentHost));

        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        while ((line = reader.readLine()) != null && !"q".equals(line)) {
            int nextHost = setNextHost(line, options.remote().size(), currentHost.get());
            if (nextHost < 0) {
                System.out.println("Invalid host number: " + line);
            } else {
                System.out.println("Using host #" + nextHost + ": " + options.remote().get(nextHost));
                currentHost.set(nextHost);
            }
        }
        System.exit(0);
    }

    private static int setNextHost(String line, int hostCount, int currentHost) {
        int nextHost = (currentHost + 1) % hostCount;
        if (!line.isEmpty()) {
            try {
                nextHost = Integer.parseInt(line);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (nextHost < 0 || nextHost >= hostCount) {
            return -1;
        } else {
            return nextHost;
        }
    }

}
