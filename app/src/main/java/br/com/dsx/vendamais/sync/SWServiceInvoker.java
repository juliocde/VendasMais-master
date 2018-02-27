package br.com.dsx.vendamais.sync;//package br.com.sankhya.service;

import android.util.Base64;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import br.com.dsx.vendamais.common.SankhyaException;
import br.com.dsx.vendamais.common.Util;


public class SWServiceInvoker {

	private String	domain;
	private String	user;
	private String	pass;
	private boolean	debug;
    private boolean	silentMode;
	private String jsessionid;
	private static final String ENCONDING = "ISO-8859-1";

	public SWServiceInvoker(String domain, String user, String pass) {
		this.domain = domain;
		this.user = user;
		this.pass = pass;
		this.jsessionid = null;
	}

	public Document call(String serviceName, String module, String body) throws Exception {

		//Se jsessionid estiver nulo é porque está deslogado.
		if (jsessionid == null){
			jsessionid = doLogin();
		}

		URLConnection conn = openConn(serviceName, module, jsessionid);
		Document docResp = callService(conn, body, serviceName);

		return docResp;
	}
	
	public String callJSON(String serviceName, String module, String body) throws Exception {

		//Se jsessionid estiver nulo é porque está deslogado.
		if (jsessionid == null){
			jsessionid = doLogin();
		}
		URLConnection conn = openConnJSON(serviceName, module, jsessionid);

		return callServiceJSON(conn, body);
	}

	public String doLogin() throws Exception {
		if (domain == null || Util.isBlank(domain))
			throw new Exception("URL Sankhya em branco. Usuário sem acesso.");

		URLConnection conn = openConn("MobileLoginSP.login", "mge", null);
		StringBuilder bodyBuf = new StringBuilder();
		bodyBuf.append("<NOMUSU>").append(user).append("</NOMUSU>");
		bodyBuf.append("<INTERNO>").append(pass).append("</INTERNO>");
		Document docResp = callService(conn, bodyBuf.toString(), "MobileLoginSP.login");
		Node jsessionNode = (Node) xpath(docResp, "//jsessionid", XPathConstants.NODE);
		return jsessionNode.getTextContent().trim();
	}

	private void doLogout() throws Exception {

		try {
			URLConnection conn = openConn("MobileLoginSP.logout", "mge", jsessionid);
			callService(conn, null, "MobileLoginSP.logout");
		} catch (Exception e) {
			e.printStackTrace(); // pode ser ignorado
		}

		jsessionid = null;
	}

	private void checkResultStatus(Node sr) throws Exception {

		Node statusNode = sr.getAttributes().getNamedItem("status");
		String status = statusNode.getTextContent().trim();
		if (!"1".equals(status) && !silentMode) {
			String msg = getChildNode("statusMessage", sr).getTextContent();
			throw new SankhyaException(decodeB64(msg.trim()));
		}
	}

	private Node getChildNode(String name, Node parent) {
		NodeList l = parent.getChildNodes();

		for (int i = 0; i < l.getLength(); i++) {
			Node n = l.item(i);
			if (n.getNodeName().equalsIgnoreCase(name)) {
				return n;
			}
		}
		return null;
	}

	private String decodeB64(String s) {
		String text = null;
		String text2;
		byte[] data = Base64.decode(s, Base64.DEFAULT);
		try {
			text = new String(data, ENCONDING);
		} catch (UnsupportedEncodingException e1){
			e1.printStackTrace();
		}
		return text;
		
	}

	public static Object xpath(Document d, String query, QName type) throws Exception {

		XPath xp = XPathFactory.newInstance().newXPath();
		XPathExpression xpe = xp.compile(query);
		return xpe.evaluate(d, type);
	}

	private void printNode(Node n) {

		NodeList l = n.getChildNodes();
		if (l != null && l.getLength() > 0) {
			for (int i = 0; i < l.getLength(); i++) {
				printNode(l.item(i));
			}
		}
	}

	private Document callService(URLConnection conn, String body, String serviceName) throws Exception {
		OutputStream out = null;
		InputStream inp = null;

		try {
			out = conn.getOutputStream();

			//MUDEI PARA UTF-8 by salazar.
			//Voltei para ISO-8859-1 dia 30/05
			OutputStreamWriter wout = new OutputStreamWriter(out, ENCONDING);

			String requestBody = buildRequestBody(body, serviceName);

			wout.write(requestBody);

			wout.flush();

			inp = conn.getInputStream();

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document doc;
			NodeList nodes;

			try {
				doc = db.parse(inp);

				nodes = doc.getElementsByTagName("serviceResponse");
			
				if (nodes == null || nodes.getLength() == 0) {
					throw new Exception("XML de resposta não possui um elemento de resposta");
				}
			} catch (Exception e) {
				throw new Exception("Erro ao interpretar resposta do servidor");
			}

			checkResultStatus(nodes.item(0));

			return doc;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception ignored) {
				}
			}

