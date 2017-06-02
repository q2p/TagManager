package q2p.tagsmanager.engine;

import java.io.OutputStream;

public class Response {
	private final OutputStream out;
	private final byte[] buffer;
	public final int bufferSize() {
		return buffer.length;
	}
	
	public Response(final OutputStream out, final int sendBufferSize) {
		this.out = out;
		buffer = new byte[sendBufferSize];
	}
	
	public final boolean flushRedirect(final String to) {
		if(to == null)
			throw new NullPointerException();
		try {
			Assist.writePartialyAndFlush(out, buffer.length,
				( "HTTP/1.1 303 See Other\n"
				+ "Location: "+to+"\n"
				+ "Connection: close\n"
				+ "Cache-Control: no-store, must-revalidate\n"
				+ "Pragma: no-cache\n"
				+ "Expires: 0\n\n"
			).getBytes(Assist.ASCII));
		} catch(final Exception e) {}
		return true;
	}
	
	public final boolean flushBody(final ResponseCode code, final ContentType contentType, final String body) {
		try {
			final byte[] bytes = body.getBytes(Assist.UTF_8);
			Assist.writePartialyAndFlush(out, buffer.length,
				( "HTTP/1.1 "+code.print+"\n"
				+ "Content-Type: "+contentType.print+"\n"
				+ "Content-Length: "+bytes.length+"\n"
				+ "Connection: close\n"
				+ "Cache-Control: no-store, must-revalidate\n"
				+ "Pragma: no-cache\n"
				+ "Expires: 0\n\n"
			).getBytes(Assist.ASCII));
			Assist.writePartialyAndFlush(out, buffer.length, bytes);
		} catch(final NullPointerException e) {
			throw e;
		} catch(final Exception e) {}
		return true;
	}
	
	public final boolean flushPlainTextWithUTF_8(final ResponseCode code, final String body) {
		try {
			final byte[] bytes = body.getBytes(Assist.UTF_8);
			Assist.writePartialyAndFlush(out, buffer.length,
				( "HTTP/1.1 "+code.print+"\n"
				+ "Content-Type: text/plain; charset=utf-8\n"
				+ "Content-Length: "+bytes.length+"\n"
				+ "Connection: close\n"
				+ "Cache-Control: no-store, must-revalidate\n"
				+ "Pragma: no-cache\n"
				+ "Expires: 0\n\n"
			).getBytes(Assist.ASCII));
			Assist.writePartialyAndFlush(out, buffer.length, bytes);
		} catch(final NullPointerException e) {
			throw e;
		} catch(final Exception e) {}
		return true;
	}
		
	public final boolean flushCode(final ResponseCode code) {
		try {
			Assist.writePartialyAndFlush(out, buffer.length,
				( "HTTP/1.1 "+code.print+"\n"
				+ "Connection: close\n"
				+ "Cache-Control: no-store, must-revalidate\n"
				+ "Pragma: no-cache\n"
				+ "Expires: 0\n\n"
			).getBytes(Assist.ASCII));
		} catch(final NullPointerException e) {
			throw e;
		} catch(final Exception e) {}
		return true;
	}
	
	public final boolean flushFileNotFound(final String path) {
		if(path == null)
			throw new NullPointerException();
		final byte[] bytes = ("Файл \"" + path + "\" не найден").getBytes(Assist.UTF_8);
		try {
			Assist.writePartialyAndFlush(out, buffer.length,
				( "HTTP/1.1 404 Not Found\n"
				+ "Content-Type: text/plain; charset=utf-8\n"
				+ "Content-Length: "+bytes.length+"\n"
				+ "Connection: close\n"
				+ "Cache-Control: no-store, must-revalidate\n"
				+ "Pragma: no-cache\n"
				+ "Expires: 0\n\n"
			).getBytes(Assist.ASCII));
			Assist.writePartialyAndFlush(out, buffer.length, bytes);
		} catch(final Exception e) {}
		return true;
	}

	public static enum ResponseCode {
		_200_OK("200 OK"),
		_304_Not_Modified("304 Not Modified"),
		_400_Bad_Request("400 Bad Request"),
		_404_Not_Found("404 Not Found"),
		_406_Not_Acceptable("406 Not Acceptable"),
		_500_Internal_Server_Error("500 Internal Server Error");
		
		public final String print;
		
		private ResponseCode(String print) {
			this.print = print;
		}
	}
	
	public static enum ContentType {
		text_plain("text/plain", "unknown"),
		text_html("text/html", "unknown", "html", "htm"),
		text_css("text/css", "unknown", "css"),
		text_javascript("text/javascript", "unknown", "js"),
		audio_aac("audio/aac", "unknown", "aac"),
		audio_mpeg("audio/mpeg", "audio", "mp3"),
		audio_ogg("audio/ogg", "audio", "ogg"),
		audio_flac("audio/x-flac", "unknown", "flac"),
		audio_wave("audio/vnd.wave", "audio", "wav"),
		image_gif("image/gif", "image", "gif"),
		image_jpeg("image/jpeg", "image", "jpg","jpeg"),
		image_png("image/png", "image", "png"),
		image_svg_xml("image/svg+xml", "unknown", "svg"),
		image_ico("image/vnd.microsoft.icon", "image", "ico","icon"),
		video_mp4("video/mp4", "video", "mp4"),
		video_webm("video/webm", "video", "webm"),
		video_flv("video/x-flv", "unknown", "flv"),
		video_mpeg("video/mpeg", "video", "mpeg"),
		application_json("application/json", "unknown", "json");
		
		public final String print;
		public final String html5;
		public final String[] extention;
		
		private ContentType(final String print, final String html5, final String... extention) {
			this.print = print;
			this.html5 = html5;
			this.extention = extention;
		}
		
		public static ContentType getByExtention(String extention) {
			extention = extention.toLowerCase();
			for(final ContentType type : values())
				for(final String ex : type.extention)
					if(ex.equals(extention))
						return type;
			
			return text_plain;
		}
	}
}