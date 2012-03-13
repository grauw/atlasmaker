import java.awt.Frame;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

import java.awt.*;


public class AboutDialog extends JDialog implements ActionListener
{
	private static final long serialVersionUID = 8668228916639817663L;
	private JEditorPane textCopyRight;
    private JPanel panelMain;
    private JButton btnClose;
	JPanel panelCopyRight;
    public AboutDialog(Frame owner)
	{
		super(owner, "About");
        addWidgets();
		setContentPane(panelMain);
        pack();
        setVisible(false);
	}

    private void addWidgets()
    {
    	textCopyRight = new JEditorPane();
    	textCopyRight.setEditable(false);
    	java.net.URL url = AtlasFrame.class.getResource(
        	"copyright.html");
    	if (url != null) 
    	{
    	    try 
    	    {
    	    	textCopyRight.setPage(url);
    	    } catch (IOException e) 
    	    {
    	        System.err.println("Attempted to read a bad URL: " + url);
    	    }
    	} 
    	else 
    	{
    	    System.err.println("Couldn't find file: copyright.html");
    	}
    	JScrollPane editorScrollPane = new JScrollPane(textCopyRight);
    	editorScrollPane.setVerticalScrollBarPolicy(
    	                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    	editorScrollPane.setPreferredSize(new Dimension(400, 245));
    	editorScrollPane.setMinimumSize(new Dimension(10, 10));
    	panelCopyRight = new JPanel();
    	panelCopyRight.setBorder(BorderFactory.createEmptyBorder());
    	panelCopyRight.add(editorScrollPane);
    	
    	btnClose = new JButton("Close");
    	btnClose.addActionListener(this);
    	btnClose.setAlignmentX(CENTER_ALIGNMENT);

        panelMain = new JPanel();
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.PAGE_AXIS));
        panelMain.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panelMain.add(panelCopyRight);
        panelMain.add(btnClose);
    }
    
    public void actionPerformed(ActionEvent event)
	{
        if (event.getSource() == btnClose)
        {
        	setVisible(false);
        }		
	}
}
