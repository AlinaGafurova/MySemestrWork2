import java.io.*;
import java.net.*;
import java.util.*;

class WebServer {
    public static void main(String args[]) throws Exception {
        String requestMessageLine;
        String fileName;


        int myPort = 5678;
        if (args.length > 0) {
            try {
                myPort = Integer.parseInt(args[0]);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println("Need port number as argument");
                System.exit(-1);
            } catch (NumberFormatException e) {
                System.out.println("Please give port number as integer");
                System.exit(-1);
            }
        }

        ServerSocket listenSocket = new ServerSocket(myPort);

        System.out.println("Web server waiting for request on port " + myPort);
        Socket connectionSocket = listenSocket.accept();

        BufferedReader inFromClient = new BufferedReader(
                new InputStreamReader(connectionSocket.getInputStream()));
        DataOutputStream outToClient = new DataOutputStream(
                connectionSocket.getOutputStream());

        requestMessageLine = inFromClient.readLine();
        System.out.println("Request: " + requestMessageLine);
        StringTokenizer tokenizedLine = new StringTokenizer(requestMessageLine);

        if (tokenizedLine.nextToken().equals("GET")) {
            fileName = tokenizedLine.nextToken();

            if (fileName.startsWith("/") == true)
                fileName = fileName.substring(1);

            File file = new File(fileName);

            int numOfBytes = (int) file.length();

            if (!fileName.equals("")&& fileName!=null) {
                FileInputStream inFile = new FileInputStream(fileName);
                byte[] fileInBytes = new byte[numOfBytes];
                inFile.read(fileInBytes);

                outToClient.writeBytes("HTTP/1.0 200 Document Follows\r\n");
                if (fileName.endsWith(".jpg"))
                    outToClient.writeBytes("Content-Type: image/jpeg\r\n");
                if (fileName.endsWith(".gif"))
                    outToClient.writeBytes("Content-Type: image/gif\r\n");
                if (fileName.endsWith(".png"))
                    outToClient.writeBytes("Content-Type: image/png\r\n");
                outToClient.writeBytes("Content-Length: " + numOfBytes + "\r\n");
                outToClient.writeBytes("\r\n");
                outToClient.write(fileInBytes, 0, numOfBytes);

                requestMessageLine = inFromClient.readLine();
                while (requestMessageLine.length() >= 5) {
                    System.out.println("Request: " + requestMessageLine);
                    requestMessageLine = inFromClient.readLine();
                }
                System.out.println("Request: " + requestMessageLine);

                connectionSocket.close();
            }
            else
            {
                while (requestMessageLine.length() >= 5) {
                    System.out.println("Request: " + requestMessageLine);
                    requestMessageLine = inFromClient.readLine();
                }

                outToClient.writeBytes("HTTP/1.0 200 Document Follows\r\n");
                if (fileName.endsWith(".jpg"))
                    outToClient.writeBytes("Content-Type: image/jpeg\r\n");
                if (fileName.endsWith(".gif"))
                    outToClient.writeBytes("Content-Type: image/gif\r\n");
                if (fileName.endsWith(".png"))
                    outToClient.writeBytes("Content-Type: image/png\r\n");
                outToClient.writeBytes("\r\n");
                outToClient.writeBytes("<html><head><title>Test</title></head><body>");
               for (int i=1;i<10;i++)
               {
                   outToClient.writeBytes("Test<br>\r\n");
                   Thread.sleep(100);
                }
                outToClient.writeBytes("</body></html>");

                connectionSocket.close();
            }
        } else {
            System.out.println("Bad Request Message");
        }

    }
}

      
          