package Configuration;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class HttpConfig {

	private Integer port = null;
	private Integer experimentalResult = null;

	public HttpConfig() {
	}

	public HttpConfig(Integer port) {
		this.port = port;
	}

	@XmlElement
	public Integer getPort() {
		return this.port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}
	
	@XmlElement
	public Integer getExperimentalResult() {
		if(this.experimentalResult == 0){
			return null;
		}
		return this.experimentalResult;
	}

	public void setExperimentalResult(Integer experimentalResult) {
		this.experimentalResult = experimentalResult;
	}
}
