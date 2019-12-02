package bittorrent.server;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import bittorrent.util.ChordException;

public class FileDistributer extends Thread {

    private Socket connection;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private int clientNum;
    private int numChunks;
    private int numPeers;
    private String rootSplitDir;


    public FileDistributer(Socket connection, int no, int numChunks, int numPeers, String rootSplitDir) {
        this.connection = connection;
        this.clientNum = no;
        this.numChunks = numChunks;
        this.numPeers = numPeers;
        this.rootSplitDir = rootSplitDir;
    }

    public void run() {
        try {
            out = new ObjectOutputStream(connection.getOutputStream());
            out.flush();
            in = new ObjectInputStream(connection.getInputStream());

            File[] chunkFiles = new File(rootSplitDir).listFiles();

            try {
                while (true) {
                    int numChunksPerPeer = (int)Math.ceil((double)numChunks/(double)numPeers);

                    if(Server.getNumChunksSent() + numChunksPerPeer > numChunks){
                        numChunksPerPeer -= Server.getNumChunksSent() + numChunksPerPeer - numChunks;
                    }

                    sendMessage(numChunksPerPeer);

                    for(int i = 0; i < numChunksPerPeer; i++){
                        int idx = Server.incNumChunksSent();
                        if (idx < chunkFiles.length) {
                            sendMessage(chunkFiles[idx]);
                        } else {
                            sendMessage(chunkFiles[idx - 1]);
                        }
                    }

                    String response = (String) in.readObject();
                    if (response.equals("true")) {
                        break;
                    }
                    else{
                        throw new ChordException("FileDistributer: Peer is not satisfied with chunks it received");
                    }
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Data received in unknown format");
            } catch (ChordException e) {
                e.printStackTrace();
            }

        } catch (IOException ioException) {
            System.out.println("Disconnect with Client " + clientNum);
        } finally {
            try {
                in.close();
                out.close();
                connection.close();
                System.out.println("Disconnect with Client " + clientNum);
            } catch (IOException e) {
                System.out.println("Disconnect with Client " + clientNum);
            }
        }
    }

    public void sendMessage(Object msg) {
        try {
            out.writeObject(msg);
            out.flush();
            System.out.println("Send message: " + msg + " to Client "
                    + clientNum);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}