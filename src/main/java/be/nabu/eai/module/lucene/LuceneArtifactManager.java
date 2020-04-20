package be.nabu.eai.module.lucene;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.managers.base.JAXBArtifactManager;
import be.nabu.libs.resources.api.ResourceContainer;

public class LuceneArtifactManager extends JAXBArtifactManager<LuceneConfiguration, LuceneArtifact> {

	public LuceneArtifactManager() {
		super(LuceneArtifact.class);
	}

	@Override
	protected LuceneArtifact newInstance(String id, ResourceContainer<?> container, Repository repository) {
		return new LuceneArtifact(id, container, repository);
	}

}
