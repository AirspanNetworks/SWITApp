package Configuration;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType (XmlAccessType.FIELD)
public class MediaSubComponent {
	@XmlElement(name="flowNumber")
	private Integer flowNumber;
	
	@XmlElement(name="flowDescriptionOut")
	private String flowDescriptionOut;
	
	@XmlElement(name="flowDescriptionIn")
	private String flowDescriptionIn;
	
	@XmlElement(name="flowUsag")
	private Integer flowUsag;

	public Integer getFlowNumber() {
		return flowNumber;
	}

	public void setFlowNumber(Integer flowNumber) {
		this.flowNumber = flowNumber;
	}

	public String getFlowDescriptionOut() {
		return flowDescriptionOut;
	}

	public void setFlowDescriptionOut(String flowDescriptionOut) {
		this.flowDescriptionOut = flowDescriptionOut;
	}

	public String getFlowDescriptionIn() {
		return flowDescriptionIn;
	}

	public void setFlowDescriptionIn(String flowDescriptionIn) {
		this.flowDescriptionIn = flowDescriptionIn;
	}

	public Integer getFlowUsag() {
		return flowUsag;
	}

	public void setFlowUsag(Integer flowUsag) {
		this.flowUsag = flowUsag;
	}
}
