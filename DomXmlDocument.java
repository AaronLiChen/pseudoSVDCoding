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
import java.io.File;
import org.xml.sax.SAXException;
import java.io.FileOutputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class DomXmlDocument implements XmlDocument {

    private DocumentBuilder init () {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            return builder;
        } catch (ParserConfigurationException e) {
            System.out.println(e.getMessage());
        }
        return null;
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
        try {
            HashMap<String, String> hmap = new HashMap<>();

            DocumentBuilder rBuilder = init();
            Document rDoc = rBuilder.parse(fileName);
            Element root = rDoc.getDocumentElement();
            iterativeParseElement(root, hmap);

            // System.out.println(hmap);
            return hmap;
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (SAXException e) {
            System.out.println(e.getMessage());
        }catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private Element iterativeCreateElement (Element elem, HashMap<String, String> hmap, Document wDoc) {
        String tagName = elem.getNodeName();
        NodeList children = elem.getChildNodes();
        Element writeElem = wDoc.createElement(tagName);

        for (int i = 0; i < children.getLength(); i++) {
            Node node = children.item(i);
            short nodeType = node.getNodeType();


            if (nodeType == Node.ELEMENT_NODE) {
                //in order to avoid empty TEXT_NODE, the following code is different from that in iterativeParseElement
                if (node.getChildNodes().getLength() == 1 && node.getChildNodes().item(0).getNodeType() == Node.TEXT_NODE) {
                    Element writeSubElem = wDoc.createElement(node.getNodeName());
                    writeElem.appendChild(writeSubElem);
                    // System.out.println(node.getNodeName());
                    writeSubElem.appendChild(wDoc.createTextNode(hmap.get(node.getNodeName())));
                }
                else {
                    Element writeSubElem = iterativeCreateElement((Element)node, hmap, wDoc);
                    writeElem.appendChild(writeSubElem);
                }
            }
        }
        return writeElem;
    }

    public void createXmlFromTemplate(String templateFileName, String fileName, HashMap<String, String> hmap) {
        try {
            DocumentBuilder rBuilder = init();
            Document rDoc = rBuilder.parse(templateFileName);
            Element rRoot = rDoc.getDocumentElement();

            DocumentBuilder wBuilder = init();
            Document wDoc = wBuilder.newDocument();

            if (wDoc != null) {
                Element wRoot = iterativeCreateElement(rRoot, hmap, wDoc);
                wDoc.appendChild(wRoot);

                // transform wDoc to streamOutput
                ((org.apache.crimson.tree.XmlDocument)wDoc).write(new FileOutputStream(fileName));
                // TransformerFactory transformerFactory = TransformerFactory.newInstance();
                // Transformer transformer = transformerFactory.newTransformer();
                // DOMSource source = new DOMSource(wDoc);
                // StreamResult result = new StreamResult(new File(fileName));
                // transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                // transformer.transform(source, result);
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (SAXException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } 
      //   catch (TransformerException e) {
      //       System.out.println(e.getMessage());
      // }
    }

    public static void main(String[] args) {
        DomXmlDocument dxd = new DomXmlDocument();
        HashMap<String, String> hmap;
        hmap = dxd.parseXml("encoder.xml");
        dxd.createXmlFromTemplate("encoder.xml", "encoderWrite.xml", hmap);
    }
}