import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JTextField;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.BorderFactory;
import java.awt.Font;
import java.awt.Color;
import java.awt.Point;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedHashMap;
import java.util.Date;
import java.util.concurrent.CancellationException;

/* This class handles all the file transfers and rename jobs*/
class FileOperations {
	/* members used in deletion process. */
	int deletionCounter;
	int copyCounter;
	int totalFilesToCopy;
	int totalFilesToDelete;
	JLabel currentFile;
	JLabel total;

	public void copyFiles(File[] files, String destination, JFrame parent, Explorer e) {
		JDialog dialog = new JDialog(parent, "Copying Files......");
		JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		total = new JLabel();
		int totalFiles = countFiles(files);
		total.setText("Total Files and Folders: " + totalFiles);
		dialog.add(total, BorderLayout.NORTH);
		currentFile = new JLabel();
		dialog.add(currentFile, BorderLayout.CENTER);
		dialog.add(progressBar, BorderLayout.SOUTH);
		int w = 460, h = 100;
		dialog.setSize(w, h);
		Dimension parentSize = parent.getSize();
		Point p = parent.getLocation();
		dialog.setLocation(p.x + parentSize.width / 2 - (w / 2), p.y + parentSize.height / 2 - (h / 2));
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setResizable(false);
		dialog.setVisible(true);
		SwingWorker worker = new SwingWorker<Void, Void>() {
			public Void doInBackground() {
				copyCounter = 0;
				totalFilesToCopy = totalFiles;
				copy(files, destination, parent, progressBar, this);
				return null;
			}

			public void done() {
				try {
					get();
				} catch (CancellationException ex) {

				} catch (Exception e) {
					e.printStackTrace();
				}
				dialog.dispose();
				e.refresh();
			}
		};
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// System.out.println("Copy Cancel Request Sent");
				worker.cancel(true);
			}
		});
		worker.execute();
	}

	private void copy(File[] files, String destination, JFrame parent, JProgressBar progressBar, SwingWorker worker) {
		for (int i = 0; i < files.length; i++) {
			if (worker.isCancelled())
				return;
			copy(files[i], destination, parent, progressBar, worker);
		}
	}

	private void copy(File file, String destination, JFrame parent, JProgressBar progressBar, SwingWorker worker) {
		if (worker.isCancelled())
			return;
		int percentage = copyCounter * 100 / totalFilesToCopy;
		currentFile.setText("Processing:" + file.getName());
		total.setText("File:" + copyCounter + "/" + totalFilesToCopy);
		progressBar.setValue(percentage);
		try {
			if (file.isFile()) {
				FileInputStream fs = new FileInputStream(file);
				byte[] b = new byte[fs.available()];
				fs.read(b);// read whole file
				File dest = new File(destination + "//" + file.getName());
				FileOutputStream fw = new FileOutputStream(dest);
				fw.write(b);
				fs.close();
				fw.close();
				copyCounter++;
			} else if (file.isDirectory()) {
				File dest = new File(destination + "//" + file.getName());
				dest.mkdir(); // create current directory
				copyCounter++;
				String[] list = file.list();
				for (int i = 0; i < list.length; i++) {
					if (worker.isCancelled())
						return;
					copy((new File(file.getPath() + "//" + list[i])), dest.getPath(), parent, progressBar, worker);
				}
			}

		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, "Error:" + e.getMessage(), "Explorer",
					JOptionPane.INFORMATION_MESSAGE);

		}

	}

	public void moveFiles(File[] files, String destination, JFrame parent, Explorer e) {
		JDialog dialog = new JDialog(parent, "Moving Files......");
		JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setStringPainted(true);
		total = new JLabel();
		int totalFiles = countFiles(files);
		total.setText("Total Fiels and Folders:" + totalFiles);
		dialog.add(total, BorderLayout.NORTH);
		currentFile = new JLabel();
		dialog.add(currentFile, BorderLayout.CENTER);
		dialog.add(progressBar, BorderLayout.SOUTH);
		int w = 460, h = 100;
		dialog.setSize(w, h);
		Dimension parentSize = parent.getSize();
		Point p = parent.getLocation();
		dialog.setLocation(p.x + parentSize.width / 2 - (w / 2), p.y + parentSize.height / 2 - (h / 2));
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setResizable(false);
		dialog.setVisible(true);
		SwingWorker worker = new SwingWorker<Void, Void>() {
			public Void doInBackground() {
				copyCounter = 0;
				totalFilesToCopy = totalFiles;
				move(files, destination, parent, progressBar, this);
				return null;
			}

			public void done() {
				try {
					get();
				} catch (CancellationException ex) {
				} catch (Exception e) {
					e.printStackTrace();
				}
				dialog.dispose();
				e.refresh();
			}
		};
		dialog.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				// System.out.println("Copy Cancel Request Sent");
				worker.cancel(true);
			}
		});
		worker.execute();

	}

	public void move(File[] files, String destination, JFrame parent, JProgressBar progressBar, SwingWorker worker) {
		if (files != null)
			for (int i = 0; i < files.length; i++) {
				if (worker.isCancelled())
					return;
				move(files[i], destination, parent, progressBar, worker);
			}
	}

	private void move(File file, String destination, JFrame parent, JProgressBar progressBar, SwingWorker worker) {
		if (worker.isCancelled())
			return;
		int percentage = copyCounter * 100 / totalFilesToCopy;
		currentFile.setText("Processing:" + file.getName());
		total.setText("File:" + copyCounter + "/" + totalFilesToCopy);
		progressBar.setValue(percentage);
		try {
			if (file.isFile()) {
				FileInputStream fs = new FileInputStream(file);
				byte[] b = new byte[fs.available()];
				fs.read(b); // read whole file
				File dest = new File(destination + "//" + file.getName());
				FileOutputStream fw = new FileOutputStream(dest);
				fw.write(b);
				fs.close();
				fw.close();
				// delete file'
				file.delete();
				copyCounter++;
			} else if (file.isDirectory()) {
				File dest = new File(destination + "//" + file.getName());
				dest.mkdir(); // create current directory
				copyCounter++;
				String[] list = file.list();
				for (int i = 0; i < list.length; i++) {
					if (worker.isCancelled())
						return;
					move((new File(file.getPath() + "//" + list[i])), dest.getPath(), parent, progressBar, worker);
				}
				file.delete(); // delete this directory
			}
		} catch (Exception e) {
			JOptionPane.showMessageDialog(parent, "Error:" + e.getMessage(), "Explorer",
					JOptionPane.INFORMATION_MESSAGE);

		}
	}

	public void delete(File[] files, JFrame parent, Explorer e) {
		int reply = JOptionPane.showConfirmDialog(parent,
				"Selected file(s) will be permanently deleted. Do you want to proceed?", "Confirm Deletion",
				JOptionPane.YES_NO_OPTION);
		if (reply == JOptionPane.YES_OPTION) {
			// create Deletion JDialog
			JDialog dialog = new JDialog(parent, "Deleting Files...");
			// start and end values of progress bar
			JProgressBar progressBar = new JProgressBar(0, 100);
			// set initial to 0
			progressBar.setValue(0);
			// we will show % complete in progress bar...
			progressBar.setStringPainted(true);
			total = new JLabel();
			int totalFiles = countFiles(files);
			total.setText("Total Files and Folders:" + totalFiles);
			dialog.add(total, BorderLayout.NORTH);
			currentFile = new JLabel();
			dialog.add(currentFile, BorderLayout.CENTER);
			dialog.add(progressBar, BorderLayout.SOUTH);
			int w = 460, h = 100;
			dialog.setSize(w, h);
			Dimension parentSize = parent.getSize();
			Point p = parent.getLocation();
			dialog.setLocation(p.x + parentSize.width / -(w / 2), p.y + parentSize.height / 2 - (h / 2));
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setResizable(false);
			dialog.setVisible(true);

			// start background worker
			SwingWorker worker = new SwingWorker<Void, Void>() {
				public Void doInBackground() {
					deletionCounter = 0;
					totalFilesToDelete = totalFiles;
					delete(files, progressBar, this);
					return null;
				}

				public void done() {
					try {
						get();
					} catch (CancellationException e) {
					} catch (Exception e) {
						e.printStackTrace();
					}
					dialog.dispose();
					e.refresh();
				}
			};
			dialog.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					// System.out.println("Deletion Cancel Request Sent");
					worker.cancel(true);
				}
			});
			worker.execute();

		}
	}

	public void delete(File[] files, JProgressBar progressBar, SwingWorker worker) {
		for (int i = 0; i < files.length; i++) {
			if (worker.isCancelled())
				return;
			delete(files[i], progressBar, worker);
		}
	}

	public void delete(File file, JProgressBar progressBar, SwingWorker worker) {
		if (worker.isCancelled())
			return;
		int percentage = deletionCounter * 100 / totalFilesToDelete;
		if (file.isFile()) {
			currentFile.setText("Processing:" + file.getName());
			file.delete();
			deletionCounter++;
			total.setText("File:" + deletionCounter + "/" + totalFilesToDelete);
			progressBar.setValue(deletionCounter * 100 / totalFilesToDelete);
		} else if (file.isDirectory()) {
			String[] list = file.list();
			for (int i = 0; i < list.length; i++) {
				if (worker.isCancelled())
					return;
				delete(new File(file.getPath() + "\\" + list[i]), progressBar, worker);
			}

			currentFile.setText("Processing:" + file.getName());
			file.delete();
			deletionCounter++;
			total.setText("File:" + deletionCounter + "/" + totalFilesToDelete);
			progressBar.setValue(deletionCounter * 100 / totalFilesToDelete);
		}
	}

	private int countFiles(File[] files) {
		int count = 0;
		if (files != null)
			for (int i = 0; i < files.length; i++)
				count += countFiles(files[i]);
		return count;
	}

	private int countFiles(File file) {
		int count = 0;
		if (file.isDirectory()) {
			String[] list = file.list();
			for (int i = 0; i < list.length; i++)
				count += countFiles(new File(file.getPath() + "\\" + list[i]));
			// add one for current directory
			count++;
		}
		return count;
	}

	public void renameFile(File file, JFrame parent) {
		String type;
		type = (file.isDirectory()) ? "Folder" : "File";
		JDialog dialog = new JDialog(parent, "Rename" + type + " " + file.getName(), true);
		dialog.setLayout(new FlowLayout(FlowLayout.CENTER));
		dialog.add(new JLabel("Enter New Name: "));
		String fileName = file.getName();
		JTextField jf = new JTextField(fileName);
		jf.setColumns(20);
		jf.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					// create
					if (rename(file, jf.getText(), parent))
						dialog.dispose();
				}
			}

			public void keyTyped(KeyEvent e) {
				// windows reserved characters
				// characters not permitted in file/folder names
				switch (e.getKeyChar()) {
					case '/':
					case '\\':
					case '*':
					case '?':
					case '<':
					case '>':
					case '|':
					case '\"':
					case ':':
						e.consume();
				}
			}
		});
		dialog.add(jf);
		JButton jb = new JButton("Rename");
		jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// create
				if (rename(file, jf.getText(), parent))
					dialog.dispose();
			}
		});
		dialog.add(jb);
		int w = 460, h = 75;
		dialog.setSize(w, h);
		Dimension parentSize = parent.getSize();
		Point p = parent.getLocation();
		dialog.setLocation(p.x + parentSize.width / 2 - (w / 2), p.y + parentSize.height / 2 - (h / 2));
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setResizable(false);
		dialog.setVisible(true);
	}

	public boolean rename(File file, String newName, JFrame parent) {
		File newFile = new File(file.getParent() + "//" + newName);
		if (newName == null || newName.isEmpty() || newName.replace(" ", "").isEmpty()) {
			JOptionPane.showMessageDialog(parent, "File name cannot be empty!", "Explorer",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			if (newFile.exists()) {
				JOptionPane.showMessageDialog(parent, "A FIle with this name already exists.", "Explorer",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				file.renameTo(newFile);
				return true;
			}
		}
		return false;
	}

	public void createFolder(String globalPath, JFrame parent) {
		JDialog dialog = new JDialog(parent, "Create New Folder", true);
		dialog.setLayout(new FlowLayout(FlowLayout.CENTER));
		dialog.add(new JLabel("Enter Name:"));
		JTextField jf = new JTextField("New Folder");
		jf.selectAll();
		jf.setColumns(25);

		jf.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					// create
					if (create(jf.getText(), globalPath, parent))
						dialog.dispose();
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					dialog.dispose();
			}

			public void keyTyped(KeyEvent e) {
				// window reserved characters
				// characters not permitted in folder names
				switch (e.getKeyChar()) {
					case '/':
					case '\\':
					case '*':
					case '?':
					case '<':
					case '>':
					case '|':
					case '\"':
					case ':':
						e.consume();
				}
			}

		});
		dialog.add(jf);
		JButton jb = new JButton("Create");
		jb.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// create
				if (create(jf.getText(), globalPath, parent))
					dialog.dispose();
			}
		});
		dialog.add(jb);
		int w = 460, h = 70;
		dialog.setSize(w, h);
		Dimension parentSize = parent.getSize();
		Point p = parent.getLocation();
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setResizable(false);
		dialog.setVisible(true);
		jf.requestFocus();
	}

	private boolean create(String name, String globalPath, JFrame parent) {
		File file = new File(globalPath + "\\" + name.trim());
		if (name == null || name.isEmpty() || name.replace(",", "").isEmpty()) {
			JOptionPane.showMessageDialog(parent, "Folder name cannot be empty!", "Explorer",
					JOptionPane.INFORMATION_MESSAGE);
		} else {
			if (file.exists()) {
				JOptionPane.showMessageDialog(parent, "This Folder already exist!", "Explorer",
						JOptionPane.INFORMATION_MESSAGE);
			} else {
				file.mkdir();
				return true;
			}
		}
		return false;
	}

	int Fctr;
	int Dctr;
	long s;

	public void showProperties(File file, Explorer e) {
		JDialog dialog = new JDialog(e.window, "Properties:" + file.getName());
		JPanel labels = new JPanel();
		labels.setBackground(Color.WHITE);
		JLabel name = new JLabel("Name: " + file.getName());
		JLabel parent = new JLabel("Location: " + file.getParent());
		JLabel size = new JLabel("Size: ");
		JLabel modified = new JLabel("Last Modified: " + new Date(file.lastModified()));
		JLabel contains = new JLabel("Contains: ");

		Font f = new Font("Arial", Font.PLAIN, 14);
		name.setFont(f);
		parent.setFont(f);
		size.setFont(f);
		modified.setFont(f);
		contains.setFont(f);

		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		name.setBorder(loweredetched);
		parent.setBorder(loweredetched);
		size.setBorder(loweredetched);
		modified.setBorder(loweredetched);
		contains.setBorder(loweredetched);
		if (file.isDirectory())
			labels.setLayout(new GridLayout(5, 1, 10, 10));
		else
			labels.setLayout(new GridLayout(4, 1, 10, 10));

		labels.add(name);
		labels.add(parent);
		labels.add(modified);
		labels.add(size);
		if (file.isDirectory())
			labels.add(contains);

		dialog.add(labels);
		dialog.setResizable(false);
		int w = 500, h = 200;
		dialog.setSize(w, h);
		Dimension parentSize = e.window.getSize();
		Point p = parent.getLocation();
		dialog.setLocation(p.x + parentSize.width / 2 - (w / 2), p.y + parentSize.height / 2 - (h / 2));
		dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		dialog.setVisible(true);
		if (file.isDirectory()) {
			SwingWorker stats = new SwingWorker<Void, Void>() {
				public Void doInBackground() {
					Fctr = 0;
					Dctr = 0;
					s = 0;
					contains.setText("Contains: 0(Files) and 0(Folders)");
					countStats(file, contains, size, this, e);
					contains.setText("contains: " + Fctr + "(Files) and " + Dctr + "(Folders)");
					size.setText("Size: " + e.convert(s));
					return null;
				}

				public void done() {
					try {
						get();
					} catch (CancellationException ex) {
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			};
			stats.execute();
			dialog.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					stats.cancel(true);
				}
			});
		} else {
			size.setText("Size: " + e.convert(file.length()));
		}
	}

	private void countStats(File file, JLabel contains, JLabel size, SwingWorker w, Explorer e) {
		if (w.isCancelled())
			return;
		String list[] = file.list();
		if (list != null)
			for (int i = 0; i < list.length; i++) {
				File inner = new File(file.getPath() + "//" + list[i]);
				if (inner.isFile()) {
					Fctr++;
					s += inner.length();
				} else {
					countStats(inner, contains, size, w, e);
					Dctr++;
				}
				contains.setText("Contains:" + Fctr + "(Files) and " + Dctr + "(Folders) and Counting....");
				size.setText("Size: " + e.convert(s) + " and counting....");
			}
	}
}