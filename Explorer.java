import java.awt.Color;
import java.awt.Dimension;
import java.awt.Desktop;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Cursor;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.RootPaneContainer;
import javax.swing.BorderFactory;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Enumeration;
import java.util.Date;
import java.util.Iterator;
import java.util.Collections;
import java.util.ArrayList;

public class Explorer extends MouseAdapter implements ActionListener, TreeWillExpandListener {
	JFrame window;
	JTree explorerTree;
	DefaultMutableTreeNode root;
	JButton back, next, refresh;
	JTextField addressBar;
	String globalPath;
	JPanel explorePanel;
	JScrollPane exploreScroll;
	SysView sys;
	LinkedHashMap<JPanel, JTextField> icons;
	LinkedHashMap<JPanel, JTextField> selectedIcons;
	JPanel selectionAnchor;
	Nav myNav;
	JLabel imageLabel;
	JLabel name, modified, size;
	boolean ctrlKey;
	boolean shiftKey;
	boolean hiddenDisable;
	Border borderBlue, borderWhite;
	String ram;
	JPopupMenu explorerPopup;
	JMenuItem open;
	JMenuItem cut;
	JMenuItem copy;
	JMenuItem paste;
	JMenuItem rename;
	JMenuItem delete;
	JMenuItem newFolder;
	JMenuItem properties;
	File[] copyFiles, cutFiles;
	boolean copyReady;
	boolean cutReady;
	boolean mousePaste;

