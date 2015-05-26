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
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
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
class WriteJFrame1 extends javax.swing.JFrame {
    
    static final SelectionType[] selectionTypes = {
        SelectionType.Farsh,
        SelectionType.Hamz,
        SelectionType.Edgham,
        SelectionType.Emalah,
        SelectionType.Naql,
        SelectionType.Mad,
        SelectionType.Sakt
    };
    
    private final String settingFile = "data.xml";
    
    private final List<JRadioButton> radioButtons;
    private final ArrayList<Selection>[] sel;
    static final int MAX_PAGE = 604;
    final int pageSizes[][] = new int[MAX_PAGE][2];
    final String[] surahName = new String[114];
    final String[] pageSurah = new String[MAX_PAGE];
    
    int page;
    
    Selection current;
    
    SelectionType currentType = SelectionType.Farsh;

    public PaintSurface getSurface() {
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
        radioButtons = Arrays.asList(new JRadioButton[] {
            jRadioButtonFarsh,
            jRadioButtonHamz,
            jRadioButtonEdgham,
            jRadioButtonEmalah,
            jRadioButtonNaql,
            jRadioButtonMadd,
            jRadioButtonSakt
        });
        javax.swing.event.ChangeListener changeListener = (javax.swing.event.ChangeEvent evt) -> {
            JRadioButton btn = (JRadioButton) evt.getSource();
            if (btn.isSelected()) {
                int idx = radioButtons.indexOf(btn);
                currentType = selectionTypes[idx];
                getSurface().drawRect = idx < 4;
            }
        };
        radioButtons.stream().forEach((r) -> {
            r.addChangeListener(changeListener);
        });
        readQuranData();
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
                    Selection s;
                    if (eElement.hasAttribute("rect")) {
                        RectSelection ss = new RectSelection();
                        String[] n = eElement.getAttribute("rect").split(",");
                        ss.origRect = new Rectangle2D.Float(Float.parseFloat(n[0]),
                                Float.parseFloat(n[1]), Float.parseFloat(n[2]), Float.parseFloat(n[3]));
                        ss.rect = surface.getScaledRectFromImageRect(new Dimension(pageSizes[p - 1][0], pageSizes[p - 1][1]),
                                (Rectangle2D.Float) ss.origRect);
                        s = ss;
                    } else {
                        LineSelection ss = new LineSelection();
                        String[] n = eElement.getAttribute("line").split(",");
                        ss.origLine = new Line2D.Float(Float.parseFloat(n[0]),
                                Float.parseFloat(n[1]), Float.parseFloat(n[2]), Float.parseFloat(n[3]));
                        ss.line = surface.getScaledLine(new Dimension(pageSizes[p - 1][0], pageSizes[p - 1][1]),
                                (Line2D.Float) ss.origLine);
                        s = ss;
                    }
                    s.type = SelectionType.fromValue(Integer.parseInt(eElement.getAttribute("type")));
                    s.page = p;
                    s.shahed = eElement.getElementsByTagName("shahed").item(0).getTextContent();
                    s.descr = eElement.getElementsByTagName("descr").item(0).getTextContent();
                    s.isNew = false;
                    sel[p - 1].add(s);
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(WriteJFrame1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void readQuranData() {
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
            nList = doc.getElementsByTagName("sura");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    int idx = Integer.parseInt(eElement.getAttribute("index"));
                    String name = eElement.getAttribute("name");
                    surahName[idx - 1] = name;
                }
            }
            nList = doc.getElementsByTagName("page");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    int idx = Integer.parseInt(eElement.getAttribute("index"));
                    int sura = Integer.parseInt(eElement.getAttribute("sura"));
                    pageSurah[idx - 1] = surahName[sura - 1];
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(WriteJFrame1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private String getSurahByPage(int p) {
        return pageSurah[p - 1];
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
        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new PaintSurface();
        jSpinner1 = new javax.swing.JSpinner();
        jRadioButtonFarsh = new javax.swing.JRadioButton();
        jRadioButtonHamz = new javax.swing.JRadioButton();
        jRadioButtonEdgham = new javax.swing.JRadioButton();
        jRadioButtonEmalah = new javax.swing.JRadioButton();
        jRadioButtonNaql = new javax.swing.JRadioButton();
        jRadioButtonMadd = new javax.swing.JRadioButton();
        jRadioButtonSakt = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();

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
        setResizable(false);
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
            .addGap(0, 438, Short.MAX_VALUE)
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

        buttonGroup1.add(jRadioButtonFarsh);
        jRadioButtonFarsh.setSelected(true);
        jRadioButtonFarsh.setText("فرش");

        buttonGroup1.add(jRadioButtonHamz);
        jRadioButtonHamz.setText("همز");

        buttonGroup1.add(jRadioButtonEdgham);
        jRadioButtonEdgham.setText("إدغام/اختلاس");

        buttonGroup1.add(jRadioButtonEmalah);
        jRadioButtonEmalah.setText("إمالة");

        buttonGroup1.add(jRadioButtonNaql);
        jRadioButtonNaql.setText("نقل");

        buttonGroup1.add(jRadioButtonMadd);
        jRadioButtonMadd.setText("مد");

        buttonGroup1.add(jRadioButtonSakt);
        jRadioButtonSakt.setText("سكت");

        jLabel1.setText("نوع التحديد");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(94, 94, 94)
                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonFarsh)
                    .addComponent(jRadioButtonHamz)
                    .addComponent(jRadioButtonEdgham)
                    .addComponent(jRadioButtonMadd)
                    .addComponent(jRadioButtonNaql)
                    .addComponent(jRadioButtonSakt)
                    .addComponent(jRadioButtonEmalah)
                    .addComponent(jLabel1))
                .addContainerGap(111, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addGap(108, 108, 108)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButtonFarsh))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButtonHamz)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButtonEdgham)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButtonEmalah)
                .addGap(26, 26, 26)
                .addComponent(jRadioButtonNaql)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonMadd)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonSakt)
                .addContainerGap(315, Short.MAX_VALUE))
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
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement("selections");
            for (int i = 0; i < sel.length; ++i) {
                for (int j = 0; j < sel[i].size(); ++j) {
                    sel[i].get(j).write(doc, rootElement, this);
                }
            }
            doc.appendChild(rootElement);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
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
            String path = String.format("quran/%04d.jpg", p);
            getSurface().image = ImageIO.read(getClass().getClassLoader().getResource(path));
            getSurface().repaint();
            jSpinner1.setValue(p);
            ((TitledBorder) getSurface().getBorder()).setTitle("سورة " + getSurahByPage(p));
            getSurface().shapes = sel[p - 1];
            page = p;
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

    private static void test() {
        File f = new File("data.xml");
        if (!f.exists())
            return;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f);
            doc.getDocumentElement().normalize();
            ArrayList<Selection> ss = new ArrayList<>();
            NodeList nList = doc.getElementsByTagName("selection");
            for (int temp = 0; temp < nList.getLength(); temp++) {
                Node nNode = nList.item(temp);
                if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElement = (Element) nNode;
                    int p = Integer.parseInt(eElement.getAttribute("page"));
                    String[] n = eElement.getAttribute("rect").split(",");
                    RectSelection s = new RectSelection();
                    s.rect = new Rectangle2D.Float(Float.parseFloat(n[0]),
                            Float.parseFloat(n[1]), Float.parseFloat(n[2]), Float.parseFloat(n[3]));
                    ss.add(s);
                }
            }
            if (ss.isEmpty()) return;
            BufferedImage read = ImageIO.read(ss.get(0).getClass().getClassLoader().getResource("quran/0001.jpg"));
            Graphics2D g = read.createGraphics();
            g.setPaint(Color.red);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .55f));
            for (Iterator<Selection> it = ss.iterator(); it.hasNext();) {
                RectSelection s = (RectSelection) it.next();
                g.fill(s.rect);
            }
            ImageIO.write(read, "jpg", new File("output.jpg"));
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            Logger.getLogger(WriteJFrame1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        //test();
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
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JPanel jPanel1;
    public javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JRadioButton jRadioButtonEdgham;
    private javax.swing.JRadioButton jRadioButtonEmalah;
    private javax.swing.JRadioButton jRadioButtonFarsh;
    private javax.swing.JRadioButton jRadioButtonHamz;
    private javax.swing.JRadioButton jRadioButtonMadd;
    private javax.swing.JRadioButton jRadioButtonNaql;
    private javax.swing.JRadioButton jRadioButtonSakt;
    private javax.swing.JSpinner jSpinner1;
    // End of variables declaration//GEN-END:variables
}

