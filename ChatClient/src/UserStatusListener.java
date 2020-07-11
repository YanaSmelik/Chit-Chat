public interface UserStatusListener {

    //notify when user comes online
    public void online(String user);

    //notify when user goes offline
    public void offline(String user);
}

