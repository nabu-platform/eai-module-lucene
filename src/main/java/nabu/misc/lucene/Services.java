package nabu.misc.lucene;

import java.io.IOException;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

import org.apache.lucene.queryparser.classic.ParseException;

import be.nabu.eai.module.lucene.LuceneArtifact;
import be.nabu.eai.module.lucene.LuceneArtifact.SearchResult;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.artifacts.api.Artifact;

@WebService
public class Services {

	@WebResult(name = "results")
	public List<SearchResult> search(@WebParam(name = "luceneId") String id, @WebParam(name = "defaultField") String defaultField, @WebParam(name = "query") String query, @WebParam(name = "limit") Integer amountOfResults) throws ParseException, IOException {
		if (id != null) {
			Artifact resolve = EAIResourceRepository.getInstance().resolve(id);
			if (resolve instanceof LuceneArtifact) {
				return ((LuceneArtifact) resolve).search(defaultField, query, amountOfResults == null ? 10 : amountOfResults);
			}
		}
		return null;
	}
	
	public void index(@WebParam(name = "luceneId") String id, @WebParam(name = "entries") List<Object> entries) throws IOException {
		if (id != null) {
			Artifact resolve = EAIResourceRepository.getInstance().resolve(id);
			if (resolve instanceof LuceneArtifact) {
				((LuceneArtifact) resolve).index(entries);
			}
		}
	}
	
	public void deleteAll(@WebParam(name = "luceneId") String id) throws IOException {
		if (id != null) {
			Artifact resolve = EAIResourceRepository.getInstance().resolve(id);
			if (resolve instanceof LuceneArtifact) {
				((LuceneArtifact) resolve).deleteAll();
			}
		}
	}
}
