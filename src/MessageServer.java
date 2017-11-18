import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

public interface MessageServer {

	public void deconnect();

	public void sendHi();

	public void sendLSRA(HashMap<Integer, Advert> advert);

	public void sendLSRA(HashMap<Integer, Advert> advert, List<Integer> listAdvert);

	public void sendNEWReponse(boolean check, boolean check2);

	public void sendSUPReponse(boolean check, boolean check2);

	public boolean sendASKReponse(boolean check, boolean check2, String idAnnonce);

	public void sendCCSVReponse(boolean check, boolean check2, String[] messageSplit, InetAddress adress,
			Socket socket);
}
