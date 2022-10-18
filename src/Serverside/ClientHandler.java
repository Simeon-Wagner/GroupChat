package Serverside;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientHandler extends Thread{

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList(); // erlaubt es uns jeden client Nachrichten zu schicken
    private Socket socket;  // Wird uns von der Server Klasse übergeben
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private String clientusername;

    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;  // Das Socket dass in der Klasse beim Serverstart intialisiert wird, wird der Klasse Clienthandler übergeben
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())); // socket.Outputstream ist ein bytestream dieses wird in ein character Stream gerwrapped,
            // was wiederum in ein BufferedWriter gewrapped wird
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientusername = bufferedReader.readLine();
            clientHandlers.add(this);  // der CLient wird der ArrayList hinzugefügt damit der Server bei neuen Nachricht jeden Socket benachrichtigen kann
            broadcastMessage("SERVER: " + clientusername + " has entered the chat!");

        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }


    @Override
    public void run() {

        String messageFromClient;

        while (socket.isConnected()) {
            /*
            das muss in einem separaten Thread passieren da der Client in der zwischenhzeit vielleicht selber was schreiben will.
            Wäre dies nicht parallelisiert würde das Programm solange anhalten bis eine Nachricht kommt.
             */
            try {
                messageFromClient = bufferedReader.readLine();
                broadcastMessage(messageFromClient);

            } catch (IOException e) {
                closeEverything(socket, bufferedReader, bufferedWriter);
                break;  // damit bei einem Catch die Schleife beendet wird
            }
        }
    }

    public void broadcastMessage( String messageToSend){
        for( ClientHandler clientHandler : clientHandlers){
            try{
                if(!clientHandler.clientusername.equals(clientusername)){

                    clientHandler.bufferedWriter.write(messageToSend);
                    clientHandler.bufferedWriter.newLine(); // Das New Line sagt dass der BufferedWriter fertig ist mit dem Senden Informationen
                    clientHandler.bufferedWriter.flush();   //Flush füllt den Buffer, ein Buffer wid nähmlich nicht gesendet solange er nicht voll ist. Mit flush füllen wir ihn "manuell"

                }
            }catch (IOException e){
                closeEverything(socket, bufferedReader, bufferedWriter);
            }
        }
    }



    public void removeClientHandler(){
        clientHandlers.remove(this);
        broadcastMessage("SERVER: "+ clientusername + " has left the chat!");
    }

    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
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
        }catch(IOException e){
            e.printStackTrace();
        }

    }
}
