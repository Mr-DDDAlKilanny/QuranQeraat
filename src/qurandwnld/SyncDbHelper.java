/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qurandwnld;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Yasser
 */
final class SyncDbHelper {
    
    static class ServerResponse {
        public int code;
        public String message;
    }
    
    private static String getSendXml() {
        StringBuilder b = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<submit>\n");
        Connection c = DbHelper.CONNECTION;
        try {
            try (Statement s = c.createStatement()) {
                b.append("<DorrahShahed>\n");
                try (ResultSet res = s.executeQuery("SELECT * FROM DorrahShahed")) {
                    while (res.next()) {
                        b.append(String.format("<row DetailID=\"%d\" BeitID=\"%d\" BeitPart=\"%d\" />\n",
                                res.getInt("DetailID"), res.getInt("BeitID"), res.getInt("BeitPart")));
                    }
                }
                b.append("</DorrahShahed>\n");
                b.append("<Khlaf>\n");
                try (ResultSet res = s.executeQuery("SELECT * FROM Khlaf")) {
                    while (res.next()) {
                        b.append(String.format("<row RewayahID=\"%d\" HasKholf=\"%d\" KhlafGroupID=\"%d\" />\n",
                                res.getInt("RewayahID"), res.getInt("HasKholf"), res.getInt("KhlafGroupID")));
                    }
                }
                b.append("</Khlaf>\n");
                b.append("<KhlafGroup>\n");
                try (ResultSet res = s.executeQuery("SELECT * FROM KhlafGroup")) {
                    while (res.next()) {
                        b.append(String.format("<row _ID=\"%d\" MawdeaKhlafDetailID=\"%d\">%s</row>\n",
                                res.getInt("_ID"), res.getInt("MawdeaKhlafDetailID"), res.getString("Description")));
                    }
                }
                b.append("</KhlafGroup>\n");
                b.append("<MawdeaKhlaf>\n");
                try (ResultSet res = s.executeQuery("SELECT * FROM MawdeaKhlaf")) {
                    while (res.next()) {
                        b.append(String.format("<row _ID=\"%d\" PageNumber=\"%d\" DetailID=\"%d\" x1=\"%f\" y1=\"%f\" x2=\"%f\" y2=\"%f\" />\n",
                                res.getInt("_ID"), res.getInt("PageNumber"), res.getInt("DetailID"), res.getFloat("x1"), res.getFloat("y1"), res.getFloat("x2"), res.getFloat("y2")));
                    }
                }
                b.append("</MawdeaKhlaf>\n");
                b.append("<MawdeaKhlafDetail>\n");
                try (ResultSet res = s.executeQuery("SELECT * FROM MawdeaKhlafDetail")) {
                    while (res.next()) {
                        b.append(String.format("<row _ID=\"%d\" OwnerMawdeaKhlaf=\"%d\" KhlafType=\"%d\" />\n",
                                res.getInt("_ID"), res.getInt("OwnerMawdeaKhlaf"), res.getInt("KhlafType")));
                    }
                }
                b.append("</MawdeaKhlafDetail>\n");
                b.append("<ShatibiyyahShahed>\n");
                try (ResultSet res = s.executeQuery("SELECT * FROM ShatibiyyahShahed")) {
                    while (res.next()) {
                        b.append(String.format("<row DetailID=\"%d\" BeitID=\"%d\" BeitPart=\"%d\" />\n",
                                res.getInt("DetailID"), res.getInt("BeitID"), res.getInt("BeitPart")));
                    }
                }
                b.append("</ShatibiyyahShahed>\n");
            }
        } catch (SQLException ex) {
            Logger.getLogger(SyncDbHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return b.append("</submit>").toString();
    }
    
    public static ServerResponse sendMySelectionsToServer(String username, String password) throws Exception {
        String urlParameters = String.format("username=%s&password=%s&xmlData=%s",
                URLEncoder.encode(username, "UTF-8"),
                URLEncoder.encode(password, "UTF-8"),
                URLEncoder.encode(getSendXml(), "UTF-8"));
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;
        String request = "http://localhost:55622/Server/SubmitUserSelections";
        URL url = new URL(request);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setUseCaches(false);
        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(postData);
        }
        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder json = new StringBuilder();
        for (int c = in.read(); c != -1; c = in.read())
            json.append((char)c);
        JSONObject obj = new JSONObject(json.toString());
        ServerResponse res = new ServerResponse();
        res.code = obj.getInt("Code");
        res.message = obj.getString("Message");
        return res;
    }
    
    public static ServerResponse bringMorag3ahFromServer(String username, String password) throws Exception {
        String urlParameters = String.format("username=%s&password=%s",
                URLEncoder.encode(username, "UTF-8"),
                URLEncoder.encode(password, "UTF-8"));
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;
        String request = "http://localhost:55622/Server/GetMorag3ahSelections";
        URL url = new URL(request);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("charset", "utf-8");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setUseCaches(false);
        try (DataOutputStream wr = new DataOutputStream(conn.getOutputStream())) {
            wr.write(postData);
        }
        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        StringBuilder json = new StringBuilder();
        for (int c = in.read(); c != -1; c = in.read())
            json.append((char)c);
        String str = json.toString();
        int idx = str.indexOf(":");
        ServerResponse res = new ServerResponse();
        res.code = Integer.parseInt(str.substring(0, idx));
        res.message = str.substring(idx + 2);
        if (res.code == 200) {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc;
            try (ByteArrayInputStream stream = new ByteArrayInputStream(
                    res.message.getBytes(StandardCharsets.UTF_8))) {
                doc = dBuilder.parse(stream);
            }
            res.message = null;
            doc.getDocumentElement().normalize();
            Connection c = DbHelper.CONNECTION;
            try (Statement s = c.createStatement()) {
                s.executeUpdate("DELETE FROM DorrahShahed_Others");
                NodeList nList = doc.getElementsByTagName("DorrahShahed");
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        s.executeUpdate(String.format("INSERT INTO DorrahShahed_Others (DetailID, BeitID, BeitPart, IsNew, IsDeleted) "
                                + "VALUES (%d, %d, %d, 0, 0)", 
                                Integer.parseInt(eElement.getAttribute("DetailID")),
                                Integer.parseInt(eElement.getAttribute("BeitID")),
                                Integer.parseInt(eElement.getAttribute("BeitPart"))));
                    }
                }
                
                s.executeUpdate("DELETE FROM ShatibiyyahShahed_Others");
                nList = doc.getElementsByTagName("ShatibiyyahShahed");
                for (int temp = 0; temp < nList.getLength(); temp++) {
                    Node nNode = nList.item(temp);
                    if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                        Element eElement = (Element) nNode;
                        s.executeUpdate(String.format("INSERT INTO ShatibiyyahShahed_Others (DetailID, BeitID, BeitPart, IsNew, IsDeleted) "
                                + "VALUES (%d, %d, %d, 0, 0)", 
                                Integer.parseInt(eElement.getAttribute("DetailID")),
                                Integer.parseInt(eElement.getAttribute("BeitID")),
                                Integer.parseInt(eElement.getAttribute("BeitPart"))));
                    }
                }
            }
        }
        return res;
    }
    
    public static ServerResponse sendMorag3ahResultsToServer(String username, String password) throws Exception {
        
        //TODO: before return refresh data
        return bringMorag3ahFromServer(username, password);
    }
    
    private SyncDbHelper() {
    }
}
