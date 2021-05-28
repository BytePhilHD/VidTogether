package gg.jte.generated;
public final class JtestartpageGenerated {
	public static final String JTE_NAME = "startpage.jte";
	public static final int[] JTE_LINE_INFO = {0,0,0,0,5,5,5,5,13};
	public static void render(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, app.HelloPage page) {
		jteOutput.writeContent("\r\n<html lang=\"en\">\r\n<body>\r\n<p>Hello visitor!</p>\r\n<p>The <b>user of the day</b> is ");
		jteOutput.setContext("p", null);
		jteOutput.writeUserContent(page.userName);
		jteOutput.writeContent(" </p>\r\n\r\n<h1>Upload example</h1>\r\n<form method=\"post\" action=\"/upload-example\" enctype=\"multipart/form-data\">\r\n    <input type=\"file\" name=\"files\" multiple>\r\n    <button>Submit</button>\r\n</form>\r\n</body>\r\n</html>");
	}
	public static void renderMap(gg.jte.html.HtmlTemplateOutput jteOutput, gg.jte.html.HtmlInterceptor jteHtmlInterceptor, java.util.Map<String, Object> params) {
		app.HelloPage page = (app.HelloPage)params.get("page");
		render(jteOutput, jteHtmlInterceptor, page);
	}
}
