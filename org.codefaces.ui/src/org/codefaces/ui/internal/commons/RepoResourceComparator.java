package org.codefaces.ui.internal.commons;

import org.codefaces.core.models.RepoFile;
import org.codefaces.core.models.RepoFolder;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

public class RepoResourceComparator extends ViewerComparator {
	@Override
	public int compare(Viewer viewer, Object obj1, Object obj2) {
		if (obj1 instanceof RepoFile && obj2 instanceof RepoFolder) {
			return 1;
		}

		if (obj1 instanceof RepoFolder && obj2 instanceof RepoFile) {
			return -1;
		}

		return super.compare(viewer, obj1, obj2);
	}
}