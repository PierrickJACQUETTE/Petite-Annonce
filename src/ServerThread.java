import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
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
	private List<String> idAdvertWhereASKA;

	public ServerThread(Socket s, HashMap<Integer, Advert> advert, HashMap<Integer, User> user, List<Integer> counter)
			throws IOException {
		this.socket = s;
		this.input = new BufferedReader(new InputStreamReader(s.getInputStream()));
		this.messageServer = new MessageServerImpl(new PrintWriter(s.getOutputStream()));
		this.end = false;
		this.advert = advert;
		this.counter = counter;
		this.user = user;
		this.idAdvertWhereASKA = new ArrayList<String>();
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

	private void freeAvailability() {
        User courant = this.user.get(this.idUser);
        if (courant.getIdUserCommunication() != -1) {
            if(this.user.get(courant.getIdUserCommunication()) !=null){
                User other = this.user.get(courant.getIdUserCommunication());
                other.setIdUserCommunication(-1);
                this.user.put(other.getId(), other);
            }
            courant.setIdUserCommunication(-1);
            this.user.put(courant.getId(), courant);
        }
    }

	private boolean checkValidity(Advert demande) {
		if (demande == null) { // annonce plus active
			return false;
		}
		if (demande.getIdVendeur() == this.idUser) { // ma propre annonce
			return false;
		}
		User vendeur = this.user.get(demande.getIdVendeur());
		if (vendeur == null) { // vendeur plus actif
			return false;
		}
		return true;
	}

	private boolean aska(String[] messageSplit) {
		Integer numAnnonce;
		try {
			numAnnonce = Integer.parseInt(messageSplit[1]);
		} catch (NumberFormatException e) {
			return false;
		}
		Advert demande = this.advert.get(numAnnonce);
		if (checkValidity(demande) == false) {
			return false;
		}
		User vendeur = this.user.get(demande.getIdVendeur());
		if (vendeur.getIdUserCommunication() != -1) { // vendeur occupe
			return false;
		}
		User courant = this.user.get(this.idUser);
		courant.setIdUserCommunication(vendeur.getId());
		vendeur.setIdUserCommunication(courant.getId());
		this.user.put(courant.getId(), courant);
		this.user.put(vendeur.getId(), vendeur);
		return true;
	}

	public void aska(String message, String[] messageSplit) {
		boolean res = this.checkLength(message, messageSplit.length, 2);
		freeAvailability();
		boolean res2 = this.aska(messageSplit);
		res = this.messageServer.sendASKReponse(res, res2, messageSplit[1]);
		if (res) {
			this.idAdvertWhereASKA.add(messageSplit[1]);
		}
	}

	public Socket ccsv(String[] messageSplit) {
		if (!this.idAdvertWhereASKA.contains(messageSplit[1])) {
			return null;
		}
		Integer numAnnonce;
		try {
			numAnnonce = Integer.parseInt(messageSplit[1]);
			Integer.parseInt(messageSplit[2]);
		} catch (NumberFormatException e) {
			return null;
		}
		Advert demande = this.advert.get(numAnnonce);
		if (checkValidity(demande) == false) {
			return null;
		}
		return this.user.get(demande.getIdVendeur()).getSocket();
	}

	public void ccsv(String message, String[] messageSplit) {
		boolean res = this.checkLengthShort(message, messageSplit.length, 3);
		Socket socket = this.ccsv(messageSplit);
		boolean res2 = (socket == null) ? false : true;
		InetAddress ia = this.user.get(this.idUser).getSocket().getInetAddress();
		this.messageServer.sendCCSVReponse(res, res2, messageSplit, ia, socket);
	}

	private void list(String message, String[] messageSplit) {
		this.checkLength(message, messageSplit.length, 1);
		freeAvailability();
		this.messageServer.sendLSRA(this.advert);
	}

	private void newa(String message, String[] messageSplit, int lenghtPartOne) {
		boolean res = this.checkLengthShort(message, messageSplit.length, 2);
		freeAvailability();
		Integer id = newIdAdvert();
		this.user.get(idUser).addListIdAdvert(id);
		Advert a = new Advert(id, message.substring(lenghtPartOne), idUser);
		Advert aRes = this.advert.put(id, a);
		this.messageServer.sendNEWReponse(res, aRes != a);
	}

	private void own(String message, String[] messageSplit) {
		this.checkLength(message, messageSplit.length, 1);
		freeAvailability();
		this.messageServer.sendLSRA(this.advert, this.user.get(idUser).getListIdAdvert());
	}

	private void quit(String message, String[] messageSplit) {
		this.checkLength(message, messageSplit.length, 1);
		freeAvailability();
		this.end = true;
	}

	private void supp(String message, String[] messageSplit) {
		boolean res = true, res2 = true;
		try {
			res = this.checkLength(message, messageSplit.length, 2);
			freeAvailability();
			Integer id = Integer.parseInt(messageSplit[1]);
			Advert a = advert.get(id);
			if (a == null) {
				throw new AdvertUnknown(id);
			} else if (a.getIdVendeur() != this.idUser) {
				throw new NotSeller(this.idUser, a.getIdVendeur());
			} else {
				this.user.get(this.idUser).removeListIdAdvert(id);
				Advert adRes = this.advert.remove(id);
				res2 = adRes != null;
			}
		} catch (NumberFormatException e) {
			res2 = false;
		} catch (NotSeller e) {
			System.err.println("Message supp not taken into account");
			res2 = false;
		} catch (AdvertUnknown e) {
			System.err.println("Message supp not taken into account");
			res2 = false;
		}
		this.messageServer.sendSUPReponse(res, res2);
	}

	private void parseReceive(String message) {
		try {
			String[] messageSplit = message.split(";");
			this.checkLengthShort(message, messageSplit.length, 0);
			switch (Message.valueOf(messageSplit[0])) {
			case QUIT:
				this.quit(message, messageSplit);
				break;
			case LIST:
				this.list(message, messageSplit);
				break;
			case OWNA:
				this.own(message, messageSplit);
				break;
			case SUPA:
				this.supp(message, messageSplit);
				break;
			case NEWA:
				this.newa(message, messageSplit, messageSplit[0].length() + 1);
				break;
			case ASKA:
				this.aska(message, messageSplit);
				break;
			case CCSV:
				this.ccsv(message, messageSplit);
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
		this.user.put(this.idUser, new User(this.idUser, socket));
		System.out.println("Connection Established : " + socket.getInetAddress() + " " + socket.getPort());
		this.messageServer.sendHi();
		String line = "";
		try {

			Runtime.getRuntime().addShutdownHook(new Thread() {
    			public void run() {
					messageServer.sendQUIT();
				}
			});

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
			e.printStackTrace();
			System.out.println("Client " + Thread.currentThread().getName() + " Closed");
		}
	}
}
