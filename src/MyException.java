class MessageTooLong extends Exception {
	public MessageTooLong(String message) {
		super("Message too long : " + message + ".");
	}
}

class MessageTooShort extends Exception {
	public MessageTooShort(String message, int length) {
		super("Message too short (normally size of " + length + ") : " + message + ".");
	}
}

class AdvertUnknown extends Exception {
	public AdvertUnknown(Integer idAdvert) {
		super("Advert Unknown : " + idAdvert + ".");
	}
}

class NotSeller extends Exception {
	public NotSeller(Integer idUser, Integer idSeller) {
		super("It is not the seller : " + idUser + "!=" + idSeller + ".");
	}
}
