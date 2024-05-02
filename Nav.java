import java.util.ArrayList;

class Nav {
	ArrayList<String> paths;
	private int current;

	Nav() {
		paths = new ArrayList<String>();
		current = -1;
	}

	public void save(String s) {
		paths.add(++current, s);
	}

	public String showBack() {
		if (current > 0)
			return paths.get(current - 1).toString();
		return null;
	}

	public String getBack() {
		try {
			if (current > 0)
				return paths.get(--current).toString();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean hasNext() {
		return current < (paths.size() - 1);
	}

	public String showNext() {
		if (hasNext())
			return paths.get(current + 1).toString();
		return null;
	}

	public String getNext() {
		if (hasNext())
			return paths.get(++current).toString();
		return null;
	}

	public void clip() {
		paths = new ArrayList<String>(paths.subList(0, current + 1));
		paths.trimToSize();
	}

	public void seek() {
		for (int i = 0; i < paths.size(); i++)
			System.out.println("Seek Index " + i + ":" + paths.get(i));
	}
}