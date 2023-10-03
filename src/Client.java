import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Client {

    public static final int PORT = 50001;

    private static BufferedReader reader;
    private static BufferedReader in;
    private static BufferedWriter out;

    private static class ClientReader implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    String serverWord = in.readLine();
                    if (serverWord != null) {
                        System.out.println(serverWord);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    private static class ClientWriter implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    out.write(reader.readLine() + "\n");
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Socket clientSocket = new Socket("localhost", PORT);

        reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));

        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
        out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8));

        Thread read = new Thread(new ClientReader());
        read.setName("Client-Read");
        read.start();

        Thread write = new Thread(new ClientWriter());
        write.setName("Client-Write");
        write.start();

        read.join();
        write.join();
        clientSocket.close();
        in.close();
        out.close();
    }
}
