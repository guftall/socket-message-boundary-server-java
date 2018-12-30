import java.nio.ByteBuffer;

public interface ConnectionListeners {

    boolean onBufferReceived(ByteBuffer buffer);
}
