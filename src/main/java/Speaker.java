import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Speaker extends TelegramLongPollingBot {

    public static void main(String[] args) {
        try {
            // Create the TelegramBotsApi object to register your bots
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            // Register your newly created Bot
            Speaker speaker = new Speaker();
            speaker.setBotToken("1738130684:AAEpSZLrQYOZ4zVpQU69LBPgDVIN18LRmow");
            speaker.setBotUsername("RakhaHasseTestBot");
            speaker.setOwnerID("401800130");
            botsApi.registerBot(speaker);

        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private String BotToken;
    private String BotUsername;

    public String getOwnerID() {
        return OwnerID;
    }

    public void setOwnerID(String ownerID) {
        OwnerID = ownerID;
    }

    private String OwnerID;

    public void setBotUsername(String botUsername) {
        BotUsername = botUsername;
    }

    @Override
    public String getBotUsername() {
        return BotUsername;
    }

    public void setBotToken(String botToken) {
        BotToken = botToken;
    }

    @Override
    public String getBotToken() {
        return BotToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("Update got");
        //Echo(update);
        setOfficeByUpdate(update);

        OfficeIntoPersonal(update);
        PersonalIntoOffice(update);

    }

   // группа в телеграм, куда должны сыпать обращения от пользователей канала
    private Office destination;

    public Office getDestination() {
        return destination;
    }

    public void setDestination(Office destination) {
        this.destination = destination;
    }

    public void setDestination (Long chatID){
      this.destination = new Office(chatID);
    }

    public void setOfficeByUpdate (Update update){
        Message message = update.getMessage();
        if (message.getChatId()<0 &&
        message.getFrom().getId() == Long.parseLong(OwnerID) &&
                message.getText().contains("/office")){
            Office office = new Office(message.getChatId());
            setDestination(office);
            System.out.println("Office successful set");
        }
    }

    public void PersonalIntoOffice(Update update){
        if (update.getMessage().getChatId()>-1){
            ForwardMessage result = new ForwardMessage();
            result.setChatId(destination.getChatID().toString());
            result.setFromChatId(update.getMessage().getChatId().toString());
            result.setMessageId(update.getMessage().getMessageId());
            try {
                execute(result);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void OfficeIntoPersonal(Update update) {
        if (0 == update.getMessage().getChatId().compareTo( destination.getChatID()) &&
        update.getMessage().isReply() &&
        update.getMessage().getReplyToMessage().getFrom().getUserName().equals(BotUsername)){
            SendMessage result = new SendMessage();
            result.setChatId(update.getMessage().getReplyToMessage().getForwardFrom().getId().toString());
            result.setText( "To this your ask:\n"+
                    update.getMessage().getReplyToMessage().getText()+
                    "...\n\nWe answer this:\n"+
                    update.getMessage().getText());
            try {
                execute(result);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }

    public void Echo (Update update){
        SendMessage result = new SendMessage();
        result.setChatId(update.getMessage().getChatId().toString());
        result.setText(update.getMessage().getText());
        try {
            execute(result);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


}
