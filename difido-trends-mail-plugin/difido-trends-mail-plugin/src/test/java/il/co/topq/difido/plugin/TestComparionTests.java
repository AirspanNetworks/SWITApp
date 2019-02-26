
package il.co.topq.difido.plugin;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.swit.TestUtils;

import il.co.topq.report.business.elastic.ElasticsearchTest;

public class TestComparionTests {

	@Test
	public void testComparePositiveByName() {
		ElasticsearchTest test0 = new ElasticsearchTest();
		ElasticsearchTest test1 = new ElasticsearchTest();

		test0.setName("foo");
		test1.setName("foo");

		boolean result = TestUtils.isTestsEquals(test0, test1);
		Assert.assertTrue(result);
	}

	@Test
	public void testCompareNegativeByName() {
		ElasticsearchTest test0 = new ElasticsearchTest();
		ElasticsearchTest test1 = new ElasticsearchTest();

		test0.setName("foo");
		test1.setName("bar");

		boolean result = TestUtils.isTestsEquals(test0, test1);
		Assert.assertFalse(result);
	}
	
	@Test
	public void testComparePositiveByParameters() {
		ElasticsearchTest test0 = new ElasticsearchTest();
		ElasticsearchTest test1 = new ElasticsearchTest();
		test0.setName("foo");
		test1.setName("foo");

		Map<String,String> params = new HashMap<String,String>();
		params.put("key0", "val0");
		params.put("key1", "val1");
		params.put("key2", "val2");
		
		test0.setParameters(params);
		test1.setParameters(params);
		
		boolean result = TestUtils.isTestsEquals(test0, test1);
		Assert.assertTrue(result);
	}
	
	@Test
	public void testCompareNegtiveByParameters() {
		ElasticsearchTest test0 = new ElasticsearchTest();
		ElasticsearchTest test1 = new ElasticsearchTest();
		test0.setName("foo");
		test1.setName("foo1");

		Map<String,String> params0 = new HashMap<String,String>();
		params0.put("key0", "val0");
		params0.put("key1", "val1");
		params0.put("key2", "val2");

		Map<String,String> params1 = new HashMap<String,String>();
		params1.put("key0", "val0");
		params1.put("key1", "foo");
		params1.put("key2", "val2");

		
		test0.setParameters(params0);
		test1.setParameters(params1);
		
		boolean result = TestUtils.isTestsEquals(test0, test1);
		Assert.assertFalse(result);
	}
	
	@Test
	public void testCompareNegtiveByParametersWithMissingParam() {
		ElasticsearchTest test0 = new ElasticsearchTest();
		ElasticsearchTest test1 = new ElasticsearchTest();
		test0.setName("foo");
		test1.setName("foo");

		Map<String,String> params0 = new HashMap<String,String>();
		params0.put("key0", "val0");
		params0.put("key0", "val0");

		Map<String,String> params1 = new HashMap<String,String>();
		params1.put("key0", "val0");
		params1.put("key0", "val0");
		
		test0.setParameters(params0);
		test1.setParameters(params1);
		
		boolean result = TestUtils.isTestsEquals(test0, test1);
		Assert.assertTrue(result);
	}

}
