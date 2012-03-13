/*
Copyright (c) 2012, Ed Welch
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

*    Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
*    Redistributions in binary form must reproduce the above copyright notice, this list of conditions 
     and the following disclaimer in the documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, 
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, 
OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, 
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

public class AtlasFrame extends JFrame implements 
	ActionListener, MouseListener, TreeSelectionListener 
{
	private static final long serialVersionUID = 4523664995494187059L;
	private JTextField textInputDir;
    private JTextField textOutFile;
    private JButton btnSelInputDir, btnSelOutFile;
    private JFileChooser fc = null;
    private JPanel panelInputDir, panelOutput, panelCreate;
    private JPanel panelMain, panelSettings;
    private JTree tree;
    private DefaultTreeModel treeModel;
    private JPanel panelOptions;
    private JTabbedPane tabbedPane;
    private JButton btnCreate;
    private JButton btnOptions;
    private JButton btnAbout;
    private JLabel labelError = null;
    private JPanel panelImageFormat;
    private JRadioButton[] btnImageFormat = new JRadioButton[4];
    private ButtonGroup buttonGroup; 
    private ArrayList<TexturePiece> listTexture;
    private ArrayList<DirNode> listDir = new ArrayList<DirNode>();
    private OptionsDialog optionsDialog;
    private AboutDialog aboutDialog;
    private Properties properties = new Properties();
    private AtlasMaker atlasMaker;
	public static final String savePlace = "atlasMaker.properties";
    public AtlasFrame() 
    {
		super("Atlas Maker");
		atlasMaker = new AtlasMaker();
		listTexture = new ArrayList<TexturePiece>();
        fc = new JFileChooser(".");
        try
        {
            properties.load(new FileInputStream(savePlace));
        }
        catch (IOException e)
        {
            System.out.println("error: "+e.toString());
        }
        addWidgets();

        optionsDialog = new OptionsDialog(this, properties);
        aboutDialog = new AboutDialog(this);
        

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        LoadTree();
        
        // Get the size of the screen
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        
        // Determine the new location of the window
        int w = getSize().width;
        int h = getSize().height;
        int x = (dim.width-w)/2;
        int y = (dim.height-h)/2;
        
        // Move the window
        setLocation(x, y);        
        setVisible(true);
        java.net.URL imgURL = AtlasFrame.class.getResource("icon.png");
        if (imgURL != null) 
        {
        	Toolkit kit = Toolkit.getDefaultToolkit();
        	Image img = kit.createImage(imgURL);
        	setIconImage(img);
        }   
    	pack();
    }

    private void addWidgets()
    {
		tabbedPane = new JTabbedPane();
        panelInputDir = new JPanel();
        panelOutput = new JPanel();
        panelOptions = new JPanel();
        panelCreate = new JPanel();
        panelImageFormat = new JPanel();
    	addInputStuff();
    	textOutFile = new JTextField(30);
    	btnSelOutFile = new JButton("...");
    	textOutFile.setEnabled(false);
    	String last = properties.getProperty("lastOutFile");
        if (last != null)
        	textOutFile.setText(last);
    	btnSelOutFile.addActionListener(this);
        //Add a border around the select panel.
        panelOutput.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Select output file"),
            BorderFactory.createEmptyBorder(5,5,5,5)));
        panelOutput.add(textOutFile);
        panelOutput.add(btnSelOutFile);

    	btnOptions = new JButton("Options...");
    	btnOptions.addActionListener(this);
    	btnAbout = new JButton("About...");
    	btnAbout.addActionListener(this);
        panelOptions.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(""),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        panelOptions.add(btnOptions);
        panelOptions.add(btnAbout);

    	labelError = new JLabel();
		labelError.setText(" ");
		labelError.setAlignmentX(CENTER_ALIGNMENT);
		labelError.setForeground(new Color(0x821B0D));
        
    	btnCreate = new JButton("Create Atlas");
    	btnCreate.addActionListener(this);
        panelCreate.add(labelError);
        panelCreate.add(btnCreate);
        
    	panelSettings = new JPanel();
    	panelSettings.setLayout(new BoxLayout(panelSettings, BoxLayout.PAGE_AXIS));
    	panelSettings.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        //Add the select and display panels to the main panel.
    	panelSettings.add(panelInputDir);
    	panelSettings.add(panelImageFormat);
    	panelSettings.add(panelOutput);
        panelSettings.add(panelOptions);
		ImageIcon icon = null;
        java.net.URL imgURL = AtlasFrame.class.getResource("settings.png");
        if (imgURL != null) 
        {
        	icon =  new ImageIcon(imgURL);
        } 
        tabbedPane.addTab("Settings", icon, panelSettings, "");

        panelMain = new JPanel();
        panelMain.setLayout(new BoxLayout(panelMain, BoxLayout.PAGE_AXIS));
        panelMain.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panelMain.add(tabbedPane);
        panelMain.add(labelError);
        panelMain.add(panelCreate);
		setContentPane(panelMain);
    }

    private void addInputStuff()
    {
    	textInputDir = new JTextField(30);
        String last = properties.getProperty("lastDir");
        if (last != null)
        	textInputDir.setText(last);
    	textInputDir.setEnabled(false);
    	btnSelInputDir = new JButton("...");
    	btnSelInputDir.addActionListener(this);
        panelInputDir.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Select input folder"),
            BorderFactory.createEmptyBorder(5,5,5,5)));
    	
    	DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("");
    	treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);       
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.addTreeSelectionListener(this);        
        DefaultTreeCellRenderer renderer = 
            new DefaultTreeCellRenderer();
        renderer.setLeafIcon(renderer.getClosedIcon());
        tree.setCellRenderer(renderer);
        JScrollPane scrollPane; 
    	scrollPane = new JScrollPane(tree);   	
    	scrollPane.setPreferredSize(new Dimension(100, 100));
    	
        panelImageFormat.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Output Format"),
                BorderFactory.createEmptyBorder(5,5,5,5)));
        buttonGroup = new ButtonGroup();
    	
    	for (int n = 0; n < btnImageFormat.length; n++)
    	{
    		btnImageFormat[n] = new JRadioButton(ImageFormat.getNameLong(n));
    		btnImageFormat[n].setEnabled(false);
    		btnImageFormat[n].addActionListener(this);
            panelImageFormat.add(btnImageFormat[n]);
            buttonGroup.add(btnImageFormat[n]);
    	}
    	      
        GroupLayout layout = new GroupLayout(panelInputDir);
        panelInputDir.setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);
        layout.setHorizontalGroup(
        		   layout.createSequentialGroup()
        		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
        		      .addComponent(textInputDir)
       		          .addComponent(scrollPane))
     		          .addComponent(btnSelInputDir)
        		);
		layout.setVerticalGroup(
		   layout.createSequentialGroup()
		      .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
		           .addComponent(textInputDir)
		           .addComponent(btnSelInputDir))
		      .addComponent(scrollPane)
		);               
    }
    
    
    public void actionPerformed(ActionEvent event)
    {
    	for (int n = 0; n < btnImageFormat.length; n++)
    	{
            if (event.getSource() == btnImageFormat[n])
            {
        		DirNode dirNode = getSelectedDirNode();
        		if (dirNode != null)
        		{
        	    	for (int f = 0; f < btnImageFormat.length; f++)
        	    	{
        	    		if (btnImageFormat[f].isSelected())
        	    			dirNode.setImageFormat(f);
        	    	}
        	    	setProperty(dirNode.getFileSpec(), dirNode.getStrImageFormat());
           		}
        		return;

            }
            
    	}
        if (event.getSource() == btnSelInputDir)
        {
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            File file = new File(textInputDir.getText());
       
            if (file.isDirectory())
            	fc.setCurrentDirectory(file);
            fc.setDialogTitle("Select Input Directory");
        	int returnVal = fc.showOpenDialog(this);
        	if (returnVal != JFileChooser.APPROVE_OPTION) return;
            File dir = fc.getSelectedFile();
            textInputDir.setText(dir.getPath());
            setProperty("lastDir", dir.getPath());
	        LoadTree();
		}
        else if (event.getSource() == btnSelOutFile)
        {
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fc.setDialogType(JFileChooser.SAVE_DIALOG);
            fc.setDialogTitle("Select Output File");
            File file = new File(textOutFile.getText());
            if (file.isFile())
            {
                fc.setSelectedFile(file);
            }
            
        	int returnVal = fc.showSaveDialog(this);
        	if (returnVal != JFileChooser.APPROVE_OPTION) return;
            File dir = fc.getSelectedFile();
            textOutFile.setText(dir.getPath());
            setProperty("lastOutFile", dir.getPath());
		}
        else if (event.getSource() == btnCreate)
        {
        	MakeAtlas();
        }
        else if (event.getSource() == btnOptions)
        {
        	optionsDialog.setLocationRelativeTo(btnOptions);
        	optionsDialog.setVisible(true);
        }
        else if (event.getSource() == btnAbout)
        {
        	aboutDialog.setLocationRelativeTo(btnAbout);
        	aboutDialog.setVisible(true);
        }
    }

    private void MakeAtlas()
    {
		labelError.setText(" ");
    	String strOutFile = textOutFile.getText();
        int pos = strOutFile.indexOf('.');
        if (strOutFile.length() < 8 || pos == -1)
        {
    		labelError.setText("Invalid Output file name");
        	return;
        }
    	String strOutDir = "";
        pos = strOutFile.lastIndexOf(File.separatorChar);
        if (pos != -1)
        {
        	strOutDir = strOutFile.substring(0, pos+1);
        }
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        listTexture.clear();
        for (int n = 0; n < listDir.size(); n++)
        {
    		atlasMaker.create(listDir.get(n), 
    				strOutDir+listDir.get(n).getName(), listTexture, optionsDialog);            	
        	if (atlasMaker.getStrErr() != "")
        		labelError.setText(atlasMaker.getStrErr());
        }
    	CreateOutputTabs();
        if (listTexture.size() == 0)
        {
        	labelError.setText("No PNG images found");
        }
        else
        {
        	WriteXML();
        }
        setCursor(Cursor.getDefaultCursor());
    }

    private void CreateOutputTabs()
    {
    	// remove in reverse order otherwise index changes as you remove
	    for (int n = tabbedPane.getTabCount()-1; n > 0; n--) 
	    {
	    	tabbedPane.remove(n);
	    }
	    ImageIcon icon;
	    for (int n = 0; n < listTexture.size(); n++) 
	    {
	    	JPanel panelDisplay;
	        panelDisplay = new JPanel();
	        
	        JLabel labelImage;		// this is where the atlas is displayed
	    	labelImage = new JLabel();
	    	labelImage.addMouseListener(this);
	    	BufferedImage bufferedImage = listTexture.get(n).getImageDisplay();
			ImageIcon imageIcon = new ImageIcon(bufferedImage);
			labelImage.setIcon(imageIcon);
			String strSize = ""+imageIcon.getIconWidth()+" x " + imageIcon.getIconHeight();
	        panelDisplay.setBorder(BorderFactory.createCompoundBorder(
	                BorderFactory.createTitledBorder(strSize),
	                BorderFactory.createEmptyBorder(5,5,5,5)));
	        panelDisplay.setLayout(new BoxLayout(panelDisplay, BoxLayout.PAGE_AXIS));
	    	int xMax = imageIcon.getIconWidth()+20;
	    	if (xMax > 1024) xMax = 1024;
	    	int yMax = imageIcon.getIconHeight()+20;
	    	if (yMax > 600) yMax = 600;

	        JScrollPane scrollPane; 
	    	scrollPane = new JScrollPane(labelImage);
	    	scrollPane.setPreferredSize(new Dimension(xMax, yMax));
	        panelDisplay.add(scrollPane);

	        icon = new ImageIcon(bufferedImage.getSubimage(0, 0, 16, 16));
	        tabbedPane.addTab(listTexture.get(n).getName(), icon, panelDisplay, "");
	    }
    	pack();
    }
        
    
    private void WriteXML()
    {
		int numImages = 0;
	    String strAtlasData = "";
	    for (int n = 0; n < listTexture.size(); n++) 
	    {
	    	numImages += listTexture.get(n).getListImage().size();
	    	strAtlasData += listTexture.get(n).getStrAtlasData();
	    }
	    File fileText = new File(textOutFile.getText());
	    FileOutputStream fileTextOutput;
	    DataOutputStream dataTextOut;
	    try 
	    {
	        fileTextOutput = new FileOutputStream(fileText);
	        dataTextOut = new DataOutputStream (fileTextOutput);
	        dataTextOut.writeBytes("<atlas numImages=\""+numImages+"\">\n");
	        dataTextOut.writeBytes(strAtlasData);
	        dataTextOut.writeBytes("</atlas>\n");
	    } 
	    catch (Exception e)
	    {
	        System.out.println("error: "+e.toString());
			labelError.setText("error: "+e.toString());
	    }
    }
    
    private void LoadTree()
    {
    	if (textInputDir.getText().length() < 5) 
    	{
    		btnCreate.setEnabled(false);
    		return;
    	}
		btnCreate.setEnabled(true);
        String inDir = textInputDir.getText();
        int pos = inDir.lastIndexOf(File.separatorChar);
        String atlasName = inDir; 
        if (pos != -1)
        {
        	atlasName = inDir.substring(pos+1);
        }
	    File[] files = null;
	    
	    Stack<DirNode> stackNode = new Stack<DirNode>();
	    DirNode dirNode = new DirNode(atlasName, textInputDir.getText(), 
	    		properties.getProperty(textInputDir.getText()));
	    DefaultMutableTreeNode root = dirNode.getNode(); 
	    stackNode.push(dirNode);
	    listDir.clear();
	    listDir.add(dirNode);
	    DirNode leaf; 
	    do
	    {
	    	dirNode = stackNode.pop();
	    	files = dirNode.getFiles();     	
		    if (files != null) 
		    {
		    	int n;
		        for (n = 0; n < files.length; n++) 
		        {
		        	if (files[n].isDirectory())
		        	{
		        		leaf = new DirNode(files[n].getName(), files[n].getAbsolutePath(),
		        				properties.getProperty(files[n].getAbsolutePath()));
		        		dirNode.getNode().add(leaf.getNode());
		        	    stackNode.push(leaf);
		        	    listDir.add(leaf);
		        	}
		        }
		    }
	    } while (!stackNode.isEmpty());
	    treeModel.setRoot(root);
    	pack();
    }
          
    // the following 5 methods must be defined if you
	// implements MouseListener
	public void mouseClicked(MouseEvent arg0)
	{
	}
	public void mouseEntered(MouseEvent arg0)
	{
	}
	public void mouseExited(MouseEvent arg0)
	{
	}
	public void mousePressed(MouseEvent mouseEvent)
	{
        int index = tabbedPane.getSelectedIndex();
        if (index < 1) return;
		ArrayList<AtlasImage> listImage = listTexture.get(index-1).getListImage();
		Point point = mouseEvent.getPoint();
		Component component = mouseEvent.getComponent();
		
		Dimension dimension = component.getSize();
    	BufferedImage bufferedImage = listTexture.get(index-1).getImageDisplay();
    	int yOffset = (dimension.height - bufferedImage.getHeight())/2;
    	for (int n = 0; n < listImage.size(); n++)
    	{
    		if (listImage.get(n).pointInside(point.x, point.y - yOffset))
    		{
    			labelError.setText(listImage.get(n).getFileName());
    			return;
    		}
    	}
		labelError.setText(" ");
	}

	public void mouseReleased(MouseEvent arg0)
	{
	}

	private DirNode getSelectedDirNode() 
	{
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)
        	tree.getLastSelectedPathComponent();
        for (int n = 0; n < listDir.size(); n++)
        {
    		if (listDir.get(n).getNode() == node)
    		{
    	    	return listDir.get(n);
    		}
        }
        return null;
	}
	
	public void valueChanged(TreeSelectionEvent e) 
	{
		DirNode dirNode = getSelectedDirNode();
    	for (int n = 0; n < btnImageFormat.length; n++)
    	{
    		btnImageFormat[n].setEnabled(dirNode != null);
    	}
		if (dirNode != null)
		{
			btnImageFormat[dirNode.getImageFormat()].setSelected(true);
   		}
	}
		
	public void setProperty(String key, String value)
	{
		properties.setProperty(key, value);
		try
		{
			properties.store(new FileOutputStream(savePlace), null);
		} catch (IOException e)
		{
	        System.out.println("error: "+e.toString());
		}
	}		

}

