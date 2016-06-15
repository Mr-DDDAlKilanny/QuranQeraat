/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qurandwnld;

import java.awt.AWTException;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EventObject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author ibraheem
 */
public class ChooseShahedJFrame2 extends javax.swing.JFrame {
    
    
    private static final Font checkBoxFont = new Font("Traditional Arabic", Font.PLAIN, 22);
    private static final Font labelFont = new Font("Traditional Arabic", Font.BOLD, 26);
    
    class BeitPart {
        int num;
        boolean isRight;
        String[] words;
    }
    
    class MyCellEditor extends AbstractCellEditor 
        implements TableCellEditor, TableCellRenderer {
        
        Object b;

        @Override
        public Object getCellEditorValue() {
            return b;
        }
        
        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            return false;
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, 
                boolean isSelected, int row, int column) {
            return get(value);
        }
        
        private Component get(Object value) {
            this.b = value;
            if (value == null || !value.getClass().equals(BeitPart.class)) {
                String str = "";
                if (value != null)
                    str += value;
                JLabel jLabel = new JLabel(str);
                jLabel.setFont(labelFont);
                return jLabel;
            }
            BeitPart b = (BeitPart) this.b;
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            //if (value == null) return panel;
            panel.removeAll();
            for (int i = 0; i < b.words.length; ++i) {
                JCheckBox box = new JCheckBox(b.words[i]);
                box.setFont(checkBoxFont);
                final int ii = i;
                Function<Byte, Boolean> f = k -> {
                    Map<Integer, Map<Boolean, Set<Byte>>> page = 
                            selection.get(currentPage);
                    if (page == null) return false;
                    Map<Boolean, Set<Byte>> bayt = page.get(b.num);
                    if (bayt == null) return false;
                    Set<Byte> w = bayt.get(!b.isRight);
                    if (w == null) return false;
                    return w.contains(k);
                };
                box.setSelected(f.apply((byte) ii));
                box.addItemListener((ItemEvent e) -> {
                    Map<Integer, Map<Boolean, Set<Byte>>> page = 
                            selection.get(currentPage);
                    if (page == null) {
                        page = new HashMap<>();
                        selection.put(currentPage, page);
                    }
                    Map<Boolean, Set<Byte>> bayt = page.get(b.num);
                    if (bayt == null) {
                        bayt = new HashMap<>();
                        page.put(b.num, bayt);
                    }
                    Set<Byte> w = bayt.get(!b.isRight);
                    if (w == null) {
                        w = new HashSet<>();
                        bayt.put(!b.isRight, w);
                    }
                    if (box.isSelected())
                        w.add((byte) ii);
                    else
                        w.remove((byte) ii);
                });
                box.setHorizontalAlignment(SwingConstants.CENTER);
                box.setVerticalAlignment(SwingConstants.BOTTOM);
                box.setHorizontalTextPosition(SwingConstants.CENTER);
                box.setVerticalTextPosition(SwingConstants.TOP);
                panel.add(box);
            }
            return panel;
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            if (value != null && value.getClass().equals(BeitPart.class))
                return get(value);
            String str = "";
            if (value != null)
                str += value;
            JLabel jLabel = new JLabel(str);
            jLabel.setFont(labelFont);
            return jLabel;
        }
    }
    
    private final boolean isDorrah;
    // page -> (beit -> (isFirstPart -> list of selected words))
    private final Map<Integer, Map<Integer, Map<Boolean, Set<Byte>>>> selection 
            = new HashMap<>();
    
    private boolean fireEventPage = true, fireEventBeit = true, fireEventTitle = true;
    private int lastBeitBeginPerPage, lastBeitEndPerPage, currentPage;
    private int lastBeitBeginPerTitle = -1, lastBeitEndPerTitle = -1;
    private int lastSelectedTitle = -1, lastBeitSearched = -1;

    public ChooseShahedJFrame2(boolean isDorrah, int initPage) {
        initComponents();
        this.isDorrah = isDorrah;
        fireEventBeit = fireEventPage = fireEventTitle = false;
        DbHelper.getMatnAbwab(isDorrah).stream().forEach(k -> jComboBox1.addItem(k));
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
            },
            new String [] {
                "رقم البيت", "الشطر الأول", "الشطر الثاني"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, BeitPart.class, BeitPart.class
            };

            @Override
            public boolean isCellEditable(int row, int column) {
                return column > 0;
            }
            
            @Override
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jTable1.getColumnModel().getColumn(0).setMaxWidth(50);
        jTable1.setDefaultRenderer(BeitPart.class, new MyCellEditor());
        jTable1.setDefaultEditor(BeitPart.class, new MyCellEditor());
        jTable1.setRowHeight(60);
        jTable1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        WriteJFrame1.rtlLayout(this);
        
        jSpinner1.addChangeListener((ChangeEvent e) -> {
            if (fireEventPage)
                showPage((int) jSpinner1.getValue());
        });
        jSpinner1.setModel(new SpinnerNumberModel(initPage, 1, DbHelper.getMatnTotalPages(isDorrah), 1));
        jSpinner2.addChangeListener((ChangeEvent e) -> {
            int num = (int) jSpinner2.getValue();
            if (fireEventBeit && (num < lastBeitBeginPerPage || num > lastBeitEndPerPage))
                showPage(DbHelper.getMatnPageByBeit(num, isDorrah));
        });
        jSpinner2.setModel(new SpinnerNumberModel(DbHelper.getBeitNumberByPage(initPage, isDorrah),
                1, DbHelper.getMatnTotalAbyat(isDorrah), 1));
        showPage(initPage);
    }
    
    private void showPage(int page) {
        fireEventBeit = fireEventPage = fireEventTitle = false;
        ArrayList<Shahed> matn = DbHelper.getMatnPage(page, isDorrah);
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        //jTable1.clearSelection();
        if (model.getRowCount() > 0) 
            clickFirstCell();
        EventQueue.invokeLater(() -> {
            model.setRowCount(0);
            lastBeitBeginPerPage = lastBeitEndPerPage = -1;
            matn.stream().forEach((m) -> {
                if (m.part2 != null) {
                    BeitPart p1 = new BeitPart(), p2 = new BeitPart();
                    p1.isRight = false;
                    p2.isRight = true;
                    p1.num = p2.num = m.id;
                    p1.words = m.part1.trim().split(" ");
                    p2.words = m.part2.trim().split(" ");
                    model.addRow(new Object[]{m.id, p1, p2});
                    if (lastBeitBeginPerPage == -1) {
                        lastBeitBeginPerPage = m.id;
                    }
                    lastBeitEndPerPage = Math.max(lastBeitEndPerPage, m.id);
                    if (lastBeitSearched == m.id) {
                        int num = model.getRowCount() - 1;
                        jTable1.setRowSelectionInterval(num, num);
                        lastBeitSearched = -1;
                    }
                } else {
                    model.addRow(new Object[]{null, m.part1, null});
                }
            });
            if (lastBeitBeginPerTitle == -1 || lastBeitBeginPerPage > lastBeitEndPerTitle
                    || lastBeitBeginPerPage < lastBeitBeginPerTitle) {
                int num = lastSelectedTitle == -1 ? lastBeitBeginPerPage : lastSelectedTitle;
                Map.Entry<Integer, Integer> r = DbHelper.getMatnBabStartEnd(num, isDorrah);
                if (lastSelectedTitle != -1) {
                    lastSelectedTitle = -1;
                }
                lastBeitBeginPerTitle = r.getKey();
                lastBeitEndPerTitle = r.getValue();
                setComboItemSelected(lastBeitBeginPerTitle);
            }
            jSpinner1.setValue(currentPage = page);
            jSpinner2.setValue(lastBeitBeginPerPage);
            fireEventBeit = fireEventPage = fireEventTitle = true;
        });
    }
    
    private void setComboItemSelected(int beit) {
        for (int i = 0; i < jComboBox1.getItemCount(); ++i) {
            MatnBab b = (MatnBab) jComboBox1.getItemAt(i);
            if (b.beit == beit) {
                jComboBox1.setSelectedIndex(i);
                break;
            }
        }
    }
    
    public Map<Integer, Map<Boolean, Set<Byte>>> getSelections() {
        Map<Integer, Map<Boolean, Set<Byte>>> res = new HashMap<>();
        selection.forEach((i, j) -> res.putAll(j));
        return res;
    }
    
    public Map<Integer, String> getSelectionsAsWordsString() {
        Map<Integer, Map<Boolean, Set<Byte>>> sel = getSelections();
        Map<Integer, String> res = new HashMap<>();
        sel.keySet().stream().forEach((k) -> {
            Map<Boolean, Set<Byte>> get = sel.get(k);
            String s = "";
            Set<Byte> get1 = get.get(true);
            if (get1 != null)
                for (Byte b : get1)
                    s += "1." + b + ";";
            get1 = get.get(false);
            if (get1 != null)
                for (Byte b : get1)
                    s += "2." + b + ";";
            if (s.length() > 0)
                res.put(k, s);
        });
        return res;
    }
    
    public void addSelections(Map<Integer, Map<Boolean, Set<Byte>>> sel) {
        int page = -1, beitBegin = -1, beitEnd = -1;
        for (Map.Entry<Integer, Map<Boolean, Set<Byte>>> el : sel.entrySet()) {
            if (page == -1 || !(el.getKey() <= beitBegin && el.getKey() >= beitEnd)) {
                page = DbHelper.getMatnPageByBeit(el.getKey(), isDorrah);
                Map.Entry<Integer, Integer> r = DbHelper.getMatnPageBeitStartEnd(
                        page, isDorrah);
                beitBegin = r.getKey(); beitEnd = r.getValue();
            }
            Map<Integer, Map<Boolean, Set<Byte>>> get = selection.get(page);
            if (get == null) {
                get = new HashMap<>();
                selection.put(page, get);
            }
            get.put(el.getKey(), el.getValue());
        }
    }
    
    public void addSelection(int beit, String words) {
        Map<Integer, Map<Boolean, Set<Byte>>> sel = new HashMap<>();
        Map<Boolean, Set<Byte>> b = new HashMap<>();
        sel.put(beit, b);
        String s[] = words.split(";");
        Arrays.sort(s, (i, j) -> i.compareTo(j));
        int idx = 0;
        for (int i = 1; i <= 2; ++i) {
            String w = "" + i;
            if (Arrays.stream(s).anyMatch(k -> k.startsWith(w))) {
                Set<Byte> set = new HashSet<>();
                while (idx < s.length && s[idx].startsWith(w)) {
                    set.add(Byte.parseByte(s[idx].substring(2)));
                    ++idx;
                }
                b.put(i == 1, set);
            }
        }
        addSelections(sel);
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel1 = new javax.swing.JLabel();
        jSpinner1 = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jSpinner2 = new javax.swing.JSpinner();
        jLabel3 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("اختيار الشاهد");

        jTable1.setFont(new java.awt.Font("Traditional Arabic", 0, 18)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "رقم البيت", "الشطر الأول", "الشطر الثاني"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Object.class, java.lang.Object.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);
        if (jTable1.getColumnModel().getColumnCount() > 0) {
            jTable1.getColumnModel().getColumn(0).setMaxWidth(320);
        }

        jLabel1.setText("اختر الصفحة:");

        jLabel2.setText("اختر رقم البيت:");

        jLabel3.setText("اختر الباب:");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<MatnBab>());
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 862, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        if (!fireEventTitle) return;
        MatnBab b = (MatnBab) jComboBox1.getSelectedItem();
        jSpinner2.setValue(lastSelectedTitle = b.beit);
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void clickFirstCell() {
        try {
            jTable1.changeSelection(0, 0, false, false);
            Point p = jTable1.getLocationOnScreen();
            Rectangle cellRect = jTable1.getCellRect(0, 0, true);
            Robot r = new Robot();
            Point mouse = MouseInfo.getPointerInfo().getLocation();
            r.mouseMove(p.x + cellRect.x + cellRect.width / 2, 
                    p.y + cellRect.y + cellRect.height / 2);
            r.mousePress(InputEvent.BUTTON1_MASK);
            r.delay(50);
            r.mouseRelease(InputEvent.BUTTON1_MASK);
            r.mouseMove(mouse.x, mouse.y);
        } catch (AWTException ex) {
            Logger.getLogger(ChooseShahedJFrame2.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            java.util.logging.Logger.getLogger(ChooseShahedJFrame2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ChooseShahedJFrame2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ChooseShahedJFrame2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ChooseShahedJFrame2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new ChooseShahedJFrame2(false, 1).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JSpinner jSpinner2;
    private javax.swing.JTable jTable1;
    // End of variables declaration//GEN-END:variables
}
