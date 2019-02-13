import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cliente {

    public static void main(String[] args) throws UnknownHostException, IOException, InterruptedException {
        Scanner teclado = new Scanner(System.in);
        DataInputStream in;
        DataOutputStream out;
        String msg = "";
        boolean conversacionActiva = true;
        int numEntradas;

        try {
            Socket sc = new Socket(args[0], Integer.parseInt(args[1]));
            in = new DataInputStream(sc.getInputStream());
            out = new DataOutputStream(sc.getOutputStream());

            msg = in.readUTF();
            String[] array = msg.split(";");
            System.out.printf(array[1]);
            int s = Validacio.validaSencer("Introduce numero pelicula:", Integer.parseInt(array[0]));
            out.writeUTF(Integer.toString(s));
            out.flush();

            msg = in.readUTF();
            array = msg.split(";");
            System.out.printf(array[1]);
            s = Validacio.validaSencer("Introduce numero Session:", Integer.parseInt(array[0]));
            out.writeUTF(Integer.toString(s));
            out.flush();

            msg = in.readUTF();
            numEntradas = Validacio.validaSencer("Introduce numero de entradas:", Integer.parseInt(msg));
            out.writeUTF(Integer.toString(numEntradas));
            out.flush();

            msg = in.readUTF();
            array = msg.split(";");
            System.out.printf(array[2]);

            for (int i = 0; i < numEntradas; i++) {
                int numFila = Validacio.validaSencer("Tria fila [1-" + array[0] + "]", Integer.parseInt(array[0]));
                int numSeient = Validacio.validaSencer("Tria seient [1-" + array[1] + "]", Integer.parseInt(array[1]));
                out.writeUTF(Integer.toString(numFila - 1) + ";" + Integer.toString(numSeient - 1));
                out.flush();

                msg = in.readUTF();
                if (msg.contains("false")){
                    i = -1;
                    System.out.println("Error entrada no disponible");
                }
            }

            msg = in.readUTF();
            array = msg.split(";");
            if (array[0].contains("true")) {
                System.out.println(array[1]);
                boolean b = Validacio.validaBoolea("Vols Pagar?[s/n]");
                if (b) {
                    out.writeUTF(Boolean.toString(b));
                    out.flush();

                    msg = in.readUTF();
                    if (msg.contains("tiempo")){
                        System.out.println("Error, tardaste demasiado en confirmar");
                    }else if (msg.contains("false")){
                        System.out.println("Error, no se a podido realizar la reserva");
                    }else {
                        msg = in.readUTF();
                        System.out.println(msg);
                        msg = in.readUTF();

                        array = msg.split(";");
                        System.out.println(array[2]);
                    }

                } else {
                    System.out.println("Compra cancelada");
                    out.writeUTF(Boolean.toString(b));
                    out.flush();
                }


            } else {
                System.out.println(array[1]);
            }

            sc.close();

        } catch (IOException ex) {
            Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}