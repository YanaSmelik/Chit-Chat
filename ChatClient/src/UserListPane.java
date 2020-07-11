import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

public class UserListPane extends JPanel implements UserStatusListener{

    private final ChatClient client;
    private JList<String> userListUI;
    private DefaultListModel<String> userListModel;

    public UserListPane(ChatClient client){
        this.client = client;
        //presence listener
        this.client.addUserStatusListener(this);
        userListModel = new DefaultListModel<>();

        userListUI = new JList<>(userListModel);
        setLayout(new BorderLayout());
        add(new JScrollPane(userListUI), BorderLayout.CENTER);

        userListUI.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(e.getClickCount() > 1){
                    String user = userListUI.getSelectedValue();
                    MessagePane messagePane = new MessagePane(client, user);

                    JFrame frame = new JFrame("Message: " + user);
                    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    frame.setSize(500, 500);
                    frame.getContentPane().add(messagePane, BorderLayout.CENTER);
                    frame.setVisible(true);
                }
            }
        });
    }

    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient("localhost", 8818);

        UserListPane userListPane = new UserListPane(client);

        // create a window
        JFrame frame = new JFrame("User List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 600);

        // add userListPane as a main component of the frame.
        frame.getContentPane().add(userListPane, BorderLayout.CENTER);
        frame.setVisible(true);

        //after frame become visible, we can log in
        if(client.connect()){
            client.login("guest", "guest");
        }
    }

    @Override
    public void online(String user) {
        userListModel.addElement(user);
    }

    @Override
    public void offline(String user) {
        userListModel.removeElement(user);
    }
}
