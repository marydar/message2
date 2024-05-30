import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private String cUsername;
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.cUsername = bufferedReader.readLine();
            clientHandlers.add(this);
            broadcastMessage("SERVER: "+cUsername+"has entered the chat");
        }catch (IOException e){
            closeEverything(socket, bufferedWriter,bufferedReader);
        }
    }

    @Override
    public void run() {
        String message;
        while (socket.isConnected()) {
            try {
                message = bufferedReader.readLine();
                String[] messageParts = message.split(":");
                if(messageParts[1].equals(" pv")) {
                    sendpv(messageParts[2], messageParts[3]);
                }
                else {
                    broadcastMessage(message);
                }
            }catch (IOException e){
                closeEverything(socket, bufferedWriter,bufferedReader);
                break;
            }
        }
    }
    public void sendpv(String message, String username) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (clientHandler.cUsername.equals(username)) {
                    clientHandler.bufferedWriter.write("pv: " + cUsername+ ": "+message);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }catch (IOException e){
                closeEverything(socket, bufferedWriter,bufferedReader);
            }
        }
    }
    public void broadcastMessage(String message) {
        for (ClientHandler clientHandler : clientHandlers) {
            try {
                if (!clientHandler.cUsername.equals(cUsername)) {
                    clientHandler.bufferedWriter.write(message);
                    clientHandler.bufferedWriter.newLine();
                    clientHandler.bufferedWriter.flush();
                }
            }catch (IOException e){
                closeEverything(socket, bufferedWriter,bufferedReader);
            }
        }
    }
    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcastMessage("SERVER: "+cUsername+"has left the chat");
    }
    public void closeEverything(Socket socket, BufferedWriter bufferedWriter, BufferedReader bufferedReader) {
        removeClientHandler();
        try{
            if(bufferedReader != null){
                bufferedReader.close();
            }
            if(bufferedWriter != null){
                bufferedWriter.close();
            }
            if(socket != null){
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
