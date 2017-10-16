package common;

public class Logger {

    public static Logger instance;

    public static Logger getInstance() {

        if (instance == null) {
            instance = new Logger();
        }

        return instance;
    }

    public void notify(Throwable t) {
        t.printStackTrace();
    }
}
