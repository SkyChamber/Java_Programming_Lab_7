package serverStuff;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.RecursiveTask;

public class ObjectSender extends RecursiveTask<String> {
    public ObjectSender(Transfer transfer) {
        this.transfer = transfer;
    }

    public ObjectSender(Transfer transfer, DatagramChannel datagramChannel, InetSocketAddress address){
        this.transfer = transfer;
        this.channel = datagramChannel;
        this.address = address;
    }

    private Transfer transfer;
    private DatagramChannel channel;
    private InetSocketAddress address;

    public void send(DatagramChannel datagramChannel, InetSocketAddress address){
        try {
            ByteBuffer buffer = ByteBuffer.allocate(65536);
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            ObjectOutputStream objectOS = new ObjectOutputStream(byteArrayOS);

            objectOS.writeObject(this.transfer);
            objectOS.flush();

            buffer.put(byteArrayOS.toByteArray());
            buffer.flip();
            datagramChannel.send(buffer, address);

            byteArrayOS.close();
            objectOS.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected String compute() {
        try {
            ByteBuffer buffer = ByteBuffer.allocate(65536);
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
            ObjectOutputStream objectOS = new ObjectOutputStream(byteArrayOS);

            objectOS.writeObject(this.transfer);
            objectOS.flush();

            buffer.put(byteArrayOS.toByteArray());
            buffer.flip();
            this.channel.send(buffer, this.address);

            byteArrayOS.close();
            objectOS.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
