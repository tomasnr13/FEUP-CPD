import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.security.NoSuchAlgorithmException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.nio.file.StandardOpenOption;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.math.BigInteger;
import java.security.MessageDigest;

public class Store implements Runnable {
    private String ip_mcast_addr;
    private int ip_mcast_port;
    private int node_id;
    private static String hashedNode;
    private int port;
    private int membershipCounter;
    private ServerSocket server;
    private Socket client;
    private InetAddress group;
    private mcastHandler mcast_thread;

    public static void main(String[] args) throws NoSuchAlgorithmException {
        if (args.length != 4) {
            System.out.println("Error args\n");
            return;
        }
        // call constructor with args
        Store node = new Store(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]),
                Integer.parseInt(args[3]));
        // create thread
        new Thread(node).start();
        // call run
        node.run();
    }

    /** Constructor */
    public Store(String m_addr, int m_port, int id, int s_port) throws NoSuchAlgorithmException {
        ip_mcast_addr = m_addr;
        ip_mcast_port = m_port;
        node_id = id;
        hashedNode = generateHashCode(Integer.toString(id));
        port = s_port;
        membershipCounter = -1;
        client = null;
        group = null;
        try {
            server = new ServerSocket(port);
            System.out.println("Node " + node_id + " is listening on port " + port);

        } catch (IOException e) {
            e.printStackTrace();
        }
        File parentDirectory = new File("store");
        if (!parentDirectory.exists()) {
            parentDirectory.mkdir();
        }
        File directory = new File("store/" + hashedNode);
        if (!directory.exists()) {
            directory.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

    }

    @Override
    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        try {
            // thread to handle tcp requests and messages
            tcpHandler tcp_thread = new tcpHandler();
            new Thread(tcp_thread).start();

            // new thread for multicast messages
            mcast_thread = new mcastHandler();
            new Thread(mcast_thread).start();

        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                    client.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String join() {
        try {
            if ((membershipCounter % 2) == 0 || (membershipCounter == 0)) {
                String message = "Error: Node is already in cluster";
                return message;
            } else {
                membershipCounter++;
                createDirectory("store/" + hashedNode, "membership_log.txt", node_id + " " + membershipCounter + "\n");
            }

            String message = "JOIN:" + node_id + ":" + membershipCounter;
            // String message2 = "MEMBERSHIPREQUEST";

            mcast_thread.sendMulticastMessage(message, ip_mcast_addr, ip_mcast_port);
            // mcast_thread.sendMulticastMessage(message2, ip_mcast_addr, ip_mcast_port);

        } catch (Exception e) {
            e.printStackTrace();
        }
        String message = "Success: Node joined the cluster";
        return message;
    }

    public String leave() throws Exception {
        if ((membershipCounter % 2) == 1) {
            String message = "Error: Node is not in cluster";
            return message;
        }
        try {
            if (server != null) {
                String oldLine = node_id + " " + membershipCounter;
                membershipCounter++;
                String newLine = node_id + " " + membershipCounter;
                overwriteLine("store/" + hashedNode + "/membership_log.txt", oldLine, newLine);

                // get files from folder
                String[] fileNames = getFileNames("store/" + hashedNode);

                for (String i : fileNames) {
                    if (!i.equals("membership_log.txt")) {
                        String[] parts = i.split("\\.");
                        if (parts.length > 1) {
                            int responsibleId = getResponsibleNode(i);
                            String value = get(parts[0]);
                            String msg = "PUT:" + responsibleId + ":" + parts[0] + ":" + value;
                            mcast_thread.sendMulticastMessage(msg, ip_mcast_addr, ip_mcast_port);
                        }
                        deletefile("store/" + hashedNode + "/" + i);
                    }
                }
                deletefile("store/" + hashedNode + "/membership_log.txt");

                System.out.println("Node " + node_id + " left the cluster");
                String message = "LEAVE:" + node_id + ":" + membershipCounter;
                mcast_thread.sendMulticastMessage(message, ip_mcast_addr, ip_mcast_port);

            }
            // pass key value to
        } catch (IOException e) {
            e.printStackTrace();
        }
        String message = "Success: Node left the cluster";
        return message;
    }

    /*
     * public String getMultiCastMessage() throws IOException {
     * byte[] buffer = new byte[1024];
     * DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
     * m_socket.receive(packet); // Exception
     * String msg = new String(packet.getData(),
     * packet.getOffset(), packet.getLength());
     * System.out.println("[Multicast UDP message received] >> " + msg);
     * return msg;
     * }
     */

    /** Key-Value Store Interface */
    public int put(String key, String value) throws Exception { // which adds a key-value pair to the store
        int responsibleNode = getResponsibleNode(key);
        if (node_id == responsibleNode) {
            createDirectory("store/" + hashedNode, key + ".txt", value);
            return -1;
        }
        return responsibleNode;
    }

    public String get(String key) throws Exception { // which retrieves the value bound to a key
        String value = "";
        int responsibleNode = getResponsibleNode(key);
        if (node_id == responsibleNode) {
            try {
                File myObj = new File("store/" + hashedNode + "/" + key + ".txt");
                Scanner myReader = new Scanner(myObj);
                while (myReader.hasNextLine()) {
                    value = myReader.nextLine();
                }
                myReader.close();
                return value;
            } catch (FileNotFoundException e) {
                System.out.println("An error occurred.");
                e.printStackTrace();
            }
        }
        value = "Responsible Node:" + responsibleNode;
        return value;
    }

    public int delete(String key) throws Exception { // which deletes a key-value pair
        int responsibleNode = getResponsibleNode(key);
        if (node_id == responsibleNode) {
            File myObj = new File("store/" + hashedNode + "/" + key + ".txt");
            if (myObj.delete()) {
                return -1;
            }
        }
        return responsibleNode;

    }

    public void deletefile(String path) throws Exception {
        File myObj = new File(path);
        myObj.delete();
    }

    private class tcpHandler implements Runnable {

        @Override
        public void run() {
            try {
                // new thread for tcp client/other nodes whenever theres a new accept
                while (true) {
                    client = server.accept();

                    System.out.println("New client connected to store node" + client.getInetAddress().getHostAddress());

                    InputStream input = client.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                    OutputStream output = client.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);

                    String line = reader.readLine();
                    if (line != null) {

                        String[] call = line.split(" ");

                        // writing the received message from client
                        System.out.printf(" Sent from the client: %s\n", line);

                        boolean isInCluster = (membershipCounter % 2 == 0) || (membershipCounter == 0);
                        String message;

                        switch (call[0]) {
                            case "put":
                                if (call.length == 2) {
                                    if (isInCluster) {
                                        String value = getFileValue(call[1]);
                                        String key = generateHashCode(value);
                                        int node = Store.this.put(key, value);
                                        if (node != -1) {
                                            message = "The node responsible for that file is " + node;
                                            writer.println(message);
                                            // send client "The node responsible for that file is " + node
                                        } else {
                                            message = "Success!";
                                            writer.println(message);
                                            // send client "Success"
                                        }
                                    } else {
                                        message = "The node you are trying to contact is not in the cluster";
                                        writer.println(message);
                                    }
                                }
                                break;
                            case "get":
                                if (call.length == 2) {
                                    if (isInCluster) {
                                        String value = Store.this.get(call[1]);
                                        String[] parts = value.split(":");
                                        if (parts[0].equals("Responsible Node")) {
                                            message = "The node responsible for that file is " + parts[1];
                                            writer.println(message);
                                            // send client "The node responsible for that file is " + node
                                        } else {
                                            message = "The value of that file is: " + value;
                                            writer.println(message);
                                            // send cliente the value in the file is value
                                        }

                                    } else {
                                        message = "The node you are trying to contact is not in the cluster";
                                        writer.println(message);
                                    }
                                }
                                break;
                            case "delete":
                                if (call.length == 2) {
                                    if (isInCluster) {
                                        int node = Store.this.delete(call[1]);
                                        if (node != -1) {
                                            message = "The node responsible for that file is " + node;
                                            writer.println(message);
                                            // send client "The node responsible for that file is " + node
                                        } else {
                                            message = "Success!";
                                            writer.println(message);
                                            // send client "Success"
                                        }
                                    } else {
                                        message = "The node you are trying to contact is not in the cluster";
                                        writer.println(message);
                                    }
                                }
                                break;
                            case "join":
                                message = Store.this.join();
                                writer.println(message);
                                break;
                            case "leave":
                                message = Store.this.leave();
                                writer.println(message);
                                break;
                            default:
                                System.out.println("Error from Server");
                                writer.println("Error from Server");
                        }
                    }
                    // out = new PrintWriter(client.getOutputStream(), true);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class mcastHandler implements Runnable {

        @Override
        public @Deprecated void run() {
            while (true) {
                try {
                    MulticastSocket m_socket = new MulticastSocket(ip_mcast_port);
                    group = InetAddress.getByName(ip_mcast_addr);
                    m_socket.joinGroup(group);
                    byte[] buffer = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    m_socket.receive(packet); // Exception
                    String mc = new String(packet.getData(), packet.getOffset(), packet.getLength());
                    boolean inCluster = ((membershipCounter % 2 == 0) || (membershipCounter == 0));
                    if (mc != null) {
                        // System.out.println("Message received: " + mc);
                        String[] message = mc.split(":");
                        switch (message[0]) {
                            case "JOIN":
                                if (message.length != 3) {
                                    break;
                                } else {
                                    if (inCluster) {
                                        String newLine = message[1] + " " + message[2] + "\n";
                                        // System.out.println("Linha: " + newLine);
                                        updateMembershipLog(newLine);

                                        String[] fileNames = getFileNames("store/" + hashedNode);

                                        for (String i : fileNames) {
                                            if (!i.equals("membership_log.txt")) {
                                                String[] parts = i.split("\\.");
                                                if (parts.length > 1) {
                                                    int responsibleId = getResponsibleNode(i);
                                                    if (responsibleId != node_id) {
                                                        String value = get(parts[0]);
                                                        String msg = "PUT:" + responsibleId + ":" + parts[0] + ":"
                                                                + value;
                                                        mcast_thread.sendMulticastMessage(msg, ip_mcast_addr,
                                                                ip_mcast_port);
                                                        deletefile("store/" + hashedNode + "/" + i);
                                                    }

                                                }
                                            }
                                        }

                                        String log = getFileValue("store/" + hashedNode + "/membership_log.txt");
                                        String msg = "MEMBERSHIPLOG:" + log;
                                        mcast_thread.sendMulticastMessage(msg, ip_mcast_addr, ip_mcast_port);
                                    }
                                }
                                break;
                            case "LEAVE":
                                if (message.length != 3) {
                                    break;
                                } else {
                                    if (inCluster) {
                                        String leaveLine = message[1] + " " + message[2] + "\n";
                                        // System.out.print("Linha leave: " + leaveLine);
                                        updateMembershipLog(leaveLine);
                                    }
                                }
                                break;
                            case "MEMBERSHIPLOG":
                                if (message.length != 2) {
                                    break;
                                } else {
                                    File file = new File("store/" + hashedNode + "/membership_log");
                                    if (file.exists()) {
                                        updateMembershipLog(message[1]);
                                    }
                                }
                                break;
                            case "PUT":
                                if (message.length != 4) {
                                    break;
                                } else {
                                    if (inCluster) {
                                        if (Integer.valueOf(message[1]) == node_id) {
                                            put(message[2], message[3]);
                                        }
                                    }
                                }
                                break;
                            case "MEMBERSHIPREQUEST":
                                if (message.length != 1) {
                                    break;
                                } else if (inCluster) {
                                    String log = getFileValue("store/" + hashedNode + "/membership_log.txt");
                                    String msg = "MEMBERSHIPLOG:" + log;
                                    mcast_thread.sendMulticastMessage(msg, ip_mcast_addr, ip_mcast_port);
                                }
                                break;

                        }
                    }
                    m_socket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public void sendMulticastMessage(String message, String m_addr, int m_port) throws IOException {
            DatagramSocket socket = new DatagramSocket();
            byte[] msg = message.getBytes();
            DatagramPacket packet = new DatagramPacket(msg, msg.length, group, ip_mcast_port);
            socket.send(packet);
            // System.out.println("Message sent: " + message);
            socket.close();
        }
    }

    public static void updateMembershipLog(String log) throws Exception {
        String membership_log = getFileValue(
                "store/" + hashedNode + "/membership_log.txt");
        String[] originalLog = membership_log.split("\\n");
        String[] newLog = log.split("\\n");
        String newMembershipLog = "";
        boolean idFound = false;
        String[] found = newLog;

        for (int i = 0; i < originalLog.length; i++) {
            String[] node_i = originalLog[i].split("\\ ");
            for (int j = 0; j < newLog.length; j++) {
                String[] node_j = newLog[j].split("\\ ");
                if (node_i[0].equals(node_j[0])) {
                    found[j] = "found";
                    idFound = true;
                    if (Integer.valueOf(node_i[1]) > Integer.valueOf(node_j[1])) {
                        newMembershipLog += node_i[0] + " " + node_i[1] + "\n";
                    } else {
                        newMembershipLog += node_j[0] + " " + node_j[1] + "\n";
                    }
                    continue;
                }
            }
            if (!idFound) {
                newMembershipLog += node_i[0] + " " + node_i[1] + "\n";
            }
            idFound = false;
        }

        for (int i = 0; i < found.length; i++) {
            if (found[i] != "found") {
                String[] node = found[i].split("\\ ");
                newMembershipLog += node[0] + " " + node[1] + "\n";
            }
        }
        createDirectory("store/" + hashedNode, "membership_log.txt", newMembershipLog);
    }

    public static int getResponsibleNode(String key) throws Exception {
        // Percorre o membership_log e retorna o id do node responsável por guardar o
        // key value
        // System.out.println("Value: " + key);
        Scanner scanner = new Scanner(new File("store/" + hashedNode + "/membership_log.txt"));

        String line = scanner.nextLine();
        String[] parts = line.split("\\ ");
        String auxHash = generateHashCode(parts[0]);
        String minHash = generateHashCode(parts[0]);
        int aux = Integer.valueOf(parts[0]);
        int min = Integer.valueOf(parts[0]);
        int nodeMembershipCount = Integer.valueOf(parts[1]);

        while ((nodeMembershipCount % 2 != 0) || (nodeMembershipCount != 0)) {
            // System.out.println(nodeMembershipCount);
            if (scanner.hasNextLine()) {
                String line2 = scanner.nextLine();
                String[] parts2 = line2.split("\\ ");
                nodeMembershipCount = Integer.valueOf(parts2[1]);
                if (nodeMembershipCount % 2 == 0) {
                    min = Integer.valueOf(parts2[0]);
                    aux = Integer.valueOf(parts2[0]);
                    minHash = generateHashCode(parts2[0]);
                    auxHash = generateHashCode(parts2[0]);
                    break;
                }
            } else {
                return -1;
            }
        }
        // System.out.println("Initial Node: " + aux + ";" + auxHash);

        int compareInitial = auxHash.compareTo(key);
        boolean isValid;
        if (compareInitial < 0) {
            isValid = false;
        } else {
            isValid = true;
        }

        while (scanner.hasNextLine()) {
            String line2 = scanner.nextLine();
            String[] parts2 = line2.split("\\ ");
            String hashedID = generateHashCode(parts2[0]);
            // int current = Integer.valueOf(parts2[0]);
            nodeMembershipCount = Integer.valueOf(parts2[1]);

            // System.out.println("membership: " + nodeMembershipCount);

            if ((nodeMembershipCount == 0) || (nodeMembershipCount % 2 == 0)) {
                int compare = key.compareTo(hashedID);
                if (compare < 0) {
                    // System.out.println("Compare Value; Node " + key + " < " + hashedID);
                    if (isValid) {
                        int compare2 = hashedID.compareTo(auxHash);
                        if (compare2 < 0) {
                            // System.out.println("Compare Node; Current" + hashedID + " < " + auxHash);
                            auxHash = hashedID;
                            aux = Integer.valueOf(parts2[0]);
                        }

                    } else {
                        auxHash = hashedID;
                        aux = Integer.valueOf(parts2[0]);
                        isValid = true;
                    }

                }
                int compare3 = hashedID.compareTo(minHash);
                if (compare3 < 0) {
                    // System.out.println(hashedID + " < " + minHash);
                    minHash = hashedID;
                    min = Integer.valueOf(parts2[0]);
                }
                // System.out.println("Node: " + current + ";" + hashedID + " Current: " + aux +
                // ";" + auxHash);
                // System.out.println();
            }
        }
        System.out.println("aux: " + aux + " min: " + min);
        int finalCompare = key.compareTo(auxHash);
        if (finalCompare < 0) {
            return aux;
        } else {
            return min;
        }
    }

    public static String generateHashCode(String string) throws NoSuchAlgorithmException {
        return toHexString(getSHA(string));
    }

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        // Static getInstance method is called with hashing SHA
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // digest() method called
        // to calculate message digest of an input
        // and return array of byte
        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash) {
        // Convert byte array into signum representation
        BigInteger number = new BigInteger(1, hash);

        // Convert message digest into hex value
        StringBuilder hexString = new StringBuilder(number.toString(16));

        // Pad with leading zeros
        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    public static void createDirectory(String directoryName, String fileName, String value) {
        File directory = new File(directoryName);
        if (!directory.exists()) {
            directory.mkdir();
            // If you require it to make the entire directory path including parents,
            // use directory.mkdirs(); here instead.
        }

        File file = new File(directoryName + "/" + fileName);
        try {
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(value);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static String getFileValue(String path) throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(path));
        String value = "";
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            value += line + "\n";
        }
        return value;
    }

    public static void overwriteLine(String path, String oldLine, String newLine) throws IOException {
        // Instantiating the Scanner class to read the file
        Scanner sc = new Scanner(new File(path));
        // instantiating the StringBuffer class
        StringBuffer buffer = new StringBuffer();
        // Reading lines of the file and appending them to StringBuffer
        while (sc.hasNextLine()) {
            buffer.append(sc.nextLine() + System.lineSeparator());
        }
        String fileContents = buffer.toString();
        // closing the Scanner object
        sc.close();
        // Replacing the old line with new line
        fileContents = fileContents.replaceAll(oldLine, newLine);
        // instantiating the FileWriter class
        FileWriter writer = new FileWriter(path);

        writer.append(fileContents);
        writer.flush();
        writer.close();
    }

    public static void appendLine(String path, String line) throws IOException {
        try {
            Files.write(Paths.get(path), line.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
        }
    }

    public static String[] getFileNames(String path) {
        File folder = new File(path);
        File[] listOfFiles = folder.listFiles();
        String fileNames = "";

        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                fileNames += listOfFiles[i].getName() + ":";
                // System.out.println("File " + listOfFiles[i].getName());
            }
        }
        String[] names = fileNames.split(":");

        return names;
    }
}
