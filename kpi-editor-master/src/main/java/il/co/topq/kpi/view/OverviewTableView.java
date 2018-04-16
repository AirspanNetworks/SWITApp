package il.co.topq.kpi.view;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import il.co.topq.kpi.model.ElasticsearchTest;

public class OverviewTableView extends AbstractTableView<ElasticsearchTest> {

	private enum Header implements TableHeader {

		// @formatter:off

		NAME("Name");
		
		// @formatter:on

		public final String headerName;

		private Header(String headerName) {
			this.headerName = headerName;
		}

		@Override
		public String getHeaderName() {
			return headerName;
		}

		@Override
		public TableHeader[] headers() {
			return values();
		}

	}

	public OverviewTableView() {
		super(Header.NAME);
	}

	@Override
	public AbstractTableView<ElasticsearchTest> populate(List<ElasticsearchTest> tests) {
		for (ElasticsearchTest test : tests) {
			boolean alreadyHadTest = false;
			String target = "";
			Map<String, Object> row = new HashMap<String, Object>();
			if(test.getScenarioProperties() != null && test.getScenarioProperties().containsKey("targetVersion"))	{
				target = test.getScenarioProperties().get("targetVersion");
				int ver = 999;
				String[] tarArr = null;
				if(target.contains("_")){
					tarArr = target.split("_");
				}
				if(target.contains(".")){
					tarArr = target.split("\\.");
				}
				if (tarArr != null)
				target = tarArr[tarArr.length-1];	
				
				if(target.length() !=3)
					continue;
				
				try { ver = Integer.parseInt(target); } catch (NumberFormatException e) { }
				if(ver > 800)
					continue;


				
				if(!table.getHeaders().contains(target)){
					table.getHeaders().add(target);
					//Set<String> headers = table.getHeaders();
					List<String> headers = table.getHeaders().stream()
					.sorted(new Comparators.CompareVersion())
					.collect(Collectors.toList());
					table.getHeaders().clear();
					table.getHeaders().addAll(headers);
				}
				
				for(Map<String, Object> temprow : table.getData()){
					if(temprow.get(Header.NAME.getHeaderName()).equals(test.getName())){
						alreadyHadTest = true;
						// new build
						if(!temprow.containsKey(target)){
							temprow.put(target, "");
						}

						testResult tr = (testResult.fromString(temprow.get(target).toString()));
						tr.total++;
						if(test.getStatus() == "success") tr.pass++;
						temprow.put(target, tr.toString());
						break;
					}
				}
			}
			//broken test record
			else continue;
			
			if(!alreadyHadTest){
				row.put(Header.NAME.getHeaderName(), test.getName());
				
				testResult tr = new testResult();
				tr.total++;
				if(test.getStatus() == "success") tr.pass++;
				row.put(target, tr.toString());
				table.addRow(row);
			}


		}
		return this;
	}
	
	public static class testResult{
		public int pass = 0;
		public int total = 0;
		@Override
		public String toString() {
			return pass + "/" + total;
		}
		
		public static testResult fromString(String str){
			testResult temp  = new testResult();
			if(str.trim().equals("")) return temp;
			temp.total = Integer.parseInt(str.split("/")[0]);
			temp.pass = Integer.parseInt(str.split("/")[1]);
			return temp;
		}
		
		
	}
	
	public static class Comparators{
		public static class CompareVersion implements Comparator<String>{

			@Override
			public int compare(String st1, String st2) {
				if(st1.equals(Header.NAME.getHeaderName()))
					return -1;
				if(st2.equals(Header.NAME.getHeaderName()))
					return 1;
				
				int ver1 = 999;
				int ver2 = 999;
				
				try { ver1 = Integer.parseInt(st1); } catch (NumberFormatException e) { }
				try { ver2 = Integer.parseInt(st2); } catch (NumberFormatException e) { }
				
					
				return ver1-ver2;
			}


			
		}
	}

}
