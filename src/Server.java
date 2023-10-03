import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Server {

    public static final int PORT = 50001;

    public static final List<String> messages = new ArrayList<>();

    private static class ServerReader implements Runnable {
        private static Socket clientSocket;
        private static BufferedReader in;

        public ServerReader(Socket client) throws IOException {
            clientSocket = client;
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String clientWord = in.readLine();
                    if (clientWord != null) {
                        System.out.println("Got message: " + clientWord);
                        messages.add(clientWord);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        clientSocket.close();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    System.out.println("Closed: " + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
                    break;
                }
            }
        }
    }

    private static class ServerWriter implements Runnable {
        private static List<Socket> clients = new ArrayList<>();
        private static List<BufferedWriter> writers = new ArrayList<>();

        public void add(Socket client) throws IOException {
            clients.add(client);
            writers.add(new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8)));
            System.out.println("Added: " + client.getInetAddress() + ":" + client.getPort());
        }

        @Override
        public void run() {
            while (true) {
                for(BufferedWriter out: writers) {
                    try {
                        for (String message : messages) {
                            out.write(message + "\n");
                            out.flush();
                        }
                    } catch (IOException e) {
                       //
                    }
                }

                messages.clear();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(PORT);
        ServerWriter serverWriter = new ServerWriter();
        new Thread(serverWriter).start();

        try {
            while (true) {
                Socket socket = server.accept();

                try {
                    new Thread(new ServerReader(socket)).start();
                    serverWriter.add(socket);
                } catch (IOException e) {
                    socket.close();
                }
            }
        } finally {
            server.close();
        }
    }
}
