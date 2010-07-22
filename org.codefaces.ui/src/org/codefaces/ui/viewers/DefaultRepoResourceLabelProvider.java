package org.codefaces.ui.viewers;

import org.codefaces.core.models.RepoFile;
import org.codefaces.core.models.RepoFolder;
import org.codefaces.core.models.RepoFolderRoot;
import org.codefaces.core.models.RepoResource;
import org.codefaces.ui.internal.Images;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class DefaultRepoResourceLabelProvider  extends LabelProvider {

	public String getText(Object obj) {
		if (obj instanceof LoadingItem) {
			return ((LoadingItem) obj).getText();
		}

		if (obj instanceof RepoResource) {
			return ((RepoResource) obj).getName();
		}

		return obj.toString();
	}

	public Image getImage(Object obj) {
		if (obj instanceof LoadingItem) {
			return ((LoadingItem) obj).getImage();
		}

		if (obj instanceof RepoFolderRoot) {
			return Images.getImageDescriptorFromRegistry(
					Images.IMG_REPO_FOLDER_ROOT).createImage();
		}

		if (obj instanceof RepoFolder) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJ_FOLDER);
		}

		if (obj instanceof RepoFile) {
			return PlatformUI.getWorkbench().getSharedImages()
					.getImage(ISharedImages.IMG_OBJ_FILE);
		}

		return null;
	}
}
