package com.wuxin.bio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

/**
 *服务端处理
 */
public class BioTimeServerHandler implements  Runnable{
    private Socket socket;

    public BioTimeServerHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        try {
            in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            String currentTime = null;
            String body = null;
            while (true) {
                body = in.readLine();
                if (body == null) break;
                System.out.println("The time server receiver order:"+ body);
                currentTime = "QUERY TIME ORDER".equalsIgnoreCase(body)? new Date().toString():"BAD ORDER";
                System.out.println(currentTime);
            }
        } catch (Exception e) {
            if (in != null) {
               try {
                   in.close();
               } catch (IOException el) {
                   el.printStackTrace();
               }
            }
            if (out != null) {
                out.close();
                out = null;
            }
            if (this.socket != null) {
                try {
                    this.socket.close();
                } catch (IOException el) {
                    el.printStackTrace();
                }
                this.socket = null;
            }
        }
    }
}
