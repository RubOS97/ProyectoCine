

import sun.rmi.runtime.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;

public class HiloServidor extends Thread {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String msg;
    private boolean conversacionActiva = true;
    Pelicula p = null;
    Sala sa = null;
    Sessio se = null;
    int sessio, pelicula, fila, seient, numEntrades;
    public HiloServidor(Socket socket) {
        this.socket = socket;
    }
    ArrayList<Seient> seients = new ArrayList<>();
    int numEntradas;
    Long tiempoCompra;
    Long tiempoResultado;

    @Override
    public void run() {
        super.run();
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());


            out.writeUTF(mostrarPelis());
            String cadenaRebuda = in.readUTF();
            p = Pelicules.retornaPelicula(Integer.parseInt(cadenaRebuda));
            System.out.println(p);

            out.writeUTF(mostrarSessions());
            cadenaRebuda = in.readUTF();
            se = p.retornaSessioPeli(Integer.parseInt(cadenaRebuda));
            System.out.println(se);
            sa = se.getSala();

            out.writeUTF(Integer.toString(sa.getTamanyFila() * sa.getFiles()));
            numEntradas = Integer.parseInt(in.readUTF());
            out.writeUTF(mapaSessio());

            for (int i = 0; i < numEntradas; i++) {
                cadenaRebuda = in.readUTF();
                String[] array = cadenaRebuda.split(";");

                if (se.getSeients()[Integer.parseInt(array[0])][Integer.parseInt(array[1])].verificaSeient()) {
                    se.getSeients()[Integer.parseInt(array[0])][Integer.parseInt(array[1])].reservantSeient();
                    seients.add(se.getSeients()[Integer.parseInt(array[0])][Integer.parseInt(array[1])]);
                    out.writeUTF("true");
                } else {
                    out.writeUTF("false");
                    i = 0;
                    for (Seient s : seients) {
                        s.alliberaSeient();
                    }
                    seients.removeAll(seients);

                }
            }

            tiempoCompra = System.currentTimeMillis();
            boolean b = pagamentEntrada(se.getPreu().multiply(BigDecimal.valueOf(numEntradas)));
            tiempoResultado = System.currentTimeMillis() - tiempoCompra;
            if (tiempoResultado > 10000){
                for (Seient s : seients) {
                    s.alliberaSeient();
                }
                out.writeUTF("tiempo");
            }else if (!b){
                for (Seient s : seients) {
                    s.alliberaSeient();
                }
                out.writeUTF("false");
            }else {
                for (Seient s : seients) {
                    s.ocupaSeient();
                }
                out.writeUTF("true");
                String st="";

                for (Seient s : seients) {
                    st+=imprimirTicket(s, se, sa, p);
                    System.out.println(s);
                }

                out.writeUTF(st);
                out.writeUTF(mapaSessio());

            }


            System.out.println("Finalizando la conexión...");
            socket.close();
            System.out.println("Conexión finalizada");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String imprimirTicket(Seient s, Sessio se, Sala sa, Pelicula p) {
        String st = "";

        st += ("\nImprimint el seu Ticket...\n");
        st += ("***************************\n");
        st += ("* ***TICKET ENTRADA *******\n");
        st += ("* PELICULA: " + p.getNomPeli() + " *\n");
        st += ("* HORARI: \n");
        st = mostraDataFormatada(st);
        st += ("*\n* Seient FILA:" + (s.getFilaSeient() + 1) + " SEIENT:" + (s.getNumeroSeient() + 1) + "*\n");
        st += ("* Preu: " + se.getPreu() + " �\n");
        st += ("****************************\n");

        return st;
    }

    public String mostraDataFormatada(String s) {
        int day = se.getData().get(Calendar.DAY_OF_MONTH);
        int month = se.getData().get(Calendar.MONTH);
        int year = se.getData().get(Calendar.YEAR);
        int hour = se.getData().get(Calendar.HOUR_OF_DAY);
        int minute = se.getData().get(Calendar.MINUTE);

        s += (day + "/" + month + "/" + year + " " + hour + ":" + minute);
        return s;
    }


    private String mostrarSessions() {
        String s = "";
        ArrayList<Sessio> sessions = p.getSessionsPeli();
        s = sessions.size() + ";";
        for (int i = 1; i <= sessions.size(); i++) {
            Sessio se = sessions.get(i - 1);
            s += (" " + i + ": " + se);
        }
        return s;

    }

    private String mostrarPelis() {
        String s = "";
        s += Cine.pelicules.quantitatPelicules() + ";";
        if (Cine.pelicules.quantitatPelicules() == 0)
            s = "\n\t No hi ha cap PELICULA registrada";

        for (int i = 1; i <= Cine.pelicules.quantitatPelicules(); i++) {
            s += "\n\t " + i + "-> " + Cine.pelicules.getPelicules().get(i - 1).toString() + "\n";
        }
        return s;
    }

    public boolean pagamentEntrada(BigDecimal preu) throws IOException {
        String s = "true;";

        s += ("Import a pagar: " + preu);
        //pagant

        out.writeUTF(s);
        String cadenaRebuda = in.readUTF();

        return Boolean.parseBoolean(cadenaRebuda);
    }

    public String mapaSessio() {
        String s = "";
        s += sa.getFiles() + ";" + sa.getTamanyFila() + ";";
        s += "\n\t --------  MAPA SESSIO  -----------\n";

        s += ("\t Seient->");
        for (int x = 1; x <= sa.getTamanyFila(); x++) {
            s += (x + "  ");
        }
        s += "\n";
        //COS de la SALA
        for (int i = 0; i < sa.getTamanyFila(); i++) {
            s += ("\t Fila " + (i + 1) + ": ");
            for (int j = 0; j < sa.getFiles(); j++) {
                s += (" " + se.getSeients()[i][j].iconaSeient() + " ");
            }//endfor	
            s += "\n";
        }//endfor
        s += ("\n\t SIMBOLOGIA: X=ocupat; O=lliure; ?=reservant\n\n");
        return s;
    }
}
