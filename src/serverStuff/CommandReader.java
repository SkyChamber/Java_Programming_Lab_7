package serverStuff;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class CommandReader implements Runnable{
    public CommandReader(DatagramChannel datagramChannel, Connection connection, ReentrantLock lock){
        this.channel = datagramChannel;
        this.connection = connection;
        this.locker = lock;
    }

    DatagramChannel channel;
    Connection connection;
    ReentrantLock locker;

    @Override
    public void run() {
        try {
            ByteBuffer commandReceiverBuffer = ByteBuffer.allocate(65536);
            SocketAddress remoteAddress = channel.receive(commandReceiverBuffer);
            commandReceiverBuffer.flip();

            String entry;
            Transfer transfer;

            if (commandReceiverBuffer.limit() == 0){
                entry = "nothing";
                transfer = new Transfer();
            } else {
                ObjectReceiver objectReceiver = new ObjectReceiver(commandReceiverBuffer);
                transfer = objectReceiver.unpack();

                entry = transfer.getCommand();

                CommandExecutor commandExecutor = new CommandExecutor(entry,transfer,channel, (InetSocketAddress) remoteAddress, locker, connection);
                Thread executeThread = new Thread(commandExecutor);
                executeThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
