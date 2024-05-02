import java.io.File;
import javax.swing.ImageIcon;

class ExpIcons {
	public ImageIcon getIcon(String fileName, String type) {
		ImageIcon i = null;
		switch (type) {
			case "Root":
				i = new ImageIcon("images//drive.png");
				System.out.println("1");
				break;
			case "Directory":
				i = new ImageIcon("images//folder.png");
				System.out.println("2");
				break;
			case "OpenFolder":
				i = new ImageIcon("images//openFolder.png");
				System.out.println("3");
				break;
			case "Computer":
				i = new ImageIcon("images//computer.png");
				System.out.println("4");
				break;
			case "Null":
				switch (getType(fileName)) {

					case "File":
						i = new ImageIcon("images//file.png");
						break;
					case "Archive":
						i = new ImageIcon("images//archive.png");
						break;
					case "Gif":
						i = new ImageIcon("images//gif.png");
						break;
					case "Jpg":
						i = new ImageIcon("images//jpg.png");
						break;
					case "Png":
						i = new ImageIcon("images//png.png");
						break;
					case "Image":
						i = new ImageIcon("images//image.png");
						break;
					case "Audio":
						i = new ImageIcon("images//audio.png");
						break;
					case "Executable":
						i = new ImageIcon("images//exe.png");
						break;
					case "MSi":
						i = new ImageIcon("images//msi.png");
						break;
					case "Html":
						i = new ImageIcon("images//html.png");
						break;
					case "Java":
						i = new ImageIcon("images//java.png");
						break;
					case "Pdf":
						i = new ImageIcon("images//pdf.png");
						break;
					case "Text":
						i = new ImageIcon("images//txt.png");
						break;
					case "Video":
						i = new ImageIcon("images//video.png");
						break;
					case "Shortcut":
						i = new ImageIcon("images//shortcut-file.png");
						break;
					case "Word":
						i = new ImageIcon("images//word.png");
						break;
					case "Excel":
						i = new ImageIcon("images//excel.png");
						break;
					case "Powerpoint":
						i = new ImageIcon("images//powerpoint.png");
						break;
					case "Publisher":
						i = new ImageIcon("images//pubisher.png");
						break;
					case "Access":
						i = new ImageIcon("images//access.png");
						break;
					case "Outlook":
						i = new ImageIcon("images//outlook.png");
						break;
					case "Photoshop":
						i = new ImageIcon("images//photoshop.png");
						break;
					default:
						System.out.println("5");
						break;
				}
		}
		return i;
		// return(ImageIcon)SysView.getFileSystemView().getSystemIcon(new
		// File(fileName));
	}

	public boolean checkRoot(String fPath) {
		if (fPath.indexOf("(") == -1 || fPath.indexOf(")") == -1)
			return false;
		else {
			fPath = fPath.substring(fPath.indexOf("(") + 1, fPath.indexOf(")"));
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
	}

	private String getType(String name) {
		// switches extensions to get a type of file
		String extension = "";
		try {
			extension = name.substring(name.lastIndexOf("."));
			extension = extension.toLowerCase();
		} catch (Exception e) {
			return "File";
		}
		String type = "";
		switch (extension) {
			// Archives
			case ".zip":
			case ".zipx":
			case ".rar":
			case ".iso":
			case ".gz":
			case ".gzip":
			case ".7z":
			case ".tgz":
			case ".tar":
			case ".bz2":
			case ".z":
			case ".Z":
			case ".ace":
			case ".apk":
			case ".cfs":
			case ".cab":
			case ".arj":
			case ".jar":
				type = "Archive";
				break;
			// Images
			case ".jpg":
			case ".jpeg":
				type = "Jpg";
				break;
			case ".gif":
				type = "Gif";
				break;
			case ".png":
				type = "Png";
				break;
			case ".tiff":
			case ".bmp":
				type = "Image";
				break;

			// Audio
			case ".mp3":
			case ".mp4a":
			case ".wav":
			case ".ogg":
			case ".oga":
			case ".aac":
			case ".wma":
				type = "Audio";
				break;

			// Executable
			case ".exe":
				type = "Executable";
				break;
			case ".msi":
				type = "Msi";
				break;

			// HTML
			case ".html":
			case ".htm":
				type = "Html";
				break;

			// Java
			case ".jnlp":
			case ".java":
				type = "Java";
				break;

			// Pdf
			case ".pdf":
				type = "Pdf";
				break;

			// Text
			case ".txt":
				type = "Text";
				break;

			// video
			case ".3gp":
			case ".avi":
			case ".mp4":
			case ".mkv":
			case ".fla":
			case ".flv":
			case ".rm":
			case ".mpeg":
			case ".mpe":
			case ".mpg":
			case ".wmv":
			case ".swf":
			case ".mov":
			case ".vob":
				type = "Video";
				break;

			// shortcut-file
			case ".lnk":
				type = "Shortcut";
				break;
			// Microsoft Office
			// Word
			case ".rtf":
			case ".doc":
			case ".docx":
				type = "Word";
				break;

			// Excel
			case ".xls":
				type = "Excel";
				break;
			case ".xlsx":
				type = "Excel";
				break;

			// Powerpoint
			case ".ppt":
				type = "Powerpoint";
				break;
			case ".pptx":
				type = "Powerpoint";
				break;

			// Publisher
			case ".pub":
				type = "Publisher";
				break;

			// Access
			case ".accdb":
			case ".mdb":
				type = "Access";
				break;

			// OutLook
			case ".pst":
			case ".vcf":
			case ".ost":
				type = "Outlook";
				break;

			// photoshop
			case ".psd":
				type = "Photoshop";
				break;
			default:
				type = "File";// unknown extension
		}
		return type;
	}

}