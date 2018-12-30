import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {

    private static Logger logger =
            Logger.getLogger("SocketMessageBoundary");


    private Log() {}

    public static void d(String message) {

        logger.log(Level.SEVERE, message);
    }

    public static void w(String message) {

        logger.log(Level.WARNING, message);
    }

    public static void e(String message) {

        logger.log(Level.SEVERE, message);
    }
}
