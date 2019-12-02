package bittorrent.util;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Util {
    public static int splitFile(File file, String rootDir, int chunkSize) {
        ArrayList<FileChunk> chunks = new ArrayList<FileChunk>();
        try {
            chunks = getFileChunks(new RandomAccessFile(file, "r"), file.getName(), chunkSize);
            FileChunk[] chunkArray = chunks.toArray(new FileChunk[chunks.size()]);
            writeFileChunksToFiles(rootDir, chunkArray);
        } catch (IOException | ChordException e) {
            e.printStackTrace();
        }
        return chunks.size();
    }

   public static ArrayList<FileChunk> getFileChunks(RandomAccessFile raf, String fileName, int chunkSize) throws IOException, ChordException {

        long numChunks = raf.length() / chunkSize;
        if (raf.length() % chunkSize != 0) {
            numChunks++;
        }

        ArrayList<FileChunk> output = new ArrayList<FileChunk>();

        for (int i = 1; i < numChunks + 1; i++) {

            byte[] bytes = new byte[chunkSize];
            int numBytes = raf.read(bytes);

            if (numBytes == -1) {
                throw new ChordException(
                        "getFileChunks: failed to read bytes from file to fileChunk");
            }

            FileChunk fc = new FileChunk(i, numChunks, bytes, fileName);

            output.add(fc);
        }
        return output;
    }
    public static void writeFileChunksToFiles(String rootDir, FileChunk[] chunks)
            throws IOException {
        for (FileChunk chunk : chunks) {
            if (chunk == null) {
                continue;
            }
            FileOutputStream fos = new FileOutputStream(rootDir + "/" + chunk.getChunkFilename());
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(chunk);

            oos.close();
            bos.close();
            fos.close();
        }
    }

    public static File rebuildFileFromFileChunks(FileChunk[] chunks, String newFileName, String rootDir) throws IOException, ChordException {
        return rebuildFileFromFileChunks(new ArrayList<FileChunk>(Arrays.asList(chunks)), newFileName, rootDir);
    }


    public static File rebuildFileFromFileChunks(ArrayList<FileChunk> chunks, String newFileName, String rootDir)
            throws IOException, ChordException {
        if (chunks.isEmpty()) {
            return null;
        }

        int numChunks = chunks.size();

        HashMap<Long, FileChunk> orderedChunks = new HashMap<Long, FileChunk>();

        for (FileChunk chunk : chunks) {
            orderedChunks.put(chunk.getNum(), chunk);
        }

        if (orderedChunks.size() != numChunks) {
            throw new ChordException("rebuildFileFromChunks: error, complete set of FileChunks not provided");
        }

        File f = (newFileName == null ? new File(rootDir + "/" + orderedChunks.get(0).getFileName()) : new File(rootDir + "/" + newFileName));
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));

        for (FileChunk chunk : orderedChunks.values()) {
            bos.write(chunk.getBytes());
        }

        bos.close();

        return f;
    }

    public static void pressAnyKeyToContinue() {
        System.out.println("Press any key to continue...");
        try {
            System.in.read();
        } catch (Exception e) {
        }
    }

    public static void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
    }

}