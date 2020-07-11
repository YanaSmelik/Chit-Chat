import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class MessagePane extends JPanel implements MessageListener{

    private final ChatClient client;
    private final String user;

    private  DefaultListModel<String> listModel = new DefaultListModel<>();
    private JList<String> messageList = new JList<>(listModel);
    private JTextField inputField = new JTextField();


    public MessagePane(ChatClient client, String user){
        this.client = client;
        this.user = user;

        client.addMessageListener(this);

        setLayout(new BorderLayout());
        add(new JScrollPane(messageList), BorderLayout.CENTER);
        add(inputField, BorderLayout.SOUTH);

        inputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String text = inputField.getText();
                    client.message(user, text);
                    listModel.addElement("You: " + text);
                    inputField.setText("");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onMessage(String sender, String messageBody) {
        if(user.equalsIgnoreCase(sender)){
            String line = sender + ": " + messageBody;
            listModel.addElement(line);
        }

    }
}
