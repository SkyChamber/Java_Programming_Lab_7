package commands.Execute;

import commands.*;
import mainPart.SpaceMarine;
import serverStuff.ObjectReceiver;
import serverStuff.ObjectSender;
import serverStuff.Transfer;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.TreeSet;

public class Execute_script {
    public Execute_script(String filename){
        String path = new java.io.File(".").getAbsolutePath();

        System.out.println(path);

        int da = path.length();
        StringBuffer sb = new StringBuffer(path);
        sb.delete(da-1,da);
        this.regex = sb + filename;

        System.out.println(this.regex);
    }
    private String regex;
    SimpleDateFormat formatForDateNow = new SimpleDateFormat("yyyy.MM.dd");

    public void execute(Date date, DatagramChannel datagramChannel, InetSocketAddress address, String author){
        BufferedReader fileReader;
        int errorBreaker = 0;
        try {
            fileReader = new BufferedReader(new FileReader(this.regex));

            String input = fileReader.readLine();

            while (input != null && input != "exit") {
                switch (input){
                    case "exit":
                        break;
                    case "help":
                        Help help = new Help();
                        help.tell();
                        Transfer helpTransfer = new Transfer();
                        helpTransfer.setCommand(input);

                        ObjectSender helpSender = new ObjectSender(helpTransfer);
                        helpSender.send(datagramChannel, address);
                        break;
                    case "info":
                        Transfer infoTransfer = new Transfer();
                        infoTransfer.setCommand(input);

                        ObjectSender infoSender = new ObjectSender(infoTransfer);
                        infoSender.send(datagramChannel, address);

                        ByteBuffer infoReceiverBuffer = ByteBuffer.allocate(65536);
                        datagramChannel.receive(infoReceiverBuffer);
                        ObjectReceiver infoReceiver = new ObjectReceiver(infoReceiverBuffer);


                        String infoIn = infoReceiver.unpack().getString();

                        System.out.println(infoIn);
                        break;
                    case "show":
                        Transfer showTransfer = new Transfer();
                        showTransfer.setCommand(input);

                        ObjectSender showSender = new ObjectSender(showTransfer);
                        showSender.send(datagramChannel, address);

                        ByteBuffer showNumberReceiverBuffer = ByteBuffer.allocate(65536);
                        datagramChannel.receive(showNumberReceiverBuffer);
                        ObjectReceiver showNumberReceiver = new ObjectReceiver(showNumberReceiverBuffer);
                        Long[] showTempArr = new Long[showNumberReceiver.unpack().getNumber()];

                        Arrays.stream(showTempArr).forEach(e->{
                            try {
                                ByteBuffer showReceiverBuffer = ByteBuffer.allocate(65536);
                                datagramChannel.receive(showReceiverBuffer);
                                ByteArrayInputStream showByteArrayIS = new ByteArrayInputStream(showReceiverBuffer.array());
                                ObjectInputStream showObjectIS = new ObjectInputStream(showByteArrayIS);

                                SpaceMarine showSpaceMarine = (SpaceMarine) showObjectIS.readObject();
                                showSpaceMarine.Test();

                                showObjectIS.close();
                                showByteArrayIS.close();
                            } catch (IOException | ClassNotFoundException ex) {
                                ex.printStackTrace();
                            }
                        });
                        break;
                    case "history":

                        Transfer historyTransfer = new Transfer();
                        historyTransfer.setCommand(input);

                        ObjectSender historySender = new ObjectSender(historyTransfer);
                        historySender.send(datagramChannel, address);

                        ByteBuffer historyReceiverBuffer = ByteBuffer.allocate(1024);
                        datagramChannel.receive(historyReceiverBuffer);
                        historyReceiverBuffer.flip();
                        int historyInLimits = historyReceiverBuffer.limit();
                        byte[] historyInBytes = new byte[historyInLimits];
                        historyReceiverBuffer.get(historyInBytes, 0, historyInLimits);
                        String historyIn = new String(historyInBytes);

                        System.out.println(historyIn);
                        System.out.println("");

                        break;
                    case "filter_starts_with_name":
                        String filterStartsWithPattern = fileReader.readLine();

                        Transfer filterStartsWithTransfer = new Transfer();
                        filterStartsWithTransfer.setCommand(input);
                        filterStartsWithTransfer.setString(filterStartsWithPattern);

                        ObjectSender filterStartsWithSender = new ObjectSender(filterStartsWithTransfer);
                        filterStartsWithSender.send(datagramChannel, address);

                        ByteBuffer filterStartsWithNumberBuffer = ByteBuffer.allocate(65536);
                        datagramChannel.receive(filterStartsWithNumberBuffer);
                        ObjectReceiver filterStartsWithNumberReceiver = new ObjectReceiver(filterStartsWithNumberBuffer);
                        Long[] filterStartsWithTempArr = new Long[filterStartsWithNumberReceiver.unpack().getNumber()];

                        Arrays.stream(filterStartsWithTempArr).forEach(e->{
                            try {
                                ByteBuffer filterStartsWithReceiverBuffer = ByteBuffer.allocate(65536);
                                datagramChannel.receive(filterStartsWithReceiverBuffer);

                                ObjectReceiver filterStartsWithReceiver = new ObjectReceiver(filterStartsWithReceiverBuffer);
                                System.out.println(filterStartsWithReceiver.unpack().getString());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });

                        break;
                    case "clear":
                        Transfer clearTransfer = new Transfer();
                        clearTransfer.setCommand(input);

                        ObjectSender clearSender = new ObjectSender(clearTransfer);
                        clearSender.send(datagramChannel, address);
                        break;
                    case "remove_by_id":
                        Transfer removeByIDTransfer = new Transfer();
                        removeByIDTransfer.setCommand(input);
                        removeByIDTransfer.setID(Long.parseLong(fileReader.readLine()));

                        ObjectSender removeByIDSender = new ObjectSender(removeByIDTransfer);
                        removeByIDSender.send(datagramChannel, address);
                        break;
                    case "add":
                        Transfer IDClaimTransfer = new Transfer();
                        IDClaimTransfer.setCommand("IDClaim");

                        ObjectSender IDClaimSender = new ObjectSender(IDClaimTransfer);
                        IDClaimSender.send(datagramChannel, address);

                        ByteBuffer IDClaimReceiverBuffer = ByteBuffer.allocate(65536);
                        datagramChannel.receive(IDClaimReceiverBuffer);
                        ObjectReceiver IDClaimReceiver = new ObjectReceiver(IDClaimReceiverBuffer);

                        long tempID = IDClaimReceiver.unpack().getID();

                        String nameadd = fileReader.readLine();
                        String xadd = fileReader.readLine();
                        String yadd = fileReader.readLine();
                        String hpadd = fileReader.readLine();
                        String astadd = fileReader.readLine();
                        String weapadd = fileReader.readLine();
                        String meleadd = fileReader.readLine();
                        String chapadd = fileReader.readLine();
                        String worldadd = fileReader.readLine();

                        Ex_add ex_add = new Ex_add(tempID,nameadd,xadd,yadd,date,hpadd,astadd,weapadd,meleadd,chapadd,worldadd, author);

                        if (ex_add.getBreaker() == 0){
                            System.out.println(ex_add.adding().getTest());

                            Transfer addTransfer = new Transfer();
                            addTransfer.setCommand(input);
                            addTransfer.setSpaceMarine(ex_add.adding());

                            ObjectSender addSender = new ObjectSender(addTransfer);
                            addSender.send(datagramChannel, address);

                            System.out.println("Object successfully added to collection");
                        }else {
                            errorBreaker = 1;
                        }
                        break;
                    case "update":
                        Transfer IDCheckTransfer = new Transfer();
                        IDCheckTransfer.setCommand("IDCheck");
                        String updateIn = fileReader.readLine();
                        IDCheckTransfer.setID(Long.parseLong(updateIn));
                        ObjectSender IDCheckSender = new ObjectSender(IDCheckTransfer);
                        IDCheckSender.send(datagramChannel, address);

                        ByteBuffer IDCheckReceiverBuffer = ByteBuffer.allocate(65536);
                        datagramChannel.receive(IDCheckReceiverBuffer);
                        ObjectReceiver IDCheckReceiver = new ObjectReceiver(IDCheckReceiverBuffer);

                        if (IDCheckReceiver.unpack().getCase()){
                            String upname = fileReader.readLine();
                            String upx = fileReader.readLine();
                            String upy = fileReader.readLine();
                            String uphp = fileReader.readLine();
                            String upast = fileReader.readLine();
                            String upweap = fileReader.readLine();
                            String upmele = fileReader.readLine();
                            String upchap = fileReader.readLine();
                            String upworld = fileReader.readLine();

                            Ex_add updateAdd = new Ex_add(Long.parseLong(updateIn), upname,upx,upy,date,uphp,upast,upweap,upmele,upchap,upworld,author);

                            if (updateAdd.getBreaker() == 0){
                                Transfer updateTransfer = new Transfer();
                                updateTransfer.setCommand(input);
                                updateTransfer.setSpaceMarine(updateAdd.adding());
                                updateTransfer.setID(Long.parseLong(updateIn));

                                ObjectSender updateSender = new ObjectSender(updateTransfer);
                                updateSender.send(datagramChannel, address);
                                System.out.println("Object successfully updated");
                            }else {
                                errorBreaker = 1;
                            }
                        }else {
                            System.out.println("There is no object with such ID");
                            errorBreaker = 1;
                        }
                        break;
                    case "Filter_less_than_health":
                        System.out.println("Enter health level");
                        Transfer filterLessThanHealthTransfer = new Transfer();
                        filterLessThanHealthTransfer.setCommand(input);
                        try {
                            filterLessThanHealthTransfer.setNumber(Integer.parseInt(fileReader.readLine()));
                        } catch (IllegalArgumentException e){
                            errorBreaker = 1;
                            break;
                        }

                        ObjectSender filterLessThanHealthSender = new ObjectSender(filterLessThanHealthTransfer);
                        filterLessThanHealthSender.send(datagramChannel, address);

                        ByteBuffer filterLessThanHealthNumberBuffer = ByteBuffer.allocate(65536);
                        datagramChannel.receive(filterLessThanHealthNumberBuffer);
                        ObjectReceiver filterLessThanHealthNumberReceiver = new ObjectReceiver(filterLessThanHealthNumberBuffer);
                        Long[] filterLessThanHealthTempArr = new Long[filterLessThanHealthNumberReceiver.unpack().getNumber()];

                        Arrays.stream(filterLessThanHealthTempArr).forEach(e->{
                            try {
                                ByteBuffer filterLessThanHealthReceiverBuffer = ByteBuffer.allocate(65536);
                                datagramChannel.receive(filterLessThanHealthReceiverBuffer);

                                ObjectReceiver filterLessThanHealthReceiver = new ObjectReceiver(filterLessThanHealthReceiverBuffer);
                                System.out.println(filterLessThanHealthReceiver.unpack().getString());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });
                        break;
                    case "Filter_greater_than_chapter":
                        Transfer filterGreaterThanChapterTransfer = new Transfer();
                        filterGreaterThanChapterTransfer.setCommand(input);

                        try {
                            filterGreaterThanChapterTransfer.setNumber(Integer.parseInt(fileReader.readLine()));
                        } catch (IllegalArgumentException e){
                            errorBreaker = 1;
                            break;
                        }

                        ObjectSender filterGreaterThanChapterSender = new ObjectSender(filterGreaterThanChapterTransfer);
                        filterGreaterThanChapterSender.send(datagramChannel, address);

                        ByteBuffer filterGreaterThanChapterNumberBuffer = ByteBuffer.allocate(65536);
                        datagramChannel.receive(filterGreaterThanChapterNumberBuffer);
                        ObjectReceiver filterGreaterThanChapterNumberReceiver = new ObjectReceiver(filterGreaterThanChapterNumberBuffer);
                        Long[] filterGreaterThanChapterTempArr = new Long[filterGreaterThanChapterNumberReceiver.unpack().getNumber()];

                        Arrays.stream(filterGreaterThanChapterTempArr).forEach(e->{
                            try {
                                ByteBuffer filterGreaterThanChapterReceiverBuffer = ByteBuffer.allocate(65536);
                                datagramChannel.receive(filterGreaterThanChapterReceiverBuffer);

                                ObjectReceiver filterGreaterThanChapterReceiver = new ObjectReceiver(filterGreaterThanChapterReceiverBuffer);
                                System.out.println(filterGreaterThanChapterReceiver.unpack().getString());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });
                        break;
                    case "add_if_max":
                        Transfer addIfMaxIDClaimTransfer = new Transfer();
                        addIfMaxIDClaimTransfer.setCommand("IDClaim");

                        ObjectSender addIfMaxIDClaimSender = new ObjectSender(addIfMaxIDClaimTransfer);
                        addIfMaxIDClaimSender.send(datagramChannel, address);

                        ByteBuffer addIfMaxIDClaimReceiverBuffer = ByteBuffer.allocate(65536);
                        datagramChannel.receive(addIfMaxIDClaimReceiverBuffer);
                        ObjectReceiver addIfMaxIDClaimReceiver = new ObjectReceiver(addIfMaxIDClaimReceiverBuffer);

                        long addIfMaxTempID = addIfMaxIDClaimReceiver.unpack().getID();

                        String nameaddm = fileReader.readLine();
                        String xaddm = fileReader.readLine();
                        String yaddm = fileReader.readLine();
                        String hpaddm = fileReader.readLine();
                        String astaddm = fileReader.readLine();
                        String weapaddm = fileReader.readLine();
                        String meleaddm = fileReader.readLine();
                        String chapaddm = fileReader.readLine();
                        String worldaddm = fileReader.readLine();

                        Ex_add addIfMaxAddIn = new Ex_add(addIfMaxTempID,nameaddm,xaddm,yaddm,date,hpaddm,astaddm,weapaddm,meleaddm,chapaddm,worldaddm,author);

                        if (addIfMaxAddIn.getBreaker() == 0){
                            Transfer addIfMaxTransfer = new Transfer();
                            addIfMaxTransfer.setCommand(input);
                            addIfMaxTransfer.setSpaceMarine(addIfMaxAddIn.adding());

                            ObjectSender addIfMaxSender = new ObjectSender(addIfMaxTransfer);
                            addIfMaxSender.send(datagramChannel, address);

                            ByteBuffer addIfMaxCaseBuffer = ByteBuffer.allocate(65536);
                            datagramChannel.receive(addIfMaxCaseBuffer);
                            ObjectReceiver addIfMaxCaseReceiver = new ObjectReceiver(addIfMaxCaseBuffer);
                            boolean addIfMaxCase = addIfMaxCaseReceiver.unpack().getCase();

                            if (addIfMaxCase){
                                System.out.println("Object successfully added");
                            } else {
                                System.out.println("Object is too small");
                            }
                        }else {
                            errorBreaker = 1;
                        }

                        break;
                    case "remove_greater":
                        Transfer removeGreaterIDClaimTransfer = new Transfer();
                        removeGreaterIDClaimTransfer.setCommand("IDClaim");

                        ObjectSender removeGreaterIDClaimSender = new ObjectSender(removeGreaterIDClaimTransfer);
                        removeGreaterIDClaimSender.send(datagramChannel, address);

                        ByteBuffer removeGreaterIDClaimReceiverBuffer = ByteBuffer.allocate(65536);
                        datagramChannel.receive(removeGreaterIDClaimReceiverBuffer);
                        ObjectReceiver removeGreaterIDClaimReceiver = new ObjectReceiver(removeGreaterIDClaimReceiverBuffer);

                        long removeGreaterTempID = removeGreaterIDClaimReceiver.unpack().getID();

                        String namerem = fileReader.readLine();
                        String xrem = fileReader.readLine();
                        String yrem = fileReader.readLine();
                        String hprem = fileReader.readLine();
                        String astrem = fileReader.readLine();
                        String weaprem = fileReader.readLine();
                        String melerem = fileReader.readLine();
                        String chaprem = fileReader.readLine();
                        String worldrem = fileReader.readLine();

                        Ex_add ex_rem = new Ex_add(removeGreaterTempID,namerem,xrem,yrem,date,hprem,astrem,weaprem,melerem,chaprem,worldrem,author);

                        if (ex_rem.getBreaker() == 0){
                            Transfer removeGreaterTransfer = new Transfer();
                            removeGreaterTransfer.setCommand(input);
                            removeGreaterTransfer.setSpaceMarine(ex_rem.adding());

                            ObjectSender removeGreaterSender = new ObjectSender(removeGreaterTransfer);
                            removeGreaterSender.send(datagramChannel, address);
                        }else {
                            errorBreaker = 1;
                        }
                        break;
                    default:
                        errorBreaker = 1;
                }
                if (errorBreaker == 1){
                    break;
                }
                input = fileReader.readLine();
            }

        }catch (IOException e){
            System.out.println("There is no such file");
        }
        if (errorBreaker == 1){
            System.out.println("There is an error in file");
        }
    }
}
