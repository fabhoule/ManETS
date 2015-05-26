package ca.etsmtl.manets;

public class Server extends NanoHTTPD {
	
	public Server(String hostname, int port) {
		super("10.0.3.15", 8080);
		// TODO Auto-generated constructor stub
	}
	

	@Override
	public Response serve(IHTTPSession session) {

		final String msg = "HELLO WORLD!!";

		
		return newFixedLengthResponse(msg);
	}
}
