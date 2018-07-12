package airspan.JiraCreateIssue;

import java.io.IOException;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main(String[] args)
    {
    	jiraRestCreateIssue();
    }
    
    private static void jiraRestCreateIssue() {
		String jiraIP = "10.3.10.49";
		String jiraPort = "8080";
		//String projectId = "10603";//SW2
		String projectId = "10604";//SW1
		String projectPrefix = "SW1";
		String auth = "eXNodGVybjprb3Rlbm9rMTI=";//yuri
		String[] issues = { "SWIT-5 Sanity Analysis",
							"SWIT-10 Sanity Analysis",
							"SWIT-33 Sanity Analysis",
							"SWIT-36 Sanity Analysis",
							"SWIT-30 Regression Analysis",
							"SWIT-29 Regression Analysis",
							"SWIT-2 Regression Analysis",
							};
		
		int[] epics = new int[]{421,372,379,351,442,443,444};
		int[] time = new int[]{2,2,2,2,4,4,4};
				
		for (int i = 0; i < issues.length; i++) {
			createIssues(jiraIP, jiraPort, projectId, projectPrefix, auth, issues, epics, time, i);
		}
	}

	private static void createIssues(String jiraIP, String jiraPort, String projectId, String projectPrefix,
			String auth, String[] issues, int[] epics, int[] time, int i) {
		for (int j = 0; j < 22; j++) {

			try {
				OkHttpClient client = new OkHttpClient();

				MediaType mediaType = MediaType.parse("application/json");
				RequestBody body = RequestBody.create(mediaType,
						"{\n    \"fields\": {\n       \"project\":\n       {\n          \"id\": \"" + projectId
								+ "\"\n       },\n       \"summary\": \""+issues[i]+"\",\n       \"components\": [{ \"name\" : \"Analysis\"}],\n       \"customfield_10000\" : "
								+ "\""+projectPrefix+"-"+(epics[i])+"\",\n       \"timetracking\": {\"originalEstimate\": \""+time[i]+"h\"},\n       \"issuetype\": {\n          \"name\": \"Story\"\n       }\n   }\n}");

				Request request = new Request.Builder().url("http://"+jiraIP+":"+jiraPort+"/rest/api/2/issue/").post(body)
						.addHeader("Content-Type", "application/json")
						.addHeader("Authorization", "Basic " + auth)//user yuri
						.addHeader("Cache-Control", "no-cache")
						.addHeader("Postman-Token", "f6c60a69-91a8-4fe8-a9a8-e1fa637d9d14").build();

				Response response = client.newCall(request).execute();
				System.out.println("response: " + response);
				System.out.println("response message: " + response.message());
				System.out.println("response string: " + response.body().string());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
