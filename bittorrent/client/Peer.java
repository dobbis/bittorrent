package bittorrent.client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;

import bittorrent.util.ChordException;
import bittorrent.util.FileChunk;
import bittorrent.util.Util;

public class Peer extends Thread{
    private final String CLIENT_ROOT_DIR = "client_file";

    private Socket fileOwnerSocket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private int fileOwnerPort = -1;
    private int uploadPort = -1;
    private int downloadPort = -1;
    private String IPaddress;

    private FileChunk[] chunksIHave = null;

    private File chunksIHaveFile;
    private long numChunks = -1L;

    public Peer(String IPaddress, int fileOwnerPort, int listenPort, int neighborPort) {
        this.IPaddress = IPaddress;
        this.fileOwnerPort = fileOwnerPort;
        this.uploadPort = listenPort;
        this.downloadPort = neighborPort;
        chunksIHaveFile = new File(CLIENT_ROOT_DIR + "/" + uploadPort + "/" + uploadPort + "clientChunks.txt");
    }

    private void connectToFileOwner() {
        try {
            fileOwnerSocket = new Socket(IPaddress, fileOwnerPort);
            System.out.println("Connected to localhost in port "
                    + fileOwnerPort);
            out = new ObjectOutputStream(fileOwnerSocket.getOutputStream());
            out.flush();
            in = new ObjectInputStream(fileOwnerSocket.getInputStream());
        } catch (IOException e) {
            System.err.println("ERROR: unable to connect");
        }
    }

    private void disconnectFromFileOwner() {
        try {
            out.flush();
            out.close();
            in.close();
            fileOwnerSocket.close();
        } catch (IOException e) {
            System.err.println("ERROR: disconnected from fileOwner");
        }

    }

    private void writeChunksIHaveToFile() throws IOException {
        FileWriter fw = new FileWriter(chunksIHaveFile);
        for (FileChunk fc : chunksIHave) {
            if (fc != null) {
                fw.write(fc.getFileName() + " " + fc.getNum() + " "
                        + fc.getTotalNum() + "\n");
            }
        }
        fw.close();
    }

    private void createClientDirectory(){
        File file = new File(CLIENT_ROOT_DIR+"/"+uploadPort);
        if (!file.exists()) {
            file.mkdir();
        }
    }

