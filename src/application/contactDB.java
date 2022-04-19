package application;

import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.sql.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

//https://stackoverflow.com/questions/11775212/how-to-save-the-result-from-jdbc-query-into-a-variable
public class contactDB {
    public Connection connection;
    public Connection getConnection() {
        String jdbcURL = "jdbc:postgresql://localhost:5432/chatDB";
        String username = "postgres";
        String password = "786";

        try {
            connection = DriverManager.getConnection(jdbcURL,username,password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void createMessage(int authorID, int recipientID, byte[] message) {
        try {
            contactDB contactDB = new contactDB();
            Connection connection = contactDB.getConnection();

            String sql = "INSERT INTO messages (author_id, recipient_id, message) VALUES (?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setObject(1, authorID+1);
            statement.setObject(2, recipientID+1);
            statement.setObject(3, message);

            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("new message inserted");
            }
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static byte [] getMessage (int authorID, int recipientID) {
        try {
            contactDB contactDB = new contactDB();
            Connection connection = contactDB.getConnection();
            String sql = "SELECT message FROM messages WHERE author_id = ? AND recipient_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1,authorID);
            statement.setObject(1,recipientID);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return rs.getBytes(1);
            }
            rs.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static List<Message> getAllMessages () {
        List<Message> messages = Collections.synchronizedList(new LinkedList<>());
        try {
            contactDB contactDB = new contactDB();
            Connection connection = contactDB.getConnection();
            String sql = "SELECT author_id FROM messages";
            String sql2 = "SELECT recipient_id FROM messages";
            String sql3 = "SELECT message FROM messages";
            PreparedStatement statement = connection.prepareStatement(sql);
            PreparedStatement statement2 = connection.prepareStatement(sql2);
            PreparedStatement statement3 = connection.prepareStatement(sql3);
            ResultSet rs = statement.executeQuery();
            ResultSet rs2 = statement2.executeQuery();
            ResultSet rs3 = statement3.executeQuery();

            while (rs.next()&&rs2.next()&&rs3.next()) {
                byte author= (byte) (rs.getByte(1)-1);
                byte recipient= (byte) (rs2.getByte(1)-1);
                Message m = new Message(author,recipient,rs3.getBytes(1));
                messages.add(m);
            }
            rs.close();
            rs2.close();
            rs3.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }






    public static void createUser(String name, Key publicKey, byte [] privateKey) {
        try {
            contactDB contactDB = new contactDB();
            Connection connection = contactDB.getConnection();

            String sql = "INSERT INTO users (username, public_key, encrypted_privatekey) VALUES (?, ?, ?)";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setObject(1, name);
            statement.setObject(2, publicKey.getEncoded());
            statement.setObject(3, privateKey);

            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("new user inserted");
            }
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setUsername(String name, int id) {
        try {
            contactDB contactDB = new contactDB();
            Connection connection = contactDB.getConnection();

            String sql = "UPDATE users SET username = ? WHERE user_id = ?";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setObject(1, name);
            statement.setObject(2,id+1);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("new user inserted");
            }
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void setPublicKey(byte [] key, int id) {
        try {
            contactDB contactDB = new contactDB();
            Connection connection = contactDB.getConnection();

            String sql = "UPDATE users SET public_key = ? WHERE user_id = ?";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setObject(1, key);
            statement.setObject(2,id+1);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("new user inserted");
            }
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void setPrivateKey(byte [] key, int id) {
        try {
            contactDB contactDB = new contactDB();
            Connection connection = contactDB.getConnection();

            String sql = "UPDATE users SET encrypted_privatekey = ? WHERE user_id = ?";

            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setObject(1, key);
            statement.setObject(2,id+1);
            int rows = statement.executeUpdate();
            if (rows > 0) {
                System.out.println("new user inserted");
            }
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static String getUsername (int id) {
        try {
            contactDB contactDB = new contactDB();
            Connection connection = contactDB.getConnection();
            String sql = "SELECT username FROM users WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1,id+1);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return rs.getString(1);
            }
            rs.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static PublicKey getPublicKey (int id) {
        PublicKey publicKey = null;
        try {
            contactDB contactDB = new contactDB();
            Connection connection = contactDB.getConnection();
            String sql = "SELECT public_key FROM users WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1,id+1);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(rs.getBytes(1)));
                return publicKey;
            }
            rs.close();
            connection.close();
        } catch (SQLException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return publicKey;
    }
    public static byte [] getPrivateKey (int id) {
        try {
            contactDB contactDB = new contactDB();
            Connection connection = contactDB.getConnection();
            String sql = "SELECT encrypted_privatekey FROM users WHERE user_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1,id+1);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return rs.getBytes(1);
            }
            rs.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static List<String> getAllUsernames () {
        List<String> usernames = Collections.synchronizedList(new LinkedList<>());
        try {
            contactDB contactDB = new contactDB();
            Connection connection = contactDB.getConnection();
            String sql = "SELECT username FROM users";
            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                usernames.add(rs.getString(1));
            }
            rs.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return usernames;
    }
    public static List<PublicKey> getAllPublicKey () {
        PublicKey publicKey;
        List<PublicKey> publicKeys = Collections.synchronizedList(new LinkedList<>());
        try {
            contactDB contactDB = new contactDB();
            Connection connection = contactDB.getConnection();
            String sql = "SELECT public_key FROM users";
            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet rs = statement.executeQuery();
            while(rs.next()) {
                publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(rs.getBytes(1)));
                publicKeys.add(publicKey);
            }
            rs.close();
            connection.close();
        } catch (SQLException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return publicKeys;
    }
    public static List<byte []> getAllPrivateKey () {
        List<byte[]> privateKeys = Collections.synchronizedList(new LinkedList<>());
        try {
            contactDB contactDB = new contactDB();
            Connection connection = contactDB.getConnection();
            String sql = "SELECT encrypted_privatekey FROM users";
            PreparedStatement statement = connection.prepareStatement(sql);

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                privateKeys.add(rs.getBytes(1));
            }
            rs.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return privateKeys;
    }






    public static int getID (String name) {
        try {
            contactDB contactDB = new contactDB();
            Connection connection = contactDB.getConnection();
            String sql = "SELECT user_id FROM users WHERE username = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1,name);
            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return rs.getInt(1)-1;
            }
            rs.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    public static int getID (byte [] key) {
        try {
            contactDB contactDB = new contactDB();
            Connection connection = contactDB.getConnection();
            String sql = "SELECT user_id FROM users WHERE public_key = ? OR encrypted_privatekey = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setObject(1,key);
            statement.setObject(2,key);

            ResultSet rs = statement.executeQuery();
            if(rs.next()) {
                return rs.getInt(1)-1;
            }
            rs.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }



    ///////



}
