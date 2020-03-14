/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.thorstenmarx.webtools.core.modules.actionsystem.dsl.graal;

import com.thorstenmarx.webtools.core.modules.actionsystem.dsl.DSLSegment;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.ScriptException;
import org.assertj.core.api.Assertions;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author marx
 */
public class GraalDSLNGTest {
	

	@Test
	public void testSomeMethod() throws ScriptException {
		
		try {
			final GraalDSL dslRunner = new GraalDSL(null, null);
			final String content = "segment().site('49aa1118-3169-49d9-bac2-f3e1f8d7812b').and(not(rule(FIRSTVISIT)))";
			
			DSLSegment build = dslRunner.build(content);
			Assertions.assertThat(build).isNotNull();
			
			CompletableFuture<DSLSegment> future = CompletableFuture.supplyAsync(() -> {
				try {
					return dslRunner.build(content);
				} catch (ScriptException ex) {
					ex.printStackTrace();
				}
				return null;
			});
			
			build = future.get();
			Assertions.assertThat(build).isNotNull();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		} catch (ExecutionException ex) {
			ex.printStackTrace();
		}
		
	}
	
}