enum SelectionType {
    Farsh(1),
    Hamz(2),
    Edgham(3),
    Emalah(4),
    Naql(5),
    Mad(6),
    Sakt(7);
    
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
            case 5:
                return Naql;
            case 6:
                return Mad;
            case 7:
                return Sakt;
            default:
                throw new IllegalArgumentException();
        }
    }
}

abstract class Selection {
    int page;
    String shahed;
    String descr;
    SelectionType type;
    boolean isNew;
    
    protected abstract void writeShape(Document doc, Element root, WriteJFrame1 frame);
    
    public void write(Document doc, Element root, WriteJFrame1 frame) {
        Element staff = doc.createElement("selection");
        root.appendChild(staff);

        Attr attr = doc.createAttribute("page");
        attr.setValue("" + page);
        staff.setAttributeNode(attr);

        writeShape(doc, staff, frame);

        attr = doc.createAttribute("type");
        attr.setValue(String.format("%d", type.getValue()));
        staff.setAttributeNode(attr);

        Element el = doc.createElement("shahed");
        staff.appendChild(el);
        el.appendChild(doc.createTextNode(shahed));

        el = doc.createElement("descr");
        staff.appendChild(el);
        el.appendChild(doc.createTextNode(descr));
    }
    
    public abstract Shape getShape();
}

