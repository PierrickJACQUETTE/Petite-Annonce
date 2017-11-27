import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public class MessageServerImpl implements MessageServer {

	private PrintWriter pw;

	public MessageServerImpl(PrintWriter pw) {
		this.pw = pw;
	}

	public void send(String message) {
		this.pw.println(message);
		this.pw.flush();
	}

	@Override
	public void deconnect() {
		if (this.pw != null) {
			this.pw.close();
			System.out.println("Socket Out Closed");
		}
	}

	@Override
	public void sendHi() {
		this.send(Message.HIHI.toString());
	}

	@Override
	public void sendLSRA(HashMap<Integer, Advert> advert) {
		StringBuilder send = new StringBuilder(Message.LSRA.name());
		for (Integer l : advert.keySet()) {
			send.append(";" + l + ";" + advert.get(l).getContent());
		}
		this.send(send.toString());
	}

	@Override
	public void sendLSRA(HashMap<Integer, Advert> advert, List<Integer> listAdvert) {
		StringBuilder send = new StringBuilder(Message.LSRA.name());
		for (Integer i : listAdvert) {
			send.append(";" + i + ";" + advert.get(i).getContent());
		}
		this.send(send.toString());
	}

	@Override
	public void sendNEWReponse(boolean check, boolean check2) {
		if (check && check2) {
			this.send(Message.NEWY.toString());
		} else {
			this.send(Message.NEWN.toString());
		}
	}

	@Override
	public void sendSUPReponse(boolean check, boolean check2) {
		if (check && check2) {
			this.send(Message.SUPY.toString());
		} else {
			this.send(Message.SUPN.toString());
		}
	}

	@Override
	public boolean sendASKReponse(boolean check, boolean check2, String idAnnonce) {
		if (check && check2) {
			this.send(Message.ASKY.toString() + ";" + idAnnonce);
			return true;
		} else {
			this.send(Message.ASKN.toString() + ";" + idAnnonce);
			return false;
		}

	}

	@Override
	public void sendCCSVReponse(boolean check, boolean check2, String[] messageSplit, InetAddress adress,
			Socket socket) {
		if (check && check2) {
			try {
				PrintWriter pw2 = new PrintWriter(socket.getOutputStream());
				String m = Message.CSVC.toString() + ";" + messageSplit[1] + ";" + messageSplit[2] + ";" + adress.toString().replaceAll("/", "");
				pw2.println(m);
				pw2.flush();
			} catch (IOException e) {
				System.err.println("Problem lors de l'envoi au vendeur");
			}
		} else {
			this.send(Message.CSVN.toString() + ";" + messageSplit[1]);
		}
	}

	@Override
	public void sendQUIT(){
		this.send("QUIT");
	}

}
