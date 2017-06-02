package q2p.tagsmanager.kostyak;

import java.util.List;
import q2p.tagsmanager.engine.CompactNumber;
import q2p.tagsmanager.engine.Encoder;
import q2p.tagsmanager.engine.Request;
import q2p.tagsmanager.engine.Response;
import q2p.tagsmanager.engine.Storage;
import q2p.tagsmanager.engine.Response.ContentType;
import q2p.tagsmanager.engine.Response.ResponseCode;
import q2p.tagsmanager.kostyak.Tag.Type;

public final class ViewPage {
	private static final String css = Storage.getResFileContents("view.css");
	private static final String js = Storage.getResFileContents("view.js");
	
	public static final boolean requestCheck(final Request request, final Response response) {
		if(!request.path.path.startsWith("/view/"))
			return false;
	
		final int id;
		try {
			id = (int)CompactNumber.from(request.path.path.substring("/view/".length()));
		} catch(final NumberFormatException e) {
			return response.flushPlainTextWithUTF_8(ResponseCode._400_Bad_Request, "Не указан номер тэга.");
		}
		
		if(id < 0 || id >= Tag.tags.size())
			return response.flushPlainTextWithUTF_8(ResponseCode._400_Bad_Request, "Номер тэга выходит за пределы.");
		
		final Tag tag = Tag.tags.get(id);
		
		StringBuilder ret = new StringBuilder("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>");
		Encoder.encodeHTML(ret, tag.name);
		ret.append("</title><style>");
		ret.append(css);
		ret.append("</style><script type=\"text/javascript\">");
		ret.append(js);
		ret.append("</script></head><body>");
		ret.append("<div class=\"flat_button\" onclick=\"save\">");
		Encoder.encodeHTML(ret, "Сохранить");
		ret.append("</div>");
		
		ret.append("<p>");
		Encoder.encodeHTML(ret, "Название:");
		ret.append("</p>");
		ret.append("<div class=\"type\" onclick=\"editName()\" style=\"background-color:#");
		ret.append(tag.type.hexDarkerColor);
		ret.append(";color:#");
		ret.append(tag.type.hexColor);
		ret.append("\">");
		Encoder.encodeHTML(ret, tag.name);
		ret.append("</div>");
		
		if(tag.description.length() != 0) {
			ret.append("<p>");
			Encoder.encodeHTML(ret, "Описание:");
			ret.append("</p>");
			ret.append("<p onclick=\"editDescription()\" style=\"cursor:pointer\">");
			Encoder.encodeHTML(ret, tag.description);
			ret.append("</p>");
		}
		
		if(!tag.implies.isEmpty()) {
			ret.append("<p>");
			Encoder.encodeHTML(ret, "Описание:");
			ret.append("</p>");
			for(int i = tag.implies.size()-1; i != -1; i--) {
				ret.append("<div class=\"type\" onclick=\"editName()\" style=\"background-color:#");
				ret.append(tag.type.hexDarkerColor);
				ret.append(";color:#");
				ret.append(tag.type.hexColor);
				ret.append("\">");
				Encoder.encodeHTML(ret, tag.name);
				ret.append("</div>");
			}
		}
		
		ret.append("<p>");
		Encoder.encodeHTML(ret, "Типы:");
		ret.append("</p>");
		for(final Type t : Tag.Type.values()) {
			ret.append("<div class=\"type\" onclick=\"setType(");
			ret.append(t.id);
			ret.append(")\" style=\"background-color:#");
			ret.append(t.hexDarkerColor);
			ret.append(";color:#");
			ret.append(t.hexColor);
			ret.append("\">");
			
			Encoder.encodeHTML(ret, t.name);
			ret.append("</div>");
		}
		
		ret.append("</body></html>");
		
		return response.flushBody(ResponseCode._200_OK, ContentType.text_html, ret.toString());
	}

	static boolean flushRedirect(final Response response, final int offset) {
		return response.flushRedirect("/search/"+offset+"/");
	}

	public static final class SearchArgs {
		public final String title;
		public final byte sort;
		public static final byte SORT_NAME = 0;
		public static final byte SORT_AMOUNT_D = 1;
		public static final byte SORT_AMOUNT_A = 2;
		public final boolean requireAlias;
		public final boolean requireImplication;
		
		public final boolean allowType[] = new boolean[Type.values().length];
		
		public SearchArgs(final Request request) {
			String arg = request.path.getArgument("title");
			
			title = arg==null?"":arg.toLowerCase();

			arg = request.path.getArgument("sort");
			
			if("title".equals(arg))
				sort = SORT_NAME;
			else if("amount_a".equals(arg))
				sort = SORT_AMOUNT_A;
			else
				sort = SORT_AMOUNT_D;

			requireAlias = "t".equals(request.path.getArgument("alias"));
			requireImplication = "t".equals(request.path.getArgument("implication"));
			
			for(final Type t : Type.values())
				allowType[t.id] = "t".equals(request.path.getArgument("t"+t.id));
		}

		public final void sort(final List<Tag> tags) {
			if(sort == SORT_NAME)
				tags.sort(TagNameComparator.INSTANCE);
			else if(sort == SORT_AMOUNT_A)
				tags.sort(TagAmountAscendingComparator.INSTANCE);
			else
				tags.sort(TagAmountDescendingComparator.INSTANCE);
		}

		public final void toUrl(final StringBuilder builder) {
			builder.append("title=");
			builder.append(title);
			builder.append("&sort=");
			if(sort == SORT_NAME)
				builder.append("title");
			else if(sort == SORT_AMOUNT_A)
				builder.append("amount_a");
			else
				builder.append("amount_d");

			if(requireAlias)
				builder.append("&alias=t");
			if(requireImplication)
				builder.append("&implication=t");
			
			for(final Type t : Type.values()) {
				if(allowType[t.id]){
					builder.append("&t");
					builder.append(t.id);
					builder.append("=t");
				}
			}
		}

		public boolean fits(final Tag tag) {
			if(title.length() != 0 && !tag.name.toLowerCase().contains(title))
				return false;

			if(requireAlias && tag.aliases.isEmpty())
				return false;
			
			if(requireImplication && tag.implies.isEmpty())
				return false;
			
			return allowType[tag.type.id];
		}
	}
}