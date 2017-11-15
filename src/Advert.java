public class Advert {

	private Integer id;
	private String content;
	private Integer idVendeur;

	public Advert(Integer id, String content, Integer idVendeur) {
		this.id = id;
		this.content = content;
		this.idVendeur = idVendeur;
	}

	public Integer getId() {
		return id;
	}

	public String getContent() {
		return content;
	}

	public Integer getIdVendeur() {
		return idVendeur;
	}

}
