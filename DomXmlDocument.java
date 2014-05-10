package cn.edu.ustc.aaron.common;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.HashMap;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.xml.sax.SAXException;

public class DomXmlDocument implements XmlDocument {
    private Document doc;
    private Element root;

    private void init (String fileName) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            this.doc = builder.parse(fileName);
            this.root = this.doc.getDocumentElement();
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (ParserConfigurationException e) {
            System.out.println(e.getMessage());
        } catch (SAXException e) {
            System.out.println(e.getMessage());
        }catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void iterativeParseElement (Element elem, HashMap<String, String> hmap) {
        String tagName = elem.getNodeName();
        NodeList children = elem.getChildNodes();

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            short nodeType = node.getNodeType();

            if (nodeType == Node.ELEMENT_NODE) {
                iterativeParseElement((Element)node, hmap);
            }
            else if (nodeType == Node.TEXT_NODE) {
                hmap.put(tagName, node.getNodeValue());
            }
        }
    }

    public HashMap<String, String> parseXml (String fileName) {
        HashMap<String, String> hmap = new HashMap<>();
        init(fileName);
        iterativeParseElement(root, hmap);

        // System.out.println(hmap);
        return hmap;   
    }

    public static void main(String[] args) {
        DomXmlDocument dxd = new DomXmlDocument();
        HashMap<String, String> hmap = dxd.parseXml("encoder.xml");
    }
}