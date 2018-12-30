public class Main {

    public static void main(String[] args) {

        TCPServer server = TCPServer.getServer();

        server.startListening();
    }
}
