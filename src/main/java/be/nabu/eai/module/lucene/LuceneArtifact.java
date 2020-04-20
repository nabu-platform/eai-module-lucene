package be.nabu.eai.module.lucene;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import be.nabu.eai.repository.api.Repository;
import be.nabu.eai.repository.artifacts.jaxb.JAXBArtifact;
import be.nabu.libs.converter.ConverterFactory;
import be.nabu.libs.property.api.Value;
import be.nabu.libs.resources.api.ResourceContainer;
import be.nabu.libs.types.ComplexContentWrapperFactory;
import be.nabu.libs.types.TypeUtils;
import be.nabu.libs.types.api.ComplexContent;
import be.nabu.libs.types.api.Element;
import be.nabu.libs.types.properties.PrimaryKeyProperty;

/**
 * There are two methods to use this artifact:
 * - push: you push every update, every deletion
 * - pull: you tell the artifact to reindex everything periodically, because we don't have data pushed to the lucene artifact, we can not correctly established deleted data
 * 
 * Most systems can tell you when something was last modified, not when something was deleted. This means a reindex will rebuild the index fully.
 * A delta index assumes established data is "correct" and does not need to be revalidated, it will use the last checked timestamp to run a delta sync.
 * 
 * The documents you push to lucene should have a primary key field, we use this to indicate a specific document
 */
public class LuceneArtifact extends JAXBArtifact<LuceneConfiguration> {

	public static class SearchResult {
		private String id;
		private float score;
		public SearchResult() {
			// auto
		}
		public SearchResult(String id, float score) {
			this.id = id;
			this.score = score;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public float getScore() {
			return score;
		}
		public void setScore(float score) {
			this.score = score;
		}
	}
	
	public LuceneArtifact(String id, ResourceContainer<?> directory, Repository repository) {
		super(id, directory, repository, "lucene.xml", LuceneConfiguration.class);
	}

	public void deltaIndex() {
		
	}
	
	// reindex all the documents
	public void reindexAll() {
	}
	
	public File getIndexLocation() {
		if (getConfig().getPath() != null) {
			return new File(getConfig().getPath().getPath());
		}
		else {
			return new File(System.getProperty("java.io.tmpdir"), getId());
		}
	}
	
	public List<SearchResult> search(String defaultField, String q, int amountOfResults) throws ParseException, IOException {
		Query query = new QueryParser(defaultField, new StandardAnalyzer()).parse(q);
		IndexReader reader = getReader();
		IndexSearcher searcher = new IndexSearcher(reader);
	    TopDocs topDocs = searcher.search(query, amountOfResults);
	    List<SearchResult> results = new ArrayList<SearchResult>();
	    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
	        Document document = searcher.doc(scoreDoc.doc);
	        IndexableField field = document.getField("$id");
			results.add(new SearchResult(field.stringValue(), scoreDoc.score));
	    }
	    return results;
	}
	
	// push new indexes, we assume 1 dimensional complex types with at least one primary key field
	@SuppressWarnings("unchecked")
	public void index(List<Object> entries) throws IOException {
		synchronized (this) {
			IndexWriter writer = getWriter();
			try {
				for (Object entry : entries) {
					if (entry == null) {
						continue;
					}
					if (!(entry instanceof ComplexContent)) {
						entry = ComplexContentWrapperFactory.getInstance().getWrapper().wrap(entry);
					}
					// wrapping failed?
					if (entry == null) {
						continue;
					}
					String keyValue = null;
					Document document = new Document();
					for (Element<?> child : TypeUtils.getAllChildren(((ComplexContent) entry).getType())) {
						Object value = ((ComplexContent) entry).get(child.getName());
						if (value != null && !(value instanceof String)) {
							value = ConverterFactory.getInstance().getConverter().convert(value, String.class);
						}
						if (value != null) {
							Value<Boolean> property = child.getProperty(PrimaryKeyProperty.getInstance());
							boolean isPrimary = property != null && property.getValue() != null && property.getValue();
							// only store it if it is a primary key, the rest only serves as index
							if (isPrimary) {
								document.add(new StringField("$id", (String) value, Store.YES));
								keyValue = (String) value;
							}
							else {
								document.add(new TextField(child.getName(), (String) value, Store.NO));
							}
						}
					}
					// we only store documents with a primary key, otherwise we have little use for them afterwards
					if (keyValue != null) {
						writer.updateDocument(new Term("$id", keyValue), document);
					}
				}
			}
			finally {
				writer.close();
			}
		}
	}

	private IndexWriter getWriter() throws IOException {
		Directory directory = getIndexDirectory();
		StandardAnalyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		return new IndexWriter(directory, indexWriterConfig);
	}

	private Directory getIndexDirectory() throws IOException {
		File indexLocation = getIndexLocation();
		if (!indexLocation.exists()) {
			indexLocation.mkdirs();
		}
		Directory directory = FSDirectory.open(indexLocation.toPath());
		return directory;
	}
	
	private IndexReader getReader() throws IOException {
		Directory indexDirectory = getIndexDirectory();
		return DirectoryReader.open(indexDirectory);
	}
	
	public void delete(List<String> ids) throws IOException {
		if (ids != null && !ids.isEmpty()) {
			synchronized(this) {
				IndexWriter writer = getWriter();
				try {
					List<Term> terms = new ArrayList<Term>();
					for (String id : ids) {
						if (id != null) {
							terms.add(new Term("$id", id));
						}
					}
					if (!terms.isEmpty()) {
						writer.deleteDocuments(terms.toArray(new Term[terms.size()]));
					}
				}
				finally {
					writer.close();
				}
			}
		}
	}
	
	public void deleteAll() throws IOException {
		synchronized(this) {
			IndexWriter writer = getWriter();
			try {
				writer.deleteAll();
			}
			finally {
				writer.close();
			}
		}
	}
}
