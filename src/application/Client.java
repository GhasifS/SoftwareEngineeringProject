package application;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class Client {
    private final Socket socket;
    private final InputStream socketInput;
    private final OutputStream socketOutput;
    private final Map<Byte, PublicKey> publicKeys;
    private final Scanner scanner = new Scanner(System.in);
    protected OutputMode mode;
    private byte id = 0;
    private KeyPair keyPair;
    enum OutputMode {
        MSG, GET, CMD, BYE, LST, PUB, LOG, PWD, NME, REG
    }

    public Client(Socket socket) throws IOException {
        this.socket = socket;
        this.socketInput = socket.getInputStream();
        this.socketOutput = socket.getOutputStream();
        this.publicKeys = new HashMap<>();
        this.mode = OutputMode.LOG;

        System.out.println("Connected to server.\nEnter \\quit to disconnect at any time.");
    }

    public static void main(String... args) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        Socket socket;
        try {
            socket = new Socket("192.168.0.15", 2020);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        Client client;
        try {
            client = new Client(socket);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error initializing client. Goodbye.");
            return;
        }
        client.run();
    }

    /**
     * @param username new username
     * @return true if the name change was successful. false if someone else already uses that name
     */
    protected boolean nme(String username) throws IOException {
        byte[] bytes = new byte[3 + 16];
        bytes[0] = 'N';
        bytes[1] = 'M';
        bytes[2] = 'E';
        System.arraycopy(username.getBytes(StandardCharsets.US_ASCII), 0, bytes, 3, username.length());
        socketOutput.write(bytes);
        bytes = socketInput.readNBytes(4);
        return bytes[3] == 'S';
    }

    protected boolean pwd(String password) throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, IOException {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        SecretKeySpec aesKey = new SecretKeySpec(sha256.digest(password.getBytes(StandardCharsets.US_ASCII)), "AES");

        byte[] newPrivate = encryptAES(this.keyPair.getPrivate().getEncoded(), aesKey);
        byte[] signature = signRSA(newPrivate, this.keyPair.getPrivate());
        byte[] output = new byte[3 + newPrivate.length + signature.length];
        output[0] = 'P';
        output[1] = 'W';
        output[2] = 'D';
        System.arraycopy(newPrivate, 0, output, 3, newPrivate.length);
        System.arraycopy(signature, 0, output, 3 + newPrivate.length, signature.length);
        socketOutput.write(output);

        return socketInput.readNBytes(4)[3] == 'S';
    }

    public void run() throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        String input;
        byte[] output;
        while (this.socket.isConnected()) {
            switch (mode) {
                case PWD:
                    System.out.println("Desired password: ");
                    input = scanner.nextLine();
                    if (modeSwitched(input)) {
                        continue;
                    }
                    if (pwd(input)) {
                        System.out.println("Password changed successfully.");
                    } else {
                        System.out.println("Unexpected error. Your password was not changed.");
                    }
                    this.mode = OutputMode.CMD;
                    break;
                case NME:
                    System.out.println("Desired username (1-16 characters): ");
                    input = scanner.nextLine();
                    if (modeSwitched(input)) {
                        continue;
                    }
                    if (input.length() > 16 || input.length() == 0) {
                        System.out.println("Invalid length. Please try again.");
                        continue;
                    } else if (input.startsWith("\\")) {
                        System.out.println("Username can not start with \\. Please try again");
                        continue;
                    }
                    if (nme(input)) {
                        System.out.println("Name change successful, you are now " + input);
                    } else {
                        System.out.println("Name change unsuccessful, someone else is already named " + input);
                    }
                    this.mode = OutputMode.CMD;
                    break;
                case LOG:
                    System.out.println("Username: ");
                    String username = scanner.nextLine();
                    if (modeSwitched(username)) {
                        continue;
                    }
                    if (username.length() > 16 || username.length() == 0) {
                        System.out.println("Invalid length. Please try again.");
                    } else if (username.startsWith("\\")) {
                        System.out.println("Username can not start with \\. Please try again");
                    } else {
                        log(username);

                        System.out.println("Password: ");
                        input = scanner.nextLine();
                        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                        SecretKeySpec aesKey = new SecretKeySpec(sha256.digest(input.getBytes(StandardCharsets.US_ASCII)), "AES");

                        output = this.socketInput.readNBytes(4);
                        if (output[3] == 'Y') {
                            this.id = this.socketInput.readNBytes(1)[0];
                            byte[] publickey = this.socketInput.readNBytes(550);
                            byte[] privatekey = this.socketInput.readNBytes(2384);
                            setKeyPair(publickey, decryptAES(privatekey, aesKey));
                            this.mode = OutputMode.CMD;

                            output = this.socketInput.readNBytes(8);
                            byte[] signedTimestamp = signRSA(output, this.keyPair.getPrivate());
                            output = new byte[3 + 1 + signedTimestamp.length];
                            output[0] = 'L';
                            output[1] = 'O';
                            output[2] = 'G';
                            output[3] = 'B';
                            System.arraycopy(signedTimestamp, 0, output, 4, signedTimestamp.length);
                            this.socketOutput.write(output);
                            if (this.socketInput.readNBytes(4)[3] == 'S') {
                                System.out.println("Logged in successfully! You can now send and receive encrypted messages.");
                            }
                        } else {
                            System.out.println("No information found for this username.");
                            System.out.println("Would you like to make an account (Y) or try again (N)?");
                            input = scanner.nextLine();
                            if (input.equals("Y")) {
                                reg(username, aesKey);
                                output = this.socketInput.readNBytes(4);
                                this.id = output[3];
                                this.mode = OutputMode.CMD;
                            }
                        }
                    }
                    break;
                case CMD:
                    System.out.println("Please select a command: \\GET, \\LST, \\MSG, \\PUB, \\NME, or \\QUIT");
                    input = scanner.nextLine();
                    if (!modeSwitched(input)) {
                        System.out.println("Invalid input. Please try again.");
                    }
                    break;
                case LST:
                    System.out.println("Requesting list of users from the server.");
                    listUsers();
                    break;
                case PUB:
                    System.out.println("Enter the ID of the user whose public key you want:");
                    getPublicKey(scanner.nextLine());
                    break;
                case MSG:
                    System.out.println("Enter the recipient's ID:");
                    sendMessage(scanner.nextLine(), "");
                    break;
                case GET:
                    System.out.println("Enter the author's ID:");
                    getMessageLog(scanner.nextLine());
                    break;
                case BYE:
                    System.out.println("Disconnecting from server.");
                    scanner.close();
                    socket.close();
                    break;
            }
        }
        System.out.println("Disconnected from server. Shutting down.");
    }

    protected boolean run(OutputMode mode, String inputOne, String inputTwo) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        this.mode = mode;
        byte[] output;
        while (this.socket.isConnected()) {
            switch (mode) {
                case PWD:
                    /*if (modeSwitched(inputOne)) {
                        continue;
                    }*/
                    //System.out.println("Password changed successfully.");
                    //System.out.println("Unexpected error. Your password was not changed.");
                    return pwd(inputOne);
                case NME:
                    System.out.println("Desired username (1-16 characters): ");
                    /*if (modeSwitched(input)) {
                        continue;
                    }*/
                    if (inputOne.length() > 16 || inputOne.length() == 0) {
                        return false;
                    } else if (inputOne.startsWith("\\")) {
                        return false;
                    }
                    //System.out.println("Name change successful, you are now " + input);
                    //System.out.println("Name change unsuccessful, someone else is already named " + input);
                    return nme(inputOne);
                case LOG:
                    /*if (modeSwitched(username)) {
                        continue;
                    }*/
                    if (inputOne.length() > 16 || inputOne.length() == 0) {
                        System.out.println("Invalid length. Please try again.");
                    } else if (inputOne.startsWith("\\")) {
                        System.out.println("Username can not start with \\. Please try again");
                    } else {
                        log(inputOne);

                        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                        SecretKeySpec aesKey = new SecretKeySpec(sha256.digest(inputTwo.getBytes(StandardCharsets.US_ASCII)), "AES");

                        output = this.socketInput.readNBytes(4);
                        if (output[3] == 'Y') {
                            this.id = this.socketInput.readNBytes(1)[0];
                            byte[] publickey = this.socketInput.readNBytes(550);
                            byte[] privatekey = this.socketInput.readNBytes(2384);
                            setKeyPair(publickey, decryptAES(privatekey, aesKey));
                            this.mode = OutputMode.CMD;

                            output = this.socketInput.readNBytes(8);
                            byte[] signedTimestamp = signRSA(output, this.keyPair.getPrivate());
                            output = new byte[3 + 1 + signedTimestamp.length];
                            output[0] = 'L';
                            output[1] = 'O';
                            output[2] = 'G';
                            output[3] = 'B';
                            System.arraycopy(signedTimestamp, 0, output, 4, signedTimestamp.length);
                            this.socketOutput.write(output);
                            if (this.socketInput.readNBytes(4)[3] == 'S') {
                                //System.out.println("Logged in successfully! You can now send and receive encrypted messages.");
                                return true;
                            }
                        } else {
                            return false;
                            /*System.out.println("No information found for this username.");
                            System.out.println("Would you like to make an account (Y) or try again (N)?");
                            input = scanner.nextLine();
                            if (input.equals("Y")) {
                                reg(username, aesKey);
                                output = this.socketInput.readNBytes(4);
                                this.id = output[3];
                                this.mode = OutputMode.CMD;
                            }*/
                        }
                    }
                    break;
                case REG:
                    log(inputOne);

                    MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                    SecretKeySpec aesKey = new SecretKeySpec(sha256.digest(inputTwo.getBytes(StandardCharsets.US_ASCII)), "AES");

                    output = this.socketInput.readNBytes(4);
                    if (output[3] == 'Y') {
                        this.id = this.socketInput.readNBytes(1)[0];
                        byte[] publickey = this.socketInput.readNBytes(550);
                        byte[] privatekey = this.socketInput.readNBytes(2384);
                        setKeyPair(publickey, decryptAES(privatekey, aesKey));
                        this.mode = OutputMode.CMD;

                        output = this.socketInput.readNBytes(8);
                        byte[] signedTimestamp = signRSA(output, this.keyPair.getPrivate());
                        output = new byte[3 + 1 + signedTimestamp.length];
                        output[0] = 'L';
                        output[1] = 'O';
                        output[2] = 'G';
                        output[3] = 'B';
                        System.arraycopy(signedTimestamp, 0, output, 4, signedTimestamp.length);
                        this.socketOutput.write(output);
                        if (this.socketInput.readNBytes(4)[3] == 'S') {
                            //System.out.println("Logged in successfully! You can now send and receive encrypted messages.");
                            return false;
                        }
                    } else {
                        reg(inputOne, aesKey);
                        output = this.socketInput.readNBytes(4);
                        this.id = output[3];
                        this.mode = OutputMode.CMD;
                        return true;
                    }

                case LST:
                    System.out.println("Requesting list of users from the server.");
                    listUsers();
                    return true;
                case PUB:
                    System.out.println("Enter the ID of the user whose public key you want:");
                    getPublicKey(scanner.nextLine());
                    return true;
                case MSG:
                    System.out.println("Enter the recipient's ID:");
                    sendMessage(inputOne, inputTwo);
                    break;
                case GET:
                    System.out.println("Enter the author's ID:");
                    getMessageLog(inputOne);
                    break;
                case BYE:
                    System.out.println("Disconnecting from server.");
                    scanner.close();
                    socket.close();
                    break;
            }
        }
        return false;
    }

    protected void sendMessage(String userID, String msg) throws IOException {
        String input;
        input = userID;
        if (modeSwitched(input)) {
            return;
        }
        byte id = Byte.parseByte(input);
        input = msg;
        if (input.equals("")) {
            System.out.println("Enter your message: ");
            input = scanner.nextLine();
        }
        if (modeSwitched(input)) {
            return;
        }
        if (!input.equals(null)) {
            msg(id, input);
        }
    }

    protected void setUsername(String username) throws IOException {

        String input;
        byte[] output;
        input = username;
        if (modeSwitched(input)) {
            return;
        }
        if (input.length() > 16 || input.length() == 0) {
            System.out.println("Invalid length. Please try again.");
        } else if (input.startsWith("\\")) {
            System.out.println("Username can not start with \\. Please try again");
        } else {
            //set(input);
            output = this.socketInput.readNBytes(4);
            if (output[0] == 'S') {
                this.id = output[3];
                this.mode = OutputMode.CMD;
            } else {
                System.out.println("Something went wrong. Please try again.");
            }
        }
    }

    // server response: GET + authorID + #ofmessages + (numberOfBlocks + aes)
    // server response: GET + #ofmessages + (authorID (numberOfBlocks + aes))
    protected ArrayList getMessageLog(String userID) throws IOException {
        ArrayList<String> msgLog = new ArrayList<>();
        String input;
        byte id;
        input = userID;
        if (modeSwitched(input)) {
            return null;
        }
        id = Byte.parseByte(input);
        System.out.println("Getting last 10 messages.");
        get(id, (byte)10);

        int numberOfMessages = socketInput.readNBytes(4)[3];
        for (int i = 0; i < numberOfMessages; i++) {
            byte authorID = socketInput.readNBytes(1)[0];
            // for each message: authorid + numberofblocks + authorkey + recipientkey + aesblocks
            byte[] message = socketInput.readNBytes(512 + 512 + socketInput.readNBytes(1)[0] * 16);
            msgLog.add(getMessage(authorID, message));
            System.out.println(getMessage(authorID, message));
        }
        mode = OutputMode.CMD;
        return msgLog;
    }

    protected void getPublicKey(String id) throws IOException {
        String input;
        byte[] output;
        input = id;
        if (modeSwitched(input)) {
            return;
        }
        pub(Byte.parseByte(input));
        output = this.socketInput.readNBytes(3 + 1 + 550);
        byte[] key = new byte[550];
        System.arraycopy(output, 4, key, 0, 550);
        try {
            publicKeys.put(Byte.parseByte(input), KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(key)));
            mode = OutputMode.CMD;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Something went wrong. Please try again.");
        }
    }

    protected ArrayList listUsers() throws IOException {
        ArrayList<String> names = new ArrayList<>();
        String nameToAdd;
        byte[] output;
        lst();
        output = this.socketInput.readNBytes(3);
        if (output[0] == 'L') {
            int len = this.socketInput.readNBytes(1)[0] * 17;
            output = this.socketInput.readNBytes(len);
            for (int i = 0; i < output.length / 17; i++) {
                System.out.print("(" + output[i * 17] + ") ");
                byte[] username = new byte[16];
                System.arraycopy(output, i * 17 + 1, username, 0, 16);
                nameToAdd = "";
                for (final byte letter : username) {
                    if (letter != 0) {
                        nameToAdd += (char)letter;
                        System.out.print((char)letter);
                    }
                }
                names.add(nameToAdd);
                System.out.println();
            }
            mode = OutputMode.CMD;
            return names;
        } else {
            System.out.println("Something went wrong. Trying again.");
        }
        return null;
    }

    private void setKeyPair(byte[] publickeyBytes, byte[] privatekeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PublicKey publickey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publickeyBytes));
        PrivateKey privatekey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privatekeyBytes));
        this.keyPair = new KeyPair(publickey, privatekey);
    }

    private String getMessage(byte authorID, byte[] encryptedMessage) {
        return decodeMsg(authorID, encryptedMessage, this.keyPair.getPrivate());
    }
