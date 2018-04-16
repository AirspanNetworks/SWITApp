
package il.co.topq.kpi.front.rest;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import il.co.topq.kpi.model.ElasticDatabase;
import il.co.topq.kpi.model.ElasticsearchTest;
import il.co.topq.kpi.view.DataTable;
import il.co.topq.kpi.view.OverviewTableView;


@RestController
@Path("api/release/{release}/test")
public class OverviewResource {

	public static final SimpleDateFormat TIMESTAMP_FORMATTER = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	private static final Logger log = LoggerFactory.getLogger(OverviewResource.class);

	@Autowired
	private ElasticDatabase db;

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public DataTable get(@PathParam("release") String release) throws IOException {
		log.debug("GET - Get list of all the tests in the current execution");
		List<ElasticsearchTest> tests = db.getTestsByRelease(release)
				.stream()
				.filter(test -> test.getScenarioProperties() != null)
				.filter(test -> test.getScenarioProperties().containsKey("Scenario"))
				.filter(test -> test.getScenarioProperties().get("Scenario").contains("Sanity"))
				.sorted(new ElasticsearchTestComparator.CompareByTest())
				.collect(Collectors.toList());
		final OverviewTableView view = new OverviewTableView();
		if (tests.isEmpty()) {
			log.warn("No tests were found in execution with id " + release);
			return view.getTable();
		}
		return view.populate(tests).getTable();
	}
	
	
	public static class ElasticsearchTestComparator{
		public static class CompareByTest implements Comparator<ElasticsearchTest>{

			
			public Map<String,Integer> testsOrdinal = new HashMap<>();
			
			public CompareByTest() {
				testsOrdinal.put("Software Upgrade",1);
				testsOrdinal.put("BasicNetworkEntry",2);
				testsOrdinal.put("TPT_PtMP_800_Udp_Qci9",3);
				testsOrdinal.put("TPT_PtMP_1024_Udp_Qci9",4);
				testsOrdinal.put("TPT_PtMP_1400_Udp_Qci9",5);
				testsOrdinal.put("TPT_PtMP_1522_Udp_Qci9",6);
				testsOrdinal.put("TPT_PtMP_800_Udp_Qci7",7);
				testsOrdinal.put("TPT_PtMP_1024_Udp_Qci7",8);
				testsOrdinal.put("TPT_PtMP_1400_Udp_Qci7",9);
				testsOrdinal.put("QOS_PtMP_800_Udp_Qci7And9",10);
				testsOrdinal.put("QOS_PtMP_1024_Udp_Qci7And9",11);
				testsOrdinal.put("QOS_PtMP_1400_Udp_Qci7And9",12);
				testsOrdinal.put("MultipleHO_X2IntraFrequency",13);
				testsOrdinal.put("MultipleHO_S1IntraFrequency",14);
			}
			
			@Override
			public int compare(ElasticsearchTest t1, ElasticsearchTest t2) {
				Integer ordT1 = 999;
				Integer ordT2 = 999;
				if(testsOrdinal.containsKey(t1.getName()))
					ordT1 = testsOrdinal.get(t1.getName());
				if(testsOrdinal.containsKey(t2.getName()))
					ordT2 = testsOrdinal.get(t2.getName());
				return ordT1-ordT2;
			}
			
		}
	}

}
