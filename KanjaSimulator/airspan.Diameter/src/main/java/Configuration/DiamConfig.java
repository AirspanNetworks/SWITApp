package Configuration;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessType;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DiamConfig {

	@XmlElement(name = "CBR1")
	private CBR1 CBR1;

	@XmlElement(name = "CBR2")
	private CBR2 CBR2;

	@XmlElement(name = "MBR")
	private MBR MBR;

	public DiamConfig() {
	}

	public CBR1 getCBR1() {
		return CBR1;
	}

	public void setCBR1(CBR1 cBR1) {
		CBR1 = cBR1;
	}

	public CBR2 getCBR2() {
		return CBR2;
	}

	public void setCBR2(CBR2 cBR2) {
		CBR2 = cBR2;
	}

	public MBR getMBR() {
		return MBR;
	}

	public void setMBR(MBR mBR) {
		MBR = mBR;
	}
}
