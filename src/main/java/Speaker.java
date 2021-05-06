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
            speaker.setBotUsername(DataManager.getInstance().getBotUsername());
            speaker.setBotToken(DataManager.getInstance().getBotToken());
            speaker.setOwnerID(DataManager.getInstance().getOwnerID());
            speaker.setDestination(Long.parseLong(DataManager.getInstance().getOfficeID()));
            botsApi.registerBot(speaker);
            System.out.println("Successfully launch bot");

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
        setOfficeByUpdate(update);
        setOwnerByUpdate(update);
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
            DataManager.getInstance().setOfficeID(office.getChatID().toString());
        }
    }

    public void PersonalIntoOffice(Update update){
        if (update.getMessage().getChatId()>-1 &&
                DataManager.getInstance().checkUserID(update)){
            ForwardMessage result = new ForwardMessage();
            result.setChatId(destination.getChatID().toString());
            result.setFromChatId(update.getMessage().getChatId().toString());
            result.setMessageId(update.getMessage().getMessageId());
            try {
                execute(result);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
            DataManager.getInstance().addMessage(
                    update.getMessage().getText(),update.getMessage().getFrom().getId(),
                    update.getMessage().getMessageId());
        }
    }

    public void OfficeIntoPersonal(Update update) {
        if (0 == update.getMessage().getChatId().compareTo( destination.getChatID()) &&
        update.getMessage().isReply() &&
        update.getMessage().getReplyToMessage().getFrom().getUserName().equals(BotUsername)){
            SendMessage result = new SendMessage();
            result.setChatId(update.getMessage().getReplyToMessage().getForwardFrom().getId().toString());
            result.setText(update.getMessage().getText());
            int replyID = DataManager.getInstance().getMessageID(
                    update.getMessage().getReplyToMessage().getForwardFrom().getId(),
                    update.getMessage().getReplyToMessage().getText()
            );
            if (replyID > 0) result.setReplyToMessageId(replyID);
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

    public void setOwnerByUpdate (Update update){
        if (update.getMessage().getChatId()>-1 &&
                update.getMessage().getText().contains("/setowner") &&
        update.getMessage().getText().contains(BotToken)){
            setOwnerID(update.getMessage().getFrom().getId().toString());
            DataManager.getInstance().setOwnerID(update.getMessage().getFrom().getId().toString());
        }
    }
}
