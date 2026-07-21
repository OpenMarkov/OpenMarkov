package org.openmarkov.integrationTests.integrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.openmarkov.core.exception.ProbNetParserException;

import java.io.IOException;


/**
 * This class implements some basic tests for ID-decide-test-tree-add, which is an influence diagram equivalent to ID-decide-test, but that,
 * instead of representing the potentials with tables, it uses Tree ADDs. All the results of inference should be the same. *
 */
public class IDDecideTestTreeADDNetworkTests extends idDecideTestNetworkTests {
	
	@Override
	@BeforeEach public void setUp() throws java.net.URISyntaxException, ProbNetParserException, IOException {
		networkName = "networks/id/ID-decide-test-tree-add.pgmx";
		super.setUp();
	}

}
