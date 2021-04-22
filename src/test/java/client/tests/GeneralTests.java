package client.tests;

import client.model.ClientApp;
import client.model.Instruction;
import client.model.utils.BufferedWriterWrapper;
import lombok.extern.slf4j.Slf4j;
import static org.junit.Assert.*;
import org.junit.Test;

import java.io.*;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

@Slf4j
public class GeneralTests {

    @Test
    public void testConnect(){
        try {
            Socket socket = new Socket(InetAddress.getByName(ClientApp.getIp()), 4004);
            BufferedWriterWrapper out = new BufferedWriterWrapper(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), ClientApp.getDefaultCharset())));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), ClientApp.getDefaultCharset()));
            out.write(Instruction.CONNECT.getCommand());
            String response = in.readLine();
            response = BufferedWriterWrapper.decrypt(response);
            assertEquals(response,Instruction.CONNECT_OK.getCommand());
        } catch (UnknownHostException e) {
            log.error("Unknown host!!!");
            e.printStackTrace();
        }catch (ConnectException e) {
            log.info("Connect failed!");
            e.printStackTrace();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