class RectSelection extends Selection {
    Rectangle2D rect, origRect;

    @Override
    protected void writeShape(Document doc, Element staff, WriteJFrame1 frame) {
        Attr attr = doc.createAttribute("rect");
        Rectangle2D.Float rect = isNew ? frame.getSurface().getImageRectFromScaled(
                new Dimension(frame.pageSizes[page - 1][0], frame.pageSizes[page - 1][1]),
                (Rectangle2D.Float) this.rect) : (Rectangle2D.Float) origRect;
        attr.setValue(String.format("%f,%f,%f,%f", rect.x,
                rect.y, rect.width, rect.height));
        staff.setAttributeNode(attr);
    }

    @Override
    public Shape getShape() {
        return rect;
    }
}

class LineSelection extends Selection {
    Line2D line, origLine;
    
    @Override
    protected void writeShape(Document doc, Element staff, WriteJFrame1 frame) {
        Attr attr = doc.createAttribute("line");
        Line2D.Float rect = isNew ? frame.getSurface().getLineFromScaled(
                new Dimension(frame.pageSizes[page - 1][0], frame.pageSizes[page - 1][1]),
                (Line2D.Float) this.line) : (Line2D.Float) origLine;
        attr.setValue(String.format("%f,%f,%f,%f", rect.x1,
                rect.y1, rect.x2, rect.y2));
        staff.setAttributeNode(attr);
    }
    
    @Override
    public Shape getShape() {
        return line;
    }
}

class PaintSurface extends JPanel {

    ArrayList<Selection> shapes;
    
    int strokeSize = 5;
    
    private Color[] colors = {Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.RED, Color.BLUE, Color.PINK, Color.GRAY};

    Point startDrag, endDrag;
    
    Image image;
    
    boolean drawRect = true;

