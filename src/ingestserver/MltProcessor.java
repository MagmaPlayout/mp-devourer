package ingestserver;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Process an MLT file after it was generated by another class.
 * 
 * @author rombus
 */
public class MltProcessor {
    private final DocumentBuilderFactory docFactory;
    private final DocumentBuilder docBuilder;
    private final Transformer transformer;

    public MltProcessor() throws ParserConfigurationException, TransformerConfigurationException{
        this.docFactory = DocumentBuilderFactory.newInstance();
        this.docBuilder = this.docFactory.newDocumentBuilder();
        this.transformer = TransformerFactory.newInstance().newTransformer();
    }

    /**
     * Process each path.
     * It replaces the title arg with the piece ID
     *
     * @param processedFiles a list of Clip object with it's pieceId as key
     */
    public void processMlts(HashMap<Integer, Clip> processedFiles){
        Iterator it = processedFiles.entrySet().iterator();
        while(it.hasNext()) {
            try {
                Map.Entry<Integer, Clip> pair = (Map.Entry)it.next();
                int pieceId = pair.getKey();
                Clip clip = pair.getValue();
                String path = clip.getPath();

                Document doc = docBuilder.parse(path);
                NamedNodeMap mltNode = doc.getElementsByTagName("mlt").item(0).getAttributes();
                Node titleAttr = mltNode.getNamedItem("title");

                titleAttr.setTextContent(String.valueOf(pieceId));
                
                // Saves the .mlt file
                Result output = new StreamResult(path);
                Source input = new DOMSource(doc);
                transformer.transform(input, output);

            } catch (SAXException ex) {
                Logger.getLogger(MltProcessor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MltProcessor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NullPointerException ex){
                Logger.getLogger(MltProcessor.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransformerException ex) {
                Logger.getLogger(MltProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
