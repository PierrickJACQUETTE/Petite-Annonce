import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Server {

	private static final Integer PORT = 1337;

	private ServerSocket serverSocket;
	private HashMap<Integer, Advert> advert;
	private HashMap<Integer, User> user;
	private List<Integer> counter;

	public Server() throws IOException {
		this.serverSocket = new ServerSocket(PORT);
		this.advert = new HashMap<Integer, Advert>();
		this.user = new HashMap<Integer, User>();
		this.counter = new ArrayList<Integer>();
		this.counter.add(0);
		this.counter.add(0);
	}

	public void run() throws IOException {
		System.out.println("Server Listening......");
		while (true) {
			Socket socket = this.serverSocket.accept();
			ServerThread st = new ServerThread(socket, advert, user, counter);
			new Thread(st).start();
		}
	}

	public static void main(String args[]) {
		try {
			Server server = new Server();
			server.run();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
