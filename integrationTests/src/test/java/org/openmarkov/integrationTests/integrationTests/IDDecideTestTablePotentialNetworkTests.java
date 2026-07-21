package org.openmarkov.integrationTests.integrationTests;

import org.junit.jupiter.api.BeforeEach;
import org.openmarkov.core.exception.ProbNetParserException;

import java.io.IOException;


/**
 * This class implements some basic tests for ID-decide-test, which is an influence diagram where all the potentials are represented as tables.
 *
 */
public class IDDecideTestTablePotentialNetworkTests extends idDecideTestNetworkTests {
	
	@Override
	@BeforeEach public void setUp() throws java.net.URISyntaxException, ProbNetParserException, IOException {
		networkName = "networks/id/ID-decide-test.pgmx";
		super.setUp();
	}

}
