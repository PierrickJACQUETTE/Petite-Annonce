import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.ServerSocket;

public class ClientP2P {

	private int port;
	private Socket sock;
	private ServerSocket server;
	private BufferedReader br, is;
	private PrintWriter os;

	public ClientP2P() {
		try {
			this.port = findFreePort();
			this.server = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public ClientP2P(int port, String adress, BufferedReader br){
		try {
			this.sock = new Socket(adress, port);
			this.br = br;
			this.is = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			this.os = new PrintWriter(sock.getOutputStream());
		} catch (IOException e) {
			System.out.println("Pas de serveur sur ce port");
			System.exit(0);
		}
	}

	private static int findFreePort() {
		ServerSocket socket = null;
		try {
			socket = new ServerSocket(0);
			socket.setReuseAddress(true);
			int port = socket.getLocalPort();
			try {
				socket.close();
			} catch (IOException e) {
				// Ignore IOException on close()
			}
			return port;
		} catch (IOException e) {
		} finally {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
				}
			}
		}
		throw new IllegalStateException("Could not find a free TCP/IP port to start embedded Jetty HTTP Server on");
	}

	/**
	* Returns value of port
	* @return
	*/
	public int getPort() {
		return this.port;
	}

	private void close() {
		try {
			this.is.close();
			this.os.close();
			this.sock.close();
		} catch (IOException e) {
			System.err.println("IO Error - close");
		}
	}

	public void init(BufferedReader br) throws IOException {
		this.sock = this.server.accept();
		this.br = br;
		this.is = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		this.os = new PrintWriter(sock.getOutputStream());
	}

	public synchronized void run() {
		String line = null, check;

		try {

			Runtime.getRuntime().addShutdownHook(new Thread() {
    			public void run() {
					os.println("QUIT");
					os.flush();
				}
			});

			System.out.println("Welcome (Enter QUIT to end):");

			String response = null, tmp = "";
			line = "";
			while (!line.replaceAll("\\s*", "").equals("QUIT")) {
				if (br.ready()) {
					line = br.readLine();
					tmp = line.replaceAll("\\s*", "");
					if (!tmp.equals("")) {
						os.println(tmp);
						os.flush();
					}
				}
				if (is.ready()) {
					response = is.readLine();
					switch (response) {
						case "QUIT":
							System.out.println("End of discussion");
							line = response;
							break;
						default:
							System.out.println("> "+response); break;
					}
				}
			}
		} catch (ConnectException e) {
			System.out.println("Disconnected from discussion");
		} catch (IOException e) {
			System.err.println("Socket read Error");
		} finally {
			close();
		}
	}
}
