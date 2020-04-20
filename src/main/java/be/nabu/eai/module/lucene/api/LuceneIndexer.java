package be.nabu.eai.module.lucene.api;

import java.util.Date;
import java.util.List;

public interface LuceneIndexer {
	public List<LuceneEntry> list(Date since);
	public List<LuceneEntry> get(List<String> id, String type);
}
