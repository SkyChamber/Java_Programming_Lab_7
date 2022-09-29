package serverStuff;

import mainPart.Chapter;
import mainPart.Coordinates;
import mainPart.Enum_explainer;
import mainPart.SpaceMarine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class CommandExecutor implements Runnable{
    public CommandExecutor(String command, Transfer intr, DatagramChannel datagramChannel, InetSocketAddress address, ReentrantLock lock, Connection connection){
        this.entry = command;
        this.transfer = intr;
        this.channel = datagramChannel;
        this.remoteAddress = address;
        this.reentrantLock = lock;
        this.connection = connection;
    }

    String entry;
    Transfer transfer;
    DatagramChannel channel;
    InetSocketAddress remoteAddress;
    ReentrantLock reentrantLock;
    Connection connection;

    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    String algorithm = "SHA-224";

    public static byte[] digest(byte[] input, String algorithm) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
        byte[] result = md.digest(input);
        return result;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    @Override
    public void run() {
        try {
            Date today = new Date();
            SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy-MM-dd");

            Statement st = connection.createStatement();

            connection.setAutoCommit(false);

            System.out.println(entry);

            switch (entry){
                case "netTest":
                    Transfer netTestTransfer = new Transfer();
                    netTestTransfer.setCommand(entry);
                    ObjectSender netTestSender = new ObjectSender(netTestTransfer,channel, remoteAddress);
                    ForkJoinPool forkJoinPool = new ForkJoinPool();
                    forkJoinPool.invoke(netTestSender);
                    break;
                case "log in":
                    String loginLogin = transfer.getAuthorise().getLogin();

                    byte[] shalogInBytes = CommandExecutor.digest(transfer.getAuthorise().getPassword().getBytes(UTF_8), algorithm);
                    String endingPass = String.format(bytesToHex(shalogInBytes));

                    boolean wellToken = false;

                    reentrantLock.lock();

                    ResultSet rs = st.executeQuery("SELECT * FROM auth");
                    while (rs.next()){
                        if (loginLogin.equalsIgnoreCase(rs.getString("login"))){
                            if (endingPass.equalsIgnoreCase(rs.getString("password"))){
                                wellToken = true;
                            }
                        }
                    }

                    reentrantLock.unlock();

                    Transfer logResultTransfer = new Transfer();
                    logResultTransfer.setCase(wellToken);

                    ObjectSender logResultSender = new ObjectSender(logResultTransfer,channel, remoteAddress);

                    ForkJoinPool loginForkJoinPool = new ForkJoinPool();
                    loginForkJoinPool.invoke(logResultSender);

                    break;
                case "register":
                    String regLogin = transfer.getAuthorise().getLogin();

                    byte[] shaInBytes = CommandExecutor.digest(transfer.getAuthorise().getPassword().getBytes(UTF_8), algorithm);
                    String endPass = String.format(bytesToHex(shaInBytes));

                    reentrantLock.lock();

                    st.execute("INSERT INTO auth VALUES(" +
                            "'"+ regLogin+"', " +
                            "'"+ endPass +"'" +
                            ")");

                    connection.commit();
                    reentrantLock.unlock();
                    break;
                case "exit":
                    System.out.println("Client initialize connections suicide ...");
                    reentrantLock.lock();

                    st.execute("INSERT INTO deathToken VALUES(1)");
                    connection.commit();

                    reentrantLock.unlock();
                    break;
                case "kill_server":
                    System.out.println("Client initialize suicide ...");
                    reentrantLock.lock();

                    st.execute("INSERT INTO deathToken VALUES(1)");
                    connection.commit();

                    reentrantLock.unlock();
                    break;
                case "help":
                    break;
                case "info":
                    reentrantLock.lock();

                    ReadCollection infoReadCollection = new ReadCollection();
                    TreeSet<SpaceMarine> infoSet = infoReadCollection.read(st);

                    reentrantLock.unlock();

                    String infoOut = "collection type: TreeSet\n"+formatForDateNow.format(today)+"\n"+"collection size: "+infoSet.size();

                    Transfer infoTransfer = new Transfer();
                    infoTransfer.setString(infoOut);

                    ObjectSender infoSender = new ObjectSender(infoTransfer,channel, remoteAddress);
                    ForkJoinPool infoForkJoinPool = new ForkJoinPool();
                    infoForkJoinPool.invoke(infoSender);
                    break;
                case "show":
                    reentrantLock.lock();
                    ReadCollection showReadCollection = new ReadCollection();
                    TreeSet<SpaceMarine> showSet = showReadCollection.read(st);

                    Transfer showTransfer = new Transfer();
                    showTransfer.setNumber(showSet.size());

                    ObjectSender showResultSender = new ObjectSender(showTransfer,channel, remoteAddress);

                    ForkJoinPool showForkJoinPool = new ForkJoinPool();
                    showForkJoinPool.invoke(showResultSender);

                    showSet.stream().forEach(e->{
                        try {
                            ByteBuffer showBuffer = ByteBuffer.allocate(65536);
                            ByteArrayOutputStream showByteArrayOS = new ByteArrayOutputStream();
                            ObjectOutputStream showObjectOS = new ObjectOutputStream(showByteArrayOS);

                            showObjectOS.writeObject(e);
                            showObjectOS.flush();

                            showBuffer.put(showByteArrayOS.toByteArray());
                            showBuffer.flip();
                            channel.send(showBuffer, remoteAddress);

                            showByteArrayOS.close();
                            showObjectOS.close();
                            Thread.sleep(100);
                        } catch (IOException | InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    });

                    reentrantLock.unlock();

                    break;
                case "IDClaim":
                    reentrantLock.lock();
                    ResultSet IDClaimrs = st.executeQuery("SELECT nextval('serial');");
                    IDClaimrs.next();
                    long tempID = IDClaimrs.getLong(1);

                    reentrantLock.unlock();

                    Transfer IDClaimTransfer = new Transfer();
                    IDClaimTransfer.setID(tempID);

                    ObjectSender IDClaimSender = new ObjectSender(IDClaimTransfer,channel, remoteAddress);
                    ForkJoinPool IDClaimForkJoinPool = new ForkJoinPool();
                    IDClaimForkJoinPool.invoke(IDClaimSender);
                    break;
                case "add":
                    SpaceMarine addMarine = transfer.getSpaceMarine();

                    reentrantLock.lock();

                    if (addMarine.getWorldName() == null){
                        st.execute("INSERT INTO ultramarines VALUES(" +
                                addMarine.getId()+ ", " +
                                "'"+addMarine.getName()+"', " +
                                "'"+addMarine.getAuthor()+"', " +
                                addMarine.getCords().getX()+", " +
                                addMarine.getCords().getY()+", " +
                                "'"+formatForDateNow.format(addMarine.getCreationDate())+"', " +
                                addMarine.getHealth()+", " +
                                "'"+addMarine.getCategory()+"', " +
                                "'"+addMarine.getWeaponType()+"', " +
                                "'"+addMarine.getMeleeWeapon()+"', " +
                                "'"+addMarine.getChapterName()+"' " +
                                ");");
                    } else {
                        st.execute("INSERT INTO ultramarines VALUES(" +
                                addMarine.getId() + ", " +
                                "'" + addMarine.getName() + "', " +
                                "'" + addMarine.getAuthor() + "', " +
                                addMarine.getCords().getX() + ", " +
                                addMarine.getCords().getY() + ", " +
                                "'" + formatForDateNow.format(addMarine.getCreationDate()) + "', " +
                                addMarine.getHealth() + ", " +
                                "'" + addMarine.getCategory() + "', " +
                                "'" + addMarine.getWeaponType() + "', " +
                                "'" + addMarine.getMeleeWeapon() + "', " +
                                "'" + addMarine.getChapterName() + "', " +
                                "'" + addMarine.getWorldName() + "'" +
                                ");");
                    }
                    connection.commit();
                    reentrantLock.unlock();
                    break;
                case "IDCheck":
                    reentrantLock.lock();
                    ResultSet IDCheckrs = st.executeQuery("SELECT * FROM ultramarines WHERE id = "+transfer.getID());
                    boolean updateToken = IDCheckrs.next();
                    if (updateToken){
                        if (transfer.getString().equalsIgnoreCase(IDCheckrs.getString("author"))){
                            updateToken = true;
                        } else {
                            updateToken = false;
                        }
                    }
                    Transfer IDCheckerTransfer = new Transfer();
                    IDCheckerTransfer.setCase(updateToken);

                    reentrantLock.unlock();

                    ObjectSender IDCheckSender = new ObjectSender(IDCheckerTransfer, channel, remoteAddress);
                    ForkJoinPool IDCheckForkJoinPool = new ForkJoinPool();
                    IDCheckForkJoinPool.invoke(IDCheckSender);
                    break;
                case "update":
                    reentrantLock.lock();
                    st.execute("DELETE FROM ultramarines WHERE id = "+transfer.getID());

                    if (transfer.getSpaceMarine().getWorldName() == null){
                        st.execute("INSERT INTO ultramarines VALUES(" +
                                transfer.getSpaceMarine().getId()+ ", " +
                                "'"+transfer.getSpaceMarine().getName()+"', " +
                                "'"+transfer.getSpaceMarine().getAuthor()+"', " +
                                transfer.getSpaceMarine().getCords().getX()+", " +
                                transfer.getSpaceMarine().getCords().getY()+", " +
                                "'"+formatForDateNow.format(transfer.getSpaceMarine().getCreationDate())+"', " +
                                transfer.getSpaceMarine().getHealth()+", " +
                                "'"+transfer.getSpaceMarine().getCategory()+"', " +
                                "'"+transfer.getSpaceMarine().getWeaponType()+"', " +
                                "'"+transfer.getSpaceMarine().getMeleeWeapon()+"', " +
                                "'"+transfer.getSpaceMarine().getChapterName()+"' " +
                                ");");
                    } else {
                        st.execute("INSERT INTO ultramarines VALUES(" +
                                transfer.getSpaceMarine().getId() + ", " +
                                "'" + transfer.getSpaceMarine().getName() + "', " +
                                "'"+transfer.getSpaceMarine().getAuthor()+"', " +
                                transfer.getSpaceMarine().getCords().getX() + ", " +
                                transfer.getSpaceMarine().getCords().getY() + ", " +
                                "'" + formatForDateNow.format(transfer.getSpaceMarine().getCreationDate()) + "', " +
                                transfer.getSpaceMarine().getHealth() + ", " +
                                "'" + transfer.getSpaceMarine().getCategory() + "', " +
                                "'" + transfer.getSpaceMarine().getWeaponType() + "', " +
                                "'" + transfer.getSpaceMarine().getMeleeWeapon() + "', " +
                                "'" + transfer.getSpaceMarine().getChapterName() + "', " +
                                "'" + transfer.getSpaceMarine().getWorldName() + "'" +
                                ");");
                    }
                    connection.commit();
                    reentrantLock.unlock();
                    break;
                case "remove_by_id":
                    reentrantLock.lock();
                    st.execute("DELETE FROM ultramarines WHERE id = "+transfer.getID()+" AND author = '"+transfer.getString()+"'");
                    reentrantLock.unlock();
                    break;
                case "clear":
                    reentrantLock.lock();
                    st.execute("DELETE FROM ultramarines WHERE author = '"+transfer.getString()+"'");
                    reentrantLock.unlock();
                    break;
                case "add_if_max":
                    reentrantLock.lock();
                    ResultSet addIMrs = st.executeQuery("SELECT * FROM ultramarines");
                    boolean addIMToken = true;

                    while (addIMrs.next()){
                        long id = addIMrs.getLong("id");
                        Date date = addIMrs.getDate("creation_date");
                        SpaceMarine addIMSpaceMarine = new SpaceMarine(id, date);
                        addIMSpaceMarine.setName(addIMrs.getString("name"));
                        addIMSpaceMarine.setHealth(addIMrs.getInt("hp"));
                        Enum_explainer enumExplainer = new Enum_explainer(addIMrs.getString("astartes"), addIMrs.getString("weapon"), addIMrs.getString("mele_weapon"));
                        addIMSpaceMarine.setCategory(enumExplainer.getAstsrtes());
                        addIMSpaceMarine.setWeaponType(enumExplainer.getWeapon());
                        addIMSpaceMarine.setMeleeWeapon(enumExplainer.getMele());
                        Coordinates coordinates = new Coordinates(addIMrs.getDouble("x_cord"), addIMrs.getDouble("y_cord"));
                        addIMSpaceMarine.setCoordinates(coordinates);
                        Chapter chapter = new Chapter(addIMrs.getString("chapter_name"), addIMrs.getString("world_name"));
                        addIMSpaceMarine.setChapter(chapter);
                        if (addIMSpaceMarine.getTest().length() > transfer.getSpaceMarine().getTest().length()){
                            addIMToken = false;
                        }
                    }

                    if (addIMToken){
                        if (transfer.getSpaceMarine().getWorldName() == null){
                            st.execute("INSERT INTO ultramarines VALUES(" +
                                    transfer.getSpaceMarine().getId()+ ", " +
                                    "'"+transfer.getSpaceMarine().getName()+"', " +
                                    "'"+transfer.getSpaceMarine().getAuthor()+"', " +
                                    transfer.getSpaceMarine().getCords().getX()+", " +
                                    transfer.getSpaceMarine().getCords().getY()+", " +
                                    "'"+formatForDateNow.format(transfer.getSpaceMarine().getCreationDate())+"', " +
                                    transfer.getSpaceMarine().getHealth()+", " +
                                    "'"+transfer.getSpaceMarine().getCategory()+"', " +
                                    "'"+transfer.getSpaceMarine().getWeaponType()+"', " +
                                    "'"+transfer.getSpaceMarine().getMeleeWeapon()+"', " +
                                    "'"+transfer.getSpaceMarine().getChapterName()+"' " +
                                    ");");
                        } else {
                            st.execute("INSERT INTO ultramarines VALUES(" +
                                    transfer.getSpaceMarine().getId() + ", " +
                                    "'" + transfer.getSpaceMarine().getName() + "', " +
                                    "'"+transfer.getSpaceMarine().getAuthor()+"', " +
                                    transfer.getSpaceMarine().getCords().getX() + ", " +
                                    transfer.getSpaceMarine().getCords().getY() + ", " +
                                    "'" + formatForDateNow.format(transfer.getSpaceMarine().getCreationDate()) + "', " +
                                    transfer.getSpaceMarine().getHealth() + ", " +
                                    "'" + transfer.getSpaceMarine().getCategory() + "', " +
                                    "'" + transfer.getSpaceMarine().getWeaponType() + "', " +
                                    "'" + transfer.getSpaceMarine().getMeleeWeapon() + "', " +
                                    "'" + transfer.getSpaceMarine().getChapterName() + "', " +
                                    "'" + transfer.getSpaceMarine().getWorldName() + "'" +
                                    ");");
                        }
                    }
                    connection.commit();
                    reentrantLock.unlock();
                    Transfer addIfMaxTransfer = new Transfer();
                    addIfMaxTransfer.setCase(addIMToken);
                    ObjectSender addIfMaxSender = new ObjectSender(addIfMaxTransfer,channel, remoteAddress);
                    ForkJoinPool aimForkJoinPool = new ForkJoinPool();
                    aimForkJoinPool.invoke(addIfMaxSender);
                    break;
                case "remove_greater":
                    reentrantLock.lock();
                    ResultSet removeGreaterrs = st.executeQuery("SELECT * FROM ultramarines");

                    while (removeGreaterrs.next()){
                        long id = removeGreaterrs.getLong("id");
                        Date date = removeGreaterrs.getDate("creation_date");
                        SpaceMarine remGrSpaceMarine = new SpaceMarine(id, date);
                        remGrSpaceMarine.setName(removeGreaterrs.getString("name"));
                        remGrSpaceMarine.setHealth(removeGreaterrs.getInt("hp"));
                        Enum_explainer enumExplainer = new Enum_explainer(removeGreaterrs.getString("astartes"),
                                removeGreaterrs.getString("weapon"), removeGreaterrs.getString("mele_weapon"));
                        remGrSpaceMarine.setCategory(enumExplainer.getAstsrtes());
                        remGrSpaceMarine.setWeaponType(enumExplainer.getWeapon());
                        remGrSpaceMarine.setMeleeWeapon(enumExplainer.getMele());
                        Coordinates coordinates = new Coordinates(removeGreaterrs.getDouble("x_cord"), removeGreaterrs.getDouble("y_cord"));
                        remGrSpaceMarine.setCoordinates(coordinates);
                        Chapter chapter = new Chapter(removeGreaterrs.getString("chapter_name"), removeGreaterrs.getString("world_name"));
                        remGrSpaceMarine.setChapter(chapter);
                        if (remGrSpaceMarine.getTest().length() > transfer.getSpaceMarine().getTest().length()){
                            st.execute("DELETE FROM ultramarines WHERE id = "+remGrSpaceMarine.getId()+" AND author = '"+transfer.getString()+"'");
                        }
                    }
                    reentrantLock.unlock();
                    break;
                case "filter_starts_with_name":
                    int filterStartsWithCounter = 0;
                    TreeSet<SpaceMarine> filterStartsWithSet = new TreeSet<SpaceMarine>();

                    reentrantLock.lock();

                    ReadCollection fswnReadCollection = new ReadCollection();

                    Object[] filterStartsWithArray = fswnReadCollection.read(st).toArray();

                    reentrantLock.unlock();

                    for (Object value : filterStartsWithArray){
                        String regex = "^" + transfer.getString() + "(\\w*)";
                        SpaceMarine marine = (SpaceMarine) value;
                        boolean matcher = Pattern.matches(regex,marine.getName());
                        if (matcher){
                            filterStartsWithCounter ++;
                            filterStartsWithSet.add(marine);
                        }
                    }
                    Transfer filterStartsWithTransfer = new Transfer();
                    filterStartsWithTransfer.setNumber(filterStartsWithCounter);

                    ObjectSender filterStartsWithSender = new ObjectSender(filterStartsWithTransfer);
                    filterStartsWithSender.send(channel, (InetSocketAddress) remoteAddress);

                    Thread.sleep(100);

                    filterStartsWithSet.stream().forEach(e->{
                        try {
                            Transfer filterStartsWithSendTransfer = new Transfer();
                            filterStartsWithSendTransfer.setString(e.getName());

                            ObjectSender filterStartsWithSendSender = new ObjectSender(filterStartsWithSendTransfer,channel, remoteAddress);
                            ForkJoinPool fswForkJoinPool = new ForkJoinPool();
                            fswForkJoinPool.invoke(filterStartsWithSendSender);

                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    });
                    break;
                case "filter_less_than_health":
                    int filterLessThanHealthCounter = 0;
                    TreeSet<SpaceMarine> filterLessThanHealthSet = new TreeSet<SpaceMarine>();

                    reentrantLock.lock();
                    ReadCollection flthReadCollection = new ReadCollection();

                    Object[] filterLessThanHealthArray = flthReadCollection.read(st).toArray();

                    reentrantLock.unlock();
                    for (Object value : filterLessThanHealthArray){
                        if (transfer.getNumber() > ((SpaceMarine) value).getHealth()){
                            filterLessThanHealthCounter ++;
                            filterLessThanHealthSet.add((SpaceMarine) value);
                        }
                    }
                    Transfer filterLessThanHealthTransfer = new Transfer();
                    filterLessThanHealthTransfer.setNumber(filterLessThanHealthCounter);

                    ObjectSender filterLessThanHealthSender = new ObjectSender(filterLessThanHealthTransfer);
                    filterLessThanHealthSender.send(channel, (InetSocketAddress) remoteAddress);

                    Thread.sleep(100);

                    filterLessThanHealthSet.stream().forEach(e->{
                        try {
                            Transfer filterLessThanHealthSendTransfer = new Transfer();
                            filterLessThanHealthSendTransfer.setString(e.getName());

                            ObjectSender filterLessThanHealthSendSender = new ObjectSender(filterLessThanHealthSendTransfer,channel, remoteAddress);
                            ForkJoinPool flthForkJoinPool = new ForkJoinPool();
                            flthForkJoinPool.invoke(filterLessThanHealthSendSender);

                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    });
                    break;
                case "filter_greater_than_chapter":
                    int filterGreaterThanChapterCounter = 0;
                    TreeSet<SpaceMarine> filterGreaterThanChapterSet = new TreeSet<SpaceMarine>();

                    reentrantLock.lock();
                    ReadCollection fgtcReadCollection = new ReadCollection();

                    Object[] filterGreaterThanChapterArray = fgtcReadCollection.read(st).toArray();

                    reentrantLock.unlock();
                    for (Object value : filterGreaterThanChapterArray){
                        if (transfer.getNumber() < ((SpaceMarine) value).getLegion().length()){
                            filterGreaterThanChapterCounter ++;
                            filterGreaterThanChapterSet.add((SpaceMarine) value);
                        }
                    }
                    Transfer filterGreaterThanChapterTransfer = new Transfer();
                    filterGreaterThanChapterTransfer.setNumber(filterGreaterThanChapterCounter);

                    ObjectSender filterGreaterThanChapterSender = new ObjectSender(filterGreaterThanChapterTransfer,channel, remoteAddress);
                    ForkJoinPool fgtcForkJoinPool = new ForkJoinPool();
                    fgtcForkJoinPool.invoke(filterGreaterThanChapterSender);

                    Thread.sleep(100);

                    filterGreaterThanChapterSet.stream().forEach(e->{
                        try {
                            Transfer filterGreaterThanChapterSendTransfer = new Transfer();
                            filterGreaterThanChapterSendTransfer.setString(e.getName());

                            ObjectSender filterGreaterThanChapterSendSender = new ObjectSender(filterGreaterThanChapterSendTransfer);
                            filterGreaterThanChapterSendSender.send(channel, (InetSocketAddress) remoteAddress);

                            Thread.sleep(100);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    });
                    break;
                default:
                    int dgfddfsd = 131;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
