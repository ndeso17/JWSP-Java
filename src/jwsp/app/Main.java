package jwsp.app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
        }
        
        SwingUtilities.invokeLater(() -> {
            ApplicationLifecycleManager lifecycle = ApplicationLifecycleManager.getInstance();
            lifecycle.initialize();
        });
    }
}
