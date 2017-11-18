import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class User {

	private Integer id;
	private Socket socket;
	private List<Integer> listIdAdvert;
	private Integer idUserCommunication;

	public User(Integer id, Socket socket) {
		this.id = id;
		this.socket = socket;
		this.listIdAdvert = new ArrayList<Integer>();
		this.idUserCommunication = -1;
	}

	public Integer getId() {
		return id;
	}

	public Socket getSocket() {
		return socket;
	}

	public List<Integer> getListIdAdvert() {
		return listIdAdvert;
	}

	public void addListIdAdvert(Integer idAdvert) {
		this.listIdAdvert.add(idAdvert);
	}

	public void removeListIdAdvert(Integer idAdvert) {
		this.listIdAdvert.remove(idAdvert);
	}

	public Integer getIdUserCommunication() {
		return idUserCommunication;
	}

	public void setIdUserCommunication(Integer idUserCommunication) {
		this.idUserCommunication = idUserCommunication;
	}

}
