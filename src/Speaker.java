import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Speaker extends TelegramLongPollingBot {

    public static void main(String[] args) {
        try {
            // Create the TelegramBotsApi object to register your bots
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);

            // Register your newly created Bot
            botsApi.registerBot(new Speaker());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
    private String BotToken;
    private String BotUsername;

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
        PersonalIntoOffice(update);
        OfficeIntoPersonal(update);
    }

   // группа в телеграм, куда должны сыпать обращения от пользователей канала
    private Office destination;


    public Office getDestination() {
        return destination;
    }

    public void setDestination(Office destination) {
        this.destination = destination;
    }

    public void setDestination (String UsernameOrLink){
      this.destination = new Office(UsernameOrLink);
    }

    public void PersonalIntoOffice(Update update){
        if (update.getMessage().getChatId()>-1){
            ForwardMessage result = new ForwardMessage();
            result.setChatId(destination.getUsername());
            result.setFromChatId(update.getMessage().getChat().getUserName());
            result.setMessageId(update.getMessage().getMessageId());
            try {
                execute(result);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public void OfficeIntoPersonal(Update update) {
        if (update.getMessage().getChat().getUserName().equals(destination.getUsername()) &&
        update.getMessage().isReply() &&
        update.getMessage().getReplyToMessage().getFrom().getUserName().equals(BotUsername)){
            SendMessage result = new SendMessage();
            result.setChatId(update.getMessage().getReplyToMessage().getForwardFrom().getUserName());
            result.setText( "To this your ask:\n"+
                    update.getMessage().getReplyToMessage().getText().substring(0,50)+
                    "...\n\nWe answer this:\n"+
                    update.getMessage().getText());
            try {
                execute(result);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }

        }
    }

}
