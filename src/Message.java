import java.io.Serializable;

public class Message implements Serializable {

    private Object picture;
    private String message;
    private String command;
    private String name;

    public Message(String name, Object picture, String message, String command) {
        this.name = name;
        this.picture = picture;
        this.message = message;
        this.command = command;
    }

    public Object getPicture() {
        return picture;
    }

    public String getMessage() {
        return message;
    }

    public String getCommand() {
        return command;
    }

    public String getName() {
        return name;
    }
}
