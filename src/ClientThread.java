import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

public class ClientThread implements Runnable {

	private Socket sock;
	private BufferedReader br, is;
	private PrintWriter os;

	public ClientThread(Socket sock) {
		try {
			this.sock = sock;
			this.br = new BufferedReader(new InputStreamReader(System.in));
			this.is = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			this.os = new PrintWriter(sock.getOutputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void close() {
		try {
			this.br.close();
			this.is.close();
			this.os.close();
			this.sock.close();
		} catch (IOException e) {
			System.err.println("IO Error - close");
		}
	}

	private String processNew() throws IOException {
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

	private void processForAdvertSupl(boolean opt) {
		if (opt) {
			System.out.print("You want to remove an advert - Please give the advert id (press q to quit): ");
		} else {
			System.out.print("You want to contact a seller - Please give the advert id (press q to quit): ");
		}
	}

	private String processForAdvert(boolean opt) throws IOException {
		String ret = "", tmp = "";
		boolean goodToGo = false;
		int res = 0;
		processForAdvertSupl(opt);
		while (!goodToGo) {
			try {
				tmp = br.readLine();
				if(tmp.equals("q")) return "";
				res = Integer.parseInt(tmp);
				goodToGo = true;
			} catch (NumberFormatException e) {
				processForAdvertSupl(opt);
			}
		}
		if (opt) {
			ret += Message.SUPA + ";" + res;
		} else {
			ret += Message.ASKA + ";" + res;
		}
		return ret;
	}

	private void sendCCSV(int ad) throws IOException {
		ClientP2P p2p = new ClientP2P();
		String tmp = Message.CCSV + ";" + ad + ";" + p2p.getPort();
		os.println(tmp); os.flush();
		p2p.init(this.br);
		p2p.run();
		System.out.println("Back on tracks");
	}

	private void processCSVC(String adress, int port){
		ClientP2P p2p = new ClientP2P(port, adress, this.br);
		p2p.run();
		System.out.println("Back on tracks");
	}

	private String process(String cmd) throws IOException {
		String ret = "";
		switch (cmd.toLowerCase()) {
			case "list":
				System.out.println("You asked for the list");
				ret += Message.LIST;
				break;
			case "new":
				ret = processNew(); break;
			case "del":
				ret = processForAdvert(true); break;
			case "ask":
				ret = processForAdvert(false); break;
			case "help":
				System.out.println("You asked for the help : \n\nLIST - Display all the advert \nNEW  - Allow you to make your own advert \nDEL  - Allow you to delete one of your advert \nQUIT - Disconnect you\n"); break;
			case "own":
				System.out.println("You asked for your own advert");
				ret += Message.OWNA;
				break;
			case "quit":
				ret += Message.QUIT + ";"; break;
			default:
				System.out.println("Unknown command, use HELP if needed - you wrote : " + cmd); break;
		}
		return ret;
	}

	public synchronized void run() {
		String line = null;
		String[] check;
		try {

			System.out.println("Enter Data to echo Server (Enter QUIT to end):");

			String response = null, tmp = "";
			line = "";
			while (!line.toUpperCase().replaceAll("\\s*", "").equals("QUIT")) {

				if (br.ready()) {
					line = br.readLine();
					tmp = process(line.replaceAll("\\s*", ""));
					if (!tmp.equals("")) {
						os.println(tmp);
						os.flush();
					}
				}

				if(is.ready()){
					response = is.readLine();
					check = response.split(";");
					switch (Message.valueOf(check[0])) {
						case NEWY:
							System.out.println("Acknowledged"); break;
						case NEWN:
							System.out.println("Error - We've met an isssue with your advert - Please try again"); break;
						case SUPY:
							System.out.println("Acknowledged"); break;
						case SUPN:
							System.out.println("Error - We couldn't delete the advert - You cannot remove others advert"); break;
						case ASKY:
							try {
								sendCCSV(Integer.parseInt(check[1]));
							} catch(NumberFormatException e){
								System.err.println("An error as occured");
							}
							break;
						case ASKN:
							System.out.println("Error - We couldn't established a connection - The user may be disconnected or occupied"); break;
						case CSVC:
							try {
								int ad = Integer.parseInt(check[1]);
								int port = Integer.parseInt(check[2]);
								String adress = check[3];
								System.out.println("You are contacted for the advert n°"+ad);
								processCSVC(adress, port);
							} catch(NumberFormatException e){
								System.err.println("An error as occured");
							}
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
							System.out.println("You are connected"); break;
						case QUIT:
							throw new ConnectException();
						default:
							System.err.println("DEFAULT"); break;
					}
				}
			}
		} catch (ConnectException e) {
			System.err.println("Server is offline");
			System.exit(-1);
		} catch (IOException e) {
			System.out.println("Socket read Error");
			e.printStackTrace();
		} catch (NullPointerException e) {
			System.err.println("An error as occured");
		} finally {
			close();
			System.out.println("Connection Closed");
		}
	}
}
