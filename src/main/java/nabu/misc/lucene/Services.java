/*
* Copyright (C) 2020 Alexander Verbruggen
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with this program. If not, see <https://www.gnu.org/licenses/>.
*/

package nabu.misc.lucene;

import java.io.IOException;
import java.util.List;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.validation.constraints.NotNull;

import org.apache.lucene.queryparser.classic.ParseException;

import be.nabu.eai.module.lucene.LuceneArtifact;
import be.nabu.eai.repository.EAIResourceRepository;
import be.nabu.libs.artifacts.api.Artifact;

@WebService
public class Services {

	@WebResult(name = "results")
	public List<Object> search(@WebParam(name = "luceneId") String id, @NotNull @WebParam(name = "typeId") String type, @WebParam(name = "query") String query, @WebParam(name = "limit") Integer amountOfResults, @WebParam(name = "minimumScore") Double minimum) throws ParseException, IOException {
		if (id != null) {
			Artifact resolve = EAIResourceRepository.getInstance().resolve(id);
			if (resolve instanceof LuceneArtifact) {
				return ((LuceneArtifact) resolve).search(type, query, amountOfResults == null ? 10 : amountOfResults, minimum);
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
