import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class ServerThread implements Runnable {

	private BufferedReader input;
	private MessageServer messageServer;
	private Socket socket;
	private Boolean end;
	private HashMap<Integer, Advert> advert;
	private HashMap<Integer, User> user;
	private List<Integer> counter;
	private Integer idUser;

	public ServerThread(Socket s, HashMap<Integer, Advert> advert, HashMap<Integer, User> user, List<Integer> counter)
			throws IOException {
		this.socket = s;
		this.input = new BufferedReader(new InputStreamReader(s.getInputStream()));
		this.messageServer = new MessageServerImpl(new PrintWriter(s.getOutputStream()));
		this.end = false;
		this.advert = advert;
		this.counter = counter;
		this.user = user;
	}

	private void deconnect() {
		try {
			for (Integer i : user.get(idUser).getListIdAdvert()) {
				advert.remove(i);
			}
			user.remove(idUser);
			System.out.println("Connection Closing..");
			if (this.input != null) {
				this.input.close();
			}
			this.messageServer.deconnect();
			if (this.socket != null) {
				this.socket.close();
			}
		} catch (IOException e) {
			System.err.println("IO Error/ Client " + Thread.currentThread().getName());
		}
	}

	private Integer newIdUser() {
		Integer res = this.counter.get(0) + 1;
		this.counter.set(0, res);
		return res;
	}

	private Integer newIdAdvert() {
		Integer res = this.counter.get(1) + 1;
		this.counter.set(1, res);
		return res;
	}

	private boolean checkLengthLong(String message, int length, int lengthExpected) {
		try {
			if (length > lengthExpected) {
				throw new MessageTooLong(message);
			}
			return true;
		} catch (MessageTooLong e) {
			System.err.println("Message intern problem endMessageIgnored");
			return false;
		}
	}

	private boolean checkLengthShort(String message, int length, int lengthExpected) {
		try {
			if (length < lengthExpected) {
				throw new MessageTooShort(message, lengthExpected);
			}
			return true;
		} catch (MessageTooShort e) {
			System.err.println("Message intern problem MessageTooShort");
			return false;
		}
	}

	private boolean checkLength(String message, int length, int lengthExpected) {
		boolean res = true, res2 = true;
		res = checkLengthShort(message, length, lengthExpected);
		res2 = checkLengthLong(message, length, lengthExpected);
		return (res && res2);
	}

	private boolean supp(String[] messageSplit) {
		try {

			Integer id = Integer.parseInt(messageSplit[1]);
			Advert a = advert.get(id);
			if (a == null) {
				throw new AdvertUnknown(id);
			} else if (a.getIdVendeur() != this.idUser) {
				throw new NotSeller(this.idUser, a.getIdVendeur());
			} else {
				this.user.get(this.idUser).removeListIdAdvert(id);
				Advert res = this.advert.remove(id);
				return (res != null);
			}
		} catch (NotSeller e) {
			System.err.println("Message supp not taken into account");
			return false;
		} catch (AdvertUnknown e) {
			System.err.println("Message supp not taken into account");
			return false;
		}
	}

	private boolean newA(String message, int lenghtPartOne) {
		Integer id = newIdAdvert();
		this.user.get(idUser).addListIdAdvert(id);
		Advert a = new Advert(id, message.substring(lenghtPartOne), idUser);
		Advert res = this.advert.put(id, a);
		return (res != a);
	}

	private void parseReceive(String message) {
		try {
			String[] messageSplit = message.split(";");
			this.checkLengthShort(message, messageSplit.length, 0);
			boolean res, res2;
			switch (Message.valueOf(messageSplit[0])) {
			case QUIT:
				this.checkLength(message, messageSplit.length, 1);
				this.end = true;
				break;
			case LIST:
				this.checkLength(message, messageSplit.length, 1);
				this.messageServer.sendLSRA(this.advert);
				break;
			case OWNA:
				this.checkLength(message, messageSplit.length, 1);
				this.messageServer.sendLSRA(this.advert, this.user.get(idUser).getListIdAdvert());
				break;
			case SUPA:
				res = this.checkLength(message, messageSplit.length, 2);
				res2 = this.supp(messageSplit);
				this.messageServer.sendSUPReponse(res, res2);
				break;
			case NEWA:
				res = this.checkLengthShort(message, messageSplit.length, 2);
				res2 = this.newA(message, messageSplit[0].length() + 1);
				this.messageServer.sendNEWReponse(res, res2);
				break;
			default:
				System.err.println("Message received is unknown : " + message);
				break;
			}
		} catch (IllegalArgumentException e) {
			System.err.println("Message received is unknown : " + message);
		}
	}

	public synchronized void run() {
		this.idUser = newIdUser();
		System.out.println("idUser " + idUser);
		this.user.put(this.idUser, new User(this.idUser, socket.getInetAddress(), socket.getPort()));
		System.out.println("Connection Established : " + socket.getInetAddress() + " " + socket.getPort());
		this.messageServer.sendHi();
		String line = "";
		try {
			while (!this.end) {
				line = this.input.readLine();
				if (line == null) {
					this.end = true;
				} else {
					parseReceive(line);
				}
			}
			this.deconnect();
		} catch (IOException e) {
			this.deconnect();
			System.err.println("IO Error/ Client " + Thread.currentThread().getName() + " terminated abruptly");
		} catch (NullPointerException e) {
			this.deconnect();
			System.out.println("Client " + Thread.currentThread().getName() + " Closed");
		}
	}
}
