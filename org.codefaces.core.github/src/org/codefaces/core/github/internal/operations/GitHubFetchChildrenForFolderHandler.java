package org.codefaces.core.github.internal.operations;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.codefaces.core.connectors.SCMConnector;
import org.codefaces.core.connectors.SCMResponseException;
import org.codefaces.core.github.internal.connectors.GitHubConnector;
import org.codefaces.core.github.internal.operations.dtos.GitHubResourceDTO;
import org.codefaces.core.github.internal.operations.dtos.GitHubResourcesDTO;
import org.codefaces.core.models.Repo;
import org.codefaces.core.models.RepoFile;
import org.codefaces.core.models.RepoFolder;
import org.codefaces.core.models.RepoFolderRoot;
import org.codefaces.core.models.RepoResource;
import org.codefaces.core.operations.SCMOperationHandler;
import org.codefaces.core.operations.SCMOperationParameters;
import org.eclipse.core.runtime.Assert;

public class GitHubFetchChildrenForFolderHandler implements SCMOperationHandler {
	public static final String ID = "org.codefaces.core.operations.SCMOperation.github.fetchChildrenForFolder";

	private static final String SHOW_GITHUB_CHILDREN = "http://github.com/api/v2/json/tree/show";

	private static final String GITHUB_TYPE_BLOB = "blob";

	private static final String GITHUB_TYPE_TREE = "tree";

	@Override
	public Collection<RepoResource> execute(SCMConnector connector,
			SCMOperationParameters parameter) {
		Object folderPara = parameter.getParameter(PARA_REPO_FOLDER);
		Assert.isTrue(folderPara instanceof RepoFolder);
		RepoFolder folder = (RepoFolder) folderPara;
		return fetchChildrenForFolder(connector, folder);
	}

	protected Collection<RepoResource> fetchChildrenForFolder(
			SCMConnector connector, RepoFolder folder) {
		RepoFolderRoot root = folder.getRoot();
		Repo repo = root.getRepo();

		Set<RepoResource> children = new HashSet<RepoResource>();
		String url = createFetchChildrenUrl(repo, folder);
		GitHubResourcesDTO resourceDto = fetchChildrenDto(
				(GitHubConnector) connector, url);
		for (GitHubResourceDTO rscDto : resourceDto.getResources()) {
			String type = rscDto.getType();
			RepoResource child = createRepoResourceFromType(type, root, folder,
					rscDto.getSha(), rscDto.getName());
			if (child == null) {
				throw new SCMResponseException("Unknown github resource type: "
						+ type);
			}

			children.add(child);
		}

		return children;
	}

	protected GitHubResourcesDTO fetchChildrenDto(GitHubConnector connector,
			String url) {
		String respBody = connector.getResponseBody(url);
		return GitHubOperationUtil.fromJson(respBody, GitHubResourcesDTO.class);
	}

	protected String createFetchChildrenUrl(Repo repo, RepoFolder folder) {
		return GitHubOperationUtil.makeURI(SHOW_GITHUB_CHILDREN, repo
				.getCredential().getOwner(), repo.getName(), folder.getId());
	}

	private RepoResource createRepoResourceFromType(String type,
			RepoFolderRoot root, RepoResource parent, String id, String name) {
		if (StringUtils.equals(GITHUB_TYPE_BLOB, type)) {
			return new RepoFile(root, parent, id, name);
		}

		if (StringUtils.equals(GITHUB_TYPE_TREE, type)) {
			return new RepoFolder(root, parent, id, name);
		}

		return null;
	}

}