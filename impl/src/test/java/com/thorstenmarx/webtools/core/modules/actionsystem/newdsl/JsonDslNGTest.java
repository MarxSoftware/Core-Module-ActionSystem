/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thorstenmarx.webtools.core.modules.actionsystem.newdsl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.assertj.core.api.Assertions.*;
import org.testng.annotations.Test;

/**
 *
 * @author marx
 */
public class JsonDslNGTest {
	
	
	JsonDsl dslGenerator = new JsonDsl();

	@Test
	public void test_parse_simple() throws IOException {
		String segment = new String(Files.readAllBytes(Paths.get("src/test/resources/segments/simple.json")));
		
		DSLSegment dslSegment = dslGenerator.parse(segment);
		
		assertThat(dslSegment).isNotNull();
		assertThat(dslSegment.site).isNotEmpty().isEqualTo("test_site");
		
		assertThat(dslSegment.conditional()).isNotNull().isInstanceOf(OR.class);
	}
	
	
	
}
