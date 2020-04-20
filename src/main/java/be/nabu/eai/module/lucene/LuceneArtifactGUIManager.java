package be.nabu.eai.module.lucene;

import java.io.IOException;
import java.util.List;

import be.nabu.eai.developer.MainController;
import be.nabu.eai.developer.managers.base.BaseJAXBGUIManager;
import be.nabu.eai.repository.resources.RepositoryEntry;
import be.nabu.libs.property.api.Property;
import be.nabu.libs.property.api.Value;

public class LuceneArtifactGUIManager extends BaseJAXBGUIManager<LuceneConfiguration, LuceneArtifact> {

	public LuceneArtifactGUIManager() {
		super("Lucene Index", LuceneArtifact.class, new LuceneArtifactManager(), LuceneConfiguration.class);
	}

	@Override
	protected List<Property<?>> getCreateProperties() {
		return null;
	}

	@Override
	protected LuceneArtifact newInstance(MainController controller, RepositoryEntry entry, Value<?>... values) throws IOException {
		return new LuceneArtifact(entry.getId(), entry.getContainer(), entry.getRepository());
	}

	@Override
	public String getCategory() {
		return "Miscellaneous";
	}
}
