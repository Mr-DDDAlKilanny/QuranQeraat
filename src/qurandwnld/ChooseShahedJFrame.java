/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qurandwnld;

import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 *
 * @author Yasser
 */
public class ChooseShahedJFrame extends javax.swing.JFrame {

    private final boolean isDorrah;
    private final boolean[] canEditColumn = new boolean [] {
        false, true, false, true, false, true
    };
    public static final byte SELECTION_ALL = 0;
    public static final byte SELECTION_PART1 = 1;
    public static final byte SELECTION_PART2 = 2;
    // page -> (beit, selectionType[0, 1, 2])
    private final Map<Integer, Map<Integer, Byte>> selection = new HashMap<>();
    
    private boolean fireEventPage = true, fireEventBeit = true, fireEventTitle = true;
    private int lastBeitBeginPerPage, lastBeitEndPerPage, currentPage;
    private int lastBeitBeginPerTitle = -1, lastBeitEndPerTitle = -1;
    private int lastSelectedTitle = -1, lastBeitSearched = -1;
    
    public ChooseShahedJFrame(boolean isDorrah, int initPage) {
        this.isDorrah = isDorrah;
        fireEventBeit = fireEventPage = fireEventTitle = false;
        initComponents();
        DbHelper.getMatnAbwab(isDorrah).stream().forEach(k -> jComboBox1.addItem(k));
        //DefaultListCellRenderer rend = new DefaultListCellRenderer();
        //rend.setHorizontalAlignment(SwingConstants.RIGHT);
        //jComboBox1.setRenderer(rend);
        //jComboBox1.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        final float[] columnWidthPercentage = { 6.0f, 6.0f, 38.0f, 6.0f, 38.0f, 6.0f };
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] { },
            new String [] {
                "#", "اختيار", "الشطر الأول", "اختيار", "الشطر الثاني", "اختيار"
            }
        ) {
            Class[] types = new Class [] {
                Integer.class, Boolean.class, String.class, Boolean.class, String.class, Boolean.class
            };
            
            @Override
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                if (!canEditColumn [columnIndex]) return false;
                if (getValueAt(rowIndex, 0) == null) return false;
                return true;
            }
        });
        // recalc table column widths
        getContentPane().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int tW = jTable1.getWidth();
                TableColumn column;
                TableColumnModel jTableColumnModel = jTable1.getColumnModel();
                int cantCols = jTableColumnModel.getColumnCount();
                for (int i = 0; i < cantCols; i++) {
                    column = jTableColumnModel.getColumn(i);
                    int pWidth = Math.round(columnWidthPercentage[i] * tW);
                    column.setPreferredWidth(pWidth);
                }
            }
        });
        jTable1.setFont(new Font("Traditional Arabic", Font.BOLD, 18));
        jTable1.setRowHeight(30);
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < jTable1.getColumnModel().getColumnCount(); ++i)
            if (!canEditColumn[i])
                jTable1.getColumnModel().getColumn(i).setCellRenderer(rightRenderer);
        jTable1.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if ("tableCellEditor".equals(evt.getPropertyName())) {
                int col = jTable1.getEditingColumn();
                int row = jTable1.getEditingRow();
                if (col < 0 && row < 0) return;
                selectionChanged(col, row);
            }
        });
        
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
    
    private byte getSelectionAtRow(int row) {
        if ((boolean) jTable1.getValueAt(row, 1)) return SELECTION_ALL;
        if ((boolean) jTable1.getValueAt(row, 3)) return SELECTION_PART1;
        if ((boolean) jTable1.getValueAt(row, 5)) return SELECTION_PART2;
        return -1;
    }
    
    private void selectionChanged(int col, int row) {
        boolean val = (boolean) jTable1.getValueAt(row, col);
        int beit = (int) jTable1.getValueAt(row, 0);
        switch (col) {
            case 1:
                jTable1.setValueAt(val, row, 3);
                jTable1.setValueAt(val, row, 5);
                put(currentPage, new AbstractMap.SimpleEntry<>(beit, getSelectionAtRow(row)));
                break;
            case 3:
                if (val && (boolean) jTable1.getValueAt(row, 5)) {
                    jTable1.setValueAt(true, row, 1);
                }
                else jTable1.setValueAt(false, row, 1);
                put(currentPage, new AbstractMap.SimpleEntry<>(beit, getSelectionAtRow(row)));
                break;
            case 5:
                if (val && (boolean) jTable1.getValueAt(row, 3)) {
                    jTable1.setValueAt(true, row, 1);
                }
                else jTable1.setValueAt(false, row, 1);
                put(currentPage, new AbstractMap.SimpleEntry<>(beit, getSelectionAtRow(row)));
                break;
        }
    }
    
    private void showPage(int page) {
        fireEventBeit = fireEventPage = fireEventTitle = false;
        ArrayList<Shahed> matn = DbHelper.getMatnPage(page, isDorrah);
        javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        lastBeitBeginPerPage = lastBeitEndPerPage = -1;
        Map<Integer, Byte> map = selection.get(page);
        for (Shahed m : matn) {
            if (m.part2 != null) {
                boolean all = false, first = false, second = false;
                if (map != null) {
                    Byte b = map.get(m.id);
                    if (b != null) {
                        switch (b) {
                            case SELECTION_ALL:
                                all = first = second = true;
                                break;
                            case SELECTION_PART1:
                                first = true;
                                break;
                            case SELECTION_PART2:
                                second = true;
                                break;
                        }
                    }
                }
                model.addRow(new Object[]{ m.id, all, m.part1, first, m.part2, second });
                if (lastBeitBeginPerPage == -1)
                    lastBeitBeginPerPage = m.id;
                lastBeitEndPerPage = Math.max(lastBeitEndPerPage, m.id);
                if (lastBeitSearched == m.id) {
                    int num = model.getRowCount() - 1;
                    jTable1.setRowSelectionInterval(num, num);
                    lastBeitSearched = -1;
                }
            } else {
                model.addRow(new Object[] {null, false, m.part1, false, m.part2, false});
            }
        }
        if (lastBeitBeginPerTitle == -1 || lastBeitBeginPerPage > lastBeitEndPerTitle
                || lastBeitBeginPerPage < lastBeitBeginPerTitle) {
            int num = lastSelectedTitle == -1 ? lastBeitBeginPerPage : lastSelectedTitle;
            Map.Entry<Integer, Integer> r = DbHelper.getMatnBabStartEnd(num, isDorrah);
            if (lastSelectedTitle != -1)
                lastSelectedTitle = -1;
            lastBeitBeginPerTitle = r.getKey();
            lastBeitEndPerTitle = r.getValue();
            setComboItemSelected(lastBeitBeginPerTitle);
        }
        jSpinner1.setValue(currentPage = page);
        jSpinner2.setValue(lastBeitBeginPerPage);
        fireEventBeit = fireEventPage = fireEventTitle = true;
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
    
    public Map<Integer, Byte> getSelections() {
        Map<Integer, Byte> res = new HashMap<>();
        selection.entrySet().stream().forEach((entry) -> {
            res.putAll(entry.getValue());
        });
        return res;
    }
    
    private void put(int page, Map.Entry<Integer, Byte> el) {
        if (el.getValue() >= 0) {
            Map<Integer, Byte> get = selection.get(page);
            if (get == null) {
                Map<Integer, Byte> map = new HashMap<>();
                map.put(el.getKey(), el.getValue());
                selection.put(page, map);
            } else {
                get.put(el.getKey(), el.getValue());
            }
        } else {
            Map<Integer, Byte> get = selection.get(page);
            if (get != null) {
                Byte get1 = get.get(el.getKey());
                if (get1 != null) {
                    get.remove(el.getKey());
                }
            }
        }
    }
    
    private void refreshTable() {
        Map<Integer, Byte> get = selection.get(currentPage);
        if (get != null) {
            for (int i = 0; i < jTable1.getRowCount(); ++i) {
                if (jTable1.getValueAt(i, 0) != null) {
                    int id = Integer.parseInt(jTable1.getValueAt(i, 0).toString());
                    Byte b = get.get(id);
                    if (b != null) {
                        boolean all = false, first = false, second = false;
                        switch (b) {
                            case SELECTION_ALL:
                                all = first = second = true;
                                break;
                            case SELECTION_PART1:
                                first = true;
                                break;
                            case SELECTION_PART2:
                                second = true;
                                break;
                        }
                        jTable1.setValueAt(all, i, 1);
                        jTable1.setValueAt(first, i, 3);
                        jTable1.setValueAt(second, i, 5);
                    }
                }
            }
        }
    }
    
    public void addSelections(Map<Integer, Byte> sel) {
        int page = -1, beitBegin = -1, beitEnd = -1;
        for (Map.Entry<Integer, Byte> el : sel.entrySet()) {
            if (page == -1 || !(el.getKey() <= beitBegin && el.getKey() >= beitEnd)) {
                page = DbHelper.getMatnPageByBeit(el.getKey(), isDorrah);
                Map.Entry<Integer, Integer> r = DbHelper.getMatnPageBeitStartEnd(page, isDorrah);
                beitBegin = r.getKey(); beitEnd = r.getValue();
            }
            put(page, el);
        }
        refreshTable();
    }
    
    public void clearSelections() {
        selection.clear();
        refreshTable();
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
        jButton1 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("اختيار الشاهد");

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null}
            },
            new String [] {
                "#", "اختيار", "الشطر الأول", "اختيار", "الشطر الثاني", "اختيار"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.Boolean.class, java.lang.String.class, java.lang.Boolean.class, java.lang.String.class, java.lang.Boolean.class
            };
            boolean[] canEdit = new boolean [] {
                false, true, false, true, false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jTable1.setDoubleBuffered(true);
        jTable1.getTableHeader().setResizingAllowed(false);
        jTable1.getTableHeader().setReorderingAllowed(false);
        jScrollPane1.setViewportView(jTable1);

        jLabel1.setText("اختر الصفحة:");

        jLabel2.setText("اختر رقم البيت:");

        jLabel3.setText("اختر الباب:");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<MatnBab>());
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jButton1.setText("بحث...");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 614, Short.MAX_VALUE)
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
                        .addComponent(jComboBox1, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jSpinner2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 331, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        if (!fireEventTitle) return;
        MatnBab b = (MatnBab) jComboBox1.getSelectedItem();
        jSpinner2.setValue(lastSelectedTitle = b.beit);
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        String search = JOptionPane.showInputDialog(this, "الرجاء إدخال جملة البحث بدون حركات", "بحث",
                JOptionPane.QUESTION_MESSAGE);
        if (search != null && !search.trim().isEmpty()) {
            int max = 20;
            ShowWaitAction action = new ShowWaitAction("البحث",
                    arg -> DbHelper.searchMatn(search, isDorrah, max));
            action.setFinishCallback(() -> {
                Map<Integer, String> res = (Map<Integer, String>) action.getActionResult();
                if (res == null)
                    JOptionPane.showMessageDialog(this, "عفوا، إن جملة البحث التي أدخلتها لها نتائج كثيرة جدا. استخدم بحثا أدق",
                            "خطأ", JOptionPane.ERROR_MESSAGE);
                else if (res.isEmpty())
                    JOptionPane.showMessageDialog(this, "لا يوجد نتائج لجملة البحث",
                            "البحث", JOptionPane.INFORMATION_MESSAGE);
                else {
                    SearchMatnJPanel panel = new SearchMatnJPanel(res);
                    do {
                        if (JOptionPane.showOptionDialog(this, panel,
                                "نتائج البحث", JOptionPane.OK_CANCEL_OPTION,
                                JOptionPane.PLAIN_MESSAGE, null,
                                new String[] {"ذهاب إلى", "إلغاء الأمر"}, null) == 0) {
                            int id = panel.getSelectedBeit();
                            if (id != -1) {
                                jSpinner2.setValue(lastBeitSearched = id);
                                break;
                            } else if (JOptionPane.showConfirmDialog(this, 
                                    "لم تقم باختيار نتيجة. هل تريد العودة لاختيار نتيجة بحث؟", "البحث", 
                                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
                                break;
                        } else
                            break;
                    } while (true);
                }
            });
            action.actionPerformed(evt);
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    public static void main(String[] args) {
        JFrame frame = new ChooseShahedJFrame(false, 1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 450);
        frame.pack();
        frame.setVisible(true);
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
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
