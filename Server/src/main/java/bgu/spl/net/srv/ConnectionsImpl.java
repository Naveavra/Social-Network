package bgu.spl.net.srv;

import bgu.spl.net.BGUNet;

import java.io.IOException;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ConnectionsImpl<T> implements Connections<T> {
    int posts;
    public ConcurrentHashMap<String,String> users; // id, password
    private ConcurrentHashMap<Integer , ConnectionHandler<T>> UserCH; //ConnectionID(unique), ConnectionHandler
    private ConcurrentHashMap<String, Boolean> OnLineUsers; // name - connected(?)
    private ConcurrentHashMap<Integer,String> id_user;
    private ConcurrentHashMap<String,Integer> user_id;
    //private ConcurrentHashMap<String,ConcurrentLinkedQueue<String>> following; // username , followers
    private ConnectionsImpl instance;
    //private ConcurrentHashMap<Integer, ConcurrentLinkedQueue<Integer>> friends; // ConnectionID(unique), friends Id's
    private static int MessageID = 0;
    public ConnectionsImpl(){
        UserCH = new ConcurrentHashMap<>();
        OnLineUsers = new ConcurrentHashMap<>();
        users = new ConcurrentHashMap<>();
        id_user = new ConcurrentHashMap<>();
        user_id = new ConcurrentHashMap<>();
    }

    @Override
    // sends msg to the client that is represented by the connectionId
    public boolean send(int connectionId, T msg) {
        if(UserCH.containsKey(connectionId)){
            UserCH.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    /*public void follow(String follower, String toFollow){
        synchronized (following){
            following.get(toFollow).add(follower); //if miki said "FOLLOW 0 dan" than dan is toFollow and miki is a new follower
        }
    }
    public void unFollow(String unFollower, String toUnFollow){
        synchronized (following){
            following.get(toUnFollow).remove(unFollower);
        }
    }*/

    public ConcurrentHashMap<String, Integer> getUser_id() {
        return user_id;
    }

    public ConcurrentHashMap<Integer, String> getId_user() {
        return id_user;
    }

    public void addNewUser(String name, String password, int conId){
        users.put(name,password);
        id_user.put(conId,name);
        //following.put(name,new ConcurrentLinkedQueue<>());
        OnLineUsers.put(name,false);
        BGUNet bguNet = BGUNet.getInstance();
        bguNet.getFollowers().put(name,new Vector<>());
        bguNet.getFollowing().put(name,new Vector<>());
    }
    public ConcurrentHashMap<String,String> getUsers(){return users;}
    public ConcurrentHashMap<Integer , ConnectionHandler<T>> getUserCH(){
        return UserCH;
    }
    // change the value of this user to false meaning he isnt online
    public void logoff(String username){
        OnLineUsers.put(username, false);
    }

    @Override
    public void broadcast(T msg) {
        synchronized (UserCH) {
            for (ConnectionHandler<T> ch : UserCH.values()) {
                ch.send(msg);
            }
        }
    }

    @Override
    public void disconnect(int connectionId) {
        if(OnLineUsers.containsKey(id_user.get(connectionId))){
            String name = id_user.get(connectionId);
            OnLineUsers.computeIfPresent(name,(k,v)->false);
        }
    }
    private static class ConnectionWrapper{
        private static final ConnectionsImpl instance= new ConnectionsImpl();
        public static ConnectionsImpl getInstance(){
            return instance;
        }
    }
    public static ConnectionsImpl getInstance(){return ConnectionWrapper.getInstance();}


    public boolean isOnline(String name){
        return OnLineUsers.get(name);
    }

    public ConcurrentHashMap<String, Boolean> getOnLineUsers() {
        return OnLineUsers;
    }
}


