import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class User {

	private Integer id;
	private InetAddress adress;
	private int port;
	private List<Integer> listIdAdvert;

	public User(Integer id, InetAddress adress, int port) {
		this.id = id;
		this.adress = adress;
		this.port = port;
		this.listIdAdvert = new ArrayList<Integer>();
	}

	public Integer getId() {
		return id;
	}

	public InetAddress getAdress() {
		return adress;
	}

	public int getPort() {
		return port;
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

}