//
//    private String[] getMessages(byte[] data) {
//        byte numberOfMessages = data[3];
//        String[] messages = new String[numberOfMessages];
//        int startPoint = 4;
//        for (int i = 0; i < numberOfMessages; i++) {
//            byte numberOfAesBlocks = data[startPoint];
//            byte[] encryptedMessage = new byte[512 + (numberOfAesBlocks * 16)];
//            System.arraycopy(data, startPoint + 1, encryptedMessage, 0, 512 + (numberOfAesBlocks * 16));
//
//            messages[i] = decodeMsg(encryptedMessage, this.keyPair.getPrivate());
//            startPoint += 512 + (numberOfAesBlocks * 16) + 1;
//        }
//        return messages;
//    }

    private void get(byte friendID, byte numberToGet) throws IOException {
        socketOutput.write(new byte[]{'G', 'E', 'T', friendID, numberToGet});
    }

    private void msg(byte recipientID, String message) throws IOException {
        Cipher cipher;
        byte[] recipientKey;
        byte[] authorKey;
        SecretKey aesKey = generateKey();
        byte[] encryptedMessage;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.ENCRYPT_MODE, publicKeys.get(recipientID));
            recipientKey = cipher.doFinal(aesKey.getEncoded());

            cipher.init(Cipher.ENCRYPT_MODE, this.keyPair.getPublic());
            authorKey = cipher.doFinal(aesKey.getEncoded());

            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            encryptedMessage = cipher.doFinal(message.getBytes(StandardCharsets.US_ASCII));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        byte[] output = new byte[3 + 1 + 1 + recipientKey.length + authorKey.length + encryptedMessage.length];
        output[0] = 'M';
        output[1] = 'S';
        output[2] = 'G';
        output[3] = recipientID;
        output[4] = (byte)(encryptedMessage.length / 16);
        System.arraycopy(recipientKey, 0, output, 5, recipientKey.length);
        System.arraycopy(authorKey, 0, output, 5 + recipientKey.length, authorKey.length);
        System.arraycopy(encryptedMessage, 0, output, 5 + recipientKey.length + authorKey.length, encryptedMessage.length);
        System.out.println("message to server length: " + output.length);
        socketOutput.write(output);
    }

    private void pub(byte id) throws IOException {
        socketOutput.write(new byte[]{'P', 'U', 'B', id});
    }

    private void lst() throws IOException {
        socketOutput.write(new byte[]{'L', 'S', 'T'});
    }

    private void reg(String username, SecretKey aesKey) throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator generator;
        generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(4096);
        this.keyPair = generator.generateKeyPair();
        byte[] encryptedPrivate = encryptAES(keyPair.getPrivate().getEncoded(), aesKey);
        byte[] output = new byte[3 + 16 + 550 + encryptedPrivate.length];
        output[0] = 'R';
        output[1] = 'E';
        output[2] = 'G';
        System.arraycopy(username.getBytes(StandardCharsets.US_ASCII), 0, output, 3, username.length());
        System.arraycopy(this.keyPair.getPublic().getEncoded(), 0, output, 3 + 16, 550);
        System.arraycopy(encryptedPrivate, 0, output, 3 + 16 + 550, encryptedPrivate.length);
        socketOutput.write(output);
    }

    private void log(String username) throws IOException {
        byte[] output = new byte[4 + 16];
        output[0] = 'L';
        output[1] = 'O';
        output[2] = 'G';
        output[3] = 'A';
        System.arraycopy(username.getBytes(StandardCharsets.US_ASCII), 0, output, 4, username.length());
        socketOutput.write(output);
    }

    private boolean modeSwitched(String input) {
        if (input.equals("\\QUIT")) {
            mode = OutputMode.BYE;
            return true;
        }
        switch (input) {
            case "\\LST":
                mode = OutputMode.LST;
                break;
            case "\\GET":
                mode = OutputMode.GET;
                break;
            case "\\MSG":
                mode = OutputMode.MSG;
                break;
            case "\\PUB":
                mode = OutputMode.PUB;
                break;
            case "\\NME":
                mode = OutputMode.NME;
                break;
            case "\\PWD":
                mode = OutputMode.PWD;
                break;
            default:
                return false;
        }
        return true;
    }

    public static String toBase64(byte[] data) {
        return Base64.getEncoder().encodeToString(data);
    }

    private String decodeMsg(byte authorID, byte[] msg, PrivateKey key) {
        Cipher cipher;
        String message;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] encryptedKey = new byte[512];

            //     authorkey + mykey + aesblocks
            System.out.println(msg.length); // should be 512 + 512 + 16 = 1040
            if (authorID != this.id) {
                System.arraycopy(msg, 0, encryptedKey, 0, 512);
            } else {
                System.arraycopy(msg, 512, encryptedKey, 0, 512);
            }
            byte[] aesKey = cipher.doFinal(encryptedKey);

            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(aesKey, "AES"));
            byte[] encryptedMessage = new byte[msg.length - 1024];
            System.arraycopy(msg, 1024, encryptedMessage, 0, msg.length - 1024);
            message = new String(cipher.doFinal(encryptedMessage), StandardCharsets.US_ASCII);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        if (authorID == this.id) {
            return "You: " + message;
        } else {
            return "Them: " + message;
        }
    }

    private static byte[] signRSA(byte[] input, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(input);
        return signature.sign();
    }

    private static byte[] encryptAES(byte[] msg, SecretKey key) {
        Cipher cipher;
        byte[] message;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            message = cipher.doFinal(msg);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return message;
    }

    private static byte[] decryptAES(byte[] msg, SecretKey key) {
        Cipher cipher;
        byte[] message;
        try {
            cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            message = cipher.doFinal(msg);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return message;
    }

    private static SecretKey generateKey() {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }
}
