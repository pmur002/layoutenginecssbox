
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
import org.fit.cssbox.layout.ElementBox;
import org.fit.cssbox.layout.TextBox;
import org.fit.cssbox.layout.VisualContext;

import org.w3c.dom.Document;

public class cssboxEngine {

    private String boxInfo(Box box) {
        String result = "";
        if (box instanceof ElementBox) {
            ElementBox el = (ElementBox) box;
            Rectangle bbox = el.getAbsoluteContentBounds();
            result = el.getElement().getTagName() + "," + 
                bbox.getX() + "," +
                bbox.getY() + "," +
                bbox.getWidth() + "," +
                bbox.getHeight() + "," +
                // Text is empty, as is font name, 
                // as is font face (bold or italic), as is font size 
                ",NA,NA,NA,NA,NA\n";
            if (el.getSubBoxNumber() > 0) {
                for (int i = el.getStartChild(); i < el.getEndChild(); i++) {
                    Box sub = el.getSubBox(i);
                    result = result + boxInfo(sub);
                }
            }
        } else if (box instanceof TextBox) {
            TextBox text = (TextBox) box;
            Rectangle bbox = text.getAbsoluteBounds();
            VisualContext vc = text.getVisualContext();
            Font font = vc.getFont();
            result = "TEXT," +
                bbox.getX() + "," +
                bbox.getY() + "," +
                bbox.getWidth() + "," +
                text.getTotalLineHeight() + "," +
                text.getText() + "," +
                font.getFamily() + "," +
                font.isBold() + "," +
                font.isItalic() + "," +
                vc.getFontSize() + "\n";
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
            
            result = boxInfo(box);
            
            docSource.close();

        } catch (Exception e) {
            result = "Error: " + e.getMessage();
        }        

        return result;
    }

    public static void main(String[] args) {
    }

}

