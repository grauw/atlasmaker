import java.awt.Frame;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import javax.swing.*;
import java.text.*;

public class OptionsDialog extends JDialog implements ActionListener, ItemListener, 
	PropertyChangeListener
{
	private static final long serialVersionUID = 8668228916639817663L;
	private JTextField textTextToolPath;
    private JFormattedTextField textAlphaThresh;   
    private JFormattedTextField textMaxSize;
    private NumberFormat nfMaxSize;
    
    private JPanel panelMain, panelAlphaThresh;
    private JPanel panelPVRDir;
    private JPanel panelDimensions;
    private JPanel panelPVROptions;
    private JButton btnSelTextToolPath;
    private JButton btnClose;
    private JRadioButton btnPVR2bit, btnPVR4bit;
    private JRadioButton btnNPOT, btnPOT;
    private JCheckBox checkConvPVR;
    private ButtonGroup buttonGroupPVR; 
    private JFileChooser fc = null;
    private Properties properties;
    public OptionsDialog(Frame owner, Properties mainProperties)
	{
		super(owner, "Options");
		properties = mainProperties;
		panelDimensions = new JPanel();
        panelAlphaThresh = new JPanel();
        panelPVRDir = new JPanel();
        panelPVROptions = new JPanel();
        
        //Add various widgets to the sub panels.
        addWidgets();
        checkPVR();

        fc = new JFileChooser(".");
		setContentPane(panelMain);
        pack();
        setVisible(false);
	}


    private void addWidgets()
    {
    	String last;
    	btnPOT = new JRadioButton("Power-of-two");
    	btnNPOT = new JRadioButton("Non power-of-two");
    	ButtonGroup buttonGroupPOT = new ButtonGroup();
    	buttonGroupPOT.add(btnPOT);
    	buttonGroupPOT.add(btnNPOT);
        last = properties.getProperty("TextureSizeType");
        if (last == null)
        	btnPOT.setSelected(true);
        else 
        {
        	if (last.equals("POT"))
        		btnPOT.setSelected(true);
        	else if (last.equals("NPOT"))
        		btnNPOT.setSelected(true);
        }   	
        JLabel labelMaxSize = new JLabel();
        labelMaxSize.setText("Maximum size:");

        textMaxSize = new JFormattedTextField(nfMaxSize);
    	last = properties.getProperty("MaxSize");
        if (last != null)
        	textMaxSize.setValue(new Integer(last));
        else textMaxSize.setValue(new Integer(1024));
        textMaxSize.setColumns(5);
        labelMaxSize.setLabelFor(textMaxSize);
        textMaxSize.addPropertyChangeListener("value", this);                
        panelDimensions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Texture parameters"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        panelDimensions.add(labelMaxSize);
        panelDimensions.add(textMaxSize);
        panelDimensions.add(btnPOT);
        panelDimensions.add(btnNPOT);
    	
    	
    	textAlphaThresh = new JFormattedTextField (3);
    	textAlphaThresh.setEnabled(true);
    	textAlphaThresh.setToolTipText("Pixels below this threshold are considered transparent and will be trimmed");
    	textAlphaThresh.setColumns(3);
    	last = properties.getProperty("lastAlphaThresh");
        if (last != null)
        	textAlphaThresh.setValue(new Integer(last));
        else textAlphaThresh.setValue(new Integer(2));
        textAlphaThresh.addPropertyChangeListener("value", this);                
        panelAlphaThresh.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Alpha Threshold"),
            BorderFactory.createEmptyBorder(5,5,5,5)));
        panelAlphaThresh.add(textAlphaThresh);
        
    	textTextToolPath = new JTextField(40);
    	textTextToolPath.setEnabled(false);
        last = properties.getProperty("TextToolPath");
        if (last != null)
        	textTextToolPath.setText(last);
    	btnSelTextToolPath = new JButton("...");
    	btnSelTextToolPath.addActionListener(this);
        panelPVRDir.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Location of PVRTexTool"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        panelPVRDir.add(textTextToolPath);
        panelPVRDir.add(btnSelTextToolPath);

    	
    	btnPVR2bit = new JRadioButton("2 bit");
    	btnPVR4bit = new JRadioButton("4 Bit");
        buttonGroupPVR = new ButtonGroup();
        buttonGroupPVR.add(btnPVR2bit);
        buttonGroupPVR.add(btnPVR4bit);
        last = properties.getProperty("PVRType");
        if (last == null)
        	btnPVR2bit.setSelected(true);
        else 
        {
        	if (last.equals("2bit"))
        		btnPVR2bit.setSelected(true);
        	else if (last.equals("4bit"))
        		btnPVR4bit.setSelected(true);
        }   	
    	checkConvPVR = new JCheckBox("Convert PVRs");
    	checkConvPVR.addItemListener(this);
        panelPVROptions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("PVR Options"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        panelPVROptions.add(checkConvPVR);
        panelPVROptions.add(btnPVR2bit);
        panelPVROptions.add(btnPVR4bit);

    	btnClose = new JButton("Close");   	
    	btnClose.addActionListener(this);
        
        panelMain = new JPanel();
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.PAGE_AXIS));
        panelMain.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panelMain.add(panelDimensions);
        panelMain.add(panelAlphaThresh);
        panelMain.add(panelPVRDir);
        panelMain.add(panelPVROptions);
        panelMain.add(btnClose);
    }
    
    public void actionPerformed(ActionEvent event)
	{
        if (event.getSource() == btnSelTextToolPath)
        {
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setDialogType(JFileChooser.OPEN_DIALOG);
            fc.setDialogTitle("Select PVRTexTool exectuable");
            File file = new File(textTextToolPath.getText());
            if (file.isFile())
            {
                fc.setSelectedFile(file);
            }           
        	int returnVal = fc.showOpenDialog(this);
        	if (returnVal != JFileChooser.APPROVE_OPTION) return;
            file = fc.getSelectedFile();
            String strDir = file.getPath();
            textTextToolPath.setText(strDir);
            properties.setProperty("TextToolPath", strDir);
        	try
        	{
				properties.store(new FileOutputStream(AtlasFrame.savePlace), null);
			} catch (IOException e)
			{
                System.out.println("error: "+e.toString());
			}
		}
        if (event.getSource() == btnClose)
        {
        	setVisible(false);
            properties.setProperty("lastAlphaThresh", textAlphaThresh.getText());
            properties.setProperty("MaxSize", textMaxSize.getText());

            if (btnPOT.isSelected())
                properties.setProperty("TextureSizeType", "POT");
            else 
                properties.setProperty("TextureSizeType", "NPOT");

            if (btnPVR2bit.isSelected())
                properties.setProperty("PVRType", "2bit");
            else 
                properties.setProperty("PVRType", "4bit");
            
        	try
        	{
    			properties.store(new FileOutputStream(AtlasFrame.savePlace), null);
    		} catch (IOException ex)
    		{
                System.out.println("error: "+ex.toString());
    		}
    		
        }
		
	}

	public String getTextToolPath()
	{
		return textTextToolPath.getText();
	}
	public boolean isPVR4Bit()
	{
		return btnPVR4bit.isSelected();
	}
	public boolean isPOT()
	{
		return btnPOT.isSelected();
	}
	public int getAlphaThresh()
	{
		return ((Number)textAlphaThresh.getValue()).intValue();
	}

	public boolean convPVR()
	{
		return checkConvPVR.isSelected();
	}
	
    private void checkPVR()
    {
    	btnPVR2bit.setEnabled(checkConvPVR.isSelected());
    	btnPVR4bit.setEnabled(checkConvPVR.isSelected());
    }
    public void itemStateChanged(ItemEvent ie) 
    {
    	checkPVR();
    }


	public void propertyChange(PropertyChangeEvent e)
	{
        Object source = e.getSource();
        if (source == textMaxSize) 
        {
        	int value = ((Number)textMaxSize.getValue()).intValue();
        	if (value < 512)
        	{
        		textMaxSize.setValue(new Integer(512));
        	}
        	else if (value > 4096)
        	{
        		textMaxSize.setValue(new Integer(4096));
        	}
        }
        else if (source == textAlphaThresh) 
        {
        	int value = ((Number)textAlphaThresh.getValue()).intValue();
        	if (value < 0)
        	{
        		textAlphaThresh.setValue(new Integer(0));
        	}
        	else if (value > 128)
        	{
        		textAlphaThresh.setValue(new Integer(128));
        	}
        }
	}
	public int getMaxSize()
	{
		return ((Number)textMaxSize.getValue()).intValue();
	}
}
