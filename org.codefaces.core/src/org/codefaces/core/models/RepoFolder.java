package org.codefaces.core.models;

public class RepoFolder extends RepoResource {
	public RepoFolder(RepoFolderRoot root, RepoResource parent, String id,
			String name) {
		this(root, parent, id, name, RepoResourceType.FOLDER);
	}

	protected RepoFolder(RepoFolderRoot root, RepoResource parent, String id,
			String name, RepoResourceType type) {
		super(root, parent, id, name, type);
	}
	
	//This method is intended to use only in testing
	protected RepoFolder(RepoFolderRoot root, RepoResource parent, String id,
			String name, RepoResourceType type, RepoFolderInfo info){
		super(root, parent, id, name, type, info);
	}

	@Override
	protected RepoFolderInfo getInfo() {
		return (RepoFolderInfo) super.getInfo();
	}
}
