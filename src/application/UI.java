package application;

import javafx.collections.FXCollections;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

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
            //System.out.println("Desired username (1-16 characters): ");
            //client.setUsername(scanner.nextLine());
        } catch (Exception e) {
            e.printStackTrace();
            //System.out.println("Error initializing client. Goodbye.");
            return;
        }

        VBox loginWindow = new VBox();
        loginWindow.setMinSize(400,150);
        ObservableList loginLayout = loginWindow.getChildren();

        TextField loginUsername = new TextField();
        loginUsername.setPromptText("Enter Username...");
        loginUsername.setPrefWidth(250);

        TextField loginPassword = new TextField();
        loginPassword.setPromptText("Enter Password");
        loginPassword.setPrefWidth(250);

        Button loginBtn = new Button("Login");
        Button createBtn = new Button("Create Account");

        loginWindow.setPadding(new Insets((10)));
        loginWindow.setAlignment(Pos.CENTER);

        Text loginStatus = new Text();

        loginLayout.addAll(loginUsername, loginPassword, loginBtn, createBtn, loginStatus);

        updateScene(stage, new Scene(loginWindow));

        /*
        Main Chat Window Portion
         */

        HBox mainWindow = gethBox(client);
        if (mainWindow == null) return;

        EventHandler eventHandlerLogin = new EventHandler() {
            @Override
            public void handle(Event event) {
                try {
                    //TODO implement login features
                    if (loginUsername.getText().length() > 16 || loginUsername.getText().length() == 0) {
                        loginStatus.setText("Invalid length. Please try again.");
                    } else if (loginUsername.getText().startsWith("\\")) {
                        loginStatus.setText("Username can not start with \\. Please try again");
                    } else if (client.run(Client.OutputMode.LOG,loginUsername.getText(),loginPassword.getText())) {
                        updateScene(stage, new Scene(mainWindow));
                    } else {
                        loginStatus.setText("Incorrect Username or Password");
                    }

                } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidKeyException e) {
                    e.printStackTrace();
                }

            }
        };
        loginBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandlerLogin);

        EventHandler eventHandlerCreate = new EventHandler() {
            @Override
            public void handle(Event event) {
                try {
                    //TODO implement login features
                    if (loginUsername.getText().length() == 0 && loginPassword.getText().length() == 0) {
                        loginStatus.setText("Enter desired Username and Password, then click this button.");
                    }
                    else if (loginUsername.getText().length() > 16 || loginUsername.getText().length() == 0) {
                        loginStatus.setText("Invalid length. Please try again.");
                    } else if (loginUsername.getText().startsWith("\\")) {
                        loginStatus.setText("Username can not start with \\. Please try again.");
                    } else if (client.run(Client.OutputMode.REG,loginUsername.getText(),loginPassword.getText())) {
                        updateScene(stage, new Scene(mainWindow));
                    } else {
                        loginStatus.setText("UNKNOWN ERROR OCCURRED");
                    }

                } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException | InvalidKeyException e) {
                    e.printStackTrace();
                }

            }
        };
        createBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, eventHandlerCreate);


        //Setting title to the Stage
        stage.setTitle("Cryptext");

        //Adding scene to the stage
        //updateScene(stage, main);

        //Displaying the contents of the stage
        stage.show();



    }

    private HBox gethBox(Client client) {
        //Instantiate window for program as an HBox
        HBox mainWindow = new HBox();
        mainWindow.setMinSize(200, 150);
        ObservableList UILayout = mainWindow.getChildren();

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
            TimeUnit.SECONDS.sleep(2);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return null;
        }

        ObservableList<String> users = FXCollections.observableArrayList(usernames);
        ListView<String> userList = new ListView<>(users);

        final ContextMenu accountSettings = new ContextMenu();
        accountSettings.setOnShowing(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent e) {
                System.out.println("showing");
            }
        });
        accountSettings.setOnShown(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent e) {
                System.out.println("shown");
            }
        });

        MenuItem item1 = new MenuItem("Change Username");
        item1.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                System.out.println("About");
            }
        });
        MenuItem item2 = new MenuItem("Change Password");
        item2.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent e) {
                System.out.println("Preferences");
            }
        });
        accountSettings.getItems().addAll(item1, item2);

        Label myAccount = new Label("Your Account");
        myAccount.setFont(Font.font(10));
        myAccount.setContextMenu(accountSettings);

        contactLayout.addAll(contactTitle, userList, myAccount);

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

        EventHandler eventHandlerSendMsg = new EventHandler() {
            @Override
            public void handle(Event event) {
                try {
                    //System.out.println(selectUserID[0]);
                    client.getPublicKey(selectUserID[0]);
                    //System.out.println("MSG IS" + msgToSend.getText());
                    if (!msgToSend.getText().equals("")) {
                        client.sendMessage(selectUserID[0], msgToSend.getText());
                        msgToSend.clear();
                        messages.remove(0, messages.size());
                        messages.addAll(client.getMessageLog(selectUserID[0]));
                    }
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
        return mainWindow;
    }

    private void updateScene(Stage stage, Scene newScene) {
        stage.setScene(newScene);
    }

    public static void main(String args[]){
        launch(args);
    }
}