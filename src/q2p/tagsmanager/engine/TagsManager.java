package q2p.tagsmanager.engine;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import q2p.tagsmanager.kostyak.Tag;

public final class TagsManager {
	private static final InetAddress inetAddress = InetAddress.getLoopbackAddress();
	public static final String ipAddress = inetAddress.getHostAddress();
	public static final int port = 3434;
	public static final String fullAddress = ipAddress+":"+port;
	
	public static final void main(final String[] args) {
		Tag.initilize();
		
		final ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(port, 0, inetAddress);
		} catch (final Exception e) {
			Assist.abort("Не удалось открыть сокет:\n"+fullAddress, e);
			return;
		}
		Assist.storeCloseable(serverSocket);
		
		System.out.println("Сервер запущен.");
		
		while(true) {
			System.gc();
			final Socket socket;
			try {
				socket = serverSocket.accept();
			} catch(Exception e) { continue; }
			HandShaker.shake(socket);
		}
	}
}