    public void run() {
        try {

            createClientDirectory();

            connectToFileOwner();

            Object request = in.readObject();
            System.out.println("PEER:	Number of chunks being sent: " + request);

            for (int i = 0; i < (Integer) request; i++) {
                File f = (File) in.readObject();
                System.out.println("PEER:	Chunk received: " + f);
                ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
                FileChunk fc = (FileChunk) ois.readObject();
                if (chunksIHave == null) {
                    numChunks = fc.getTotalNum();
                    chunksIHave = new FileChunk[(int) numChunks];
                }
                chunksIHave[(int) fc.getNum() - 1] = fc;
            }

            Util.writeFileChunksToFiles(CLIENT_ROOT_DIR + "/" + uploadPort, chunksIHave);

            writeChunksIHaveToFile();

            sendMessage("true");

            disconnectFromFileOwner();

            PeerUploader uploader = new PeerUploader(uploadPort);
            uploader.start();

            PeerDownloader download = new PeerDownloader(IPaddress, downloadPort);
            download.start();


        } catch (ConnectException e) {
            System.err.println("Connection refused");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found");
        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                fileOwnerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    void sendMessage(String msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    synchronized ArrayList<Integer> getChunkNumsINeed(){
        ArrayList<Integer> output = new ArrayList<Integer>();
        for(int i = 0; i < chunksIHave.length; i++){
            if(chunksIHave[i] == null){
                output.add(i+1);
            }
        }
        return output;
    }

    synchronized int countNumChunksIHave() {
        int i = 0;
        for (FileChunk f : chunksIHave) {
            if (f != null) {
                i++;
            }
        }
        return i;
    }

    synchronized void addToChunksIHave(FileChunk f, int i){
        chunksIHave[i] = f;
    }

    void printChunksIHave(){
        for(FileChunk f : chunksIHave){
            if(f == null){
                System.out.print("X");
            }
            else{
                System.out.print(f.getNum());
            }
        }
        System.out.println();
    }

    public class PeerDownloader extends Thread {
        private ObjectInputStream in;
        private ObjectOutputStream out;
        private Socket connection;

        public PeerDownloader(String IPaddress, int downloadPort) {
            while(true){
                try {
                    connection = new Socket(IPaddress, downloadPort);
                    in = new ObjectInputStream(connection.getInputStream());
                    out = new ObjectOutputStream(connection.getOutputStream());
                    out.flush();
                    break;
                } catch (IOException e) {
                    System.err.println("DOWNLOAD:	Failed to connect to peer for download. Retry in 1 second");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            System.out.println("DOWNLOAD:	Connected to " + downloadPort);
        }

        void sendMessage(Object msg) {
            System.out.println("PeerDownloader send: " + msg);
            try {
                out.writeObject(msg);
                out.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        public void run() {
            System.out.println("DOWNLOAD:	START");

            try {

                while (countNumChunksIHave() != numChunks) {

                    ArrayList<Integer> chunksINeed = getChunkNumsINeed();

                    System.out.println("DOWNLOAD:	Requesting chunks " + chunksINeed.toString() + " from neighbor");

                    sendMessage(chunksINeed);

                    ArrayList<FileChunk> response = (ArrayList<FileChunk>) in.readObject();

                    if (response != null && response.size() != 0) {
                        for(FileChunk f : response){
                            System.out.println("DOWNLOAD:	Received chunk " + f + " from neighbor");
                            addToChunksIHave(f, (int)f.getNum()-1);

                            Util.writeFileChunksToFiles(CLIENT_ROOT_DIR + "/" + uploadPort, chunksIHave);
                        }
                        printChunksIHave();
                        writeChunksIHaveToFile();

                        Util.writeFileChunksToFiles(CLIENT_ROOT_DIR+"/"+uploadPort, chunksIHave);
                    }

                    Thread.sleep(1000);

                }

                System.out.println("Got all file chunks");
                System.out.println(chunksIHave.length);

                sendMessage(new ArrayList<Integer>());

                Util.rebuildFileFromFileChunks(chunksIHave, "Rebuild" + uploadPort + chunksIHave[0].getFileName(), CLIENT_ROOT_DIR+"/"+uploadPort);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch(IOException e){
                e.printStackTrace();
            } catch (ChordException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    in.close();
                    out.close();
                    connection.close();
                    System.out.println("Download socket gracefully closed");
                } catch (IOException e) {
                    System.out.println("Peer failed to close download socket");
                }
            }
        }
    }

    public class PeerUploader extends Thread {
        ObjectInputStream in;
        ObjectOutputStream out;
        private ServerSocket uploadingSocket;
        private Socket connection;

        public PeerUploader(int uploadPort) throws IOException {
            uploadingSocket = new ServerSocket(uploadPort);
        }

        void sendMessage(Object msg) {
            System.out.println("PeerUploader send: " + msg);
            try {
                out.writeObject(msg);
                out.flush();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

        public void run() {
            System.out.println("UPLOAD:	START");

            try {
                System.out.println("UPLOAD:	Listening for connections on " + uploadPort);
                connection = uploadingSocket.accept();
                out = new ObjectOutputStream(connection.getOutputStream());
                out.flush();
                in = new ObjectInputStream(connection.getInputStream());

                while (true) {
                    ArrayList<Integer> request = (ArrayList<Integer>) in.readObject();
                    System.out.println("UPLOAD:	Upload neighbor is requesting chunks " + request.toString());

                    if (request.size() == 0) {
                        System.out.println("UPLOAD:	Upload neighbor has all their chunks");
                        break;
                    } else {
                        ArrayList<FileChunk> sendingChunks = new ArrayList<FileChunk>();
                        for(Integer i : request){
                            if(chunksIHave[i-1] != null){
                                sendingChunks.add(chunksIHave[i-1]);
                            }
                        }

                        System.out.println("UPLOAD:	Sending chunks " + sendingChunks.toString());
                        sendMessage(sendingChunks);
                    }
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            } finally {
                try {
                    connection.close();
                    in.close();
                    out.close();
                    uploadingSocket.close();
                    System.out.println("Upload socket gracefully closed");
                } catch (IOException e) {
                    System.out.println("Peer failed to close upload socket");
                }
            }
        }
    }

}