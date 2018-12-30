import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPServer {

    private static final int SERVER_PORT = 5310;
    private static final int SERVER_BACKLOG = 5;

    private ServerSocket serverSocket;
    private Thread serverThread;

    private ConnectionManager connectionManager = new ConnectionManager();

    private static boolean serverThreadStarted = false;
    private static boolean continueListening = false;

    private static TCPServer server;

    static TCPServer getServer() {
        if (server == null) {
            try {
                InetAddress address = InetAddress.getByName("127.0.0.1");
                server = new TCPServer(address);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (server == null) {
                throw new RuntimeException("initializing server failed");
            }
        }

        return server;
    }

    private TCPServer(@NotNull InetAddress serverAddress) throws IOException {

        Log.d("creating server socket[" + serverAddress.getHostName() + " , " + SERVER_PORT + "]");
        serverSocket = new ServerSocket(SERVER_PORT, SERVER_BACKLOG, serverAddress);
    }

    public void startListening() {

        if (serverSocket == null) {
            Log.d("server socket is null");
            return;
        }

        this.continueListening = true;
        this.startServerThread();
    }

    private void startServerThread() {
        Log.d("attempt to start server thread");

        if (serverThreadStarted || serverThread != null) {
            Log.d("server thread already started");
            return;
        }
        serverThreadStarted = true;

        serverThread = new Thread(new Runnable() {
            @Override
            public void run() {

                while (continueListening && !serverThread.isInterrupted()) {

                    try {
                        Socket clientSocket = serverSocket.accept();
                        Log.d(
                                "new client connected ["
                                        + clientSocket.getInetAddress().getCanonicalHostName()
                                        + " , "
                                        + clientSocket.getPort() + "]");

                        connectionManager.handleIncomingConnection(clientSocket);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Log.w("exiting server thread");
            }
        });
        serverThread.setName("ServerThread");
        Log.d("starting server thread");
        serverThread.start();
    }
}




























