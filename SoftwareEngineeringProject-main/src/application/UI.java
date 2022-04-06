package application;

import javafx.collections.FXCollections;
import javafx.application.Application;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class UI extends Application {
    @Override
    public void start(Stage stage) {
        Socket socket;
        try {
            socket = new Socket("localhost", 1155);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Client client;
        Scanner scanner = new Scanner(System.in);
        try {
            client = new Client(socket);
            System.out.println("Desired username (1-16 characters): ");
            client.setUsername(scanner.nextLine());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error initializing client. Goodbye.");
            return;
        }


        //Instantiate window for program as an HBox
        HBox window = new HBox();
        window.setMinSize(200, 150);
        ObservableList UILayout = window.getChildren();

        //Instantiate left portion of window as VBox
        VBox contacts = new VBox();
        ObservableList contactLayout = contacts.getChildren();
        contacts.setPadding(new Insets(10));
        contacts.setAlignment(Pos.CENTER);

        Text contactTitle = new Text("Contacts");
        contactTitle.setFont(Font.font("",FontWeight.BLACK, 25));

        ArrayList usernames = new ArrayList();
        try {
            usernames = client.listUsers();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        ObservableList<String> users = FXCollections.observableArrayList(usernames);
        ListView<String> userList = new ListView<>(users);




        contactLayout.addAll(contactTitle, userList);

        VBox chat = new VBox();
        ObservableList chatLayout = chat.getChildren();
        chat.setPadding(new Insets(10));
        chat.setAlignment(Pos.CENTER);

        Text selectUser = new Text();
        selectUser.setFont(Font.font("", FontWeight.BLACK, 20));

        ObservableList<String> messages = FXCollections.observableArrayList();
        ListView<String> msgLog = new ListView<>(messages);

        final String[] selectUserID = new String[1];
        EventHandler<MouseEvent> eventHandlerSelectUser = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                try {
                    selectUserID[0] = Integer.toString(userList.getSelectionModel().getSelectedIndex());
                    //System.out.println(selectUserID[0]);
                    client.getPublicKey(selectUserID[0]);
                    messages.remove(0,messages.size());
                    messages.addAll(client.getMessageLog(selectUserID[0]));
                    users.remove(0,users.size());
                    users.addAll(client.listUsers());
                    selectUser.setText(users.get(Integer.parseInt(selectUserID[0])));
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        };
        userList.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandlerSelectUser);

        HBox myMsg = new HBox();
        ObservableList sendingMsg = myMsg.getChildren();

        TextField msgToSend = new TextField();
        msgToSend.setPromptText("Enter Message...");
        msgToSend.setPrefWidth(250);

        Button sendMsg = new Button("Send");

        //BUG: CRASHES IF TEXT FIELD IS EMPTY
        //CHECK IN THIS CLASS OR IF ITS IN CLIENT
        EventHandler eventHandlerSendMsg = new EventHandler() {
            @Override
            public void handle(Event event) {
                try {
                    //System.out.println(selectUserID[0]);
                    client.getPublicKey(selectUserID[0]);
                    System.out.println("MSG IS" + msgToSend.getText());
                    client.sendMessage(selectUserID[0], msgToSend.getText());
                    msgToSend.clear();
                    messages.remove(0, messages.size());
                    messages.addAll(client.getMessageLog(selectUserID[0]));
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        };
        sendMsg.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandlerSendMsg);
        //sendMsg.addEventHandler(KeyEvent.KEY_PRESSED.getName(KeyCode.getKeyCode("k")), eventHandlerSendMsg);


        sendingMsg.addAll(msgToSend, sendMsg);

        chatLayout.addAll(selectUser, msgLog, myMsg);

        UILayout.addAll(contacts, chat);

        //Creating a scene object
        Scene scene = new Scene(window);


        //Setting title to the Stage
        stage.setTitle("Cryptext");

        //Adding scene to the stage
        stage.setScene(scene);

        //Displaying the contents of the stage
        stage.show();

    }
    public static void main(String args[]){
        launch(args);
    }
}