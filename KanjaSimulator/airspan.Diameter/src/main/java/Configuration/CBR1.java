package Configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class CBR1 {

	@XmlElement(name = "subscriptionIdType")
	private Integer subscriptionIdType;

	@XmlElement(name = "mediaComponentDescription")
	private MediaComponentDescription mediaComponentDescription;

	public CBR1() {
	}

	public Integer getSubscriptionIdType() {
		return subscriptionIdType;
	}

	public void setSubscriptionIdType(Integer subscriptionIdType) {
		this.subscriptionIdType = subscriptionIdType;
	}

	public MediaComponentDescription getMediaComponentDescription() {
		return mediaComponentDescription;
	}

	public void setMediaComponentDescription(MediaComponentDescription mediaComponentDescription) {
		this.mediaComponentDescription = mediaComponentDescription;
	}
}