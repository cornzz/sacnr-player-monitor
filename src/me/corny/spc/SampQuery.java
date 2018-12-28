/**
 * @author Edward McKnight (EM-Creations.co.uk)
 */

/* *****************************************************************
// SampQuery.java
// Version 1.1.1
// This class connects to a specific SA-MP server via sockets.
// Copyright 2012 Edward McKnight (EM-Creations.co.uk)
// Creative Commons Attribution-NoDerivs 3.0 Unported License
* *****************************************************************/

package me.corny.spc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.StringTokenizer;

public class SampQuery {
    private DatagramSocket socket = null;
    private InetAddress server = null;
    private String serverString = "";
    private int port = 0;
    private PrintWriter out = null;
    private BufferedReader in = null;

    /**
     * Creates a new SampQuery object.
     * @param server hostname of the server
     * @param port port of the server
     */
    public SampQuery(String server, int port) throws Exception {
        // Constructor
        this.serverString = server;
        this.server = InetAddress.getByName(this.serverString);

        socket = new DatagramSocket(); // DatagramSocket for UDP connections
        socket.setSoTimeout(2000); // Set timeout to 2 seconds

        this.port = port;
    }

    /**
     * Returns a multidimensional String array of basic player information.
     * @return String[][]:<br />
     *   String[][0]:<br />
     *       players[0] = playername<br />
     *       players[1] = score<br />
     * @see
     */
    public String[][] getBasicPlayers() { // Finished
        DatagramPacket packet = this.assemblePacket("c");
        this.send(packet);
        byte[] reply = this.receiveBytes();
        ByteBuffer buff = ByteBuffer.wrap(reply);
        buff.order(ByteOrder.LITTLE_ENDIAN);
        buff.position(11);

        short playerCount = buff.getShort();
        String[][] players = new String[playerCount][2];

        for (int i = 0; players.length > i; i++) {
            byte len = buff.get();
            byte[] nameBA = new byte[len];

            for (int j = 0; len > j; j++) {
                nameBA[j] = buff.get();
            }
            String name = new String(nameBA);
            int score = buff.getInt();

            players[i][0] = name;
            players[i][1] = "" + score;
        }
        return players;
    }

    /**
     * Returns whether a successful connection was made.
     * @return boolean
     */
    public boolean connect() {
        DatagramPacket packet = this.assemblePacket("p0101");
        this.send(packet);
        String reply = this.receive();

        try {
            // Clean up reply
            reply = reply.substring(10);
            reply = reply.trim();

            if (reply.equals("p0101")) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Closes the connection.
     */
    public void close() {
        socket.close();
    }

    private DatagramPacket assemblePacket(String type) {
        DatagramPacket sendPacket = null;
        try {
            StringTokenizer tok = new StringTokenizer(this.server.getHostAddress(), ".");

            String packetData = "SAMP";

            while (tok.hasMoreTokens()) {
                packetData += (char) (Integer.parseInt(tok.nextToken()));
            }

            packetData += (char) (this.port & 0xFF);
            packetData += (char) (this.port >> 8 & 0xFF);
            packetData += type;

            byte[] data = packetData.getBytes("US-ASCII");

            sendPacket = new DatagramPacket(data, data.length, this.server, this.port);

        } catch (Exception e) {
            System.out.println(e);
        }
        return sendPacket;
    }

    private void send(DatagramPacket packet) {
        try {
            socket.send(packet);
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    private String receive() {
        String modifiedSentence = null;
        byte[] receivedData = new byte[1024];
        try {
            DatagramPacket receivedPacket = new DatagramPacket(receivedData, receivedData.length);
            socket.receive(receivedPacket);
            modifiedSentence = new String(receivedPacket.getData());
        } catch (IOException e) {
            System.out.println(e);
        }
        return modifiedSentence;
    }

    private byte[] receiveBytes() {
        byte[] receivedData = new byte[3072];
        DatagramPacket receivedPacket = null;
        try {
            receivedPacket = new DatagramPacket(receivedData, receivedData.length);
            socket.receive(receivedPacket);
        } catch (IOException e) {
            System.out.println(e);
        }
        return receivedPacket.getData();
    }
}