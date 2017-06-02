package q2p.tagsmanager.kostyak;

import q2p.tagsmanager.engine.Request;
import q2p.tagsmanager.engine.Response;
import q2p.tagsmanager.engine.Response.ResponseCode;

public final class RequestType {
	public static final void decide(final Request request, final Response response) {
		if(MainPage.requestCheck(request, response)) return;
		if(ViewPage.requestCheck(request, response)) return;
		response.flushPlainTextWithUTF_8(ResponseCode._400_Bad_Request, "Не обрабатываемый запрос.");
	}
}