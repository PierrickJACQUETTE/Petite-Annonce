import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

public class Client {

	public final static int PORT = 1337;

	private Socket sock;
	private BufferedReader br, is;
	private PrintWriter os;

	/**
	* Default Client constructor
	*/
	public Client() {
		try {
			this.sock = new Socket((String) null, PORT);
			this.br = new BufferedReader(new InputStreamReader(System.in));
			this.is = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			this.os = new PrintWriter(sock.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() throws IOException {
		this.br.close();
		this.is.close();
		this.os.close();
		this.sock.close();
	}

	public String processNew() throws IOException {
		String ret = "", tmp = "", advert = "";
		System.out.println("You want to make a new advert - press ENTER then type END to confirm your advert :");
		while (tmp.toUpperCase().compareTo("END") != 0) {
			tmp = this.br.readLine();
			if (tmp.toUpperCase().compareTo("END") != 0)
				advert += tmp + " ";
		}

		if (advert.replaceAll("\\s*", "").equals("")) {
			System.out.println("Error - You cannot have a blank advert");
			ret = "";
		} else {
			ret += Message.NEWA + ";" + advert.replaceAll(";", ",");
		}
		return ret;
	}

	public String processDel() throws IOException {
		String ret = "", tmp = "";
		System.out.print("You want to remove an advert - Please give the advert id : ");
		boolean goodToGo = false;
		int res = 0;
		while (!goodToGo) {
			try {
				tmp = br.readLine();
				res = Integer.parseInt(tmp);
				goodToGo = true;
			} catch (NumberFormatException e) {
				System.out.print("You want to remove an advert - Please give the advert id : ");
			}
		}
		ret += Message.SUPA + ";" + res;
		return ret;
	}

	public String process(String cmd) throws IOException {
		String ret = "";
		switch (cmd.toLowerCase()) {
		case "list":
			System.out.println("You asked for the list");
			ret += Message.LIST;
			break;
		case "new":
			ret = processNew();
			break;
		case "del":
			ret = processDel();
			break;
		case "help":
			System.out.println(
					"You asked for the help : \n\nLIST - Display all the advert \nNEW  - Allow you to make your own advert \nDEL  - Allow you to delete one of your advert \nQUIT - Disconnect you\n");
			break;
		case "quit":
			ret += Message.QUIT+";";
			break;
		default:
			System.out.println("Unknown command, use HELP if needed - you wrote : " + cmd);
			break;
		}
		return ret;
	}

	public static void main(String args[]) throws IOException {
		String line = null;
		String[] check;

		Client client = new Client();


		try {

			System.out.println("Enter Data to echo Server (Enter QUIT to end):");

			String response = null, tmp = "";
			line = "";
			while (!line.toUpperCase().replaceAll("\\s*", "").equals("QUIT")) {
				if (client.br.ready()) {
					line = client.br.readLine();
					tmp = client.process(line.toLowerCase().replaceAll("\\s*", ""));
					if (!tmp.equals("")) {
						client.os.println(tmp);
						client.os.flush();
					}
				}

				if (client.is.ready()) {
					response = client.is.readLine();

					check = response.split(";");
					switch (Message.valueOf(check[0])) {
					case NEWY:
						System.out.println("Acknowledged");
						break;
					case NEWN:
						System.out.println("Error - We've met an isssue with your advert - Please try again");
						break;
					case SUPY:
						System.out.println("Acknowledged");
						break;
					case SUPN:
						System.out.println("Error - We couldn't delete the advert - You cannot remove others advert");
						break;
					case LSRA:
						if (check.length == 1) {
							System.out.println("No advert");
						}
						for (int i = 1; i < check.length; i += 2) {
							System.out.println("Advert n°" + check[i] + " : " + check[i + 1]);
						}
						break;
					case HIHI:
						System.out.println("You are connected");
						break;
					default:
						System.out.println("DEFAULT");
						break;
					}
				}
			}
		} catch (ConnectException e) {
			System.err.println("Server is offline");
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Socket read Error");
			e.printStackTrace();
		} finally {
			client.close();
			System.out.println("Connection Closed");

		}
	}
}
