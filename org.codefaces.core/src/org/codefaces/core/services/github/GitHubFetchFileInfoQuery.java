package org.codefaces.core.services.github;

import org.codefaces.core.models.Repo;
import org.codefaces.core.models.RepoFile;
import org.codefaces.core.models.RepoFileInfo;
import org.codefaces.core.models.RepoFolder;
import org.codefaces.core.services.SCMQuery;
import org.codefaces.core.services.SCMQueryParameter;
import org.codefaces.core.services.github.dtos.GitHubFileDataDto;
import org.codefaces.core.services.github.dtos.GitHubFileDto;
import org.codefaces.httpclient.SCMHttpClient;
import org.codefaces.httpclient.SCMResponseException;
import org.eclipse.core.runtime.Assert;

public class GitHubFetchFileInfoQuery implements SCMQuery<RepoFileInfo> {
	private static final String GET_GITHUB_FILE = "http://github.com/api/v2/json/blob/show";

	@Override
	public RepoFileInfo execute(SCMHttpClient client,
			SCMQueryParameter parameter) {
		Object file = parameter.getParameter(PARA_REPO_FILE);
		Assert.isTrue(file instanceof RepoFile);

		try {
			RepoFile repoFile = (RepoFile)file;
			String fileName = repoFile.getName();
			RepoFolder folder = (RepoFolder)repoFile.getParent();
			Repo repo = repoFile.getRoot().getRepo();

			String fileUrl = createFetchFileInfoUrl(repo, folder, fileName);

			GitHubFileDataDto fileDataDto = fetchFileDataDto(client, fileUrl);

			return new RepoFileInfo(fileDataDto.getData(),
					fileDataDto.getMime_type(), fileDataDto.getMode(),
					fileDataDto.getSize());
		} catch (Exception e) {
			throw new SCMResponseException(e.getMessage(), e);
		}
	}

	protected String createFetchFileInfoUrl(Repo repo, RepoFolder folder,
			String fileName) {
		return GitHubUtil.makeURI(GET_GITHUB_FILE, repo.getCredential()
				.getOwner(), repo.getName(), folder.getId(), fileName);
	}

	private GitHubFileDataDto fetchFileDataDto(SCMHttpClient client,
			String getGitHubFileMetadataUrl) {
		GitHubFileDto gitHubFileDto = GitHubUtil.fromJson(
				client.getResponseBody(getGitHubFileMetadataUrl),
				GitHubFileDto.class);
		
		return gitHubFileDto.getBlob();
	}

}
