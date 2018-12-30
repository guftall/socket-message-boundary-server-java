import com.sun.javaws.exceptions.InvalidArgumentException;
import com.sun.media.sound.InvalidDataException;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

public class Packet {

    private int id;
    private int packetLength = -1;
    private ByteBuffer buffer;

    private boolean completed;

    Packet() {

        this.buffer = ByteBuffer.allocate(2048);
    }

    public boolean completed() {

        return this.completed;
    }

    public int getPacketLength() {
        return this.packetLength;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public boolean headerReceived() {

        return this.buffer.position() >= 4;
    }

    public boolean readHeader(ByteBuffer newBuffer) throws InvalidDataException {

        int mPos = this.buffer.position();

        if (newBuffer.remaining() >= 4 - mPos) {

            newBuffer.get(this.buffer.array(), mPos, 4 - mPos);
            this.buffer.position(this.buffer.position() + 4 - mPos);

            int oldPosition = this.buffer.position();
            this.buffer.position(0);
            this.packetLength = this.buffer.getInt();
            this.buffer.position(oldPosition);

            if (packetLength > 2048 - 4) {
                Log.e("packet size is greater than 2044");
                throw new InvalidDataException("packet size is greater than 2044");
            }

            this.buffer.limit(packetLength + 4);
            Log.d("Packet(" + this + ") header completed with packetLength=" + this.packetLength);

            return true;
        } else {

            int read = newBuffer.remaining();
            newBuffer.get(this.buffer.array(), mPos, newBuffer.remaining());
            this.buffer.position(mPos + read);
            return false;
        }
    }

    public void appendBuffer(ByteBuffer newBuffer) throws InvalidDataException {

        if (this.buffer.position() >= 2048 || this.completed) {
            Log.e("packet size must be less than or equal to 2048");
            return;
        }

        if (!this.headerReceived() && !this.readHeader(newBuffer)) {

            return;
        }

        int readCount = 0;

        if (newBuffer.remaining() >= this.buffer.remaining()) {

            // if new buffer have more data than local buffer limit, only read needed data
            readCount = this.buffer.remaining();
        } else {

            readCount = newBuffer.remaining();
        }

        newBuffer.get(this.buffer.array(), this.buffer.position(), readCount);

        // set local buffer position to last index, because all of remaining buffer filled in upper line
        this.buffer.position(this.buffer.position() + readCount);

        if (this.buffer.remaining() == 0) {
            Log.d("Packet(" + this + ") completed");
            this.completed = true;

            Log.d(new String(this.buffer.array(), 4, this.packetLength));
        }
    }
}
