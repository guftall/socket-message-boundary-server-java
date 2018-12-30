import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {

    private List<Connection> connections = new ArrayList<>();

    private Thread readThread;
    private boolean reading;

    ConnectionManager() {

        startReadThread();
    }

    private void startReadThread() {

        if (readThread != null) {
            Log.w("read thread already started");
            return;
        }

        reading = true;

        this.readThread = new Thread(new Runnable() {
            @Override
            public void run() {

                ByteBuffer tmpByteBuffer = ByteBuffer.allocate(1024);
                InputStream inputStream;
                Connection connToRemove = null;
                while (reading) {

                    try {
                        synchronized (connections) {
                            for (Connection conn : connections) {

                                if (!conn.connected()) {
                                    connToRemove = conn;
                                    continue;
                                }
                                inputStream = conn.getInputStream();
                                int read;
                                while (inputStream.available() > 0) {

                                    tmpByteBuffer.clear();
                                    read = inputStream.read(tmpByteBuffer.array(), 0, tmpByteBuffer.capacity());

                                    tmpByteBuffer.limit(read);

                                    if (conn.onBufferReceived(tmpByteBuffer) == false) {
                                        connToRemove = conn;
                                        continue;
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (connToRemove != null) {
                        synchronized (connections) {
                            connections.remove(connToRemove);
                        }
                        connToRemove = null;
                    }
                }
            }
        });

        this.readThread.setName("readThread");
        this.readThread.start();
    }

    public void handleIncomingConnection(Socket clientSocket) {

        Connection connection = new Connection(clientSocket);
        synchronized (connections) {

            connections.add(connection);
        }
    }
}
