package junit;

import org.junit.Test;

import amidar.AmidarSimulator;

public class AmidarSimultatorTest {
	
	@Test
	public void testWholeSimulator(){
		org.junit.Assert.assertTrue(AmidarSimulator.test());
	}

}