	public static void main(String... s) {
		// catch all exceptions....
		try {
			new Explorer();
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}

	public Explorer() {
		window = new JFrame("Explorer [Developed By Manoj Vadehra]");
		// flags for CTRL and SHIFT Masks
		ctrlKey = false;
		shiftKey = false;
		// setting main JFrame Focusable to trap events
		window.setFocusable(true);
		// key event listener for frame and explorerPanel
		KeyAdapter k = new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				// toggle hidden files window +H shortcut
				if (e.getKeyCode() == KeyEvent.VK_H && ctrlKey) {
					hiddenDisable = !hiddenDisable;
					refresh();
				} else if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
					back();
				else if ((e.getKeyCode() == KeyEvent.VK_RIGHT) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
					next();
				else if ((e.getKeyCode() == KeyEvent.VK_A) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
					selectAll();
				else if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
					copy();
				else if ((e.getKeyCode() == KeyEvent.VK_X) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
					cut();
				// else if((e.getKeyCode()==KeyEvent.VK_X)&&((e.getModifiers()&
				// KeyEvent.CTRL_MASK)!=0))cut();
				else if (e.getKeyCode() == KeyEvent.VK_V && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0))
					paste();
				else if (e.getKeyCode() == KeyEvent.VK_DELETE)
					delete();
				else if (e.getKeyCode() == KeyEvent.VK_CONTROL)
					ctrlKey = true;
				else if (e.getKeyCode() == KeyEvent.VK_SHIFT)
					shiftKey = true;
				else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
					selectNext();
				else if (e.getKeyCode() == KeyEvent.VK_ENTER)
					execute();
				else if (e.getKeyCode() == KeyEvent.VK_F5)
					refresh();
				else if (e.getKeyCode() == KeyEvent.VK_LEFT && selectionAnchor != null)
					selectPrevious();
				else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					clearSelections();
					selectionAnchor = null;
					setParentProperties();
				} else if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU && selectedIcons.size() != 0)
					popup();

			}

			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_CONTROL)
					ctrlKey = false;
				else if (e.getKeyCode() == KeyEvent.VK_SHIFT)
					shiftKey = false;
			}
		};
		// add Keylistener to JFrame
		window.addKeyListener(k);
		window.setBackground(Color.WHITE);
		// Top and Left Panels
		JPanel treePanel, addressPanel;
		/*
		 * Object of Custom class SysView which extends abstract class FileSystemView.
		 * Usage: To get full names of Root Directories.
		 */
		sys = new SysView();
		/*
		 * A LinkedHashMap to temporarily store all icons(JPanels and their JTextFields)
		 */
		icons = new LinkedHashMap<JPanel, JTextField>();
		/*
		 * Nav:Custom class which traverse an ArrayList in on demand order.
		 * Usage: Manages Explorer Back and Next Pattern
		 */
		myNav = new Nav();
		// Tree Panel
		// root node
		root = new DefaultMutableTreeNode("My Computer");
		explorerTree = new JTree(root);
		/*
		 * This Event is fired before the node expands.
		 */
		explorerTree.addTreeWillExpandListener(this);
		/*
		 * Mouse Listener for opening directories and files directly from the tree.
		 */
		explorerTree.addMouseListener(this);
		/*
		 * Avoid events getting trapped by JTree
		 */
		// explorerTree.setFocusable(false);
		// scroll pane for JTree
		JScrollPane explorerTreeScroll = new JScrollPane(explorerTree);
		// main tree panel
		treePanel = new JPanel();
		treePanel.setLayout(new BorderLayout());
		treePanel.add(explorerTreeScroll, BorderLayout.CENTER);
		// explorePanel
		/*
		 * Our main panel
		 * Icons will be displayed here.
		 */
		explorePanel = new JPanel();
		explorePanel.setBackground(Color.WHITE);
		/*
		 * Mouse Listener to make selectios, open files and directories.
		 */
		explorePanel.addMouseListener(this);
		/*
		 * Key listener to listen for All keyboard commands such as:
		 * F5, Ctrl, Shift, Ctrl+A etc.
		 */
		explorePanel.addKeyListener(k);
		/*
		 * Set this panel as focusable to trap events
		 */
		explorePanel.setFocusable(true);
		/*
		 * ScrollPane for explore Panel
		 */
		exploreScroll = new JScrollPane(explorePanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		exploreScroll.getVerticalScrollBar().setUnitIncrement(16);
		/*
		 * JSplit Pane for tree and explore panel
		 * it works as a seperator that draws a seperator line among componenets and
		 * that line is resizable.
		 */
		JSplitPane jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treePanel, exploreScroll);
		jsp.setDividerSize(5);
		jsp.setResizeWeight(.10);
		// top panel
		/*
		 * address panel (Shows globalPath)
		 */
		addressPanel = new JPanel();
		addressPanel.setLayout(new BorderLayout());
		/*
		 * Navigation and refresh button
		 */
		back = new JButton();
		next = new JButton();
		refresh = new JButton();
		/*
		 * ToolTips
		 */
		back.setToolTipText("Back");
		next.setToolTipText("Next");
		refresh.setToolTipText("Refresh");
		/*
		 * Sizes
		 */
		back.setPreferredSize(new Dimension(20, 20));
		next.setPreferredSize(new Dimension(20, 20));
		refresh.setPreferredSize(new Dimension(20, 20));
		back.setMaximumSize(new Dimension(20, 20));
		next.setMaximumSize(new Dimension(20, 20));
		refresh.setMaximumSize(new Dimension(20, 20));

		JPanel buttonPanel = new JPanel();
		/*
		 * Images for buttons
		 */
		Image imgBack = (new ImageIcon("images\\back.png")).getImage();
		Image newimgBack = imgBack.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		Image imgNext = (new ImageIcon("images\\forward.png")).getImage();
		Image newimgNext = imgNext.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		Image imgRefresh = (new ImageIcon("images\\refresh.png")).getImage();
		Image newimgRefresh = imgRefresh.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		/*
		 * Set images
		 */
		back.setIcon(new ImageIcon(newimgBack));
		next.setIcon(new ImageIcon(newimgNext));
		refresh.setIcon(new ImageIcon(newimgRefresh));
		/*
		 * Disable borders.
		 */
		back.setBorder(null);
		next.setBorder(null);
		refresh.setBorder(null);
		/*
		 * disable navigation buttons initially.
		 */
		back.setEnabled(false);
		next.setEnabled(false);
		/*
		 * add listeners
		 */
		back.addActionListener(this);
		next.addActionListener(this);
		refresh.addActionListener(this);
		/*
		 * add navigatin buttons
		 */
		buttonPanel.add(back);
		buttonPanel.add(next);

		addressBar = new JTextField();
		addressBar.setToolTipText("Current File System Path");
		// addressBar.setEditable(false);
		addressBar.setBackground(Color.WHITE);
		globalPath = "My Computer";
		/*
		 * Set global Path to My Computer
		 */
		updatePath();
		/*
		 * add components to TopPanel
		 */
		addressPanel.add(buttonPanel, BorderLayout.WEST);
		addressPanel.add(addressBar, BorderLayout.CENTER);
		addressPanel.add(refresh, BorderLayout.EAST);
		/*
		 * container for first JSplitPanel
		 */
		JPanel exp = new JPanel();
		exp.setLayout(new BorderLayout());
		exp.add(jsp, BorderLayout.CENTER);
		/*
		 * Bottom properties panel
		 */
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		imageLabel = new JLabel();
		bottomPanel.add(imageLabel, BorderLayout.WEST);
		JPanel details = new JPanel();
		details.setLayout(new FlowLayout(FlowLayout.LEFT, 20, 20));
		/*
		 * Fields for properties
		 */
		name = new JLabel();
		modified = new JLabel();
		size = new JLabel();
		details.add(name);
		details.add(modified);
		details.add(size);

		// Hint Panel
		JPanel hint = new JPanel();
		hint.add(new JLabel("Press ctrl+H to toggle Hidden Files"));

		bottomPanel.add(hint, BorderLayout.EAST);
		bottomPanel.add(details, BorderLayout.CENTER);
		/*
		 * JSplitPane for exp and bottomPanel
		 */
		JSplitPane fsp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, exp, bottomPanel);
		fsp.setDividerSize(5);
		fsp.setResizeWeight(.95);
		/*
		 * JFrame Properties
		 */
		window.add(addressPanel, BorderLayout.NORTH);
		window.add(fsp, BorderLayout.CENTER);
		/*
		 * More instances of explorer may exist! So dispose this copy!
		 */
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		window.setSize(1000, 500);
		// window.setExtendedState(JFrame.MAXIMIZED_BOTH);
		/*
		 * Set first navigation entry to My Computer
		 */
		myNav.save("My Computer");

		selectedIcons = new LinkedHashMap<JPanel, JTextField>();
		borderBlue = BorderFactory.createLineBorder(Color.BLUE);
		borderWhite = BorderFactory.createLineBorder(Color.WHITE);
		/*
		 * get System RAM
		 */
		getSystemRAM();
		/*
		 * construct Tree with Root Drives
		 */
		prepareTree();
		// disable hidden files.
		hiddenDisable = true;
		/*
		 * Load initial screen icons i.e., Root Drives
		 */
		refresh();
		/*
		 * Finally, make JFrame visible
		 */
		window.setVisible(true);
		createPopupMenus();
	}

	private void createPopupMenus() {
		explorerPopup = new JPopupMenu();
		copyReady = false;
		cutReady = false;
		// PopupMenuAdapter pop=new PopupMenuAdapter(){

		// };
		explorerPopup.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent popupMenuEvent) {
				explorerPopup.removeAll();
				int size = selectedIcons.size();
				if (globalPath.equals("My Computer")) {
					if (size == 1)
						explorerPopup.add(open);
					return;
				}
				if (size == 1) {
					explorerPopup.add(open);
					explorerPopup.addSeparator();
				}
				explorerPopup.add(cut);
				explorerPopup.add(copy);
				if (size == 0 || size == 1) {
					explorerPopup.add(paste);
					if (copyReady || cutReady)
						paste.setEnabled(true);
					else
						paste.setEnabled(false);
				}
				if (size > 0) {
					explorerPopup.addSeparator();
					explorerPopup.add(delete);
				}
				if (size == 1) {
					explorerPopup.addSeparator();
					explorerPopup.add(rename);
					explorerPopup.addSeparator();
					explorerPopup.add(properties);
				}
				if (size == 0) {
					explorerPopup.addSeparator();
					explorerPopup.add(newFolder);
				}
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
		open = new JMenuItem("Open");
		cut = new JMenuItem("Cut");
		copy = new JMenuItem("Copy");
		paste = new JMenuItem("Paste");
		rename = new JMenuItem("Rename");
		delete = new JMenuItem("Delete");
		newFolder = new JMenuItem("New Folder");
		properties = new JMenuItem("Properties");
		open.addActionListener(this);
		cut.addActionListener(this);
		copy.addActionListener(this);
		paste.addActionListener(this);
		rename.addActionListener(this);
		delete.addActionListener(this);
		newFolder.addActionListener(this);
		properties.addActionListener(this);
	}

	private void getSystemRAM() {
		// Code from stackoverflow.commands
		// query physical memory
		try {
			// platform dependent package- may throw Exception
			com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory
					.getOperatingSystemMXBean();
			long physicalMemorySize = os.getTotalPhysicalMemorySize();
			ram = convert(physicalMemorySize);
		} catch (Exception e) {
			System.out.println("RAM size could not be determined.");
			ram = null;
		}
	}

	/*
	 * This method is executed whenever right arrow key is pressed.
	 */
	private void selectNext() {
		// if there are no icons quit
		if (icons.size() == 0)
			return;
		/*
		 * If no icon is selected. Select the first icon.
		 */
		if (selectionAnchor == null) {
			// check if any icons exist or not.
			if (icons.size() > 0) {
				// if yes, select first icon.
				// get first panel
				selectionAnchor = icons.keySet().iterator().next();
				// set selection border
				selectionAnchor.setBorder(borderBlue);
				// clear any selection icons in LinkedHashMap
				selectedIcons.clear();
				// put the current selection in the LinkedHashMap
				selectedIcons.put(selectionAnchor, icons.get(selectionAnchor));
			} else
				setParentProperties();
		} else {
			if (!shiftKey) {
				/*
				 * If shift key is not pressed....... deselect current icon and select next icon
				 */
				Iterator<JPanel> i = icons.keySet().iterator();
				while (i.hasNext()) {
					// current Panel in iterator..
					JPanel currentPanel = i.next();
					// find current panel in the LinkedHashMap
					if (currentPanel == selectionAnchor) {
						// check if any more icons exist in the directory..
						if (i.hasNext()) {
							currentPanel = i.next();
							// deselect current icon
							clearSelections();
							// select next icon
							selectionAnchor = currentPanel;
							// set selection border
							selectionAnchor.setBorder(borderBlue);
							// put it in the list.
							selectedIcons.put(selectionAnchor, icons.get(selectionAnchor));
							// scroll to icon
							scrollToPanel(selectionAnchor);
							break;
						}
					}
				}
			} else// shift key pressed
			{
				// shift key pressed with right arrow
				// if selection direction is right.
				if (selectedIcons.size() == 1 || getSelectionDirection().equals("Right")) {
					// last selected panel
					JPanel last = null;
					Iterator<JPanel> j = selectedIcons.keySet().iterator();
					while (j.hasNext())
						last = j.next();

					// select one more beyond last
					Iterator<JPanel> i = icons.keySet().iterator();
					while (i.hasNext()) {
						if (last == i.next()) {
							if (i.hasNext()) {
								JPanel current = i.next();
								current.setBorder(borderBlue);
								selectedIcons.put(current, icons.get(current));
								scrollToPanel(current);
							}
						}
					}
				} else if (getSelectionDirection().equals("Left")) {
					// delete last selection if it is not selectionAnchor
					// last selected panel
					JPanel last = null;
					Iterator<JPanel> j = selectedIcons.keySet().iterator();
					while (j.hasNext())
						last = j.next();
					if (last != selectionAnchor) {
						last.setBorder(borderWhite);
						selectedIcons.remove(last);
						scrollToPanel(last);
					}
				}
				// update the properties
				updateProperties();
			}
			/*
			 * This method is executed whenever left arrow key is pressed.
			 * Similar to selectNext() with opposite functionality
			 */
		}
	}

	private void selectPrevious() {
		if (!shiftKey) {
			JPanel previousPanel = null;
			Iterator<JPanel> i = icons.keySet().iterator();
			while (i.hasNext()) {
				JPanel currentPanel = i.next();
				if (currentPanel == selectionAnchor) {
					if (previousPanel == null) {
						// no more panels exist on left
					} else {
						clearSelections();
						selectionAnchor = previousPanel;
						selectionAnchor.setBorder(borderBlue);
						selectedIcons.put(selectionAnchor, icons.get(selectionAnchor));
						scrollToPanel(selectionAnchor);
						break;
					}
				} else {
					previousPanel = currentPanel;
				}
			}
		} else { // shift key
					// shift with right arrow
					// if selection direction is right

			if (selectedIcons.size() == 1 || getSelectionDirection().equals("Left")) {
				// last selected panel
				JPanel last = null;
				JPanel previous = null;
				Iterator<JPanel> j = selectedIcons.keySet().iterator();
				while (j.hasNext())
					last = j.next();
				// select one more beyond last
				Iterator<JPanel> i = icons.keySet().iterator();
				while (i.hasNext()) {
					JPanel current = i.next();
					if (current == last) {
						if (previous != null) {
							previous.setBorder(borderBlue);
							selectedIcons.put(previous, icons.get(previous));
							scrollToPanel(previous);
						}
						break;
					} else
						previous = current;
				}
			} else if (getSelectionDirection().equals("Right")) {
				// delete last selection if it is not selectionAnchor
				// last selected panel
				JPanel last = null;
				Iterator<JPanel> j = selectedIcons.keySet().iterator();
				while (j.hasNext())
					last = j.next();
				if (last != selectionAnchor) {
					last.setBorder(borderWhite);
					selectedIcons.remove(last);
					scrollToPanel(last);
				}
			}
		}
		updateProperties();
	}

	/* Checks the selection direction */
	private String getSelectionDirection() {
		Iterator<JPanel> j = selectedIcons.keySet().iterator();
		JPanel last = null;
		while (j.hasNext())
			last = j.next();
		Iterator<JPanel> i = icons.keySet().iterator();
		String direction = "";
		while (i.hasNext()) {
			JPanel current = i.next();
			// if anchor is found first..... direction is right
			if (current == selectionAnchor) {
				direction = "Right";
				break;
			}
			// if last is found first... direction is left
			if (current == last) {
				direction = "Left";
				break;
			}
		}
		return direction;
	}

	/* This method scroll to the mentioned panel */
	private void scrollToPanel(JPanel panel) {
		explorePanel.scrollRectToVisible(panel.getBounds());
	}

	/* This method prepares the JTree for first use. (Loads Root Nodes) */
	private void prepareTree() {
		// load initial Drives..
		// System.out.println("prepare Tree Executed");
		root.removeAllChildren(); // it will remove all the child roots and only root node will be visible
		File[] drives = File.listRoots(); // it returns all the roots of the file. i.e. all drives of the system.

		for (int i = 0; i < drives.length; i++) {
			String fullName = sys.getSystemDisplayName(drives[i]);
			// System.out.println(fullName);
			if (fullName.equals(""))
				fullName = "(" + drives[i].toString().substring(0, 2) + ")";
			DefaultMutableTreeNode drive = new DefaultMutableTreeNode(fullName);

			drive.add(new DefaultMutableTreeNode(""));
			root.add(drive);
			// ((DefaultTreeModel) explorerTree.getModel()).reload(root);
			((DefaultTreeModel) explorerTree.getModel()).nodeStructureChanged(root);
		}
	}

	/* This method redirects the MouseEvent object to corresponding source. */
	public void mouseClicked(MouseEvent e) {
		try {
			// clicked on tree
			if (e.getSource() == explorerTree)
				mouseClickOnTree(e);
			else
			// check if clicked on a panel
			if (icons.containsKey(e.getSource())) {
				explorePanel.requestFocus();
				mouseClickOnIcon(e);
			} else {
				// clicked on explorePanel- Reset all icon selections
				if (e.getSource() == explorePanel) {
					if (!selectedIcons.isEmpty()) {
						clearSelections();
						setParentProperties();
						selectionAnchor = null;
					}
					explorePanel.requestFocus();
					if (e.getButton() == MouseEvent.BUTTON3)
						explorerPopup.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/* This method updates the globalPath..... */
	private void updatePath() {
		addressBar.setText(globalPath);
	}

	private void mouseClickOnTree(MouseEvent e) {
		System.out.println("mouse clicked");
		// event on tree
		// get path of node clicked
		TreePath tp = explorerTree.getPathForLocation(e.getX(), e.getY());
		System.out.println(); // [My Computer, New Volume (D:)]
		// if null stop processing
		if (tp == null)
			return;
		String fPath = translateTreePath(tp.toString());
		System.out.println(fPath);
		// check physical path if not exists stop processing
		if (fPath == null)
			return;
		File file = new File(fPath);
		if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
			// double click from left mouse buttonPanel
			if (file.exists() && file.isFile()) {
				// if it is a file execute it.
				// Using Desktop instance throws exception in case of files without
				// a default Applciation
				// Desktop.getDesktop().open(file);

				// Directly call windows rundll32 utility(windows) or ShellExec for Linux to
				// execute a file
				try {
					Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + file.getAbsolutePath());
				} catch (IOException ex) {
					JOptionPane.showMessageDialog(window, "Could not execute this file!", "Explorer",
							JOptionPane.INFORMATION_MESSAGE);
				}
			}
		} else {
			// any number of clicks.....
			// if directory.... open it in explorer panel
			System.out.println(globalPath + "    " + fPath);
			boolean isRoot = checkRoot(fPath);
			// System.out.println("path: "+fPath+"isDirectory: "+file.isDirectory());
			if (isRoot || file.isDirectory() || fPath.equals("My Computer")) {
				if (!globalPath.equals(fPath)) {
					globalPath = fPath;

					System.out.println(myNav.paths + "hffh");
					if (myNav.showNext() == null) {
						// no next paths in list
						myNav.save(globalPath);
						next.setEnabled(false);
					} else if (myNav.showNext() != null && (!myNav.showNext().equals(globalPath))) {
						// if folder being opened is not in navigation list
						// clip or delete all next paths in navigation list.
						next.setEnabled(false);
						System.out.println(myNav.paths);
						myNav.clip();
						System.out.println(myNav.paths);
						// save current path
						myNav.save(globalPath);
					} else
					// if next path is path being opened
					if (myNav.showNext().equals(globalPath))
						myNav.getNext(); // remove it from the list

					// enable back button
					back.setEnabled(true);
					updatePath();
					// load directory
					refresh();

				}
			}
		}

	}

	private void mouseClickOnIcon(MouseEvent e) {
		if (selectedIcons.isEmpty())
			selectionAnchor = null;
		if (e.getButton() == MouseEvent.BUTTON1) {
			if (shiftKey && selectionAnchor != null) {
				// System.out.println("Shift Selection");
				JPanel endPanel = (JPanel) e.getSource();
				// clear all other selections
				clearSelections();
				// select all panels between endPanel and selectionAnchor
				Iterator<JPanel> i = icons.keySet().iterator();
				boolean selection = false;
				boolean needReverse = false;
				while (i.hasNext()) {
					JPanel currentPanel = i.next();
					if (currentPanel == endPanel || currentPanel == selectionAnchor) {
						// if endPanel is found first... reverse the selection
						if (!selection && currentPanel == endPanel)
							needReverse = true;
						// toggle selection mode.
						selection = !selection;
						// last panel
						if (!selection) {
							currentPanel.setBorder(borderBlue);
							selectedIcons.put(currentPanel, icons.get(currentPanel));
							break;
						}
					}
					if (selection) {
						currentPanel.setBorder(borderBlue);
						selectedIcons.put(currentPanel, icons.get(currentPanel));
					}
				}
				// reverse.....
				if (needReverse) {
					reverseSelectedIcons();
				}
			} else if (selectedIcons.isEmpty()
					|| (!selectedIcons.isEmpty() && ctrlKey && !selectedIcons.containsKey(e.getSource()))) {
				// simply select
				JPanel selectedIcon = (JPanel) e.getSource();
				selectedIcons.put(selectedIcon, icons.get(selectedIcon));
				selectedIcon.setBorder(borderBlue);
				updateProperties();
				selectionAnchor = selectedIcon;
			} else if (!selectedIcons.isEmpty() && ctrlKey && selectedIcons.containsKey(e.getSource())) {
				// unselect it...
				JPanel selectedIcon = (JPanel) e.getSource();
				selectedIcons.remove(selectedIcon);
				selectedIcon.setBorder(borderWhite);
			} else if (!selectedIcons.isEmpty() && !ctrlKey) {
				// clear all first.
				clearSelections();
				selectionAnchor = null;
				// then select next
				JPanel selectedIcon = (JPanel) e.getSource();
				selectedIcons.put(selectedIcon, icons.get(selectedIcon));
				selectedIcon.setBorder(borderBlue);
				updateProperties();
				selectionAnchor = selectedIcon;
			}
			updateProperties();
			// check double left click
			if (e.getClickCount() == 2) {
				// check folder or file...........
				String filePath = "";
				String fileName = (icons.get(e.getSource())).getText();
				if (globalPath.equals("My Compputer")) {
					filePath = fileName.substring(fileName.lastIndexOf("(") + 1, fileName.lastIndexOf(")"));
				} else
					filePath = globalPath + "\\" + fileName;
				navigateOrRun(filePath);
			}
		}
		if (e.getButton() == MouseEvent.BUTTON3) {
			if (!selectedIcons.containsKey(e.getSource())) {
				// if not
				// clear selection
				clearSelections();
				// select right clicked panel.
				selectionAnchor = (JPanel) e.getSource();
				selectionAnchor.setBorder(borderBlue);
				selectedIcons.put(selectionAnchor, icons.get(selectionAnchor));
			}
			explorerPopup.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	private void reverseSelectedIcons() {
		// reverse LinkedHashMap
		ArrayList<JPanel> ip = new ArrayList<>(selectedIcons.keySet());
		ArrayList<JTextField> jf = new ArrayList<>(selectedIcons.values());
		Collections.reverse(ip);
		Collections.reverse(jf);
		selectedIcons.clear();
		Iterator<JPanel> i1 = ip.iterator();
		Iterator<JTextField> i2 = jf.iterator();
		while (i1.hasNext() && i2.hasNext())
			selectedIcons.put(i1.next(), i2.next());
	}

	private void execute() {
		if (!selectedIcons.isEmpty() && selectionAnchor != null) {
			// execute the first selected file or open first selected directory.
			JTextField jf = icons.get(selectionAnchor);
			String fileName = jf.getText();
			String filePath = "";
			// if my computer, convert Display name to root directory.
			if (globalPath.equals("My Computer")) {
				filePath = fileName.substring(fileName.lastIndexOf("(") + 1, fileName.lastIndexOf(")"));
			} else
				filePath = globalPath + "\\" + fileName;
			navigateOrRun(filePath);

		}
	}

	private void navigateOrRun(String filePath) {
		File file = new File(filePath);
		if (file.isDirectory() || checkRoot(file.toString())) {
			globalPath = filePath;
			if (myNav.showNext() == null) {
				// System.out.println("Condition:Null");
				myNav.save(globalPath);
				next.setEnabled(false);
			} else if (myNav.showNext() != null && (!myNav.showNext().equals(globalPath))) {
				// clip
				// System.out.println("Condition: Not Null and Not Equals");
				next.setEnabled(false);
				myNav.clip();
				myNav.save(globalPath);
			} else if (myNav.showNext() != null && myNav.showNext().equals(globalPath)) {
				// System.out.println("Condition:Equal");
				myNav.getNext();

			}
			back.setEnabled(true);
			updatePath();
			refresh();
		} else {
			// throws exception if file does not have a default application
			// Desktop.getDesktop().open(file);
			/*
			 * use rundll utility to execute files with default application as well as
			 * provide Windows
			 * default selection for files without default application.
			 */
			try {
				Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL" + file.getAbsolutePath());

			} catch (IOException e) {
				JOptionPane.showMessageDialog(window, "Could not execute this file!", "Explorer",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	/*
	 * Select all icons
	 */
	private void selectAll() {
		clearSelections();
		Iterator<JPanel> i = icons.keySet().iterator();
		while (i.hasNext()) {
			JPanel current = i.next();
			current.setBorder(borderBlue);
			selectedIcons.put(current, icons.get(current));
		}
		updateProperties();

	}

	public void clearSelections() {
		Iterator<JPanel> i = selectedIcons.keySet().iterator();
		while (i.hasNext()) {
			JPanel selectedIcon = i.next();
			selectedIcon.setBorder(borderWhite);
		}
		selectedIcons.clear();
	}

	private void clearProperties() {
		name.setText("");
		size.setText("");
		modified.setText(null);
		imageLabel.setIcon(null);
	}

	/* Sets properties of parent when no icon is selected */
	private void setParentProperties() {
		clearProperties();
		if (globalPath.equals("My Computer")) {
			imageLabel.setIcon((new ExpIcons()).getIcon("", "Computer"));
			name.setText("My Computer");
			if (ram != null)
				size.setText("Total System Memory: " + ram);
		} else {
			if (checkRoot(globalPath)) {
				imageLabel.setIcon((new ExpIcons()).getIcon("", "Root"));
				name.setText("System Drive:" + globalPath);
			} else {
				imageLabel.setIcon((new ExpIcons()).getIcon("", "Directory"));
				name.setText((new File(globalPath)).getName());
			}
		}
		/* Set propertied of directory/file(s) selected */
	}

	private void updateProperties() {
		clearProperties();
		if (globalPath.equals("My Computer")) {
			imageLabel.setIcon((new ExpIcons()).getIcon("", "Root"));
			if (selectedIcons.size() == 1) {
				String driveName = icons.get(selectedIcons.keySet().iterator().next()).getText();
				driveName = driveName.substring(driveName.lastIndexOf("(") + 1, driveName.lastIndexOf(")"));
				File drive = new File(driveName + "\\");
				name.setText("System Drive" + driveName);
				size.setText("Total Size: " + convert(drive.getTotalSpace()) + "  Free Space: "
						+ convert(drive.getFreeSpace()));
			} else {
				// multiple drive selection
				Iterator<JPanel> i = selectedIcons.keySet().iterator();
				String driveName = "", drive = "";
				long sizeFree = 0, sizeTotal = 0;
				while (i.hasNext()) {
					JTextField jf = selectedIcons.get(i.next());
					String fileName = jf.getText();
					drive = fileName.substring(fileName.lastIndexOf("(") + 1, fileName.lastIndexOf(")"));
					driveName += drive + "   ";
					File d = new File(drive + "\\");
					sizeFree += d.getFreeSpace();
					sizeTotal += d.getTotalSpace();
				}
				name.setText("System Drives " + driveName);
				size.setText("Combined Drives Size: " + convert(sizeTotal) + "  Combined Drives Free Space: "
						+ convert(sizeFree));

			}
		} else {
			if (selectedIcons.size() == 1) {
				String fileName = icons.get(selectedIcons.keySet().iterator().next()).getText();
				File file = new File(globalPath + "\\" + fileName);
				if (file.isDirectory()) {
					imageLabel.setIcon((new ExpIcons()).getIcon("", "Directory"));
					name.setText(fileName);
					modified.setText("Last Modified: " + new Date(file.lastModified()));
				} else {
					imageLabel.setIcon((new ExpIcons()).getIcon(fileName, "Null"));
					name.setText(fileName);
					modified.setText("Last Modified: " + new Date(file.lastModified()));
					size.setText("File Size: " + convert(file.length()));
				}
			} else {
				imageLabel.setIcon((new ExpIcons()).getIcon("", "OpenFolder"));
				name.setText(selectedIcons.size() + "Items Selected.");
			}
		}
	}

	private String str(double d) {
		return d + "";
	}

	/* Converts between computing Units */
	public String convert(long s) {
		double size = s;
		if (size < 1000)
			return str(size).substring(0, str(size).indexOf(".") + 2) + " B";
		else {
			size = size / 1024;
			if (size < 1000)
				return str(size).substring(0, str(size).indexOf(".") + 2) + "KB";
			else {
				size = size / 1024;
				if (size < 1000)
					return str(size).substring(0, str(size).indexOf(".") + 2) + "MB";
				else {
					size = size / 1024;
					if (size < 1000)
						return str(size).substring(0, str(size).indexOf(".") + 2) + "GB";
					else {
						size = size / 1024;
						return str(size).substring(0, str(size).indexOf(".") + 2) + " TB";
					}
				}

			}
		}

	}

	/* Checks if a path mentioned is of a root drive or not */
	private boolean checkRoot(String fPath) {
		boolean res = false;
		File list[] = File.listRoots();
		for (int i = 0; i < list.length; i++) {

			if (list[i].toString().equals(fPath + "\\")) {
				res = true;
				break;
			}
		}
		return res;
	}

	/* Loads nodes dynamically [ON DEMAND] */
	public void treeWillExpand(TreeExpansionEvent evt) {
		JTree tree = (JTree) evt.getSource();
		TreePath path = evt.getPath();
		if (path.toString().equals("[My Computer]")) {
			prepareTree();
			return;
		}
		// translate tree path to file System structure
		String fPath = translateTreePath(path.toString());
		DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) path.getLastPathComponent();
		try {
			addNodes(selectedNode, fPath);
		} catch (Exception e) {
			System.out.println(e);
			System.out.println("fwefw");
			e.printStackTrace();
		}
	}

	public void treeWillCollapse(TreeExpansionEvent evt) {
	}

	public void refresh() {

		RootPaneContainer r = ((RootPaneContainer) explorePanel.getTopLevelAncestor());
		r.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		r.getGlassPane().setVisible(true);

		// selectedIcon=null
		// clear explorePanel
		explorePanel.removeAll();
		explorePanel.repaint();
		// empty linked hashmap
		icons.clear();
		// clear properties panel
		clearProperties();
		// refresh explorer Panel based on globalPath
		// list drives in case of my computer
		if (globalPath.equals("My Computer")) {
			explorePanel.setLayout(new WrapLayout(FlowLayout.LEFT, 20, 20));
			File[] drives = File.listRoots();
			for (int i = 0; i < drives.length; i++) {
				String fullName = sys.getSystemDisplayName(drives[i]);
				if (fullName.equals(""))
					fullName = "(" + drives[i].toString().substring(0, 2) + ")";
				createIcon(fullName, "Root");
			}
		} else {
			// get files list in the directory or system root drive
			String[] fileNames = new File(globalPath + "\\").list();
			// check null values. Empty Root Drives Return null whereas, empty directories
			// reutrn a zero length array
			if (fileNames == null || fileNames.length == 0) {
				// System.out.println("Putting Empty Directory");
				explorePanel.setLayout(new FlowLayout());
				explorePanel.add(new JLabel("Directory Empty!"));
			} else {
				// set wrap layout
				explorePanel.setLayout(new WrapLayout(FlowLayout.LEFT, 20, 20));
				for (int i = 0; i < fileNames.length; i++) {
					// access one file at a time..
					File newFileOrDirectory = new File(globalPath + "\\" + fileNames[i]);
					if (newFileOrDirectory.isHidden() && hiddenDisable)
						continue;
					if (newFileOrDirectory.isDirectory()) {
						// create directory icon
						createIcon(fileNames[i].toString(), "Directory");
					} else {
						if (newFileOrDirectory.isFile()) {
							/*
							 * create file icon.
							 * Null is sent to ExpIcons.getIcon(String,String) method as a file type.
							 * It automatically gets the type of the file by its extension and
							 * returns suitable icon
							 */
							String name = fileNames[i].toString();
							createIcon(name, "Null");
						}
					}
				}
			}
		}

		// refres the explorepanel
		explorePanel.revalidate();
		// reset icon selections
		selectionAnchor = null;
		selectedIcons.clear();
		// show parent drive or directory info
		setParentProperties();
		explorePanel.requestFocus();

		// set normal cursorr
		r.getGlassPane().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		r.getGlassPane().setVisible(false);

		// Refresh COmplete....
		// Request Garbage Collection...
		System.gc();

	}

	/*
	 * create Icons
	 */
	private void createIcon(String name, String type) {
		/*
		 * ExpIcons
		 * Custom clss which returns Icons for:
		 * Root drives. Directories, Files,archieve etc.
		 */
		ExpIcons e = new ExpIcons();
		JPanel master = new JPanel();
		master.setBackground(Color.WHITE);
		master.setBorder(borderWhite);
		master.setLayout(new BorderLayout());
		master.setToolTipText(name);
		master.addMouseListener(this);
		JPanel icon = new JPanel();
		icon.setBackground(Color.WHITE);
		icon.setLayout(new BorderLayout());
		JLabel iconLabel = new JLabel(e.getIcon(name, type));
		icon.add(iconLabel, BorderLayout.CENTER);
		JTextField tf = new JTextField(name);
		tf.setHorizontalAlignment(SwingConstants.CENTER);
		tf.setEditable(false);
		tf.setColumns(10);
		tf.setCaretPosition(0);
		tf.setBackground(Color.WHITE);
		master.add(icon, BorderLayout.CENTER);
		master.add(tf, BorderLayout.SOUTH);
		explorePanel.add(master);
		icons.put(master, tf);
	}

	private void addNodes(DefaultMutableTreeNode parentNode, String path) {
		// remove components from parent directory
		parentNode.removeAllChildren();
		((DefaultTreeModel) explorerTree.getModel()).nodeStructureChanged(parentNode);
		// parent directory file object....
		File f = new File(path + "\\");
		// get List
		String sub[] = f.list();
		if (sub == null || sub.length == 0) {
			// System.out.pritln("Adding Empty node");
			DefaultMutableTreeNode newNode = new DefaultMutableTreeNode("*EMPTY*");
			parentNode.add(newNode);
		} else {
			for (int i = 0; i < sub.length; i++) {
				File file = new File(path + "\\" + sub[i].trim());
				if (!file.isHidden() || !hiddenDisable) {
					if (file.isFile()) {
						// file
						DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(file.getName());
						// parentNode.add(newNode);//does not notify tree model.. avoid using it...
						// directly add to model
						((DefaultTreeModel) explorerTree.getModel()).insertNodeInto(newNode, parentNode,
								parentNode.getChildCount());

					} else if (file.isDirectory()) {
						// directory
						DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(file.getName());
						DefaultMutableTreeNode subNode = new DefaultMutableTreeNode(file.getName());
						// newNode.add(subNode);//does not Notify tree model.. aboid using it.
						// parentNode.add(newNode);//does not notify tree model... avoid usin it
						// directly add to model
						((DefaultTreeModel) explorerTree.getModel()).insertNodeInto(subNode, newNode,
								newNode.getChildCount());
						((DefaultTreeModel) explorerTree.getModel()).insertNodeInto(newNode, parentNode,
								parentNode.getChildCount());
					}
				}

			}

		}
	}

	private String translateTreePath(String s) {
		// translate path of selected node in the tree to File Ssytem Path
		String fPath = "";

		s = s.replace("[", "");
		s = s.replace("]", "");
		s = s.trim();
		String parts[] = s.split(",");
		if (parts.length == 1)
			return "My Computer";

		fPath = parts[1].trim();
		fPath = fPath.substring(fPath.lastIndexOf("(") + 1, fPath.lastIndexOf(")"));
		for (int i = 2; i < parts.length; i++) {
			fPath = fPath + "\\" + parts[i].trim();
		}
		return fPath;
	}

	public void actionPerformed(ActionEvent e) {
		// navigation
		if (e.getSource() == next)
			next();
		else if (e.getSource() == back)
			back();
		else if (e.getSource() == refresh) {
			updatePath();
			refresh();
		} else {
			// Popup Menu
			if (e.getSource() == open) {
				execute();
			} else {
				if (e.getSource() == newFolder) {
					(new FileOperations()).createFolder(globalPath, window);
					refresh();
				} else {
					if (e.getSource() == delete) {
						delete();
					} else if (e.getSource() == rename) {
						(new FileOperations())
								.renameFile(new File(globalPath + "//" + icons.get(selectionAnchor).getText()), window);
						refresh();
					} else if (e.getSource() == copy) {
						copy();
					} else if (e.getSource() == cut) {
						cut();
					} else if (e.getSource() == paste) {
						mousePaste = true;
						paste();
					} else if (e.getSource() == properties) {
						properties();
					}
				}
			}
		}
	}

	public void back() {
		if (!back.isEnabled())
			return;
		globalPath = myNav.getBack();
		updatePath();
		refresh();
		next.setEnabled(true);
		if (myNav.showBack() == null)
			back.setEnabled(false);
	}

	public void next() {
		if (!next.isEnabled())
			return;
		if (!next.isEnabled())
			return;
		globalPath = myNav.getNext();
		updatePath();
		refresh();
		back.setEnabled(true);
		if (myNav.showNext() == null)
			next.setEnabled(false);

	}

	public void delete() {
		if (globalPath.equals("My Computer"))
			return;
		int ctr = 0;
		File[] files = new File[selectedIcons.size()];
		Iterator<JPanel> i = selectedIcons.keySet().iterator();
		while (i.hasNext())
			files[ctr++] = new File(globalPath + "\\" + icons.get(i.next()).getText());

		// pass 'this' for refreshing the exploerer.
		(new FileOperations()).delete(files, window, this);
	}

	private void copy() {
		if (globalPath.equals("My Computer"))
			return;
		cutReady = false;
		copyReady = true;
		// prepare for copy
		Iterator<JPanel> i = selectedIcons.keySet().iterator();
		int ctr = 0;
		copyFiles = new File[selectedIcons.size()];
		while (i.hasNext())
			copyFiles[ctr++] = new File(globalPath + "\\" + icons.get(i.next()).getText());
	}

	private void cut() {
		if (globalPath.equals("MyComputer"))
			return;
		copyReady = false;
		cutReady = true;
		// prepare for move
		Iterator<JPanel> i = selectedIcons.keySet().iterator();
		int ctr = 0;
		cutFiles = new File[selectedIcons.size()];
		while (i.hasNext()) {
			cutFiles[ctr++] = new File(globalPath + "\\" + icons.get(i.next()).getText());
		}

	}

	private void paste() {
		if (globalPath.equals("My Computer"))
			return;
		if (copyReady) {
			// check source and destination
			boolean sourceRoot = false;
			String sourcePath = copyFiles[0].getParent();
			if (sourcePath.length() == 3)// root drive
				sourceRoot = true;
			if (!mousePaste) {
				if ((sourceRoot && globalPath.equals(sourcePath.substring(0, 2))) || globalPath.equals(sourcePath)) {
					JOptionPane.showMessageDialog(window,
							"Error: Cannot Copy. \n File Source and destination are same!", "Explorer",
							JOptionPane.INFORMATION_MESSAGE);

				} else {
					if (globalPath.equals(copyFiles[0].getPath())) {
						JOptionPane.showMessageDialog(window,
								"Error:Cannot Copy.\n You are trying to copy a folder inside that folder!", "Explorer",
								JOptionPane.INFORMATION_MESSAGE);
					} else {
						// source and destination are different
						// call copyFiles method.
						(new FileOperations()).copyFiles(copyFiles,
								globalPath + "//" + icons.get(selectionAnchor).getText(), window, this);
						mousePaste = false;
					}
					// keep files in copyFiles.

				}
			}
		} else if (cutReady) {
			boolean sourceRoot = false;
			String sourcePath = cutFiles[0].getParent();
			if (sourcePath.length() == 3)// root drive
				sourceRoot = true;

			if (!mousePaste) {
				if ((sourceRoot && globalPath.equals(sourcePath.substring(0, 2))) || globalPath.equals(sourcePath)) {
					JOptionPane.showMessageDialog(window, "Error: Cannot Copy.\n File Source and destination are same!",
							"Explorer", JOptionPane.INFORMATION_MESSAGE);
				} else if (globalPath.equals(cutFiles[0].getPath())) {
					JOptionPane.showMessageDialog(window,
							"Error: Cannot Copy.\n You are trying to copy a folder inside that folder!", "Explorer",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					// source and destinations are different.
					// call CopyFiles method
					(new FileOperations()).moveFiles(cutFiles, globalPath + "//" + icons.get(selectionAnchor).getText(),
							window, this);
					mousePaste = false;
				}
			} else {
				// copy in selected folder'
				(new FileOperations()).moveFiles(cutFiles, globalPath + "//" + icons.get(selectionAnchor).getText(),
						window, this);
				mousePaste = false;
			}
			// Delete files from cutFiles
			cutFiles = null;
			cutReady = false;
		}
	}

	private void popup() {
		JPanel last = null;
		Iterator<JPanel> i = selectedIcons.keySet().iterator();
		while (i.hasNext())
			last = i.next();
		Rectangle lastPanel = last.getBounds();
		explorerPopup.show(last, (int) (lastPanel.width), (int) (lastPanel.height));

	}

	private void properties() {
		if (globalPath.equals("My Computer"))
			return;
		(new FileOperations()).showProperties(new File(globalPath + "//" + icons.get(selectionAnchor).getText()), this);
		refresh();
	}

}
