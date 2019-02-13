
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Servidor extends Thread {

	@Override
	public void run() {
		ServerSocket servidor = null;
		Socket sc = null;
		HiloServidor hServer;

		int end = 300000000;
		try {
			servidor = new ServerSocket(9999);
			System.out.println("--- Servidor iniciado ---");

			long tempsInicial = System.currentTimeMillis();
			servidor.setSoTimeout(end);
			while (System.currentTimeMillis() - tempsInicial < end) {
				sc = servidor.accept();
				hServer = new HiloServidor(sc);
				hServer.start();
			}
			
			if(!servidor.isClosed()) {
				servidor.close();
			}

		}  catch (IOException e) {
			// TODO Auto-generated catch block
			if(e instanceof SocketTimeoutException) 
				System.out.println("Timeout!!! Excepcio pel venciment del temporitzador de "+ end/1000 + " segs");
			else
				e.printStackTrace();
		}
	}

}