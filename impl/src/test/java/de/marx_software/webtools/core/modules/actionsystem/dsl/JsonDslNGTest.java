/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.marx_software.webtools.core.modules.actionsystem.dsl;

import com.thorstenmarx.modules.api.DefaultServiceRegistry;
import de.marx_software.webtools.core.modules.actionsystem.Context;
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
	
	
	JsonDsl dslGenerator = new JsonDsl(new DefaultServiceRegistry());

	@Test
	public void test_parse_simple() throws IOException {
		String segment = new String(Files.readAllBytes(Paths.get("src/test/resources/segments/simple.json")));
		Context context = new Context();
		DSLSegment dslSegment = dslGenerator.parse(segment, context);
		
		assertThat(dslSegment).isNotNull();
		
		assertThat(dslSegment.conditional()).isNotNull().isInstanceOf(OR.class);
	}
	
	
	
}
