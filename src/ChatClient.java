import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

import java.math.BigInteger;
import java.net.Socket;
import java.util.Base64;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.BorderFactory;
import javax.swing.text.BadLocationException;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ChatClient extends Thread{
    String serverAddress;
    BufferedReader in;
    DataOutputStream out;
    JFrame frame = new JFrame("quadChat");
    JLabel invia = new JLabel();
    JTextField textField = new JTextField(50);
    JTextPane messageArea = new JTextPane();
    Color green = new Color(0, 153, 76);
    public ChatClient(BigInteger privateKey[], BigInteger publicKey[]) throws Exception{
        RSA_Cripta crypt = new RSA_Cripta();
        BigInteger pubKey[] = new BigInteger[2];
        //Impostazioni finestra (aggiunta textView/textBox/risoluzione)
        textField.setEditable(false);
        messageArea.setEditable(false);
        frame.getContentPane().add(textField, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        textField.setBorder(BorderFactory.createEmptyBorder());
        JScrollPane scrollPane = new JScrollPane(messageArea);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.pack();
        frame.setSize(800, 600);
        messageArea.setBackground(Color.LIGHT_GRAY);

        Font font = new Font("Font", Font.BOLD, 18);
        messageArea.setFont(font);

        //Inizio thread runClient
        new Thread(){
            public void run(){
                try{
                    runClient(privateKey, publicKey);
                }catch(Exception E){

                }
            }
        }.start();
        //Routine per l'invio del messaggio
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                BigInteger enc;
                if(textField.getText().length() == 0 || textField.getText().length() > 250){ //Controllo sulla lunghezza del messaggio
                    textField.setText("");
                    addColoredText(messageArea,'\n' + "MESSAGGIO NON VALIDO, INVIO RIFIUTATO", Color.RED, 0);
                }
                else{
                    try{
                        out.writeBytes("SND" + '\n'); //Invio richiesta di invio al server (il thread della lettura dei messaggi riceve una richiesta di wait dal server)
                        int numClient;
                        sleep(10); //10ms di delay prima del prossimo readline, così i 2 thread si desincronizzano e non ci sono problemi di concorrenza
                        numClient = Integer.parseInt(in.readLine()) - 1; //Lettura numero client connessi (meno quello attuale)
                        for(int i = 0; i < numClient; i++){
                            pubKey[0] = new BigInteger(in.readLine()); //Lettura chiave pubblica client destinatario
                            pubKey[1] = new BigInteger(in.readLine());
                            enc = crypt.crypt(privateKey[0], privateKey[1], textField.getText()); //Messaggio viene criptato con la privata del client mittente
                            enc = crypt.crypt(pubKey[0], pubKey[1], enc); //Messaggio viene criptato con la pubblica del client destinatario
                            out.writeBytes(enc.toString() + '\n'); //Messaggio/chiavi vengono inviati al server
                            out.writeBytes(publicKey[0].toString() + '\n');
                            out.writeBytes(publicKey[1].toString() + '\n');
                        }
                        out.flush(); //Flush del buffer (non si sa mai)
                        addColoredText(messageArea," " + "IO:" + textField.getText() + " " + '\n', green, 1);//Il messaggio in chiaro viene messo in output nel client
                        messageArea.setCaretPosition(messageArea.getDocument().getLength()); //La textArea fa lo scrolling automatico 
                        textField.setText(""); //La barra di testo viene resettata

                        new Thread(){ //Il thread della lettura dei messaggi riprende l'esecuzione
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

    private String getUsername() { //Dialog per l'input dell'username
        return JOptionPane.showInputDialog(frame, "Scegli un nome utente:", "Selezione del nome utente",
                JOptionPane.PLAIN_MESSAGE);
    }
    private String getAddress() { //Dialog per l'input dell'IP/hostname
        return JOptionPane.showInputDialog(frame, "Inserisci l'indirizzo IP del server:", "Connessione al server",
                JOptionPane.PLAIN_MESSAGE);
    }
    private void serverClosed() { //Dialog per l'uscita causata dalla chiusura del server
        JOptionPane.showMessageDialog(frame, "Il server alla quale ti eri connesso è stato chiuso", "Server chiuso",
        JOptionPane.ERROR_MESSAGE);
    }

    synchronized private void runClient(BigInteger privateKey[], BigInteger publicKey[]) throws Exception{ //Thread per la lettura dei messaggi (sincronizzato perchè così la wait funziona)
        this.frame.setTitle("quadChat (Premi invio per inviare un messaggio)"); //Viene settato il titolo della finestra
        int flag = 0;
        String addr;
        Socket socket = null;
        try {
            do{
                try{
                    addr = getAddress(); //Richiesta indirizzo IP/Hostname
                    if(addr == null){ //Controllo se utente ha cliccato Annulla
                        System.exit(1);
                    }
                    socket = new Socket(addr, 6789);
                    flag = 0;
                }catch(Exception ex){
                    flag = 1;
                }
            }while(flag == 1);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream())); //Creazione buffer
            out = new DataOutputStream(socket.getOutputStream());

            String decr, username, recv;
            BigInteger enc;
            BigInteger pubOut[] = new BigInteger[2];
            RSA_Decripta decrypt = new RSA_Decripta();

            out.writeBytes(publicKey[0].toString() + '\n'); //Invio chiavi pubbliche al server
            out.writeBytes(publicKey[1].toString() + '\n');

            do{
                username = getUsername(); //Richiesta username
                if(username == null){
                    System.exit(2);
                }
                out.writeBytes(Base64.getEncoder().encodeToString(username.getBytes()) + '\n'); //Invio username (codificato in Base64)
            }while(in.readLine().equals("1"));
            textField.setEditable(true);
            while (true) {
                do{
                    recv = in.readLine(); //Attesa di messaggio dal server
                    if(recv.equals("STPMSG") == true){ //Controllo se messaggio è STPMSG (richiesta di blocco thread della Lettura dei messaggi)
                        wait(); //Il thead si mette in wait fino a quando un altro thread lo "sblocca"
                    }
                }while(recv.equals("STPMSG") == true);

                enc = new BigInteger(recv); //Il messaggio ricevuto viene castato in BigInteger
                username = in.readLine(); //Lettura Username
                username = new String(Base64.getDecoder().decode(username)); //Decodifica Username da Base64 a Stringa
                pubOut[0] = new BigInteger(in.readLine()); //Lettura chiave pubblica
                pubOut[1] = new BigInteger(in.readLine());
                if(username.equals("SERVER") == true){ //Controllo se l'username è SERVER (in caso di annuncio di connessione/disconnessione da parte del server, li il messaggio viene criptato solo con una coppia di chiavi)
                    decr = decrypt.decrypt(pubOut[0], pubOut[1], enc);
                    addColoredText(messageArea," " + username + ": " + decr + " " +"\n", Color.RED, 0);//Il messaggio in output viene messo in output insieme all'username
                }else{
                    enc = decrypt.decrypt_bi(privateKey[0], privateKey[1], enc);
                    decr = decrypt.decrypt(pubOut[0], pubOut[1], enc);
                    addColoredText(messageArea," " + username + ": " + decr + " " +"\n", new Color(64, 64, 64), 0);//Il messaggio in output viene messo in output insieme all'username
                }
                messageArea.setCaretPosition(messageArea.getDocument().getLength()); //La textArea fa lo scrolling automatico 
            }
        }catch(Exception e){
            System.out.println(e);
            frame.setVisible(false);
            serverClosed();
            frame.dispose();
        }
    }
    public static void main(String[] args) throws Exception {
        BigInteger privateKey[], publicKey[], Keys[];
        RSA_GenKey genKey = new RSA_GenKey();

        Keys = new BigInteger[3];
        privateKey = new BigInteger[2];
        publicKey = new BigInteger[2];

        Keys = genKey.GenKeys(); //Generazione chiavi

        publicKey[0] = Keys[0];
        publicKey[1] = Keys[1];
        privateKey[0] = Keys[2];
        privateKey[1] = Keys[1];

        ChatClient client = new ChatClient(privateKey, publicKey); //Inizio 
        client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        client.frame.setVisible(true);
    }

    synchronized public void restart(){
        notify();
    }
    
    public void addColoredText(JTextPane pane, String text, Color color, int isCurrentUser) {
        StyledDocument doc = pane.getStyledDocument();
        SimpleAttributeSet left = new SimpleAttributeSet();
        StyleConstants.setAlignment(left, StyleConstants.ALIGN_LEFT);

        MutableAttributeSet style = pane.addStyle("Color Style", null);
        StyleConstants.setBackground(style, color);
        StyleConstants.setForeground(style, Color.WHITE);
        doc.setParagraphAttributes(doc.getLength(), 1, left, false);

        if(isCurrentUser == 1){
            SimpleAttributeSet right = new SimpleAttributeSet();
            StyleConstants.setAlignment(right, StyleConstants.ALIGN_RIGHT);
            doc.setParagraphAttributes(doc.getLength(), 1, right, false);
        }
        try {
            doc.insertString(doc.getLength(), text, style);
        } 
        catch (BadLocationException e) {
            e.printStackTrace();
        }           
    }
}
