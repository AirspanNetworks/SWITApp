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
    public static void main( String[] args )
    {
    	jiraRestCreateIssue();
    }
    
    private static void jiraRestCreateIssue() {//10.3.10.49
		String jiraIP = "10.3.10.49";
		String jiraPort = "8080";
    	String setupName = "SWIT-8";
		String epicKey = "SW-348";
		String projectId = "10603";//SW2
		String[] issues = { "SWIT-1 Regression Analysis",
							"SWIT-6 Regression Analysis",
							"SWIT-32 Sanity Analysis",
							"SWIT-25 Sanity Analysis",
							"SWIT-26 Sanity Analysis",
							"SWIT-34 Sanity Analysis",
							"SWIT-35 Sanity Analysis",
							"SWIT-37 Sanity Analysis",
							"SWIT-31 Sanity Analysis",
							"SWIT-8 Sanity Analysis" };
		
		int[] epics = new int[]{370,371,372,373,374,375,376,377,378,348};
		
		for (int i = 0; i < issues.length; i++) {

			for (int j = 0; j < 20; j++) {

				try {
					OkHttpClient client = new OkHttpClient();

					MediaType mediaType = MediaType.parse("application/json");
					RequestBody body = RequestBody.create(mediaType,
							"{\n    \"fields\": {\n       \"project\":\n       {\n          \"id\": \"" + projectId
									+ "\"\n       },\n       \"summary\": \""+issues[i]+"\",\n       \"components\": [{ \"name\" : \"Analysis\"}],\n       \"customfield_10000\" : "
									+ "\"SW2-"+(epics[i])+"\",\n       \"timetracking\": {\"originalEstimate\": \"2h\"},\n       \"issuetype\": {\n          \"name\": \"Story\"\n       }\n   }\n}");
					
					Request request = new Request.Builder().url("http://"+jiraIP+":"+jiraPort+"/rest/api/2/issue/").post(body)
							.addHeader("Content-Type", "application/json")
							.addHeader("Authorization", "Basic aGdvbGRidXJkOnN1YmFydTk3")//user hen
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
}
