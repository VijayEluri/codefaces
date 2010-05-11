package org.codefaces.core;

import org.codefaces.core.models.Repo;
import org.codefaces.core.models.RepoResource;
import org.eclipse.core.runtime.IAdapterFactory;

public class RepoResourceAdapterFactory implements IAdapterFactory {
	private static final Class<?>[] ADAPTER_LIST = new Class[] { Repo.class };

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adaptableObject instanceof Repo) {
			return adaptableObject;
		}

		if (adaptableObject instanceof RepoResource
				&& adapterType == Repo.class) {
			return getRepo((RepoResource) adaptableObject);
		}

		return null;
	}

	/**
	 * Recursively getting the parent to find the repo. Repo is the root of the
	 * tree.
	 * 
	 * @param repoResource
	 * @return
	 */
	private Repo getRepo(RepoResource repoResource) {
		RepoResource parentResource = repoResource.getParent();

		if (parentResource instanceof Repo) {
			return (Repo) parentResource;
		}

		if (parentResource != null) {
			return getRepo(parentResource);
		}

		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return ADAPTER_LIST;
	}

}
