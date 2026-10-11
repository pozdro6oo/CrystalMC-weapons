package eu.crystalmc.weapons.utils;


import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;


import java.io.*;
import java.nio.file.*;

public class HTTPServer {

    private Path path;

    private String urlPath;

    public void setPath(Path path) {
        this.path = path.toAbsolutePath().normalize();
        urlPath = "/" + path.getFileName();
    }

    public Path getPath() {
        return path;
    }

    public String getUrlPath() {
        return urlPath;
    }

    public void host(int port) throws IOException {
        try (ServerSocket server = new ServerSocket(port)) {
            while (true) {
                Socket client = server.accept();
                handleClient(client);
            }
        }
    }

    private void handleClient(Socket client) throws IOException {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(
                        client.getInputStream(),
                        StandardCharsets.US_ASCII));

        OutputStream out = client.getOutputStream();

        String request = in.readLine();
        if (request == null) return;

        String[] parts = request.split(" ");
        if (parts.length < 2 || !parts[0].equals("GET")) {
            return;
        }

        String requestedPath = parts[1].split("\\?", 2)[0];

        if (!urlPath.equals(requestedPath)) {
            return;
        }

        if (!Files.isRegularFile(path)) {
            return;
        }

        byte[] data = Files.readAllBytes(path);

        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        String headers =
                "HTTP/1.1 200 OK\r\n" +
                        "Content-Type: " + contentType + "\r\n" +
                        "Content-Length: " + data.length + "\r\n" +
                        "Content-Disposition: inline; filename=\"" + path.toFile().getName() +"\"\r\n" +
                        "Connection: close\r\n\r\n";

        out.write(headers.getBytes(StandardCharsets.US_ASCII));
        out.write(data);
        out.flush();

    }

}
