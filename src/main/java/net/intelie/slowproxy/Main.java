package net.intelie.slowproxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws Exception {
        if (args.length == 0)
            printUsageAndExit();
        final Options options;
        try {
            options = Options.parse(args);
        } catch (Exception e) {
            System.out.println(e.getClass().getName() + ": " + e.getMessage());
            printUsageAndExit();
            return;
        }

        ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
        ExecutorService executor = Executors.newCachedThreadPool();
        CyclicCounter currentHost = new CyclicCounter(options.remote().size());

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

    private static void printUsageAndExit() throws IOException {
        InputStream input = Main.class.getResourceAsStream("/usage.txt");
        byte[] buffer = new byte[1024]; // Adjust if you want
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1) {
            System.out.write(buffer, 0, bytesRead);
        }
        System.exit(1);
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
