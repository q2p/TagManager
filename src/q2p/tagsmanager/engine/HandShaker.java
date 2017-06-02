package q2p.tagsmanager.engine;

import java.net.Socket;
import q2p.tagsmanager.engine.Response.ResponseCode;
import q2p.tagsmanager.kostyak.RequestType;

final class HandShaker {
	static final void shake(final Socket socket) {
		Assist.storeCloseable(socket);
		final Request request;
		final Response response;
		try {
			response = new Response(socket.getOutputStream(), socket.getSendBufferSize());
		} catch(final Exception e) {
			Assist.safeClose(socket);
			return;
		}
		try {
			request = Request.fabric(socket.getInputStream());
		} catch(final AnsweringException e) {
			response.flushPlainTextWithUTF_8(ResponseCode._400_Bad_Request, e.message);
			Assist.safeClose(socket);
			return;
		} catch(final Exception e) {
			Assist.safeClose(socket);
			return;
		}

		try {
			if(request == null) {
				response.flushCode(ResponseCode._400_Bad_Request);
			} else {
				RequestType.decide(request, response);
			}
		} catch(final Exception e) {
			throw e;
		} finally {
			Assist.safeClose(socket);
		}
	}
}