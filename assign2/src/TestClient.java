import java.net.*;
import java.io.*;

public class TestClient {
    public static void main(String[] args) {
        if (args.length < 2)
            return;

        int port = Integer.parseInt(args[0]);
        String operation = args[1];

        try (Socket socket = new Socket("localhost", port)) {
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            switch (operation) {
                case "put":
                    if (args.length != 3) {
                        System.out.println("ERROR: put format must be: > put <file_path>");
                        return;
                    }
                    writer.println(operation + " " + args[2]);
                    break;
                case "get":
                    if (args.length != 3) {
                        System.out.println("ERROR: get format must be: > get <SHA-256 key>");
                        return;
                    }
                    writer.println(operation + " " + args[2]);
                    break;
                case "delete":
                    if (args.length != 3) {
                        System.out.println("ERROR: delete format must be: > delete <SHA-256 key>");
                        return;
                    }
                    writer.println(operation + " " + args[2]);
                    break;
                case "join":
                    if (args.length != 2) {
                        System.out.println("ERROR: join operation has no arguments");
                        return;
                    }
                    writer.println(operation);

                    break;
                case "leave":
                    if (args.length != 2) {
                        System.out.println("ERROR: leave operation has no arguments");
                        return;
                    }
                    writer.println(operation);
                    break;
                default:
                    System.out.println("Error from Client");
                    writer.println("Error from Client");
            }

            InputStream input = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            String message = reader.readLine();

            System.out.println(message);

        } catch (UnknownHostException ex) {

            System.out.println("Server not found: " + ex.getMessage());

        } catch (IOException ex) {

            System.out.println("I/O error: " + ex.getMessage());
        }
        return;
    }
}
