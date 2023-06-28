package bgu.spl.net;


import java.util.ArrayList;
import java.util.Vector;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class BGUNet {

    private ConcurrentHashMap<String, Vector<String>> Followers; //username -> vector of usernames followers
    private ConcurrentHashMap<String, Vector<String>> Following; //username -> vector of all followings
    private ConcurrentHashMap<String, Vector<String>> posts; //holds the number of posts of every connectionID
    private ConcurrentHashMap<String, Integer> birthdays; //holds the age of every user
    private Vector<String> OnLineUsers;
    private ConcurrentHashMap<String, Vector<String>> notifications; //username - notifiactions
    private final ArrayList<String> filtered;
    private ConcurrentHashMap<String, Vector<String>> blocked; //user - blocked

    public BGUNet(){
        Followers = new ConcurrentHashMap<>();
        posts = new ConcurrentHashMap<>();
        birthdays = new ConcurrentHashMap<>();
        Following = new ConcurrentHashMap<>();
        OnLineUsers = new Vector<>();
        filtered = new ArrayList<>();
        notifications = new ConcurrentHashMap<>();
        blocked = new ConcurrentHashMap<>();
        filtered.add("dan");
        filtered.add("miki");
        filtered.add("threads");
        filtered.add("fuck");

    }

    public ConcurrentHashMap<String, Vector<String>> getFollowers() {
        return Followers;
    }

    public boolean ReplaceFiltered(String word){
        return filtered.contains(word.toLowerCase());
    }

    public void AddToFollowers(String username, String follower){ //if miki said "FOLLOW 0 dan" than dan is username and miki is a new follower
        /*Vector<String> followers = Followers.get(username);
        followers.add(follower);
        Vector<String> following = Following.get(follower);
        following.add(username);*/
        getFollowers().get(follower).add(username);
        getFollowing().get(username).add(follower);
    }



    public void Block(String user, String blocked_user){
        Vector<String> blocked_by_the_user = blocked.get(user);
        blocked_by_the_user.add(blocked_user);
        Vector<String> blocked_by_the_blocked = blocked.get(blocked_user);
        blocked_by_the_blocked.add(user);
        Vector<String> followers = getFollowers().get(user);
        followers.remove(blocked_user);
        Vector<String> followers2 = getFollowing().get(user);
        followers2.remove(blocked_user);
        Vector<String> followers3 = getFollowers().get(blocked_user);
        followers3.remove(user);
        Vector<String> followers4 = getFollowing().get(blocked_user);
        followers4.remove(user);
    }

    public ConcurrentHashMap<String, Vector<String>> getBlocked() {
        return blocked;
    }

    public ConcurrentHashMap<String, Vector<String>> getNotifications() {
        return notifications;
    }

    public ArrayList<String> getFiltered(){
        return filtered;
    }

    public ConcurrentHashMap<String, Vector<String>> getFollowing() {
        return Following;
    }

    public void UnFollowers(String username, String Unfollow){
        /*Vector<String> followers = Followers.get(username);
        followers.remove(Unfollow);
        Vector<String> following = Following.get(Unfollow);
        following.remove(username);*/
        getFollowers().get(Unfollow).remove(username);
        getFollowing().get(username).remove(Unfollow);
    }
//
//    public void AddToFollowing(String username){
//        int num = this.Following.get(username);
//        Following.put(username, num+1);
//    }

    public void AddToPosts(String username, String post){
        Vector<String> postss = this.posts.get(username);
        postss.add(post);
    }



    public Vector<String> getPosts(String username) {
        if(posts.containsKey(username)){
            return posts.get(username);
        }
        return new Vector<>();
    }



    public int getBirthdays(String name) {
        if(birthdays.containsKey(name)){
            return birthdays.get(name);
        }
        else return -1;
    }



    public Vector<String> getOnLineUsers() {
        return OnLineUsers;
    }

    public void AddOnLineUser(String username){
        OnLineUsers.add(username);
    }

    public void OfflineUser(Integer conID){
        OnLineUsers.remove(conID);
    }

    public void CreateNewUser(String username, Integer Birthday){
        Followers.put(username, new Vector<String>());
        Following.put(username, new Vector<String>());
        posts.put(username,new Vector<String>());
        birthdays.put(username, Birthday);
        notifications.put(username, new Vector<>());
        blocked.put(username, new Vector<>());
    }

    public void addNotifications(String user_to_send, String s) {
        synchronized (notifications){
            notifications.computeIfAbsent(user_to_send,(k)->new Vector<String>());
            notifications.get(user_to_send).add(s);
        }
    }
    public Vector<String> getNotifi(String username){
        synchronized (notifications) {
            if (notifications.containsKey(username)) {
                return notifications.get(username);
            } else return null;
        }
    }
    public void gotAllNotifi(String username){
        synchronized (notifications){
            notifications.computeIfPresent(username,(k,v)-> new Vector<String>());
        }
    }
    private static class BGUNetWrapper{
        private static final BGUNet instance= new BGUNet();
        public static BGUNet getInstance(){
            return instance;
        }
    }
    public static BGUNet getInstance(){
        return BGUNetWrapper.instance;
    }




}
