package BGUServer;

import java.util.HashMap;
import java.util.Map;

public class Message {
    private String opcode;
    private Map<String, String> headers;
    private String content;

    public String getOpcode() {
        return opcode;
    }

    public void setOpcode(String opcode){
        this.opcode = opcode;
    }

    public String getContent(){
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Map<String, String> getHeaders(){
        return headers;
    }

    public Message (String msg){
        headers = new HashMap<>();
        String[] parts = msg.split("\n", -1); //TODO shouldnt be " "?
        if(parts.length > 0 ){
            opcode = parts[0];
        }

        int index = 1;
        while (index < parts.length && !parts[index].equals("")){
            int delimeter = parts[index].indexOf(':');
            String opcode = parts[index].substring(0, delimeter);
            String value = parts[index].substring(delimeter+1);
            headers.put(opcode, value);
            index++;
        }
        index++; //TODO DISCUSS

    }
}
