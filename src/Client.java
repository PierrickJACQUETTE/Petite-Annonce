import java.io.IOException;
import java.net.Socket;

public class Client {

	public final static int PORT = 1337;

	private Socket sock;

	public Client() {
		try {
			this.sock = new Socket((String) null, PORT);
		} catch (IOException e) {
			System.out.println("Pas de serveur sur ce port");
			System.exit(0);
		}
	}

	public void run() throws IOException {
		ClientThread ct = new ClientThread(this.sock);
		new Thread(ct).start();
	}

	public static void main(String args[]) {
		try {
			Client client = new Client();
			client.run();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
