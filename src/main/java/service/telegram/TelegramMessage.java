package service.telegram;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramMessage {
    private Long update_id;
    private Message message;
    private CallbackQuery callback_query;

    // Getters et Setters
    public Long getUpdate_id() { return update_id; }
    public void setUpdate_id(Long update_id) { this.update_id = update_id; }

    public Message getMessage() { return message; }
    public void setMessage(Message message) { this.message = message; }

    public CallbackQuery getCallback_query() { return callback_query; }
    public void setCallback_query(CallbackQuery callback_query) { this.callback_query = callback_query; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private Long message_id;
        private Chat chat;
        private String text;
        private From from;
        private Long date;
        private List<Entity> entities;

        public Long getMessage_id() { return message_id; }
        public void setMessage_id(Long message_id) { this.message_id = message_id; }

        public Chat getChat() { return chat; }
        public void setChat(Chat chat) { this.chat = chat; }

        public String getText() { return text; }
        public void setText(String text) { this.text = text; }

        public From getFrom() { return from; }
        public void setFrom(From from) { this.from = from; }

        public Long getDate() { return date; }
        public void setDate(Long date) { this.date = date; }

        public List<Entity> getEntities() { return entities; }
        public void setEntities(List<Entity> entities) { this.entities = entities; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Chat {
        private Long id;
        private String type;
        private String first_name;
        private String last_name;
        private String username;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getFirst_name() { return first_name; }
        public void setFirst_name(String first_name) { this.first_name = first_name; }

        public String getLast_name() { return last_name; }
        public void setLast_name(String last_name) { this.last_name = last_name; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class From {
        private Long id;
        private Boolean is_bot;
        private String first_name;
        private String last_name;
        private String username;
        private String language_code;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public Boolean getIs_bot() { return is_bot; }
        public void setIs_bot(Boolean is_bot) { this.is_bot = is_bot; }

        public String getFirst_name() { return first_name; }
        public void setFirst_name(String first_name) { this.first_name = first_name; }

        public String getLast_name() { return last_name; }
        public void setLast_name(String last_name) { this.last_name = last_name; }

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }

        public String getLanguage_code() { return language_code; }
        public void setLanguage_code(String language_code) { this.language_code = language_code; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CallbackQuery {
        private String id;
        private From from;
        private Message message;
        private String data;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public From getFrom() { return from; }
        public void setFrom(From from) { this.from = from; }

        public Message getMessage() { return message; }
        public void setMessage(Message message) { this.message = message; }

        public String getData() { return data; }
        public void setData(String data) { this.data = data; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Entity {
        private Integer offset;
        private Integer length;
        private String type;

        public Integer getOffset() { return offset; }
        public void setOffset(Integer offset) { this.offset = offset; }

        public Integer getLength() { return length; }
        public void setLength(Integer length) { this.length = length; }

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }
    }
}
