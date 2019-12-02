package bittorrent;

import bittorrent.client.Peer;
import bittorrent.server.Server;

import java.io.*;

public class Main{
    public static void main(String[] args) {
        
        String IPaddress_client1 = null;
        String IPaddress_client2 = null;
        String IPaddress_client3 = null;
        String filepath = null;

        int PORTNUM_client1 = 0;
        int PORTNUM_client2 = 0;
        int PORTNUM_client3 = 0;
        int PORTNUM_server = 0;

        BufferedReader reader = null;
        try {
            reader =
                new BufferedReader(new InputStreamReader(new FileInputStream(("configuration.txt"))));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            IPaddress_client1 = reader.readLine();
            PORTNUM_client1 = Integer.parseInt(reader.readLine());

            IPaddress_client2 = reader.readLine();
            PORTNUM_client2 = Integer.parseInt(reader.readLine());

            IPaddress_client3 = reader.readLine();
            PORTNUM_client3 = Integer.parseInt(reader.readLine());

            filepath = reader.readLine();
            PORTNUM_server = Integer.parseInt(reader.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Peer client1 = new Peer(IPaddress_client1, PORTNUM_server, PORTNUM_client1, PORTNUM_client2);
        Peer client2 = new Peer(IPaddress_client2, PORTNUM_server, PORTNUM_client2, PORTNUM_client3);
        Peer client3 = new Peer(IPaddress_client3, PORTNUM_server, PORTNUM_client3, PORTNUM_client1);

        Server server = new Server(filepath, PORTNUM_server, client1, client2, client3);

        Thread thread_server = new Thread(server);

        thread_server.start();

    }
}
