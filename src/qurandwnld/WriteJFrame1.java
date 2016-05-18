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
import java.awt.Cursor;
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
import java.awt.event.MouseMotionListener;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToolTip;
import javax.swing.SpinnerNumberModel;
import javax.swing.ToolTipManager;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
    
    static final int MAX_PAGE = 604;
    final String[] surahName = new String[114];
    final String[] pageSurah = new String[MAX_PAGE];
    public static final boolean isMadinaMushaf;
    public static final Dimension pageSize;
    
    int page;
    ArrayList<Ayah> pageAyaat;
    
    Selection current;
    
    JToolTip surfaceTooltip;
    
    static {
        isMadinaMushaf = JOptionPane.showOptionDialog(null, "اختر مصحف التسجيل", "مسجل القراءات", 
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, 
                new String[] {"مصحف المدينة", "مصحف الشمرلي"}, null) == 0;
        pageSize = isMadinaMushaf ? new Dimension(1024, 1656) : new Dimension(886, 1377);
    }
    
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
        initComponents();
        readQuranData();
        rtlLayout(this);
        JToolTip tooltip = new JToolTip();
        tooltip.setComponent(getSurface());
        surfaceTooltip = tooltip;
        jSpinner1.setModel(new SpinnerNumberModel(1, 1, MAX_PAGE, 1));
        showPage(page = 1);
    }
    
    private void readQuranData() {
        File f = new File(getClass().getClassLoader().getResource("quran-data.xml").getFile());
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f);
            doc.getDocumentElement().normalize();
            NodeList nList = doc.getElementsByTagName("sura");
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
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jPanel1 = new PaintSurface(this);
        jSpinner1 = new javax.swing.JSpinner();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem6 = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        jMenuItem7 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        jMenuItem8 = new javax.swing.JMenuItem();
        jMenuItem10 = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        jMenuItem9 = new javax.swing.JMenuItem();

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
        jPopupMenu1.add(jSeparator2);

        jMenuItem4.setText("تحريك");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem4);

        jMenuItem5.setText("تغيير الحجم");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        jPopupMenu1.add(jMenuItem5);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("تسجيل القراءات");
        setResizable(false);

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

        jMenu1.setText("القائمة");

        jMenuItem6.setText("مساعدة...");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem6);
        jMenu1.add(jSeparator3);

        jMenuItem7.setText("إرسال مواضع التسجيل للخادم");
        jMenuItem7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem7ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem7);
        jMenu1.add(jSeparator5);

        jMenuItem8.setText("إحضار مواضع المراجعة من الخادم");
        jMenuItem8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem8ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem8);

        jMenuItem10.setText("إرسال مواضع المراجعة إلى الخادم");
        jMenuItem10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem10ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem10);
        jMenu1.add(jSeparator4);

        jMenuItem9.setText("خروج");
        jMenuItem9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem9ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem9);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(94, 94, 94)
                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 66, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(218, Short.MAX_VALUE))
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
                .addContainerGap(471, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jSpinner1StateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jSpinner1StateChanged
        showPage((int) jSpinner1.getValue());
    }//GEN-LAST:event_jSpinner1StateChanged

    boolean editSelection(Selection s) {
        EditSelectionJPanel j = new EditSelectionJPanel(this, true);
        j.set(s);
        if (JOptionPane.showConfirmDialog(this,
                        j.getContentPane(),
                        "تحرير بيانات القراءة",
                        JOptionPane.OK_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION) {
            j.get();
            return true;
        } else
            return false;
    }
    
    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        if (editSelection(current))
            DbHelper.updateSelection(current, getSurface());
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        if (JOptionPane.showConfirmDialog(this, "متأكد من رغبتك في حذف هذا الموضع؟",
                "تأكيد", JOptionPane.OK_CANCEL_OPTION)
                == JOptionPane.OK_OPTION) {
            getSurface().shapes.remove(current);
            DbHelper.deleteSelection(current);
            current = null;
            getSurface().repaint();
        }
    }//GEN-LAST:event_jMenuItem2ActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        getSurface().beginMoveMode(current);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        ResizeSelectionJPanel panel = new ResizeSelectionJPanel(current, getSurface());
        if (JOptionPane.showOptionDialog(this, panel,
                "تغيير حجم موضع", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE, null,
                new String[] {"تغيير الحجم", "إلغاء الأمر"}, null) == 0)
            panel.accept(pageSize);
        else
            panel.cancel();
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem9ActionPerformed
        dispose();
    }//GEN-LAST:event_jMenuItem9ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        JOptionPane.showMessageDialog(this, "إرسال مواضع التسجيل للخادم: إرسال المواضع التي قمت بإضافتها في مصحف القراءات\n"
                + "إحضار مواضع التسجيل من الخادم: تحميل مواضع تم تسجيلها سابقا بواسطة المستخدمين الآخرين لتقوم بعملية مراجعتها\n"
                + "إرسال مواضع التسجيل إلى الخادم: بعد الانتهاء من عملية المراجعة، تقوم بإرسال نتائجها إلى الخادم",
                "مساعدة", JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_jMenuItem6ActionPerformed

    private void sendToServer(java.awt.event.ActionEvent evt, boolean bring) {
        AuthinticationJPanel panel = new AuthinticationJPanel();
        while (true) {
            if (JOptionPane.showConfirmDialog(this, panel, "أدخل معلومات المصادقة",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                String username = panel.getUsername();
                String password = panel.getPassword();
                if (username == null || username.isEmpty() || password == null || password.isEmpty())
                    JOptionPane.showMessageDialog(this, "فضلا أدخل معلومات المصادقة كاملة",
                        "خطأ", JOptionPane.ERROR_MESSAGE);
                else {
                    ShowWaitAction action = new ShowWaitAction("المزامنة", arg -> {
                        try {
                            SyncDbHelper.ServerResponse res;
                            if (!bring)
                                res = SyncDbHelper.sendMySelectionsToServer(username, password);
                            else
                                res = SyncDbHelper.bringMorag3ahFromServer(username, password);
                            if (res.code == 200)
                                return null;
                            else
                                return "رفض الخادم استقبال الرسالة:\n" + res.message;
                        } catch (Exception ex) {
                            Logger.getLogger(WriteJFrame1.class.getName()).log(Level.SEVERE, null, ex);
                            return "حدث خطأ أثناء محاولة الاتصال:\n" + ex.getMessage()
                                    + "\nفضلا تأكد من اتصالك بالإنترنت";
                        }
                    });
                    action.setFinishCallback(() -> {
                        String str = (String) action.getActionResult();
                        if (str == null)
                            JOptionPane.showMessageDialog(this, "تمت العملية بنجاح");
                        else
                            JOptionPane.showMessageDialog(this, str, "خطأ", JOptionPane.ERROR_MESSAGE);
                    });
                    action.actionPerformed(evt);
                    break;
                }
            } else break;
        }
    }
    
    private void jMenuItem7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem7ActionPerformed
        sendToServer(evt, false);
    }//GEN-LAST:event_jMenuItem7ActionPerformed

    private void jMenuItem8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem8ActionPerformed
        sendToServer(evt, true);
    }//GEN-LAST:event_jMenuItem8ActionPerformed

    private void jMenuItem10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem10ActionPerformed
        
    }//GEN-LAST:event_jMenuItem10ActionPerformed

    private void showPage(int p) {
        try {
            String path = String.format(isMadinaMushaf ? 
                    "width_1024/page%03d.png" : "shamraly/%03d.png", p);
            getSurface().image = ImageIO.read(getClass().getClassLoader().getResource(path));
            getSurface().repaint();
            jSpinner1.setValue(p);
            ((TitledBorder) getSurface().getBorder()).setTitle("سورة " + getSurahByPage(p));
            getSurface().shapes = DbHelper.getPageSelections(p, getSurface());
            page = p;
            pageAyaat = DbHelper.getPageAyat(page);
        } catch (IOException ex) {
        }
    }
    
    public int getAyahIndexByPos(int x, int y) {
        if (pageAyaat != null) {
            for (int i = 0; i < pageAyaat.size(); ++i)
                for (Rectangle2D rect : pageAyaat.get(i).rects)
                    if (getSurface().getScaledRectFromImageRect(pageSize, 
                            (Rectangle2D.Float) rect).contains(x, y))
                        return pageAyaat.get(i).ayahIndex;

        }
        return -1;
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
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException 
                | javax.swing.UnsupportedLookAndFeelException ex) {
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
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem10;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenuItem jMenuItem7;
    private javax.swing.JMenuItem jMenuItem8;
    private javax.swing.JMenuItem jMenuItem9;
    private javax.swing.JPanel jPanel1;
    public javax.swing.JPopupMenu jPopupMenu1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JSpinner jSpinner1;
    // End of variables declaration//GEN-END:variables
}

