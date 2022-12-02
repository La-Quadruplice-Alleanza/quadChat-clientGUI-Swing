import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Base64;
import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class ChatClient extends Thread{

    String serverAddress;
    BufferedReader in;
    DataOutputStream out;
    JFrame frame = new JFrame("quadChat");
    JTextField textField = new JTextField(50);
    JTextArea messageArea = new JTextArea(16, 50);
    public ChatClient(BigInteger privateKey[], BigInteger publicKey[]){
        RSA_Cripta crypt = new RSA_Cripta();
        BigInteger pubKey[] = new BigInteger[2];
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();
        frame.setSize(800, 600);
        new Thread(){
            public void run(){
                try{
                    runClient(privateKey, publicKey);
                }catch(Exception E){

                }
            }
        }.start();

        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                BigInteger enc;
                if(textField.getText().length() == 0 || textField.getText().length() >= 250){
                    textField.setText("");
                    messageArea.append("\n" + "MESSAGGIO NON VALIDO, INVIO RIFIUTATO");
                }
                else{
                    try{
                        out.writeBytes("SND" + '\n');
                        int numClient;
                        sleep(10);
                        numClient = Integer.parseInt(in.readLine()) - 1;
                        for(int i = 0; i < numClient; i++){
                            pubKey[0] = new BigInteger(in.readLine());
                            pubKey[1] = new BigInteger(in.readLine());
                            enc = crypt.crypt(privateKey[0], privateKey[1], textField.getText());
                            enc = crypt.crypt(pubKey[0], pubKey[1], enc);
                            out.writeBytes(enc.toString() + '\n');
                            out.writeBytes(publicKey[0].toString() + '\n');
                            out.writeBytes(publicKey[1].toString() + '\n');
                        }
                        out.flush();
                        messageArea.append('\n' + textField.getText());
                        textField.setText("");
                        new Thread(){
                        public void run(){
                                restart();
                        } 
                        }.start();
                    }catch(Exception ex){
                        System.out.println(ex);
                    }
                }
            }
        });
    }

    private String getUsername() {
        return JOptionPane.showInputDialog(frame, "Scegli un nome utente:", "Selezione del nome utente",
                JOptionPane.PLAIN_MESSAGE);
    }
    private String getAddress() {
        return JOptionPane.showInputDialog(frame, "Inserisci l'indirizzo IP del server:", "Connessione al server",
                JOptionPane.PLAIN_MESSAGE);
    }
    

    synchronized private void runClient(BigInteger privateKey[], BigInteger publicKey[]) throws Exception{
        this.frame.setTitle("quadChat (Premi invio per inviare un messaggio)");
        int flag = 0;
        String addr;
        Socket socket = null;
        try {
            do{
                try{
                    addr = getAddress();
                    if(addr == null){
                        System.exit(1);
                    }
                    socket = new Socket(addr, 6789);
                    flag = 0;
                }catch(Exception ex){
                    flag = 1;
                }
            }while(flag == 1);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());
            String decr, username, recv;
            BigInteger enc;
            BigInteger pubOut[] = new BigInteger[2];
            RSA_Decripta decrypt = new RSA_Decripta();

            out.writeBytes(publicKey[0].toString() + '\n');
            out.writeBytes(publicKey[1].toString() + '\n');

            do{
                username = getUsername();
                if(username == null){
                    System.exit(2);
                }
                out.writeBytes(Base64.getEncoder().encodeToString(username.getBytes()) + '\n');
            }while(in.readLine().equals("1"));
            textField.setEditable(true);
            while (true) {
                do{
                    recv = in.readLine();
                    if(recv.equals("STPMSG") == true){
                        wait();
                    }
                }while(recv.equals("STPMSG") == true);
                enc = new BigInteger(recv);
                username = in.readLine();
                username = new String(Base64.getDecoder().decode(username));
                pubOut[0] = new BigInteger(in.readLine());
                pubOut[1] = new BigInteger(in.readLine());
                if(username.equals("SERVER") == true){
                    decr = decrypt.decrypt(pubOut[0], pubOut[1], enc);
                }else{
                    enc = decrypt.decrypt_bi(privateKey[0], privateKey[1], enc);
                    decr = decrypt.decrypt(pubOut[0], pubOut[1], enc);
                }
                messageArea.append('\n' + username + ": " + decr);
                messageArea.setCaretPosition(messageArea.getDocument().getLength());
            }
        }catch(Exception e){
            System.out.println(e);
            frame.setVisible(false);
            frame.dispose();
        }
    }
    public static void main(String[] args) throws Exception {
        BigInteger privateKey[], publicKey[], Keys[];
        RSA_GenKey genKey = new RSA_GenKey();

        Keys = new BigInteger[3];
        privateKey = new BigInteger[2];
        publicKey = new BigInteger[2];

        Keys = genKey.GenKeys();

        publicKey[0] = Keys[0];
        publicKey[1] = Keys[1];
        privateKey[0] = Keys[2];
        privateKey[1] = Keys[1];
        ChatClient client = new ChatClient(privateKey, publicKey);
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
    }

    synchronized public void restart(){
        notify();
    }
}