			if (inp != null) {
				try {
					inp.close();
				} catch (Exception ignored) {
				}
			}
		}
	}


	private URLConnection openConnJSON(String serviceName, String module, String sessionID) throws Exception {
		StringBuilder buf = new StringBuilder();

		buf.append(domain)
				.append(domain.endsWith("/") ? "" : "/")
				.append(module == null ? "mge" : module)
				.append("/service.sbr")
				.append("?serviceName=").append(serviceName)
				.append("&outputType=json")
				.append("&preventTransform=false");
		
		if (sessionID != null) {
			buf.append("&mgeSession=").append(sessionID);
		}

		//buf.append("&resourceID=br.com.sankhya.DbExplorer");

		URL u = new URL(buf.toString());

		URLConnection uc = u.openConnection();
		HttpURLConnection connection = (HttpURLConnection) uc;

		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("content-type", "application/json");
		
		if(sessionID != null) {
			connection.setRequestProperty("Cookie", "JSESSIONID=" + sessionID);
		}
		
		connection.setRequestProperty("User-Agent", "SWServiceInvoker");

		return connection;
	}


	private URLConnection openConn(String serviceName, String module, String sessionID) throws Exception {
		StringBuilder buf = new StringBuilder();

		buf.append(domain);
		buf.append(domain.endsWith("/") ? "" : "/");
		buf.append(module == null ? "mge" : module);
		buf.append("/service.sbr");
		buf.append("?serviceName=").append(serviceName);

		if (sessionID != null) {
			buf.append("&mgeSession=").append(sessionID);
		}

		URL u = new URL(buf.toString());

		URLConnection uc = u.openConnection();
		HttpURLConnection connection = (HttpURLConnection) uc;

		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("content-type", "text/xml");

		if(sessionID != null) {
			connection.setRequestProperty("Cookie", "JSESSIONID=" + sessionID);
		}

		connection.setRequestProperty("User-Agent", "SWServiceInvoker");

		return connection;
	}

	
	public static void printDocument(Document doc) throws Exception {
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		transformer.transform(source, result);
		String xmlString = result.getWriter().toString();
		System.out.println("----inicio---");
		System.out.println(xmlString);
		System.out.println("----fim-----");
	}
	
	private String callServiceJSON(URLConnection conn, String body) throws Exception {
		OutputStream out = null;
		InputStream inp = null;

		try {
			

			//String requestBody = buildRequestBody(body, serviceName);
			if (debug) {
				System.out.println("---- INICIO REQUEST BODY ----");
				System.out.println(body);
				System.out.println("---- FIM REQUEST BODY -----------------------------------------");
			}
			out = conn.getOutputStream();
			OutputStreamWriter wout = new OutputStreamWriter(out, ENCONDING);
			
			wout.write(body);

			wout.flush();

			inp = conn.getInputStream();

			String retorno = getStringFromInputStream(inp);
			
			if (debug) {
				System.out.println("---- INICIO RETORNO -----------------------------------------");
				System.out.println(retorno);
				System.out.println("---- FIM RETORNO -----------------------------------------");
			}
			

			return retorno;
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (Exception ignored) {
				}
			}

			if (inp != null) {
				try {
					inp.close();
				} catch (Exception ignored) {
				}
			}
		}
	}

	// convert InputStream to String
	public String getStringFromInputStream(InputStream entityResponse) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = 0;
		while ((length = entityResponse.read(buffer)) != -1) {
			baos.write(buffer, 0, length);
		}
		return baos.toString( ENCONDING );
	}

	/**
	 *
	 * @param body principal da requisição.
	 * @param serviceName do serviço no SankhyaW. Por exemplo DbExplorerSP.executeQuery
	 * @return Retorna o XML que será utilizado na requisição
	 */
	private String buildRequestBody(String body, String serviceName) {
		StringBuilder buf = new StringBuilder();

		buf.append("<?xml version=\"1.0\" encoding=\"");
		buf.append(ENCONDING);
		buf.append("\"?>\n");
		buf.append("<serviceRequest serviceName=\"").append(serviceName).append("\">\n");
		buf.append("<requestBody>\n");
		buf.append(body == null ? "" : body);
		buf.append("</requestBody>\n");
		buf.append("</serviceRequest>");

		return buf.toString();
	}


	public static String buildRequestQueryJSON(String sql){
		return "{"
				+ "\"serviceName\": \"DbExplorerSP.executeQuery\","
				+ "\"requestBody\": {"
				+ "\"sql\": \""
				+ sql
				+ "\""
				+ "}}";
	}

}
