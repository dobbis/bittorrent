package bittorrent.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import bittorrent.Main;
import bittorrent.util.ChordException;
import bittorrent.util.Util;
import bittorrent.client.Peer;

public class Server extends Thread{

    private static String filepath;
    private static int S_PORT;
    private static final String ROOT_SPLIT_DIR = "server_file/splits";
    private static final int CHUNK_SIZE = 10 * 1024;
    private static final int NUM_PEERS = 3;

    private static int numChunks = -1;
    private static int numChunksSent = 0;

    private static Peer client1;
    private static Peer client2;
    private static Peer client3;

    static synchronized int incNumChunksSent(){
        return numChunksSent++;
    }

    static synchronized int getNumChunksSent(){
        return numChunksSent;
    }

    public Server(String filepath, int port_num, Peer client1, Peer client2, Peer client3) {
        this.filepath = filepath;
        this.S_PORT = port_num;
        this.client1 = client1;
        this.client2 = client2;
        this.client3 = client3;
    }
    public void run() {
        System.out.println("The FileOwner is running.");

        File inputFile = null;

        inputFile = new File(filepath);
        Util.deleteFolder(new File(ROOT_SPLIT_DIR));

        numChunks = Util.splitFile(inputFile, ROOT_SPLIT_DIR, CHUNK_SIZE);

        try {
            if(numChunks < 3){
                throw new ChordException("File must be large enough to split into at least 3, 10KB chunks");
            }

            System.out.println("File Size: " + inputFile.length() + " bytes");
            System.out.println("Split file into " + numChunks  + " parts");
            System.out.println("Writing files into: " + ROOT_SPLIT_DIR);

            ServerSocket listener = new ServerSocket(S_PORT);
            System.out.println("Listening on port: " + S_PORT);

            Thread thread_client1 = new Thread(client1);
            Thread thread_client2 = new Thread(client2);
            Thread thread_client3 = new Thread(client3);
    
            thread_client1.start();
            thread_client2.start();
            thread_client3.start();

            int peerNum = 0;
            try {
                while (true) {
                    new FileDistributer(listener.accept(), peerNum, numChunks, NUM_PEERS, ROOT_SPLIT_DIR).start();
                    System.out.println("Client " + peerNum + " is connected!");
                    peerNum++;

                    if(peerNum == NUM_PEERS){
                        break;
                    }
                }
            } finally {
                listener.close();
            }
        } catch (ChordException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}