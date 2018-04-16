package Configuration;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "mediaComponentDescription")
@XmlAccessorType(XmlAccessType.FIELD)
public class MediaComponentDescription {
	@XmlElement(name = "mediaComponentNumber")
	private Integer mediaComponentNumber;

	@XmlElement(name = "mediaSubComponent")
	private List<MediaSubComponent> mediaSubComponent;

	@XmlElement(name = "mediaType")
	private Integer mediaType;

	@XmlElement(name = "maxRequestedUl")
	private Integer maxRequestedUl;

	@XmlElement(name = "maxRequestedDl")
	private Integer maxRequestedDl;

	@XmlElement(name = "flowStatus")
	private Integer flowStatus;

	@XmlElement(name = "codecDataUl")
	private String codecDataUl;

	@XmlElement(name = "codecDataDl")
	private String codecDataDl;

	public Integer getMediaComponentNumber() {
		return mediaComponentNumber;
	}

	public void setMediaComponentNumber(Integer mediaComponentNumber) {
		this.mediaComponentNumber = mediaComponentNumber;
	}

	public List<MediaSubComponent> getMediaSubComponent() {
		return mediaSubComponent;
	}

	public void setMediaSubComponent(List<MediaSubComponent> mediaSubComponent) {
		this.mediaSubComponent = mediaSubComponent;
	}

	public Integer getMediaType() {
		return mediaType;
	}

	public void setMediaType(Integer mediaType) {
		this.mediaType = mediaType;
	}

	public Integer getMaxRequestedUl() {
		return maxRequestedUl;
	}

	public void setMaxRequestedUl(Integer maxRequestedUl) {
		this.maxRequestedUl = maxRequestedUl;
	}

	public Integer getMaxRequestedDl() {
		return maxRequestedDl;
	}

	public void setMaxRequestedRl(Integer maxRequestedDl) {
		this.maxRequestedDl = maxRequestedDl;
	}

	public Integer getFlowStatus() {
		return flowStatus;
	}

	public void setFlowStatus(Integer flowStatus) {
		this.flowStatus = flowStatus;
	}

	public String getCodecDataUl() {
		return codecDataUl;
	}

	public void setCodecDataUl(String codecDataUl) {
		this.codecDataUl = codecDataUl;
	}

	public String getCodecDataDl() {
		return codecDataDl;
	}

	public void setCodecDataDl(String codecDataDl) {
		this.codecDataDl = codecDataDl;
	}
}
