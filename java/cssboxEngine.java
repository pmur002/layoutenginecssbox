
import java.io.PrintWriter;
import java.net.URL;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.fit.cssbox.css.CSSNorm;
import org.fit.cssbox.css.DOMAnalyzer;
import org.fit.cssbox.css.NormalOutput;
import org.fit.cssbox.css.Output;
import org.fit.cssbox.io.DOMSource;
import org.fit.cssbox.io.DefaultDOMSource;
import org.fit.cssbox.io.DefaultDocumentSource;
import org.fit.cssbox.io.DocumentSource;
import org.fit.cssbox.layout.BrowserCanvas;
import org.fit.cssbox.layout.Box;
import org.fit.cssbox.layout.CSSDecoder;
import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.TextBox;
import org.fit.cssbox.layout.VisualContext;

import org.w3c.dom.Document;

public class cssboxEngine {

    // Use element 'id' if that exists, 
    // otherwise, element tag name plus child index
    private String elementName(ElementBox el, String tagName,
                               int index, String parentName) {
        String id = el.getElement().getAttribute("id");
        if (id.length() > 0) {
            return parentName + "." + tagName + "." + id;
        } else {
            return parentName + "." + tagName + "." + index;
        }
    }

    private String textName(int index, String parentName) {
        return parentName + "." + "TEXT" + "." + index;
    }

    private String borderWidth(ElementBox el, String which) {
        String borderWidth = 
            el.getStylePropertyValue(which);
        if (borderWidth.length() == 0) {
            return "NA";
        } else {
            return borderWidth.replaceAll("px", "");
        }        
    }

    private String boxInfo(Box box, DOMAnalyzer da, 
                           int index, String parentName) {
        String result = "";
        if (box instanceof ElementBox) {
            ElementBox el = (ElementBox) box;
            Rectangle bbox = el.getAbsoluteContentBounds();
            VisualContext vc = el.getVisualContext();
            CSSDecoder dec = new CSSDecoder(vc);
            String tagName = el.getElement().getTagName();
            String elName = elementName(el, tagName, index, parentName);
            result = tagName + "," +
                elName + "," + 
                bbox.getX() + "," +
                bbox.getY() + "," +
                bbox.getWidth() + "," +
                bbox.getHeight() + "," +
                // Text is empty, as is font name, 
                // as is font face (bold or italic), as is font size 
                "NA,NA,NA,NA,NA" + "," + 
                // Does the element display anything?
                String.valueOf(el.affectsDisplay()).toUpperCase() + "," +
                borderWidth(el, "border-left-width") + "," +
                borderWidth(el, "border-top-width") + "," +
                borderWidth(el, "border-right-width") + "," +
                borderWidth(el, "border-bottom-width") + 
                "\n";
            if (el.getSubBoxNumber() > 0) {
                for (int i = el.getStartChild(); i < el.getEndChild(); i++) {
                    Box sub = el.getSubBox(i);
                    result = result + 
                        boxInfo(sub, da, i + 1, elName);
                }
            }
        } else if (box instanceof TextBox) {
            TextBox text = (TextBox) box;
            Rectangle bbox = text.getAbsoluteBounds();
            VisualContext vc = text.getVisualContext();
            Font font = vc.getFont();
            result = "TEXT" + "," + 
                textName(index, parentName) + "," +
                bbox.getX() + "," +
                bbox.getY() + "," +
                bbox.getWidth() + "," +
                text.getTotalLineHeight() + "," +
                text.getText() + "," +
                font.getFamily() + "," +
                String.valueOf(font.isBold()).toUpperCase() + "," +
                String.valueOf(font.isItalic()).toUpperCase() + "," +
                vc.getFontSize() + "," + 
                // affectsDisplay not used
                "NA" + "," +
                // No border properties
                "NA,NA,NA,NA" + 
                "\n";
        }
        return result;
    }

    public String layout(String HTMLfile, int width, int height,
                         boolean fractionalMetrics) {

        String result = "";

        try {
            //Open the network connection 
            DocumentSource docSource = new DefaultDocumentSource(HTMLfile);
            //Parse the input document
            DOMSource parser = new DefaultDOMSource(docSource);
            //doc represents the obtained DOM
            Document doc = parser.parse(); 
            DOMAnalyzer da = new DOMAnalyzer(doc, docSource.getURL());
            //convert the HTML presentation attributes to inline styles
            da.attributesToStyles(); 
            //use the standard style sheet
            da.addStyleSheet(null, CSSNorm.stdStyleSheet(), 
                             DOMAnalyzer.Origin.AGENT); 
            //use the additional style sheet
            da.addStyleSheet(null, CSSNorm.userStyleSheet(), 
                             DOMAnalyzer.Origin.AGENT); 
            //load the author style sheets
            da.getStyleSheets(); 

            // Configure the BrowserCanvas
            Dimension dim = new Dimension(width, height);
            BrowserCanvas browser =  new BrowserCanvas(da.getRoot(),
                                                       da,
                                                       dim,
                                                       new URL(HTMLfile),
                                                       fractionalMetrics);

            ElementBox box = browser.getRootBox();
            
            result = boxInfo(box, da, 1, "ROOT");
            
            docSource.close();

        } catch (Exception e) {
            result = "Error: " + e.getMessage();
        }        

        return result;
    }

    public static void main(String[] args) {
    }

}

