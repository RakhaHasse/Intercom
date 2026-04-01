import java.util.ArrayList;

public class Office {
    private final String prefix = "https://t.me/";
    private ArrayList<String> Staff = new ArrayList<String>();
    private Long ChatID;

   // Привязываемся к конкретной группе
    public Office (Long ChatID){
        this.setChatID(ChatID);

    }

    public Long getChatID() {
        return ChatID;
    }

    public void setChatID(Long chatID) {
        ChatID = chatID;
    }
}
