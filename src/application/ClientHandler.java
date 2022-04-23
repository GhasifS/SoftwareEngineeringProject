package application;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread {
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    String received;

    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos) {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run() {
        stateUsername();
        try {
            this.dis.close();
            this.dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stateUsername() {
        while (true) {
            try {
                dos.writeUTF("Username: ('QUIT' - TO EXIT)");
                received = dis.readUTF();
                if (received.equals("QUIT")) {
                    System.out.println("application.Client " + this.s + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.s.close();
                    System.out.println("Connection closed");
                    break;
                }
                //Server.createUser(received);
                dos.writeUTF("USER CREATED: " + received);
                stateUserAddList(received);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stateUserAddList(String u1) {
        while (true) {
            try {
                //      dos.writeUTF("Send a message to: ('SEND' - GO TO USER SEND LIST) ('QUIT' - TO EXIT)\n" + Server.users.toString());
                received = dis.readUTF();
                if (received.equals("QUIT")) {
                    System.out.println("application.Client " + this.s + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.s.close();
                    System.out.println("Connection closed");
                    break;
                }
                if (received.equals("SEND")) {
                    dos.writeUTF("SWITCHING TO USER SEND LIST");
                    stateUserMessageList(u1);
                    break;
                }
                //if (Server.users.contains(received)) {
                //      Server.addKeys(u1, Server.usersPublicKeys.get(received));

                //     Server.addKeys(received, Server.usersPublicKeys.get(u1));
                //     dos.writeUTF("ADDED: "+received);
                //     stateUserMessageList(u1);
                //     break;
                //}
                dos.writeUTF("REFRESHED");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stateUserMessageList(String u1) {
        while (true) {
            try {
                //       dos.writeUTF("application.Message: ('BACK' - GO BACK TO USER ADD LIST) ('QUIT' - TO EXIT)\n" + Server.users.toString());
                received = dis.readUTF();
                if (received.equals("QUIT")) {
                    System.out.println("application.Client " + this.s + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.s.close();
                    System.out.println("Connection closed");
                    break;
                }
                if (received.equals("BACK")) {
                    dos.writeUTF("SWITCHING TO USER ADD LIST");
                    stateUserAddList(u1);
                    break;
                }
                // if (Server.users.contains(received)) {
                //    String u2 = received;
                //     dos.writeUTF("MESSAGING: "+u2);
                //     stateMessage(u1, u2);
                // }
                dos.writeUTF("REFRESHED");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stateMessage(String u1, String u2) {
        while (true) {
            try {
                dos.writeUTF("Type a message: ");
                received = dis.readUTF();
                if (received.equals("QUIT")) {
                    System.out.println("application.Client " + this.s + " sends exit...");
                    System.out.println("Closing this connection.");
                    this.s.close();
                    System.out.println("Connection closed");
                    break;
                }
                switch (received) {
                    case "PRINT":
                        // String printMsg = Server.printStringMsg(u1, u2);
                        // dos.writeUTF(printMsg);
                        break;
                    case "BACK":
                        dos.writeUTF("SWITCHING TO USER SEND LIST");
                        stateUserMessageList(u1);
                        break;
                    default:
                        //  Server.sendEncMSG(u1, u2, received);
                        dos.writeUTF("SENT");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}