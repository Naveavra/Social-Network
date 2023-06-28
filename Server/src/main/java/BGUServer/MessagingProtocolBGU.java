package BGUServer;

import bgu.spl.net.BGUNet;
import bgu.spl.net.api.BidiMessagingProtocol;
import bgu.spl.net.srv.Connections;
import bgu.spl.net.srv.ConnectionsImpl;

import java.util.ArrayList;
import java.util.Date;
import bgu.spl.net.api.MessagingProtocol;

import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class MessagingProtocolBGU implements BidiMessagingProtocol<String> {
    private ConnectionsImpl<String> connections;
    private ConcurrentHashMap<String, String> subscribers;
    private String username;
    private String opcode;
    private BGUNet BGunet;
    private boolean register;
    private boolean logged;
    private boolean terminate;
    private int connectionId;
    public boolean testValue = false;

    public MessagingProtocolBGU(){
        subscribers = new ConcurrentHashMap<>();
        terminate = false;
        register = false;
        BGunet = BGUNet.getInstance();
        connections = ConnectionsImpl.getInstance();
    }
    @Override
    public void process(String msg) {
        //String action;
        int ind = msg.indexOf(' ');
        if (ind != -1) {
            opcode = msg.substring(0, ind);
            switch (opcode) {
                case "REGISTER":
                    msg = msg.substring(ind + 1);
                    if (Register_Validation(msg).equals("ACK 1")) { //meaning all good with the registraion
                        register = true;
                        connections.getUserCH().get(connectionId).send("ACK 1");
                    } else {
                        connections.getUserCH().get(connectionId).send("Error 1");
                    }
                    opcode ="";
                    break;
                case "LOGIN":
                    msg = msg.substring(ind + 1);
                    if (Login_Validation(msg).equals("ACK 2")) {
                        logged = true;
                        connections.getOnLineUsers().computeIfPresent(username, (k,v)->true);
                        connections.getUserCH().get(connectionId).send("ACK 2");
                        Vector<String> notifi = BGunet.getNotifi(username);
                        if(notifi.size()>0){
                            synchronized (notifi){
                                for(String ms: notifi){
                                    connections.getUserCH().get(connectionId).send(ms);
                                }
                            }
                            BGunet.gotAllNotifi(username);
                        }
                    } else {
                        connections.getUserCH().get(connectionId).send("Error 2");
                    }
                    opcode ="";
                    break;
                case "LOGOUT":
                    if (LogOut_Validation().equals("ACK 3")) {
                        connections.disconnect(connectionId);
                        logged = false;

                    } else {
                        connections.getUserCH().get(connectionId).send("Error 3");
                    }
                    opcode ="";
                    break;
                case "FOLLOW":
                    msg = msg.substring(ind+1, msg.length()-1);
                    String validOutput = Follow_Validation(msg);
                    if (msg.startsWith("0")) {
                        //msg = msg.substring(1); //TODO we dont need that
                        if (!(validOutput.equals("Error"))) {
                            connections.getUserCH().get(connectionId).send(validOutput);
                        } else {
                            connections.getUserCH().get(connectionId).send("Error 4");
                        }
                    } else if (msg.startsWith("1")) {
                        //msg = msg.substring(ind + 1);

                        if (!(validOutput.equals("Error"))) {
                            connections.getUserCH().get(connectionId).send(validOutput);
                        } else {
                            connections.getUserCH().get(connectionId).send("Error 4");

                        }
                    }
                    opcode ="";
                    break;
                case "POST":
                    msg = msg.substring(ind + 1);
                    if (!(connections.isOnline(username))) {
                        connections.getUserCH().get(connectionId).send("Error 5");
                    } else {
                        Posts_Operator(msg);
                    }
                    opcode ="";
                    break;
                case "PM":
                    msg = msg.substring(ind + 1);
                    if (!(connections.isOnline(username))) {
                        connections.getUserCH().get(connectionId).send("Error 6");
                    } else {
                        Handle_PM(msg);
                    }
                    opcode ="";
                    break;
                case "LOGSTAT":
                    msg = msg.substring(ind + 1);
                    if (!(connections.isOnline(username))) {
                        connections.getUserCH().get(connectionId).send("Error 7");
                    } else {
                        Handle_Logstat(msg);
                    }
                    opcode ="";
                    break;
                case "STAT":
                    msg = msg.substring(ind + 1);
                    if (!(connections.isOnline(username))) {
                        connections.getUserCH().get(connectionId).send("Error 8");
                    } else {
                        Handle_Stat(msg);
                    }
                    opcode ="";
                    break;
                case "BLOCK":
                    msg = msg.substring(ind + 1);
                    Handle_Block(msg);
                    opcode ="";
                    break;
            }
            }
        }

    private void Handle_Block(String usertoblock) {
        Vector<String> blocked = BGunet.getBlocked().get(username);
        if (!(connections.getUser_id().containsKey(usertoblock))) {
            connections.getUserCH().get(connectionId).send("Error 12");
        } else {
            if (blocked.contains(usertoblock)) {
                connections.getUserCH().get(connectionId).send("Error 12");
            } else {
                BGunet.Block(username, usertoblock);
                connections.getUserCH().get(connectionId).send("ACK 12");
            }
        }
    }
    private void Handle_Stat(String msg) {
        Vector<String> users = new Vector<>();
        String name = "";
      //  int ind = msg.indexOf(" ");
        for (int i=0; i<msg.length(); i++){
            if (msg.charAt(i) != '|'){
                if(i!= msg.length()-1) {
                    name += msg.charAt(i);
                }
            }
            else{
                users.add(name);
                name = "";
            }
        }
        users.add(name);
        String info="";
        Vector<String> blocked = BGunet.getBlocked().get(username);
            for (String userinf: users){
                if (!(blocked.contains(userinf))){
                    String fixed_age = String.valueOf(BGunet.getBirthdays(userinf));
                    String numofposts = String.valueOf(BGunet.getPosts(userinf).size());
                    String followers = String.valueOf(BGunet.getFollowers().get(userinf).size());
                    String following = String.valueOf(BGunet.getFollowing().get(userinf).size());
                    info += "ACK 8 " + userinf + " AGE: " + fixed_age + ",NUMBER OF POSTS: " + numofposts + ", FOLLOWERS: " + followers + "FOLLOWING: " + following + "\n";
                }
                else {
                    connections.getUserCH().get(connectionId).send("Error 8");
                    return;

                }
            }
        connections.getUserCH().get(connectionId).send(info);
        }





    @Override
    public boolean shouldTerminate() {
        return terminate;
    }

    public void Handle_PM(String msg){
        int index = msg.indexOf(" ");
        String user_to_send = msg.substring(0, index);
        int conID = connections.getUser_id().get(user_to_send);
        String[] content = msg.substring(index+1).split(" ");
        for (int i=0; i<content.length; i++){
            if (BGunet.getFiltered().contains(content[i])){
                content[i] = "<filtered>";
            }
        }
        msg = String.join(" ", content);
        int coniD = connections.getUser_id().get(username);
        if (!(BGunet.getBlocked().get(username).contains(user_to_send)) || BGunet.getFollowing().get(username).contains(user_to_send)){
            connections.getUserCH().get(coniD).send("ACK 6");
            if(connections.isOnline(user_to_send)) {
                connections.getUserCH().get(conID).send("NOTIFICATION PM " + username + " " + msg);
            }else{
                BGunet.addNotifications(user_to_send,"NOTIFICATION PM " + username + " " + msg);
            }
        }
        else{
            connections.getUserCH().get(coniD).send("Error 6");
        }
    }
    public void Handle_Logstat(String msg){
        String info="";
        Vector<String> blocked = BGunet.getBlocked().get(username);
        synchronized (connections.getOnLineUsers()){
            for (String userinf: connections.getOnLineUsers().keySet()){
                if (connections.isOnline(userinf)){
                    String fixed_age = String.valueOf(BGunet.getBirthdays(userinf));
                    String numofposts = String.valueOf(BGunet.getPosts(userinf).size());
                    String followers = String.valueOf(BGunet.getFollowers().get(userinf).size());
                    String following = String.valueOf(BGunet.getFollowing().get(userinf).size());
                    info += "ACK 8 " + userinf + " AGE: " + fixed_age + ", NUMBER OF POSTS: " + numofposts + ", FOLLOWERS: " + followers + ", FOLLOWING: " + following + "\n";
            }}
                }
        connections.getUserCH().get(connectionId).send(info);
    }


    public void Posts_Operator(String msg){
        StringBuilder ans = new StringBuilder();
        ans.append("NOTIFICATION Public ").append(username).append(" ");
        Vector<String> followers = BGunet.getFollowers().get(username);
        String[] content = msg.split(" ");
        Vector<String> fixed_content = new Vector<>();
        for (int i=1; i<content.length-1; i++){
            if (!Objects.equals(content[i], "")){
                fixed_content.add(content[i]);
            }
        }
        ArrayList<String> tags = new ArrayList<>();
        for (int i=0; i<fixed_content.size(); i++ ){
            if (fixed_content.elementAt(i).startsWith("@")){
                String user1 = fixed_content.elementAt(i).substring(1);
                if (!(BGunet.getBlocked().get(username).contains(user1))) {
                    tags.add(fixed_content.elementAt(i).substring(1));
                }
            }
            else if (BGunet.ReplaceFiltered(fixed_content.elementAt(i))){
                    fixed_content.set(i, "<filtered>");
                }
            }

        for (int i=0; i<followers.size(); i++){
            if (!(tags.contains(followers.elementAt(i)))){
                tags.add(followers.elementAt(i));
            }
        }
        ans.append(fixed_content.elementAt(0));
        for (int i=1; i<fixed_content.size(); i++){
            if (i != fixed_content.size()-1) {
                ans.append(" ").append(fixed_content.elementAt(i));
            }
            else {
                ans.append(" ");
                ans.append(fixed_content.elementAt(i));
            }
        }
        fixed_content.clear();
        for (String user:tags){
            if (!connections.isOnline(user)){
                BGunet.addNotifications(user, ans.toString());
            }
            else {
                int conid = connections.getUser_id().get(user);
                connections.getUserCH().get(conid).send(ans.toString());
            }

        }
        connections.getUserCH().get(connectionId).send("ACK 5");


    }

    public String Login_Validation(String msg){
        String[] body = msg.split(" ");
        String username = body[0];
        String password= body[1].substring(0, body[1].length()-2);
        short captcha = Short.parseShort(String.valueOf(body[1].charAt(body[1].length() -1)));
        if (!Objects.equals(connections.getUsers().get(username), password) || captcha != 1 || connections.isOnline(username)){ //meaning the user doesn't exist
            return "Login Error";
        }
        else {
            BGunet.AddOnLineUser(username);
            this.username = username;
            return "ACK 2";
        }
    }

    public String Follow_Validation(String msg){
        if (msg.startsWith("0")){
            int ind = msg.indexOf("0");
            String usertofollow = msg.substring(ind+1);
            if (!(connections.isOnline(username))){
                return "Error";
            }
            if (BGunet.getFollowing().get(username).contains(usertofollow)){
                return "Error";
            }
            if (BGunet.getBlocked().get(username).contains(usertofollow)){
                return "Error";
            }
            BGunet.AddToFollowers(username,usertofollow);
            return "ACK 4 0 " + usertofollow;
        }
        else {
                int ind = msg.indexOf("1");
                String usertoUnfollow = msg.substring(ind+1);
                if (!(connections.isOnline(username))){
                    return "Error";
                }
                if (!BGunet.getFollowing().get(username).contains(usertoUnfollow)){
                    return "Error";
                }
                BGunet.UnFollowers(username, usertoUnfollow);
                return "ACK 4 1 " + usertoUnfollow;
            }
        }





    public String Register_Validation(String msg){
        String[] body = msg.split(" ");
        String username = body[0];
        String password = body[1];
        String birth_day = body[2];
        if(connections.getUsers().containsKey(username)){
           return "Registration Error";
        }
        else {
            this.username = username;
            connections.addNewUser(username,password,connectionId);
            int age = getage(birth_day);
            BGunet.CreateNewUser(username, age);
            connections.getUser_id().put(username, connectionId);
            return "ACK 1";
        }
    }

    private String LogOut_Validation() {
        if (username==null || !connections.isOnline(username)){
            return "LogOut Error";
        } else {
            connections.getUserCH().get(connectionId).send("ACK 3");
            BGunet.OfflineUser(connectionId);
            connections.disconnect(connectionId);
            return "ACK 3";
        }
    }

    private Integer getage(String date){
        String birthyear  = "";
        int counter = 0;
        for (int i=0; i<date.length()-1; i++) {
            if (date.charAt(i) == '-') {
                counter += 1;
            }
            if (counter == 2 && date.charAt(i) != '-') {
                birthyear += date.charAt(i);

            }
        }
        return 2022-Integer.parseInt(birthyear);
    }


    @Override
    public void start(int connectionId, Connections connections){
        this.connections = (ConnectionsImpl)connections;
        this.connectionId = connectionId;
    }

    //TESTER FUNC TO CHECK STRING EQUALITY
    public boolean checkEq(String a, String b){
        int j = a.length();
        int k = b.length();
        if(a.length()!= b.length()){
            return false;
        }
        else{
            for(int i = 0; i< a.length(); i++){
                if(a.charAt(i)!= b.charAt(i)){
                    return false;
                }
            }
            return true;
        }
    }
}
