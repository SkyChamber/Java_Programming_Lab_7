import commands.*;
import mainPart.*;
import serverStuff.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Server {
    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
        long count = 4;
        Date today = new Date();
        SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd");

        Scanner scanner = new Scanner(System.in);

        Class.forName("org.postgresql.Driver");
        String url = "jdbc:postgresql://pg:5432/studs";
//        String url = "jdbc:postgresql://localhost:9000/studs";
        System.out.println(url);
        String path = new java.io.File(".").getAbsolutePath();
        StringBuffer sb = new StringBuffer(path);
        int da = path.length();
        sb.delete(da-1,da);
        String regex = sb + "config.txt";
        BufferedReader configReader = new BufferedReader(new FileReader(regex));
        String configInput = configReader.readLine();
        Matcher userMatcher = Pattern.compile("^[a-zA-Z0-9]+").matcher(configInput);
        Matcher passMatcher = Pattern.compile("[a-zA-Z0-9]+$").matcher(configInput);

        Properties info = new Properties();
        if (userMatcher.find() && passMatcher.find()){
            info.setProperty("user",userMatcher.group());
            info.setProperty("password", passMatcher.group());
        }

        System.out.println(info);
        Connection connection = DriverManager.getConnection(url, info);
        System.out.println(connection);
        Statement st = connection.createStatement();

        ReentrantLock reentrantLock = new ReentrantLock();


        try (DatagramChannel server = DatagramChannel.open()){
            InetSocketAddress iAdd = new InetSocketAddress("localhost", 40000);
            server.bind(iAdd);

            System.out.println("Server Started: " + iAdd);

            server.configureBlocking(false);

            boolean deathToken = false;

            st.execute("DELETE FROM deathToken");
            ResultSet death = st.executeQuery("SELECT * FROM deathToken");
            deathToken = death.next();
            while(!deathToken) {
                CommandReader commandReader = new CommandReader(server, connection, reentrantLock);
                Thread readingThread = new Thread(commandReader);
                readingThread.start();

                death = st.executeQuery("SELECT * FROM deathToken");
                deathToken = death.next();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
