package net.intelie.slowproxy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Options {
    private final boolean balance;
    private final SpeedDefinition speed;
    private final HostDefinition local;
    private final List<HostDefinition> remote;

    public Options(boolean balance, SpeedDefinition speed, HostDefinition local, List<HostDefinition> remote) {
        this.balance = balance;
        this.speed = speed;
        this.local = local;
        this.remote = remote;
    }

    public boolean balance() {
        return balance;
    }

    public SpeedDefinition speed() {
        return speed;
    }

    public HostDefinition local() {
        return local;
    }

    public List<HostDefinition> remote() {
        return remote;
    }

    public static Options parse(String... args) {
        List<HostDefinition> remoteHosts = new ArrayList<HostDefinition>();
        ArrayDeque<String> deque = new ArrayDeque<String>(Arrays.asList(args));

        boolean balance = "balance".equals(deque.peekFirst());
        if (balance) deque.poll();

        SpeedDefinition speedDefinition = deque.peekFirst().charAt(0) == '@' ? SpeedDefinition.parse(deque.pollFirst().substring(1)) : SpeedDefinition.NO_LIMIT;

        HostDefinition localHost = HostDefinition.parseWithOptionalHost(deque.pollFirst());
        while (!deque.isEmpty())
            remoteHosts.add(HostDefinition.parse(deque.pollFirst()));
        return new Options(balance, speedDefinition, localHost, remoteHosts);
    }
}
