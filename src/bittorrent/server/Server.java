package bittorrent.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import bittorrent.util.ChordException;
import bittorrent.util.Util;

public class Server extends Thread{

    private static String filepath;
    private static int S_PORT;
    private static final String ROOT_SPLIT_DIR = "server_file\\splits";
    private static final int CHUNK_SIZE = 10 * 1024;
    private static final int NUM_PEERS = 3;

    private static int numChunks = -1;
    private static int numChunksSent = 0;

    static synchronized int incNumChunksSent(){
        return numChunksSent++;
    }

    static synchronized int getNumChunksSent(){
        return numChunksSent;
    }

    public Server(String filepath, int port_num) {
        this.filepath = filepath;
        this.S_PORT = port_num;
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