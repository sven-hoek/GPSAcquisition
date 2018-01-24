package generator;


import java.io.File;
import java.util.Collection;
import java.util.HashSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import cgramodel.CgraModel;
import io.AttributeParser;

@RunWith(Parameterized.class)
class VerilogGeneratorTest {

	static VerilogGenerator generator;
	static AttributeParser attparser;
	static String attributeJson;
	static String testfolder = "junittest";
	
	public VerilogGeneratorTest(String attributeJson){
		VerilogGeneratorTest.attributeJson = attributeJson;
	}
	
	@Parameters
	public static Collection getParameters(){
		return null;
	};
	
	@Test
	public void test() {
		
		CgraModel model = attparser.loadCgra(attributeJson);
		generator.printVerilogDescription(testfolder, model);
	}
	
	@After
	public void deleteTestFolders(){
		File file = new File(testfolder);
		recursiveDelete(file);
	}
	
	private void recursiveDelete(File file){
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] list = file.listFiles();
				if (list != null) {
					for (File subFile : list) {
						recursiveDelete(subFile);
					}
				}
			}
			file.delete();
		}
	}

	public static class VerilogGeneratorAmidarTest extends VerilogGeneratorTest{

		public VerilogGeneratorAmidarTest(String attributeJson) {
			super(attributeJson);
		}
		
		@Parameters
		public static Collection getParameters(){
			
			String currentDir = System.getProperty("user.dir");
		      System.out.println("Current dir using System:"   +currentDir);
			
			Collection<Object[]> parameters = new HashSet<Object[]>();
			target.Processor.Instance = target.Amidar.Instance;
			attparser = target.Processor.Instance.getAttributeParser();
			generator = target.Processor.Instance.getGenerator();
			parameters.add(new Object[]{"config/amidar/junittests/CGRA_4"});
			parameters.add(new Object[]{"config/amidar/junittests/CGRA_9"});
			parameters.add(new Object[]{"config/amidar/junittests/CGRA_16"});
			return parameters;
		}
	}
	
	public static class VerilogGeneratorUltrasynthTest extends VerilogGeneratorTest{

		public VerilogGeneratorUltrasynthTest(String attributeJson) {
			super(attributeJson);
		}
		
		@Parameters
		public static Collection getParameters(){
			
			String currentDir = System.getProperty("user.dir");
		      System.out.println("Current dir using System:"   +currentDir);
			
			Collection<Object[]> parameters = new HashSet<Object[]>();
			target.Processor.Instance = target.UltraSynth.Instance;
			attparser = target.Processor.Instance.getAttributeParser();
			generator = target.Processor.Instance.getGenerator();
			// Todo - Fill in US jsons
			return parameters;
		}
	}
	

}
