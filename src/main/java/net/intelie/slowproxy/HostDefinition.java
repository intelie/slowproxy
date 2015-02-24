package net.intelie.slowproxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HostDefinition {
    private final String host;
    private final int port;

    public HostDefinition(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String host() {
        return host;
    }

    public int port() {
        return port;
    }

    public static HostDefinition parse(String s) {
        String[] strings = s.split(":", 2);
        return new HostDefinition(strings[0], Integer.parseInt(strings[1]));
    }

    public static HostDefinition parseWithOptionalHost(String s) {
        String[] strings = s.split(":", 2);
        if (strings.length == 1)
            return new HostDefinition(null, Integer.parseInt(strings[0]));
        else
            return new HostDefinition(strings[0], Integer.parseInt(strings[1]));
    }

    public Socket newSocket() throws IOException {
        return new Socket(host, port);
    }

    public ServerSocket newServerSocket() throws IOException {
        if (host != null) {
            InetAddress address = InetAddress.getByName(host);
            return new ServerSocket(port, 50, address);
        } else {
            return new ServerSocket(port);
        }
    }

    @Override
    public String toString() {
        return (host != null ? host : "") + ":" + port;
    }
}
