/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qurandwnld;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author ibraheem
 */
public class WriteJFrame1 extends javax.swing.JFrame {
    
    private final String settingFile = "data.xml";
    
    private final ArrayList<Selection>[] sel;
    private static final int MAX_PAGE = 604;
    private final int pageSizes[][] = new int[MAX_PAGE][2];
    
    int page;
    
    Selection current;

    private PaintSurface getSurface() {
        return (PaintSurface) jPanel1;
    }
    
    public static void rtlLayout(Container c) {
        c.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        for (Component comp : c.getComponents())
            if (comp instanceof Container)
                rtlLayout((Container) comp);
    }
    
    /**
     * Creates new form NewJFrame1
     */
    public WriteJFrame1() {
        this.sel = new ArrayList[MAX_PAGE];
        for (int i = 0; i < sel.length; i++) {
            sel[i] = new ArrayList<>();
        }
        initComponents();
        readPageSizes();
        readSetting();
        rtlLayout(this);
        jSpinner1.setModel(new SpinnerNumberModel(1, 1, MAX_PAGE, 1));
        showPage(page = 1);
    }
    
    private void readSetting() {
        File f = new File(settingFile);
        if (!f.exists())
            return;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder;
        PaintSurface surface = getSurface();
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("selection");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    int p = Integer.parseInt(eElement.getAttribute("page"));
                    String[] n = eElement.getAttribute("rect").split(",");
                    Selection s = new Selection();
                    s.rect = surface.getScaledRectFromImageRect(new Dimension(pageSizes[p - 1][0], pageSizes[p - 1][1]),
                            new Rectangle2D.Float(Integer.parseInt(n[0]),
                            Integer.parseInt(n[1]), Integer.parseInt(n[2]), Integer.parseInt(n[3])));
                    s.type = SelectionType.fromValue(Integer.parseInt(eElement.getAttribute("type")));
                    s.page = p;
                    s.shahed = eElement.getElementsByTagName("shahed").item(0).getTextContent();
                    s.descr = eElement.getElementsByTagName("descr").item(0).getTextContent();
                    
                    sel[p - 1].add(s);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(WriteJFrame1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void readPageSizes() {
        File f = new File(getClass().getClassLoader().getResource("quran-data.xml").getFile());
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("pagedim");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    int page = Integer.parseInt(eElement.getAttribute("index"));
                    int width = Integer.parseInt(eElement.getAttribute("width"));
                    int height = Integer.parseInt(eElement.getAttribute("height"));
                    pageSizes[page - 1][0] = width;
                    pageSizes[page - 1][1] = height;
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(WriteJFrame1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String getSurahByPage(int p) {
        File f = new File(getClass().getClassLoader().getResource("quran-data.xml").getFile());
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("page");
            String s = null;
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    if (eElement.getAttribute("index").equals(p + "")) {
                        s = eElement.getAttribute("sura");
                        break;
                    }
                }
            }
            if (s == null)
                return null;
            nList = doc.getElementsByTagName("sura");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    if (eElement.getAttribute("index").equals(s)) {
                        return eElement.getAttribute("name");
                    }
                }
            }
            return null;
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(WriteJFrame1.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPopupMenu1 = new javax.swing.JPopupMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jPanel1 = new PaintSurface();
        jSpinner1 = new javax.swing.JSpinner();

        jMenuItem1.setText("تعديل...");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem1);

        jMenuItem2.setText("حذف");
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem2);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("تسجيل القراءات");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 393, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jSpinner1.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jSpinner1StateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(94, 94, 94)
                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(263, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(128, 128, 128)
                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(492, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jSpinner1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner1StateChanged
        showPage((int) jSpinner1.getValue());
    }//GEN-LAST:event_jSpinner1StateChanged

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            PaintSurface surface = getSurface();
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("selections");
            for (int i = 0; i < sel.length; ++i) {
                for (int j = 0; j < sel[i].size(); ++j) {
                    Element staff = doc.createElement("selection");
                    rootElement.appendChild(staff);
                    
                    Attr attr = doc.createAttribute("page");
                    attr.setValue("" + (i + 1));
                    staff.setAttributeNode(attr);
                    
                    attr = doc.createAttribute("rect");
                    Rectangle2D.Float rect = surface.getImageRectFromScaled(
                            new Dimension(pageSizes[i][0], pageSizes[i][1]),
                            (Rectangle2D.Float) sel[i].get(j).rect);
                    attr.setValue(String.format("%d,%d,%d,%d", (int) rect.x,
                            (int) rect.y, (int) rect.width, (int) rect.height));
                    staff.setAttributeNode(attr);
                    
                    attr = doc.createAttribute("type");
                    attr.setValue(String.format("%d", sel[i].get(j).type.getValue()));
                    staff.setAttributeNode(attr);
                    
                    Element el = doc.createElement("shahed");
                    staff.appendChild(el);
                    el.appendChild(doc.createTextNode(sel[i].get(j).shahed));
                    
                    el = doc.createElement("descr");
                    staff.appendChild(el);
                    el.appendChild(doc.createTextNode(sel[i].get(j).descr));
                }
            }
            doc.appendChild(rootElement);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(settingFile));
            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);
        } catch (ParserConfigurationException | TransformerException ex) {
            Logger.getLogger(WriteJFrame1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_formWindowClosing

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        InputQeraatJDialog j = new InputQeraatJDialog(this, true);
        j.type(current.type);
        j.shahed(current.shahed);
        j.descr(current.descr);
        if (JOptionPane.showConfirmDialog(this,
                        j.getContentPane(),
                        "تحرير بيانات القراءة",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            current.descr = j.descr();
            current.shahed = j.shahed();
            current.type = j.type();
        }
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        if (JOptionPane.showConfirmDialog(this, "متأكد من رغبتك في حذف هذا الموضع؟",
                "تأكيد", JOptionPane.OK_CANCEL_OPTION)
                == JOptionPane.OK_OPTION) {
            getSurface().shapes.remove(current);
            getSurface().repaint();
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void showPage(int p) {
        try {
            jSpinner1.setValue(p);
            ((TitledBorder) getSurface().getBorder()).setTitle("سورة " + getSurahByPage(p));
            getSurface().shapes = sel[p - 1];
            String path = String.format("quran/%04d.jpg", p);
            getSurface().image = ImageIO.read(getClass().getClassLoader().getResource(path));
            getSurface().repaint();
        } catch (IOException ex) {
        }
    }
    
    
    /**
     * Converts an image to a binary one based on given threshold
     *
     * @param image the image to convert. Remains untouched.
     * @param threshold the threshold in [0,255]
     * @return a new BufferedImage instance of TYPE_BYTE_GRAY with only 0'S and
     * 255's
     */
    public BufferedImage thresholdImage(BufferedImage image, int threshold) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        result.getGraphics().drawImage(image, 0, 0, null);
        WritableRaster raster = result.getRaster();
        int[] pixels = new int[image.getWidth()];
        for (int y = 0; y < image.getHeight(); y++) {
            raster.getPixels(0, y, image.getWidth(), 1, pixels);
            for (int i = 0; i < pixels.length; i++) {
                if (pixels[i] < threshold) {
                    pixels[i] = 0;
                } else {
                    pixels[i] = 255;
                }
            }
            raster.setPixels(0, y, image.getWidth(), 1, pixels);
        }
        return result;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(WriteJFrame1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(WriteJFrame1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(WriteJFrame1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(WriteJFrame1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new WriteJFrame1().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    public javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JSpinner jSpinner1;
    // End of variables declaration//GEN-END:variables
}

enum SelectionType {
    Farsh(1),
    Hamz(2),
    Edgham(3),
    Emalah(4);
    
    private final int value;
    private SelectionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
    
    public static SelectionType fromValue(int t) {
        switch (t) {
            case 1:
                return Farsh;
            case 2:
                return Hamz;
            case 3:
                return Edgham;
            case 4:
                return Emalah;
            default:
                throw new IllegalArgumentException();
        }
    }
}

class Selection {
    Rectangle2D rect;
    int page;
    String shahed;
    String descr;
    SelectionType type;
}

class PaintSurface extends JPanel {

    ArrayList<Selection> shapes;

    Point startDrag, endDrag;
    
    Image image;

    public PaintSurface() {
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger())
                    isPopup(e);
                startDrag = new Point(e.getX(), e.getY());
                endDrag = startDrag;
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger())
                    isPopup(e);
                if (Math.abs(startDrag.x - e.getX()) < 3
                        || Math.abs(startDrag.y - e.getY()) < 3)
                    return;
                WriteJFrame1 f = (WriteJFrame1) PaintSurface.this.getParent().getParent().getParent().getParent();
                InputQeraatJDialog j = new InputQeraatJDialog(f, true);
                if (JOptionPane.showConfirmDialog(f,
                        j.getContentPane(),
                        "تحرير بيانات القراءة",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                    Selection s = new Selection();
                    s.page = f.page;
                    s.rect = makeRectangle(startDrag.x, startDrag.y, e.getX(), e.getY());
                    s.descr = j.descr();
                    s.shahed = j.shahed();
                    s.type = j.type();
                    shapes.add(s);
                    startDrag = null;
                    endDrag = null;
                    repaint();
                }
            }

            public void isPopup(MouseEvent e) {
                for (Selection s : shapes) {
                    if (s.rect.contains(e.getX(), e.getY())) {
                        WriteJFrame1 f = (WriteJFrame1) PaintSurface.this
                                .getParent().getParent().getParent().getParent();
                        f.current = s;
                        f.jPopupMenu1.show(e.getComponent(), e.getX(), e.getY());
                        break;
                    }
                }
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                endDrag = new Point(e.getX(), e.getY());
                repaint();
            }
        });
    }
    
    public Dimension getScaledImageSize() {
        Dimension d = ((TitledBorder) getBorder()).getMinimumSize(this);
        return new Dimension(getWidth() - d.width / 2, getHeight() - d.height);
    }
    
    public Rectangle2D.Float getImageRectFromScaled(Dimension bmp, Rectangle2D.Float r) {
        Dimension d = getScaledImageSize();
        float w = (float) bmp.width / d.width;
        float h = (float) bmp.height / d.height;
        return new Rectangle2D.Float(r.x,
                r.y,
                r.width * w,
                r.height * h);
    }
    
    public Rectangle2D.Float getScaledRectFromImageRect(Dimension bmp, Rectangle2D.Float r) {
        Dimension d = getScaledImageSize();
        float w = d.width / (float) bmp.width;
        float h = d.height / (float) bmp.height;
        return new Rectangle2D.Float(r.x,
                r.y,
                r.width * w,
                r.height * h);
    }

    private void paintBackground(Graphics2D g2) {
        g2.setPaint(Color.LIGHT_GRAY);
//        for (int i = 0; i < getSize().width; i += 10) {
//            Shape line = new Line2D.Float(i, 0, i, getSize().height);
//            g2.draw(line);
//        }
//
//        for (int i = 0; i < getSize().height; i += 10) {
//            Shape line = new Line2D.Float(0, i, getSize().width, i);
//            g2.draw(line);
//        }
        Dimension d = ((TitledBorder) getBorder()).getMinimumSize(this);
        g2.clearRect(getInsets().left, getInsets().top,
                getWidth() - d.width / 2, getHeight() - d.height);
        g2.drawImage(image, getInsets().left, getInsets().top,
                getWidth() - d.width / 2, getHeight() - d.height, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); //To change body of generated methods, choose Tools | Templates.
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintBackground(g2);
        Color[] colors = {Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.RED, Color.BLUE, Color.PINK};
        //int colorIndex = 0;

        g2.setStroke(new BasicStroke(2));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f));

        for (Selection s : shapes) {
            g2.setPaint(Color.BLACK);
            g2.draw(s.rect);
            //g2.setPaint(colors[(colorIndex++) % 6]);
            g2.setPaint(colors[s.type.getValue() - 1]);
            g2.fill(s.rect);
        }

        if (startDrag != null && endDrag != null) {
            g2.setPaint(Color.LIGHT_GRAY);
            Shape r = makeRectangle(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
            g2.draw(r);
        }
    }

    private Rectangle2D.Float makeRectangle(int x1, int y1, int x2, int y2) {
        return new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
    }
}
