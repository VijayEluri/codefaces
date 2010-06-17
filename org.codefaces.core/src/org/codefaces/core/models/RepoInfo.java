package org.codefaces.core.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.codefaces.core.CodeFacesCoreActivator;
import org.codefaces.httpclient.RepoResponseException;

public class RepoInfo extends RepoResourceInfo {
	private Collection<RepoBranch> branches;

	protected RepoInfo(Repo context) {
		super(context);
	}

	@Override
	public Repo getContext() {
		return (Repo) super.getContext();
	}

	public Collection<RepoBranch> getBranches() {
		if (branches == null) {
			branches = fetchBranches();
		}

		return branches;
	}

	@Override
	public boolean hasChildren() {
		return true;
	}

	private Collection<RepoBranch> fetchBranches() {
		return CodeFacesCoreActivator.getDefault().getRepoService()
				.fetchBranches(getContext());
	}

	@Override
	public Collection<RepoResource> getChildren() {
		List<RepoResource> children = new ArrayList<RepoResource>();
		try {
			for (RepoBranch branch : getBranches()) {
				children.add(branch);
			}
		} catch (RepoResponseException e) {
			e.printStackTrace();
		}

		return children;
	}
}
