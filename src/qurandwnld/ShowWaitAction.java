/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package qurandwnld;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

/**
 *
 * @author Yasser
 */
public class ShowWaitAction extends AbstractAction {

    private static final Font defaultFont = new Font("Traditional Arabic", Font.BOLD, 20);
    
    /**
     * @return the finishCallback
     */
    public Runnable getFinishCallback() {
        return finishCallback;
    }

    /**
     * @param finishCallback the finishCallback to set
     */
    public void setFinishCallback(Runnable finishCallback) {
        this.finishCallback = finishCallback;
    }

    /**
     * @return the actionArgument
     */
    public Object getActionArgument() {
        return actionArgument;
    }

    /**
     * @param actionArgument the actionArgument to set
     */
    public void setActionArgument(Object actionArgument) {
        this.actionArgument = actionArgument;
    }

    /**
     * @return the actionResult
     */
    public Object getActionResult() {
        return actionResult;
    }
    
    public static interface BackgroundAction {
        Object doInBackground(Object arg);
    }
    
    private final BackgroundAction action;
    
    private Runnable finishCallback;
    private Object actionArgument, actionResult;

    public ShowWaitAction(String name, BackgroundAction action) {
        super(name);
        this.action = action;
    }

    @Override
    public void actionPerformed(ActionEvent evt) {
        SwingWorker<Void, Void> mySwingWorker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                actionResult = action.doInBackground(getActionArgument());
                Runnable finish = getFinishCallback();
                if (finish != null) {
                    java.awt.EventQueue.invokeLater(finish);
                }
                return null;
            }
        };

        Window win = SwingUtilities.getWindowAncestor((Component) evt.getSource());
        final JDialog dialog = new JDialog(win, "مصحف القراءات", Dialog.ModalityType.APPLICATION_MODAL);
        mySwingWorker.addPropertyChangeListener((PropertyChangeEvent evt1) -> {
            if (evt1.getPropertyName().equals("state")) {
                if (evt1.getNewValue() == SwingWorker.StateValue.DONE) {
                    dialog.dispose();
                }
            }
        });
        mySwingWorker.execute();

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(progressBar, BorderLayout.CENTER);
        JLabel jLabel = new JLabel("الرجاء الانتظار....");
        jLabel.setFont(defaultFont);
        panel.add(jLabel, BorderLayout.PAGE_START);
        dialog.add(panel);
        dialog.pack();
        dialog.setLocationRelativeTo(win);
        dialog.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        dialog.setVisible(true);
    }
}
