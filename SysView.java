import javax.swing.filechooser.FileSystemView;
import java.io.File;

class SysView extends FileSystemView {
	public File createNewFolder(File containingDir) {
		return null;
	}
}