package IO;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class FileUtility {
    public void sendFile(Socket socket, File file) throws IOException {
        InputStream is = new FileInputStream (file);
        long size = file.length ();
        int count = (int) (size/8192) /10, readBuckets = 0;
        try (DataOutputStream os = new DataOutputStream (socket.getOutputStream ())){
            byte [] buffer = new byte[8192];
            while(is.available ()>0){
                int readBytes = is.read(buffer);
                readBuckets++;
                if (readBuckets % count ==0){
                    System.out.println ("=");
                }
            }
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

}
