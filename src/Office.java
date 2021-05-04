import java.util.ArrayList;

public class Office {
    private String username;
    private String link;
    private final String prefix = "https://t.me/";
    private ArrayList<String> Staff = new ArrayList<String>();

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
// Привязываемся к конкретной группе
    public Office (String UsernameOrLink){
        if (!UsernameOrLink.contains(prefix)){
        this.username=UsernameOrLink;
        this.link=prefix+username;}
        else {
            this.link=UsernameOrLink;
            this.username = UsernameOrLink.substring(prefix.length());
        }
    }

}
