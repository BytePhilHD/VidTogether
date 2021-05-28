package gg.jte.generated;
public final class JtestartpageGenerated {
	public static final String JTE_NAME = "startpage.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,13,13,13,13,21};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, app.HelloPage page) {
		jteOutput.writeContent("\r\n<html lang=\"en\">\r\n<meta charset=\"UTF-8\">\r\n<head>\r\n    <script>\r\n        ws = new WebSocket(\"ws://\" + location.hostname + \":\" + location.port + \"/websockets\");\r\n        ws.close();\r\n    </script>\r\n</head>\r\n<body>\r\n<p>Hallöle</p>\r\n<p>Hier kannst du was hochladen! </p>\r\n<p>Aktuelle Uhrzeit: <b>");
		jteOutput.setContext("b", null);
		jteOutput.writeUserContent(page.getTime());
		jteOutput.writeContent("</b></p>\r\n\r\n<h1>Upload example</h1>\r\n<form method=\"post\" action=\"/upload-example\" enctype=\"multipart/form-data\">\r\n    <input type=\"file\" name=\"files\" multiple>\r\n    <button>Submit</button>\r\n</form>\r\n</body>\r\n</html>");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		app.HelloPage page = (app.HelloPage)params.get("page");
		render(jteOutput, jteHtmlInterceptor, page);
	}
}
