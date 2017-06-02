package q2p.tagsmanager.kostyak;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import q2p.tagsmanager.engine.Assist;
import q2p.tagsmanager.engine.CompactNumber;
import q2p.tagsmanager.engine.Encoder;
import q2p.tagsmanager.engine.Request;
import q2p.tagsmanager.engine.Response;
import q2p.tagsmanager.engine.Storage;
import q2p.tagsmanager.engine.Response.ContentType;
import q2p.tagsmanager.engine.Response.ResponseCode;
import q2p.tagsmanager.kostyak.Tag.Type;

public final class MainPage {
	private static final int TAGS_PER_PAGE = 500;

	private static final String css = Storage.getResFileContents("main.css");
	private static final String js = Storage.getResFileContents("main.js");
	
	public static final boolean requestCheck(final Request request, final Response response) {
		if(!request.path.path.startsWith("/search/"))
			return false;
	
		String argument = request.path.path.substring("/search/".length());
		
		int idx = argument.indexOf('/');
		if(idx == -1)
			return flushRedirect(response, 0);

		final SearchArgs args = new SearchArgs(request);
		
		final LinkedList<Tag> buffer = new LinkedList<Tag>();
		
		for(final Tag t : Tag.tags)
			if(args.fits(t))
				buffer.addLast(t);
		
		args.sort(buffer);
				
		final int offset;
		try {
			offset = Integer.parseInt(argument.substring(0, idx));
		} catch(final NumberFormatException e) {
			return flushRedirect(response, args, 0);
		}
		if(offset < 0)
			return flushRedirect(response, args, 0);
		
		int end = offset+TAGS_PER_PAGE;
		
		if(end > buffer.size()) {
			if(offset != 0) {
				return flushRedirect(response, args, Math.max(0, buffer.size()-TAGS_PER_PAGE));
			} else {
				end = buffer.size();
			}
		}
		
		StringBuilder ret = new StringBuilder("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><title>");
		Encoder.encodeHTML(ret, "Тэги");
		ret.append("</title><style>");
		ret.append(css);
		ret.append("</style><script type=\"text/javascript\">");
		ret.append(js);
		ret.append("</script></head><body><form id=\"sidebar\" action=\"/search/0/\" method=\"get\">");
		ret.append("<p>");
		Encoder.encodeHTML(ret, "Имя");
		ret.append("</p>");
		ret.append("<input name=\"title\" type=\"search\" value=\"");
		Encoder.encodeHTML(ret, args.title);
		ret.append("\"/>");
		ret.append("<p>");
		Encoder.encodeHTML(ret, "Сортировка");
		ret.append("</p>");
		ret.append("<p><input type=\"radio\" name=\"sort\" value=\"title\"");
		if(args.sort == SearchArgs.SORT_NAME)
			ret.append(" checked=\"checked\"");
		ret.append("/> ");
		Encoder.encodeHTML(ret, "По имени");
		ret.append("</p>");
		ret.append("<p><input type=\"radio\" name=\"sort\" value=\"amount_d\"");
		if(args.sort == SearchArgs.SORT_AMOUNT_D)
			ret.append(" checked=\"checked\"");
		ret.append("/> ");
		Encoder.encodeHTML(ret, "По количеству +");
		ret.append("</p>");
		ret.append("<p><input type=\"radio\" name=\"sort\" value=\"amount_a\"");
		if(args.sort == SearchArgs.SORT_AMOUNT_A)
			ret.append(" checked=\"checked\"");
		ret.append("/> ");
		Encoder.encodeHTML(ret, "По количеству -");
		ret.append("</p>");
		ret.append("<p>");
		Encoder.encodeHTML(ret, "Типы");
		ret.append("</p>");
		for(final Type t : Tag.Type.values()) {
			ret.append("<p><input type=\"checkbox\" value=\"t\" name=\"t");
			ret.append(t.id);
			ret.append("\" value=\"amount_a\"");
			if(args.allowType[t.id])
				ret.append(" checked=\"checked\"");
			ret.append("/> ");
			Encoder.encodeHTML(ret, t.name);
			ret.append("</p>");
		}
		ret.append("<p>");
		Encoder.encodeHTML(ret, "Требования");
		ret.append("</p>");
		ret.append("<p><input type=\"checkbox\" name=\"alias\" value=\"t\"");
		if(args.requireAlias)
			ret.append(" checked=\"checked\"");
		ret.append("/> ");
		Encoder.encodeHTML(ret, "Синоним");
		ret.append("</p>");
		ret.append("<p><input type=\"checkbox\" name=\"implication\" value=\"t\"");
		if(args.requireImplication)
			ret.append(" checked=\"checked\"");
		ret.append("/> ");
		Encoder.encodeHTML(ret, "Причастие");
		ret.append("</p>");
		ret.append("<input type=\"submit\" value=\"");
		Encoder.encodeHTML(ret, "Найти");
		ret.append("\"/>");
		ret.append("</form><div id=\"content\">");
		
		if(buffer.isEmpty()) {
			ret.append("<div class=\"nothing\">");
			Encoder.encodeHTML(ret, "По запросу ничего не было найдено :(");
			ret.append("</div>");
		} else {
			if(offset != 0) {
				ret.append("<a class=\"page\" href=\"/search/");
				ret.append(Math.max(0, offset-TAGS_PER_PAGE));
				ret.append("/?");
				args.toUrl(ret);
				ret.append("\">");
				Encoder.encodeHTML(ret, "Предыдущие");
				ret.append(" (");
				ret.append(offset);
				ret.append(")</a>");
			}
			
			ListIterator<Tag> iterator = buffer.listIterator(offset);
			for(int i = end-offset; i != 0; i--) {
				final Tag tag = iterator.next();
				ret.append("<div class=\"item\" style=\"background-color:#");
				ret.append(tag.type.hexDarkerColor);
				ret.append("\"><div class=\"number\">");
				for(char c : Assist.appendChars(Tag.biggestLength, ""+tag.amount, ' ')) {
					ret.append("<div>");
					ret.append(c);
					ret.append("</div>");
				}
				ret.append("</div>");
				if(!tag.aliases.isEmpty())
					ret.append("<div class=\"info_block\">A</div>");
				if(!tag.implies.isEmpty())
					ret.append("<div class=\"info_block\">I</div>");
				if(tag.description.length() != 0)
					ret.append("<div class=\"info_block\">D</div>");
				ret.append("<a target=\"_blank\" class=\"title\" style=\"color:#");
				ret.append(tag.type.hexColor);
				ret.append("\" href=\"/view/");
				ret.append(CompactNumber.to(tag.id));
				ret.append("\">");
				Encoder.encodeHTML(ret, tag.name);
				ret.append("</a></div>");
			}

			if(end != buffer.size()) {
				ret.append("<a class=\"page\" href=\"/search/");
				ret.append(Math.min(buffer.size(), end+TAGS_PER_PAGE)-TAGS_PER_PAGE);
				
				ret.append("/?");
				args.toUrl(ret);
				ret.append("\">");
				Encoder.encodeHTML(ret, "Следующие");
				ret.append(" (");
				ret.append(buffer.size() - end);
				ret.append(")</a>");
			}
		}
		
		ret.append("</div></body></html>");
		
		return response.flushBody(ResponseCode._200_OK, ContentType.text_html, ret.toString());
	}

	private static boolean flushRedirect(final Response response, final SearchArgs args, final int offset) {
		final StringBuilder builder = new StringBuilder("/search/");
		builder.append(offset);
		builder.append("/?");
		args.toUrl(builder);
		return response.flushRedirect(builder.toString());
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