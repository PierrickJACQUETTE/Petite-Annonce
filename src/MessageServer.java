import java.util.HashMap;
import java.util.List;

public interface MessageServer {

	public void deconnect();

	public void sendHi();

	public void sendLSRA(HashMap<Integer, Advert> advert);
	
	public void sendLSRA(HashMap<Integer, Advert> advert, List<Integer> listAdvert);

	public void sendNEWReponse(boolean check, boolean check2);

	public void sendSUPReponse(boolean check, boolean check2);
}
