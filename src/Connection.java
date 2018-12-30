import com.sun.media.sound.InvalidDataException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class Connection implements ConnectionListeners {

    private Socket socket;

    private InputStream inputStream;
    private OutputStream outputStream;


    private List<Packet> packets = new LinkedList<>();
    private Packet lastPacket;

    public Connection(Socket socket) {

        this.socket = socket;
        try {

            this.inputStream = socket.getInputStream();
            this.outputStream = socket.getOutputStream();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onNewPacketReceived(Packet packet) {

        if (!this.lastPacket.completed()) {
            Log.e("onNewPacketReceived called before last packet finished");
            return;
        }

        this.lastPacket = packet;

    }


    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public boolean onBufferReceived(ByteBuffer buffer) {

        /*
        *           <Header> < Data >
        *           <4 byte> <2044 byte>
        *
        * */
        Log.d("Connection(" + this + ") received " + buffer.limit() + " bytes of data");

        while (buffer.remaining() > 0) {
            try {
                if (lastPacket != null && !lastPacket.completed()) {

                    lastPacket.appendBuffer(buffer);
                } else {

                    Packet newPacket = new Packet();
                    Log.d("creating new Packet(" + newPacket + ") for Connection(" + this + ")");
                    this.lastPacket = newPacket;
                    this.lastPacket.appendBuffer(buffer);
                }
            } catch (InvalidDataException e) {

                Log.e("closing socket");
                this.close();
                return false;
            }

            if (lastPacket.completed()) {

                synchronized (packets) {
                    this.packets.add(this.lastPacket);
                }
                lastPacket = null;
            }
        }

        return true;
    }

    private void close() {

        if (this.socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean connected() {
        return !this.socket.isClosed();
    }
}



















