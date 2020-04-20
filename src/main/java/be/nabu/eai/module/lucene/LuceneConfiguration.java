package be.nabu.eai.module.lucene;

import java.net.URI;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import be.nabu.eai.repository.jaxb.ArtifactXMLAdapter;
import be.nabu.libs.services.api.DefinedService;

@XmlRootElement(name = "lucene")
public class LuceneConfiguration {
	private DefinedService list, get;
	private URI path;
	public URI getPath() {
		return path;
	}
	public void setPath(URI path) {
		this.path = path;
	}
	
	@XmlJavaTypeAdapter(value = ArtifactXMLAdapter.class)
	public DefinedService getList() {
		return list;
	}
	public void setList(DefinedService list) {
		this.list = list;
	}
	
	@XmlJavaTypeAdapter(value = ArtifactXMLAdapter.class)
	public DefinedService getGet() {
		return get;
	}
	public void setGet(DefinedService get) {
		this.get = get;
	}

}
