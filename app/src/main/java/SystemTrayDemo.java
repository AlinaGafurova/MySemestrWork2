import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SystemTrayDemo{

    public static void main(String []args){

        if(!SystemTray.isSupported()){
            System.out.println("System tray is not supported !!! ");
            return ;
        }
        SystemTray systemTray = SystemTray.getSystemTray();

        Image image = Toolkit.getDefaultToolkit().getImage("src/images/1.gif");

        PopupMenu trayPopupMenu = new PopupMenu();

        MenuItem action = new MenuItem("Action");
        action.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "Action Clicked");
            }
        });
        trayPopupMenu.add(action);

        MenuItem close = new MenuItem("Close");
        close.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        trayPopupMenu.add(close);

        TrayIcon trayIcon = new TrayIcon((new SystemTrayDemo()).createImageIcon("dot.png","Dot").getImage(), "SystemTray Demo", trayPopupMenu);

        trayIcon.setImageAutoSize(true);

        try{
            systemTray.add(trayIcon);
        }catch(AWTException awtException){
            awtException.printStackTrace();
        }
        System.out.println("End of main");

    }

    protected  ImageIcon createImageIcon(String path, String description) {

        java.net.URL imgURL = getClass().getClassLoader().getResource(path);
        System.out.println(imgURL);
        if (imgURL != null) {

            return new ImageIcon(imgURL, description);
        } else {
            System.err.println("Could not find file: " + path);
            return null;
        }
    }

}