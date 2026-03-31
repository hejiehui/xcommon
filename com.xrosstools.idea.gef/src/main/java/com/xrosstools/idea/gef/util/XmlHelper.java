package com.xrosstools.idea.gef.util;

import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.List;

public class XmlHelper {
	public static boolean isValidNode(Node node) {
		return !node.getNodeName().equals("#text");
	}
	
	public static boolean isValidNode(Node node, String name) {
		return node.getNodeName().equals(name);
	}
	
	public static Node getFirstValidNode(Node node) {
		NodeList children = node.getChildNodes();
		for(int i = 0; i < children.getLength(); i++){
			if(isValidNode(children.item(i)))
				return children.item(i);
		}
		
		return null;
	}
	
	public static List<Node> getValidChildNodes(Node node) {
		List<Node> nl = new ArrayList();
		if(node == null)
		    return nl;
		
		NodeList nodeList = node.getChildNodes();
		for(int i = 0; i < nodeList.getLength(); i++){
			if(isValidNode(nodeList.item(i)))
				nl.add(nodeList.item(i));
		}
		return nl;
	}

	public static List<Node> getValidChildNodes(Node node, String name) {
		List<Node> nl = new ArrayList();
		if(node == null)
			return nl;

		NodeList nodeList = node.getChildNodes();
		for(int i = 0; i < nodeList.getLength(); i++){
			if(isValidNode(nodeList.item(i), name))
				nl.add(nodeList.item(i));
		}
		return nl;
	}


	public static int getIntAttribute(Node node, String attributeName, int defaultValue) {
		String value = getAttribute(node, attributeName);
		return value == null ? defaultValue : Integer.parseInt(value);
	}

	public static int getIntAttribute(Node node, String attributeName) {
		return Integer.parseInt(getAttribute(node, attributeName));
	}

	public static float getFloatAttribute(Node node, String attributeName, float defaultValue) {
		String value = getAttribute(node, attributeName);
		return value == null ? defaultValue : Float.parseFloat(value);
	}

	public static String getAttribute(Node node, String attributeName) {
		NamedNodeMap map = node.getAttributes();
		for (int i = 0; i < map.getLength(); i++) {
			if (attributeName.equals(map.item(i).getNodeName()))
				return map.item(i).getNodeValue();
		}

		return null;
	}

	public static String getAttribute(Node node, String attributeName, String defaultValue) {
		NamedNodeMap map = node.getAttributes();
		for (int i = 0; i < map.getLength(); i++) {
			if (attributeName.equals(map.item(i).getNodeName()))
				return map.item(i).getNodeValue();
		}

		return defaultValue;
	}

	public static String getChildNodeText(Node node, String childName) {
		Node child = getChildNode(node, childName);
		if(child == null)
			return null;

		return child.getTextContent();
	}

	public static Node getChildNode(Node node, String name) {
		List<Node> children = getValidChildNodes(node);
		Node found = null;
		for(int i = 0; i < children.size(); i++){
			if(!children.get(i).getNodeName().equalsIgnoreCase(name))
				continue;
			found = children.get(i);
			break;
		}
		return found;
	}

	public static boolean hasAttribute(Node node, String attributeName) {
		NamedNodeMap map = node.getAttributes();
		for (int i = 0; i < map.getLength(); i++) {
			if (attributeName.equals(map.item(i).getNodeName()))
				return true;
		}

		return false;
	}

	public static Element createNode(Document doc, String nodeName, String value) {
		Element node = doc.createElement(nodeName);
		if (value != null)
			node.appendChild(doc.createTextNode(value));
		return node;
	}

	public static Element createNode(Document doc, Element parent, String name) {
		Element node = (Element)doc.createElement(name);
		parent.appendChild(node);
		return node;
	}

	public static Element createTextNode(Document doc, Element node, String name, String value) {
		Element textNode = (Element)doc.createElement(name);
		node.appendChild(textNode);
		if(value == null)
			return textNode;

		textNode.appendChild(doc.createTextNode(value));
		return textNode;
	}


// For format xml, please use the following code segment

//import org.dom4j.io.OutputFormat;
//import org.dom4j.io.SAXReader;
//import org.dom4j.io.XMLWriter;

//	public static String format(Document doc) throws Exception {
//    	ByteArrayOutputStream out = new ByteArrayOutputStream();
//    	TransformerFactory tFactory =TransformerFactory.newInstance();
//    	Transformer transformer = tFactory.newTransformer();
//    	DOMSource source = new DOMSource(doc);
//    	StreamResult result = new StreamResult(out);
//    	transformer.transform(source, result);
//
//    	// To make well formated document
//    	SAXReader reader = new SAXReader();
//    	org.dom4j.Document document = reader.read(new ByteArrayInputStream(out.toByteArray()));
//
//    	XMLWriter writer = null;
//        StringWriter stringWriter = new StringWriter();
//        OutputFormat format = new OutputFormat(" ", true);
//        writer = new XMLWriter(stringWriter, format);
//        writer.write(document);
//        writer.flush();
//        return stringWriter.toString();
//    }
}