    public PaintSurface() {
        this.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    isPopup(e);
                    return;
                }
                startDrag = new Point(e.getX(), e.getY());
                endDrag = startDrag;
                repaint();
            }

            public void mouseReleased(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    isPopup(e);
                    return;
                }
                if (startDrag == null) return;
                if (Math.abs(startDrag.x - e.getX()) < 3
                        || (drawRect ? Math.abs(startDrag.y - e.getY()) < 3
                                : Math.abs(startDrag.y - e.getY()) > 3)) {
                    startDrag = null;
                    return;
                }
                WriteJFrame1 f = (WriteJFrame1) PaintSurface.this.getParent().getParent().getParent().getParent();
                InputQeraatJDialog j = new InputQeraatJDialog(f, true);
                j.type(f.currentType);
                if (JOptionPane.showConfirmDialog(f,
                        j.getContentPane(),
                        "تحرير بيانات القراءة",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
                    Selection s;
                    if (drawRect) {
                        RectSelection r = new RectSelection();
                        r.rect = makeRectangle(startDrag.x, startDrag.y, e.getX(), e.getY());
                        s = r;
                    } else {
                        LineSelection r = new LineSelection();
                        r.line = makeLine(startDrag.x, startDrag.y, e.getX(), e.getY());
                        s = r;
                    }
                    s.page = f.page;
                    s.descr = j.descr();
                    s.shahed = j.shahed();
                    s.isNew = true;
                    s.type = f.currentType;
                    shapes.add(s);
                    startDrag = null;
                    endDrag = null;
                    repaint();
                }
            }

            public void isPopup(MouseEvent e) {
                WriteJFrame1 f = (WriteJFrame1) PaintSurface.this
                        .getParent().getParent().getParent().getParent();
                for (Selection ss : shapes) {
                    Rectangle2D.Float tmp;
                    if (ss instanceof RectSelection) {
                        tmp = (Rectangle2D.Float) ((RectSelection) ss).rect;
                    } else {
                        Line2D.Float s = (Line2D.Float) ((LineSelection) ss).line;
                        tmp = new Rectangle2D.Float(s.x1,
                                s.y1 - strokeSize,
                                Math.abs(s.x2 - s.x1),
                                Math.abs(s.y2 - s.y1) + strokeSize);
                    }
                    if (tmp.contains(e.getX(), e.getY())) {
                        f.current = ss;
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
    
    private Rectangle getDrawingArea() {
        //Dimension d = ((TitledBorder) getBorder()).getMinimumSize(this);
        //return new Rectangle(d.width / 4, d.height / 4, getWidth() - d.width / 2, getHeight() - d.height / 2);
        
        // getInsets(): right == left & top == bottom
        return new Rectangle((int) Math.round(getInsets().left / 2.0f), (int) Math.round(getInsets().top / 2.0f),
                getWidth() - getInsets().left, getHeight() - getInsets().top);
    }
    
    public Line2D.Float getLineFromScaled(Dimension bmp, Line2D.Float r) {
        Rectangle d = getDrawingArea();
        float w = (float) bmp.width / d.width;
        float h = (float) bmp.height / d.height;
        return new Line2D.Float(r.x1 * w - d.x,
                r.y1 * h - d.y,
                r.x2 * w - d.x,
                r.y2 * h - d.y);
    }
    
    public Rectangle2D.Float getImageRectFromScaled(Dimension bmp, Rectangle2D.Float r) {
        Rectangle d = getDrawingArea();
        float w = (float) bmp.width / d.width;
        float h = (float) bmp.height / d.height;
        return new Rectangle2D.Float(r.x * w - d.x,
                r.y * h - d.y,
                r.width * w,
                r.height * h);
    }
    
    public Line2D.Float getScaledLine(Dimension bmp, Line2D.Float r) {
        Rectangle d = getDrawingArea();
        float w = d.width / (float) bmp.width;
        float h = d.height / (float) bmp.height;
        return new Line2D.Float(r.x1 * w + d.x,
                r.y1 * h + 10,
                r.x2 * w + d.x,
                r.y2 * h + 10);
    }
    
    public Rectangle2D.Float getScaledRectFromImageRect(Dimension bmp, Rectangle2D.Float r) {
        Rectangle d = getDrawingArea();
        float w = d.width / (float) bmp.width;
        float h = d.height / (float) bmp.height;
        return new Rectangle2D.Float(r.x * w + d.x,
                r.y * h + d.y + 10,
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
        Rectangle d = getDrawingArea();
        g2.clearRect(d.x, d.y, d.width, d.height);
        g2.drawImage(image, d.x, d.y, d.width, d.height, null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); //To change body of generated methods, choose Tools | Templates.
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        paintBackground(g2);
        //int colorIndex = 0;

        g2.setStroke(new BasicStroke(strokeSize));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.50f));

        for (Selection s : shapes) {
            g2.setPaint(Color.BLACK);
            g2.draw(s.getShape());
            //g2.setPaint(colors[(colorIndex++) % 6]);
            g2.setPaint(colors[s.type.getValue() - 1]);
            g2.fill(s.getShape());
        }

        if (startDrag != null && endDrag != null) {
            g2.setPaint(Color.LIGHT_GRAY);
            Shape r = drawRect ? makeRectangle(startDrag.x, startDrag.y, endDrag.x, endDrag.y)
                    : makeLine(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
            g2.draw(r);
        }
    }

    private Rectangle2D.Float makeRectangle(int x1, int y1, int x2, int y2) {
        return new Rectangle2D.Float(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
    }
    
    private Line2D.Float makeLine(int x1, int y1, int x2, int y2) {
        return new Line2D.Float(x1, y1, x2, y2);
    }
}
