import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginWindow extends JFrame {

    private final ChatClient client;
    JTextField userNameField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JButton loginButton = new JButton("Login");

    public LoginWindow() throws IOException {
        super("Login");

        this.client = new ChatClient("localhost", 8818);
        client.connect();


        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(userNameField);
        panel.add(passwordField);
        panel.add(loginButton);

        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    doLogin();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        getContentPane().add(panel, BorderLayout.CENTER);
        pack();
        setVisible(true);
    }

    private void doLogin() throws IOException {
        String user = userNameField.getText();
        String password = passwordField.getText();


        if(client.login(user, password)){
            //bring up the userlist window
            ChatClient client = new ChatClient("localhost", 8818);

            UserListPane userListPane = new UserListPane(client);

            // create a window
            JFrame frame = new JFrame("User List");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(400, 600);

            // add userListPane as a main component of the frame.
            frame.getContentPane().add(userListPane, BorderLayout.CENTER);
            frame.setVisible(true);

            setVisible(false);
        }else{
            //show error message
            JOptionPane.showMessageDialog(this, "Invalid username or password.");
        }
    }

    public static void main(String[] args) throws IOException {
        LoginWindow loginWindow = new LoginWindow();
        loginWindow.setVisible(true);

    }
}
