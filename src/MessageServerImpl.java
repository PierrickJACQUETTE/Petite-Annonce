import java.io.PrintWriter;
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

}