class PaintSurface extends JPanel {

    public final WriteJFrame1 parent;
    
    ArrayList<Selection> shapes;
    
    private int strokeSize = 5;
    
    private Color[] colors = {Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.RED, Color.BLUE, Color.PINK, Color.GRAY};

    private Point startDrag, endDrag;
    
    Image image;
    
    //true: draw new rect, false: move an existing rect
    private boolean drawMode = true;
    private Selection toMove;
    private double moveX1, moveY1, moveX2, moveY2;
    
    public PaintSurface(WriteJFrame1 parent) {
        this.parent = parent;
        addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseDragged(MouseEvent e) {
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (!drawMode) {
                    moveSelection(e.getX(), e.getY());
                }
                for (Selection ss : shapes) {
                    Rectangle2D tmp = ss.scaledRect;
                    if (tmp.contains(e.getX(), e.getY())) {
                        parent.surfaceTooltip.setTipText(ss.ayahWord);
                        ToolTipManager.sharedInstance().mouseMoved(
                                new MouseEvent(PaintSurface.this, 0, 0, 0,
                                        e.getX(), e.getY(), // X-Y of the mouse for the tool tip
                                        0, false));
                        break;
                    }
                }
            }
        });
        this.addMouseListener(new MouseAdapter() {
            
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    isPopup(e);
                    return;
                }
                startDrag = new Point(e.getX(), e.getY());
                endDrag = startDrag;
                repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!drawMode) {
                    finishMoveMode(e.getButton() == MouseEvent.BUTTON1);
                    return;
                }
                if (e.isPopupTrigger()) {
                    isPopup(e);
                    return;
                }
                if (startDrag == null) return;
                if (Math.abs(startDrag.x - e.getX()) < 3
                        || Math.abs(startDrag.y - e.getY()) < 3) {
                    startDrag = null;
                    return;
                }
                Selection s = new Selection();
                s.ayahIndex = parent.getAyahIndexByPos(
                        startDrag.x + (e.getX() + startDrag.x) / 2,
                        startDrag.x + (e.getY() + startDrag.y) / 2);
                s.scaledRect = makeRectangle(startDrag.x, startDrag.y, e.getX(), e.getY());
                parent.editSelection(s);
                DbHelper.insertSelection(s, PaintSurface.this);
                shapes.add(s);
                startDrag = null;
                endDrag = null;
                repaint();
            }

            private void isPopup(MouseEvent e) {
                for (Selection ss : shapes) {
                    Rectangle2D tmp = ss.scaledRect;
                    if (tmp.contains(e.getX(), e.getY())) {
                        parent.current = ss;
                        parent.jPopupMenu1.show(e.getComponent(), e.getX(), e.getY());
                        break;
                    }
                }
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                endDrag = new Point(e.getX(), e.getY());
                repaint();
            }
        });
    }
    
    private void moveSelection(double x, double y) {
        //if (toMove instanceof LineSelection) {
        //    Line2D line = ((LineSelection) toMove).line;
        //    line.setLine(x, WIDTH, x, WIDTH);
        //} else {
            Rectangle2D rect = (toMove).scaledRect;
            rect.setRect(moveX2 = x, moveY2 = y, rect.getWidth(), rect.getHeight());
        //}
        repaint();
    }
    
    public void beginMoveMode(Selection s) {
        if (!drawMode)
            throw new RuntimeException("finishMoveMode() should be called before calling this");
        drawMode = false;
        toMove = s;
        shapes.remove(s);
        repaint();
        setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
    }
    
    public void finishMoveMode(boolean save) {
        if (drawMode)
            throw new RuntimeException("beginMoveMode() should be called before calling this");
        drawMode = true;
        shapes.add(toMove);
        if (save) {
            DbHelper.updateSelectionLocation(toMove, this);
        } else 
            moveSelection(moveX2, moveY2);
        toMove = null;
        setCursor(Cursor.getDefaultCursor());
        repaint();
    }
    
    private Rectangle2D.Float getDrawingArea() {
        //Dimension d = ((TitledBorder) getBorder()).getMinimumSize(this);
        //return new Rectangle(d.width / 4, d.height / 4, getWidth() - d.width / 2, getHeight() - d.height / 2);
        
        // getInsets(): right == left & top == bottom
        return new Rectangle.Float(getInsets().left / 2.0f, 
                getInsets().top / 2.0f,
                getWidth() - getInsets().left, getHeight() - getInsets().top);
    }
    
    public Line2D.Float getLineFromScaled(Dimension bmp, Line2D.Float r) {
        Rectangle2D.Float d = getDrawingArea();
        float w = (float) bmp.width / d.width;
        float h = (float) bmp.height / d.height;
        return new Line2D.Float(r.x1 * w - d.x,
                r.y1 * h - d.y,
                r.x2 * w - d.x,
                r.y2 * h - d.y);
    }
    
    public Rectangle2D.Float getImageRectFromScaled(Dimension bmp, Rectangle2D.Float r) {
        Rectangle2D.Float d = getDrawingArea();
        float w = (float) bmp.width / d.width;
        float h = (float) bmp.height / d.height;
        return new Rectangle2D.Float((r.x - d.x) * w,
                (r.y - d.y) * h,
                r.width * w,
                r.height * h);
    }
    
    public Line2D.Float getScaledLine(Dimension bmp, Line2D.Float r) {
        Rectangle2D.Float d = getDrawingArea();
        float w = d.width / (float) bmp.width;
        float h = d.height / (float) bmp.height;
        return new Line2D.Float(r.x1 * w + d.x,
                r.y1 * h + d.y,
                r.x2 * w + d.x,
                r.y2 * h + d.y);
    }
    
    public Rectangle2D.Float getScaledRectFromImageRect(Dimension bmp, Rectangle2D.Float r) {
        Rectangle2D.Float d = getDrawingArea();
        float w = d.width / (float) bmp.width;
        float h = d.height / (float) bmp.height;
        return new Rectangle2D.Float((r.x + d.x)  * w,
                (r.y + d.y) * h,
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
        Rectangle2D.Float d = getDrawingArea();
        g2.clearRect((int) d.x, (int) d.y, (int) d.width, (int) d.height);
        g2.drawImage(image, (int) d.x, (int) d.y, (int) d.width, (int) d.height, null);
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

        Function<Selection, Void> draw = s -> {
            g2.setPaint(Color.BLACK);
            g2.draw(s.getShape());
            //g2.setPaint(colors[(colorIndex++) % 6]);
            g2.setPaint(colors[0]);
            g2.fill(s.getShape());
            return null;
        };
        
        shapes.stream().forEach((s) -> {
            draw.apply(s);
        });
        if (!drawMode && toMove != null)
            draw.apply(toMove);

        if (startDrag != null && endDrag != null) {
            g2.setPaint(Color.LIGHT_GRAY);
            Shape r = makeRectangle(startDrag.x, startDrag.y, endDrag.x, endDrag.y);
            g2.draw(r);
        }
        //debugRect();
    }
    
    private void debugRect() {
        Image img = new BufferedImage(image.getWidth(null), image.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) img.getGraphics();
        g2.drawImage(image, 0, 0, null);
        for (Selection s : shapes) {
            //if (!(s instanceof RectSelection)
            //        || ((RectSelection) s).naturalRect == null) continue;
            g2.setPaint(Color.BLACK);
            g2.draw(s.naturalRect);
            g2.setPaint(colors[s.details.get(0).type.getValue() - 1]);
            g2.fill(s.naturalRect);
        }
        try {
            ImageIO.write((RenderedImage) img, "png", new File("debug.png"));
        } catch (IOException ex) {
            Logger.getLogger(PaintSurface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private Rectangle2D.Float makeRectangle(int x1, int y1, int x2, int y2) {
        return new Rectangle2D.Float(Math.min(x1, x2) - strokeSize,
                Math.min(y1, y2) - strokeSize, Math.abs(x1 - x2), Math.abs(y1 - y2));
    }
    
    private Line2D.Float makeLine(int x1, int y1, int x2, int y2) {
        return new Line2D.Float(x1, y1, x2, y2);
    }
}
