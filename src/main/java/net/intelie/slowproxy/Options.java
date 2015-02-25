package net.intelie.slowproxy;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Options {
    private final SpeedDefinition speed;
    private final HostDefinition local;
    private final List<HostDefinition> remote;

    public Options(SpeedDefinition speed, HostDefinition local, List<HostDefinition> remote) {
        this.speed = speed;
        this.local = local;
        this.remote = remote;
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
        HostDefinition localHost;
        List<HostDefinition> remoteHosts = new ArrayList<HostDefinition>();
        SpeedDefinition speedDefinition;

        ArrayDeque<String> deque = new ArrayDeque<String>(Arrays.asList(args));

        speedDefinition = deque.peekFirst().charAt(0) == '@' ? SpeedDefinition.parse(deque.pollFirst().substring(1)) : new SpeedDefinition(-1, -1);
        localHost = HostDefinition.parseWithOptionalHost(deque.pollFirst());
        while (!deque.isEmpty())
            remoteHosts.add(HostDefinition.parse(deque.pollFirst()));
        return new Options(speedDefinition, localHost, remoteHosts);
    }
}
