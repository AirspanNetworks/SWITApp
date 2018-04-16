package Configuration;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ConfigurationUtils {
	private static final String configFileMessagesDiam = "diam-messages-config.xml";
	private static final String configFileHttp = "http-config.xml";
	private static final Logger log = LogManager.getLogger(ConfigurationUtils.class);

	public static DiamConfig loadDiamConfigFile() {
		log.debug("loading config file " + configFileMessagesDiam);
		DiamConfig diamConfig = new DiamConfig();
		JAXBContext jaxbContext;
		try {
			jaxbContext = JAXBContext.newInstance(DiamConfig.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			diamConfig = (DiamConfig) jaxbUnmarshaller.unmarshal(new File(configFileMessagesDiam));

		} catch (JAXBException e) {
			log.error(e.getMessage());
			e.printStackTrace();
		}
		return diamConfig;
	}

	public static HttpConfig loadHttpFileConfig() {
		log.debug("loading config file " + configFileHttp);
		HttpConfig httpConfig = new HttpConfig();
		try {
			File file = new File(configFileHttp);
			JAXBContext jContext = JAXBContext.newInstance(HttpConfig.class);
			Unmarshaller unmarshallerObj = jContext.createUnmarshaller();
			httpConfig = (HttpConfig) unmarshallerObj.unmarshal(file);

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		return httpConfig;
	}
}
