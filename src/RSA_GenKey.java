import java.math.BigInteger;
import java.io.BufferedReader;
import java.io.FileReader;

public class RSA_GenKey {
    public BigInteger[] GenKeys() throws Exception{
        BigInteger p, q, n, z, e, d;
        //RandomAccessFile listaNumeriPrimi = new RandomAccessFile("numprimi.txt", "r");
        FileReader f = new FileReader("numprimi.txt");
        BufferedReader listaNumeriPrimi = new BufferedReader(f);
        long lines = 0;
        long i;
        BigInteger Keys[] = new BigInteger[3];
        
        
        //BigInteger sms_encr = new BigInteger("0");
        //BigInteger sms_decr[];
        //Input gesInput = new Input();

        
        
        while(listaNumeriPrimi.readLine() != null){
            lines++;
        }

        long rigaCasualeP = 1 + (long) (Math.random() * (lines - 1));
        long rigaCasualeQ = 1 + (long) (Math.random() * (lines - 1));

        //System.out.println(rigaCasualeP);
        //System.out.println(rigaCasualeQ);

        p = new BigInteger("0");
        q = new BigInteger("0");

        //listaNumeriPrimi.seek(0);
        f.close();
        f = new FileReader("numprimi.txt");
        listaNumeriPrimi = new BufferedReader(f);

        for(i = 0; i < rigaCasualeP; i++){
            p = new BigInteger(listaNumeriPrimi.readLine());
        }
        //listaNumeriPrimi.seek(0);
        f.close();
        f = new FileReader("numprimi.txt");
        listaNumeriPrimi = new BufferedReader(f);
        for(i = 0; i < rigaCasualeQ; i++){
            q = new BigInteger(listaNumeriPrimi.readLine());
        }
        
        n = p.multiply(q);
        z = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE));

        long rigaCasualeE = 1 + (long) (Math.random() * (rigaCasualeP - 1));

        //listaNumeriPrimi.seek(0);
        f.close();
        f = new FileReader("numprimi.txt");
        listaNumeriPrimi = new BufferedReader(f);
        e = new BigInteger("0");
        for(i = 0; i < rigaCasualeE; i++){
            e = new BigInteger(listaNumeriPrimi.readLine());
        }
        
        //cont = new BigInteger("0");
        //d = new BigInteger("0");
        /* 
        do{
            d = cont.multiply(z);
            
            d = d.add(BigInteger.ONE);
            if((d.mod(e)).compareTo(BigInteger.ZERO) != 0){
                cont = cont.add(BigInteger.ONE);
            }
            else{
                d = d.divide(e);
                flag = 1;
            }
        }while(flag == 0);
        */
        d = e.modInverse(z);
        //System.out.println("Chiave pubblica: (" + e + "," + n + ")");
        //System.out.println("Chiave privata: (" + d + "," + n + ")");
        Keys[0] = e;
        Keys[1] = n;
        Keys[2] = d;

        listaNumeriPrimi.close();
        return Keys;
    }

}
